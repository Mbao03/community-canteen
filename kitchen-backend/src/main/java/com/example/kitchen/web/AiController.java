package com.example.kitchen.web;

import com.example.kitchen.dto.AiChatRequest;
import com.example.kitchen.model.User;
import com.example.kitchen.service.AiService;
import com.example.kitchen.utils.R;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * AI 聊天控制器
 * 提供与 AI 营养助手对话的 REST 接口。
 * 接收前端发送的用户消息、用户画像和对话历史，调用 AiService 获取 AI 回复后返回。
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    /** AI 服务层，处理实际的大模型调用、提示词构建和本地兜底推荐 */
    @Resource
    private AiService aiService;

    /**
     * POST /ai/chat — AI 对话接口
     * 请求体包含：
     *   <li><b>message</b> — 用户当前输入的消息文本</li>
     *   <li><b>userProfile</b> — 用户画像（姓名、性别、生日、饮食偏好标签、健康备注）</li>
     *   <li><b>history</b> — 历史对话记录（role + text 键值对列表），用于维持多轮对话上下文</li>
     * 处理流程：
     *   <li>将前端传来的 UserProfile JSON 转为后端 User 模型对象</li>
     *   <li>将字符串格式的生日 "yyyy-MM-dd" 解析为 Date 对象</li>
     *   <li>提取用户 ID，用于查询该用户的历史点餐记录</li>
     *   <li>调用 aiService.chat() 获取 AI 回复文本</li>
     *   <li>将结果包装为统一响应体返回</li>
     * @param request AI 聊天请求体，包含消息、用户画像和历史记录
     * @return 统一响应体 Map，其中 data 字段为 AI 回复的文本内容
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody AiChatRequest request) {
        // ================================================================
        // Step 1: 解析用户画像 — 将前端传入的 UserProfile 转换为 User 实体
        // ================================================================
        User userProfile = null;
        if (request.getUserProfile() != null) {
            userProfile = new User();
            userProfile.setUserid(request.getUserProfile().getUserid());
            userProfile.setUsername(request.getUserProfile().getUsername());
            userProfile.setGender(request.getUserProfile().getGender());

            // 将 "2000-01-01" 格式的生日字符串解析为 java.util.Date
            String birthdayStr = request.getUserProfile().getBirthday();
            if (birthdayStr != null && !birthdayStr.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    userProfile.setBirthday(sdf.parse(birthdayStr));
                } catch (Exception e) {
                    // 生日解析失败不影响后续流程，仅无法提供年龄信息
                }
            }
            userProfile.setDietaryTags(request.getUserProfile().getDietaryTags());
            userProfile.setHealthNotes(request.getUserProfile().getHealthNotes());
        }

        // ================================================================
        // Step 2: 提取用户 ID — 用于查询点餐历史和个性化推荐
        // ================================================================
        Integer userid = request.getUserProfile() != null ? request.getUserProfile().getUserid() : null;

        // ================================================================
        // Step 3: 调用 AI 服务 — 构建提示词、请求大模型、异常兜底
        // ================================================================
        String result = aiService.chat(request.getMessage(), userProfile, userid, request.getHistory());

        // ================================================================
        // Step 4: 返回统一格式的响应
        // ================================================================
        return R.getResultMap(200, "success", result);
    }
}
