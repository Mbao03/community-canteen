package com.example.kitchen.service;

import com.example.kitchen.model.User;

import java.util.List;
import java.util.Map;

/**
 * AI 聊天服务接口
 * <p>
 * 定义与 AI 大模型对话的核心业务方法。
 * 实现类负责构建提示词、调用大模型 API 以及在异常时提供本地兜底推荐。
 * </p>
 */
public interface AiService {

    /**
     * AI 对话入口
     * <p>
     * 接收用户消息、用户画像和对话历史，返回 AI 回复文本。
     * 内部处理流程包括：
     * <ol>
     *   <li>查询菜品数据和用户点餐记录</li>
     *   <li>构建包含上下文信息的提示词（Prompt）</li>
     *   <li>调用大模型 API（支持 chat/completions 和 responses 两种端点）</li>
     *   <li>API 失败时降级为本地规则推荐（Local Fallback）</li>
     * </ol>
     * </p>
     *
     * @param message     用户当前输入的文本消息
     * @param userProfile 用户画像对象（含姓名、性别、年龄、饮食标签、健康备注）
     * @param userid      用户 ID，用于查询该用户的历史点餐记录
     * @param history     历史对话记录列表，每项包含 role（user/assistant）和 text 字段
     * @return AI 回复文本，失败时返回本地兜底推荐内容
     */
    String chat(String message, User userProfile, Integer userid, List<Map<String, String>> history);
}
