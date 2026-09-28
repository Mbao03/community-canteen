<!-- ==============================================================
     AI 聊天组件
     功能：提供与 AI 营养助手的对话界面，支持多轮对话上下文。
     数据流：
       sendMessage()
         → chatWithUserProfile(message, userProfile, history)
           → POST /ai/chat { message, userProfile, history }
             → AiController.chat() → AiServiceImpl.chat()
               → buildPrompt() → requestChatCompletions()
     ============================================================== -->
<template>
  <!-- 聊天容器：消息展示区域 + 底部输入区域 -->
  <div class="ai-chat">
    <!-- 聊天消息展示区域，自动滚动 -->
    <div class="chat-window">
      <!-- 遍历消息列表，用户消息靠右，AI 消息靠左 -->
      <div v-for="(msg, i) in messages" :key="i" :class="['chat-message', msg.role]">
        <div class="avatar">{{ msg.role === 'user' ? 'U' : 'AI' }}</div>
        <div class="message-content">{{ msg.text }}</div>
      </div>
      <!-- 加载中状态：显示 "正在思考..." -->
      <div v-if="isLoading" class="chat-message assistant">
        <div class="avatar">AI</div>
        <div class="message-content">正在思考...</div>
      </div>
    </div>

    <!-- 底部输入区域：文本输入框 + 发送按钮 -->
    <div class="chat-input">
      <textarea
        v-model="userInput"
        :disabled="isLoading"
        placeholder="请输入你的菜谱或营养问题..."
        @keydown.enter.exact.prevent="sendMessage"
      ></textarea>
      <button :disabled="isLoading" @click="sendMessage">{{ isLoading ? '-发送中...' : '发送' }}</button>
    </div>
  </div>
</template>

<script>
/**
 * AI 聊天组件 JavaScript 逻辑
 *
 * 功能：
 * - 组件挂载时自动获取当前登录用户的用户画像
 * - 用户发送消息时构建 history 传给后端
 * - 支持多轮对话上下文记忆
 */

import { chatWithUserProfile } from '@/api/ai'     // AI 聊天 API
import { getResidentInfo } from '@/api/resident'   // 用户信息 API
import { mapGetters } from 'vuex'

