<!-- ==============================================================
     AI 聊天页面
     功能：AI 助手页面的容器，引入 AiChat 聊天组件。
     ============================================================== -->
<template>
  <div class="ai-page">
    <h2 style="margin-left: 10px">智慧社区食堂 AI 营养助手</h2>
    <!-- 引入 AiChat 聊天组件，包含完整的对话界面和交互逻辑 -->
    <AiChat />
  </div>
</template>

<script>
/**
 * AI 聊天页面组件
 * 作为 AiChat 组件的页面级容器，布局和路由挂载点。
 */
import AiChat from '@/components/AiChat.vue'

export default {
  name: 'AiPage',
  components: { AiChat }
}
</script>