export default {
  name: 'AiChat',

  /** 从 Vuex 获取当前用户信息 */
  computed: {
    ...mapGetters(['id', 'name', 'token'])
  },

  data() {
    return {
      /** 用户输入框绑定的文本 */
      userInput: '',
      /** 是否正在请求 AI 回复（加载中） */
      isLoading: false,
      /** 当前用户画像数据（姓名、性别、生日、饮食标签、健康备注） */
      userProfile: null,
      /** 对话消息列表，每条含 role（user/assistant）和 text 字段 */
      messages: [{
        role: 'assistant',
        text: '你好，我是社区配餐 AI 助手。你可以咨询菜谱推荐、营养搭配和订餐建议。'
      }]
    }
  },

  /** 组件挂载后拉取用户画像 获取用户数据 */
  mounted() {
    this.fetchUserProfile()
  },

  methods: {
    /**
     * 获取当前登录用户的画像信息
     * 调用 /resident/getResidentInfo 接口，
     * 成功后提取 userid、username、gender、birthday、dietaryTags、healthNotes，
     * 这些数据会随每次聊天请求发送给后端，用于个性化推荐。
     */
    fetchUserProfile() {
      if (!this.token) return
      getResidentInfo(this.token).then((res) => {
        if (res && res.status === 200 && res.data) {
          this.userProfile = {
            userid: res.data.userid,
            username: res.data.username,
            gender: res.data.gender || '',
            birthday: res.data.birthday || '',
            dietaryTags: res.data.dietaryTags || '',
            healthNotes: res.data.healthNotes || ''
          }
        }
      }).catch(() => {})
    },

    /**
     * 解析 AI 回复中的结构化段落
     * 将带 [标签] 的文本按标签切分为 sections 数组
     * 支持标签：Conclusion, Recommended Dishes, Meal Plan, Tips
     *
     * @param {string} text AI 回复的原始文本
     * @returns {Array} 段落数组 [{ label, body }]
     */
    parseSections(text) {
      if (!text) return []
      const sections = []
      const labelPattern = /\[(Conclusion|Recommended Dishes|Meal Plan|Tips)\]/g
      let match
      const positions = []
      while ((match = labelPattern.exec(text)) !== null) {
        positions.push({ label: match[1], index: match.index })
      }
      if (positions.length === 0) return []
      for (let i = 0; i < positions.length; i++) {
        const start = positions[i].index + positions[i].label.length + 2
        const end = i + 1 < positions.length ? positions[i + 1].index : text.length
        const raw = text.substring(start, end).trim()
        const body = raw.split('\n').map(l => l.trim()).filter(l => l.length > 0).join('\n')
        sections.push({ label: positions[i].label, body: body })
      }
      return sections
    },

    /**
     * 滚动聊天窗口到底部
     * 在发送消息或收到回复后通过 nextTick 调用
     */
    scrollToBottom() {
      this.$nextTick(() => {
        const chatWindow = this.$el.querySelector('.chat-window')
        if (chatWindow) chatWindow.scrollTop = chatWindow.scrollHeight
      })
    },

    /**
     * 发送消息主方法
     *
     * 处理流程：
     *   1. 校验输入不为空且不在加载中
     *   2. 将用户消息推入消息列表，清空输入框
     *   3. 构造 history（排除当前消息自身，只取 user/assistant 角色）
     *   4. 调用 chatWithUserProfile 发送请求
     *   5. 将 AI 回复推入消息列表
     *   6. 异常时显示错误提示
     *   7. 每次操作后滚动到底部
     */
    async sendMessage() {
      const message = this.userInput.trim()
      if (!message || this.isLoading) return

      // 将用户消息加入列表并清空输入框
      this.messages.push({ role: 'user', text: message })
      this.userInput = ''
      this.isLoading = true
      this.scrollToBottom()

      try {
        // 构造历史对话记录（去掉最新添加的用户消息自身）
        const history = this.messages.slice(0, -1)
          .filter(msg => msg.role === 'user' || msg.role === 'assistant')

        // 调用后端 AI 接口
        const answer = await chatWithUserProfile(message, this.userProfile, history)
        this.messages.push({ role: 'assistant', text: answer })
      } catch (err) {
        console.error(err)
        this.messages.push({ role: 'assistant', text: '请求失败，请检查后端 AI 配置后重试。' })
      } finally {
        this.isLoading = false
        this.scrollToBottom()
      }
    }
  }
}
</script>

<style scoped>
.ai-chat { display: flex; flex-direction: column; height: 600px; border: 1px solid #ddd; border-radius: 10px; background-color: #fafafa; }
.chat-window { flex: 1; padding: 16px; overflow-y: auto; }
.chat-message { display: flex; margin-bottom: 14px; }
.chat-message.user { justify-content: flex-end; }
.chat-message.assistant { justify-content: flex-start; }
.avatar { width: 36px; height: 36px; line-height: 36px; text-align: center; border-radius: 50%; background-color: #409eff; color: #fff; font-weight: bold; margin-right: 10px; flex-shrink: 0; }
.chat-message.user .avatar { background-color: #67c23a; margin-left: 10px; margin-right: 0; }
.message-content { max-width: 70%; padding: 12px 14px; border-radius: 10px; background-color: #fff; word-break: break-word; line-height: 1.7; font-size: 14px; color: #333; }
.chat-message.user .message-content { background-color: #e6f7ff; }
.chat-input { display: flex; padding: 10px 12px; border-top: 1px solid #e5e5e5; background-color: #fff; border-radius: 0 0 10px 10px; }
textarea { flex: 1; resize: none; height: 52px; border-radius: 8px; padding: 10px 12px; border: 1px solid #ddd; font-size: 14px; outline: none; }
textarea:focus { border-color: #409eff; }
button { margin-left: 10px; padding: 8px 20px; background-color: #409eff; border: none; color: #fff; border-radius: 8px; cursor: pointer; font-size: 14px; }
button:hover { background-color: #66b1ff; }
button:disabled { background-color: #a0cfff; cursor: not-allowed; }

.ai-section { margin-bottom: 14px; }
.ai-section:last-child { margin-bottom: 0; }
.ai-section-label { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 6px; }
.ai-section-body { font-size: 14px; line-height: 1.8; color: #333; white-space: pre-wrap; }
</style>
