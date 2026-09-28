package com.example.kitchen.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.kitchen.model.DishInfo;
import com.example.kitchen.model.MealOrder;
import com.example.kitchen.model.User;
import com.example.kitchen.service.AiService;
import com.example.kitchen.service.DishInfoService;
import com.example.kitchen.mapper.MealOrderMapper;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 聊天服务实现
 * <p>
 * 核心功能：构建提示词（Prompt）→ 调用大模型 API → 异常兜底。
 * <p>
 * 架构说明：
 * <ol>
 *   <li><b>提示词构建（buildPrompt）</b>：将用户消息、用户画像、点餐历史、菜品数据和对话历史
 *       拼接为一个完整的提示词文本，通过 Prompt Injection 方式注入上下文。</li>
 *   <li><b>API 调用（requestChatCompletions）</b>：优先调用 OpenAI 兼容的
 *       /v1/chat/completions 端点，支持普通模式和流式（stream）模式。</li>
 *   <li><b>降级策略</b>：chat/completions 失败后尝试 /v1/responses 端点（fallback），
 *       两个端点都失败则使用本地规则推荐（generateLocalAdvice）。</li>
 *   <li><b>本地兜底（generateLocalAdvice）</b>：基于关键词匹配的简单推荐，
 *       按热量排序或按蛋白质排序，确保 API 不可用时仍有推荐结果。</li>
 * </ol>
 * </p>
 */
@Service
public class AiServiceImpl implements AiService {

    /** OpenAI API 密钥，从配置或环境变量注入 */
    @Value("${spring.ai.openai.api-key:${OPENAI_API_KEY:}}")
    private String apiKey;

    /** OpenAI API 基础 URL，默认指向内部网关 */
    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    /** 使用的模型名称，如 deepseek-v4-flash、Claude Opus 4.6 */
    @Value("${openai.api.model:Claude Opus 4.6}")
    private String model;

    /** 连接超时时间（毫秒） */
    @Value("${openai.api.connect-timeout-ms:10000}")
    private int connectTimeoutMs;

    /** 读取超时时间（毫秒） */
    @Value("${openai.api.read-timeout-ms:60000}")
    private int readTimeoutMs;

    /** HTTP 代理主机，为空则不使用代理 */
    @Value("${openai.proxy.host:}")
    private String proxyHost;

    /** HTTP 代理端口 */
    @Value("${openai.proxy.port:0}")
    private int proxyPort;

    /**
     * 兼容性流式降级开关
     * <p>
     * 当 API 返回 usage.prompt_tokens 类型解析错误时，
     * 是否尝试以 stream 模式重试请求。
     * </p>
     */
    @Value("${openai.api.compat-stream-fallback:true}")
    private boolean compatStreamFallback;

    /** 菜品信息服务，用于获取当前所有菜品数据 */
    @Resource
    private DishInfoService dishInfoService;

    /** 订单 Mapper，用于查询用户的点餐历史 */
    @Resource
    private MealOrderMapper mealOrderMapper;

    /** Jackson JSON 解析器，用于解析 API 响应 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** HTTP 请求客户端，支持代理和自定义超时 */
    private RestTemplate restTemplate;

    /**
     * 初始化方法（Bean 创建后自动执行）
     * <p>
     * 1. 清理 API Key：去除首尾空格和引号包裹
     * 2. 配置 HttpClient：设置连接和读取超时，支持 HTTP 代理
     * 3. 创建 RestTemplate：使用自定义的 HttpClient 工厂
     * </p>
     */
    @PostConstruct
    public void init() {
        // 清理 API Key — 去除空格和包裹的引号
        if (apiKey != null) {
            apiKey = apiKey.trim();
            if ((apiKey.startsWith("\"") && apiKey.endsWith("\"")) || (apiKey.startsWith("'") && apiKey.endsWith("'"))) {
                apiKey = apiKey.substring(1, apiKey.length() - 1).trim();
            }
        }

        // 配置 HTTP 客户端超时参数
        RequestConfig.Builder cfg = RequestConfig.custom()
                .setConnectTimeout(connectTimeoutMs)
                .setSocketTimeout(readTimeoutMs);

        // 配置 HTTP 代理（如有设置）
        if (proxyHost != null && !proxyHost.trim().isEmpty() && proxyPort > 0) {
            cfg.setProxy(new HttpHost(proxyHost.trim(), proxyPort));
        }

        // 构建自定义 HttpClient
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(cfg.build())
                .build();

        // 将 HttpClient 注入 RestTemplate
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        restTemplate = new RestTemplate(factory);
    }

    /**
     * AI 对话主入口
     * <p>
     * 执行流程：
     * <ol>
     *   <li>查询所有菜品数据</li>
     *   <li>检查 API Key 是否已配置，未配置则直接返回本地兜底推荐</li>
     *   <li>构建完整提示词（含用户画像、点餐历史、菜品数据、对话历史）</li>
     *   <li>先调用 /v1/chat/completions（主端点）</li>
     *   <li>若主端点返回 usage 类型兼容错误且 compatStreamFallback 开启，尝试 stream 模式重试</li>
     *   <li>主端点失败后调用 /v1/responses（备用端点）</li>
     *   <li>所有端点均失败 → 返回本地规则推荐（generateLocalAdvice）</li>
     * </ol>
     * </p>
     *
     * @param message     用户当前输入的文本消息
     * @param userProfile 用户画像（可 null）
     * @param userid      用户 ID（可 null）
     * @param history     历史对话记录（可 null）
     * @return AI 回复文本，或本地兜底推荐文本
     */
    @Override
    public String chat(String message, User userProfile, Integer userid, List<Map<String, String>> history) {
        // Step 1: 获取全量菜品列表，用于提示词中的菜品上下文 获取菜品数据
        List<DishInfo> dishes = dishInfoService.queryDishInfos();

        // Step 2: API Key 未配置 → 直接返回本地兜底推荐
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("${")) {
            return "AI key is not configured.\n\n" + generateLocalAdvice(message, dishes, userProfile);
        }

        try {
            // Step 3: 构建完整提示词（含所有上下文）
            String prompt = buildPrompt(message, dishes, userProfile, userid, history);
            HttpHeaders headers = buildHeaders();

            // Step 4: 准备主端点和备用端点 URL
            String primaryUrl = buildChatCompletionsUrl();
            String fallbackUrl = buildResponsesUrl();
            String content;
            HttpStatusCodeException primaryHttpErr = null;

            // Step 5: 调用主端点 /v1/chat/completions
            try {
                content = requestChatCompletions(primaryUrl, headers, prompt, false);
                if (content != null && !content.trim().isEmpty()) {
                    return content;
                }
            } catch (HttpStatusCodeException e) {
                primaryHttpErr = e;
                // 如果是 usage 类型兼容错误，尝试以 stream 模式重试
                if (compatStreamFallback && isUsageTypeCompatError(e)) {
                    try {
                        content = requestChatCompletions(primaryUrl, headers, prompt, true);
                        if (content != null && !content.trim().isEmpty()) {
                            return content;
                        }
                    } catch (HttpStatusCodeException streamErr) {
                        primaryHttpErr = streamErr;
                    }
                }
            }

            // Step 6: 主端点失败 → 调用备用端点 /v1/responses
            try {
                content = requestResponses(fallbackUrl, headers, prompt);
                if (content != null && !content.trim().isEmpty()) {
                    return content;
                }
            } catch (HttpStatusCodeException fallbackErr) {
                // 主端点和备用端点都失败 → 返回详细错误信息 + 本地推荐
                if (primaryHttpErr != null) {
                    return "AI upstream error: primary/fallback both failed."
                            + "\nPrimary URL: " + primaryUrl
                            + "\nPrimary HTTP: " + primaryHttpErr.getStatusCode().value() + " " + primaryHttpErr.getStatusText()
                            + "\nPrimary Body: " + safeBody(primaryHttpErr.getResponseBodyAsString())
                            + "\nFallback URL: " + fallbackUrl
                            + "\nFallback HTTP: " + fallbackErr.getStatusCode().value() + " " + fallbackErr.getStatusText()
                            + "\nFallback Body: " + safeBody(fallbackErr.getResponseBodyAsString())
                            + "\nModel: " + model
                            + "\n\n" + generateLocalAdvice(message, dishes, userProfile);
                }
                throw fallbackErr;
            }

            // Step 7: 主端点报错但备用端点返回空 → 返回警告 + 本地推荐
            if (primaryHttpErr != null) {
                return "AI upstream warning: chat/completions failed, responses returned empty."
                        + "\nPrimary URL: " + primaryUrl
                        + "\nPrimary HTTP: " + primaryHttpErr.getStatusCode().value() + " " + primaryHttpErr.getStatusText()
                        + "\nPrimary Body: " + safeBody(primaryHttpErr.getResponseBodyAsString())
                        + "\nFallback URL: " + fallbackUrl
                        + "\nModel: " + model
                        + "\n\n" + generateLocalAdvice(message, dishes, userProfile);
            }

            // Step 8: 两个端点都返回空 → 返回本地兜底推荐
            return generateLocalAdvice(message, dishes, userProfile);

        } catch (HttpStatusCodeException e) {
            // HTTP 状态码异常（如 401、403、429）
            return "AI upstream error: HTTP " + e.getStatusCode().value() + " " + e.getStatusText()
                    + "\nURL: " + buildChatCompletionsUrl()
                    + "\nModel: " + model
                    + "\nBody: " + safeBody(e.getResponseBodyAsString())
                    + "\n\n" + generateLocalAdvice(message, dishes, userProfile);
        } catch (ResourceAccessException e) {
            // 网络超时或网关不可达
            return "AI network timeout or gateway unreachable.\n\n" + generateLocalAdvice(message, dishes, userProfile);
        } catch (Exception e) {
            // 其他未知异常
            return "AI request failed: " + e.getClass().getSimpleName() + " - " + nvl(e.getMessage())
                    + "\n\n" + generateLocalAdvice(message, dishes, userProfile);
        }
    }

    /**
     * 构建 HTTP 请求头
     * <p>
     * 设置 Content-Type 为 application/json，并附加 Bearer Token 认证。
     * </p>
     *
     * @return 包含认证信息的 HTTP 请求头
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }

    /**
     * 构建完整的提示词（Prompt）
     * <p>
     * 将以下信息拼接为一个纯文本提示词：
     * <ol>
     *   <li><b>系统指令</b> — 角色设定、回答规则（简体中文、不强制推荐、不用 Markdown）</li>
     *   <li><b>用户信息</b> — 姓名、性别、年龄、饮食偏好、健康备注（用于个性化推荐）</li>
     *   <li><b>最近点餐记录</b> — 最近 10 条订单（菜品名称、数量、餐段、日期）</li>
     *   <li><b>菜品数据</b> — 当前食堂提供的菜品列表（最多 30 道，含名称、厨师、分类、描述）</li>
     *   <li><b>历史对话</b> — 多轮对话上下文（用户/助手交替），实现上下文记忆</li>
     *   <li><b>当前问题</b> — 用户本次输入的消息</li>
     * </ol>
     * 核心策略：将全部上下文通过 Prompt Injection 方式嵌入到用户消息中，
     * 而非使用 messages 数组的多角色分离方式。
     * </p>
     *
     * @param message     用户当前消息
     * @param dishes      所有菜品列表
     * @param userProfile 用户画像
     * @param userid      用户 ID
     * @param history     历史对话记录
     * @return 完整的提示词文本
     */
    private String buildPrompt(String message, List<DishInfo> dishes, User userProfile, Integer userid, List<Map<String, String>> history) {
        // ---------- 构建菜品上下文列表（最多 30 道） ----------
        StringBuilder dishContext = new StringBuilder();
        for (int i = 0; i < Math.min(dishes.size(), 30); i++) {
            DishInfo d = dishes.get(i);
            dishContext.append(i + 1)
                    .append(". Name: ").append(nvl(d.getDishName()))
                    .append("; Chef: ").append(nvl(d.getChefName()))
                    .append("; Category: ").append(nvl(d.getDishTypeName()))
                    .append("; Desc: ").append(nvl(d.getDishDesc()))
                    .append("\n");
        }

        // ---------- 系统指令：角色设定和回答规则 ----------
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个社区食堂的智能助手，用简体中文回答问题。\n");
        prompt.append("根据用户的问题自然地回答，问什么答什么，不要强制推荐菜品或给出饮食建议。\n");
        prompt.append("只有在用户主动问菜谱推荐、营养搭配或点餐建议时，才结合下方菜品数据给出推荐。\n");
        prompt.append("使用纯文本，不要用Markdown格式、不要用emoji、不要用装饰符号。\n");
        prompt.append("使用正确的中文标点符号。\n");

        // ---------- 用户画像：用于个性化推荐 ----------
        if (userProfile != null) {
            prompt.append("\n[用户信息]\n");
            if (userProfile.getUsername() != null) {
                prompt.append("姓名：").append(userProfile.getUsername()).append("\n");
            }
            if (userProfile.getGender() != null) {
                prompt.append("性别：").append(userProfile.getGender()).append("\n");
            }
            if (userProfile.getBirthday() != null) {
                // 根据生日计算用户年龄
                long ageMs = System.currentTimeMillis() - userProfile.getBirthday().getTime();
                int ageYears = (int) (ageMs / (365.25 * 24 * 60 * 60 * 1000));
                prompt.append("年龄：大约").append(ageYears).append("岁\n");
            }
            if (userProfile.getDietaryTags() != null && !userProfile.getDietaryTags().trim().isEmpty()) {
                prompt.append("饮食偏好：").append(userProfile.getDietaryTags()).append("\n");
            }
            if (userProfile.getHealthNotes() != null && !userProfile.getHealthNotes().trim().isEmpty()) {
                prompt.append("健康备注：").append(userProfile.getHealthNotes()).append("\n");
            }
            prompt.append("当用户询问饮食建议时，参考以上信息给出个性化推荐。\n");
        }

        // ---------- 最近点餐记录（最近 10 条） ----------
        // 当 userid 不为 null 时，从 meal_order 表查询该用户最近 10 条订单，按菜品名称、数量、餐段、日期格式化后注入提示词。
        if (userid != null) {
            Map<String, Object> searchParam = new HashMap<>();
            searchParam.put("userid", userid);
            searchParam.put("begin", 0);
            searchParam.put("size", 10);
            List<MealOrder> recentOrders = mealOrderMapper.selectBySearch(searchParam);
            if (recentOrders != null && !recentOrders.isEmpty()) {
                prompt.append("\n[最近点餐记录]\n");
                for (MealOrder order : recentOrders) {
                    if (order.getDishName() != null) {
                        prompt.append(order.getDishName());
                        if (order.getQuantity() != null) {
                            prompt.append(" x").append(order.getQuantity());
                        }
                        if (order.getMealSlot() != null) {
                            prompt.append("（").append(order.getMealSlot()).append("）");
                        }
                        if (order.getMealOrdertime() != null) {
                            prompt.append(" ").append(new java.text.SimpleDateFormat("MM-dd").format(order.getMealOrdertime()));
                        }
                        prompt.append("\n");
                    }
                }
                prompt.append("可以参考用户的历史点餐习惯，在推荐时考虑用户经常选择的菜品。\n");
            }
        }

        // ---------- 菜品数据：仅推荐时参考 ----------
        prompt.append("\n下方是食堂当前的菜品数据，仅在需要推荐时参考：\n\n");
        prompt.append(dishContext).append("\n");

        // ---------- 历史对话：保持多轮上下文记忆 ----------
        if (history != null && !history.isEmpty()) {
            prompt.append("[历史对话]\n");
            for (Map<String, String> msg : history) {
                String role = msg.get("role");
                String text = msg.get("text");
                if (role != null && text != null && !text.trim().isEmpty()) {
                    if ("user".equals(role)) {
                        prompt.append("用户：").append(text).append("\n");
                    } else if ("assistant".equals(role)) {
                        prompt.append("助手：").append(text).append("\n");
                    }
                }
            }
            prompt.append("\n");
        }

        // ---------- 当前问题 ----------
        prompt.append("当前问题：\n").append(nvl(message));

        return prompt.toString();
    }

    /**
     * 调用 OpenAI 兼容的 /v1/chat/completions 端点
     * <p>
     * 构建 messages 数组（system + user），发送 POST 请求并解析响应。
     * 支持普通模式和流式（stream）模式：
     * <ul>
     *   <li><b>普通模式</b> — 直接从 response.choices[0].message.content 获取文本</li>
     *   <li><b>流式模式</b> — 解析 SSE 格式的 data: 行，逐片段拼接 delta 内容</li>
     * </ul>
     * </p>
     *
     * @param url        API 完整 URL
     * @param headers    HTTP 请求头（含 Bearer Token）
     * @param prompt     构建好的提示词文本
     * @param streamMode 是否使用流式模式
     * @return AI 回复文本
     * @throws Exception 请求失败或解析异常时抛出
     */
    private String requestChatCompletions(String url, HttpHeaders headers, String prompt, boolean streamMode) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);

        // Build messages array: system + prompt (prompt already includes history + user profile + dish data)
        List<Object> messages = new ArrayList<>();
        messages.add(createMessage("system", "你是一个社区食堂的智能助手。根据用户的问题自然地回答，问什么答什么。只有用户主动询问菜谱、营养或点餐建议时才推荐菜品。使用纯文本，不用Markdown、不用emoji、不用装饰符号。使用正确中文标点。"));
        messages.add(createMessage("user", prompt));
        body.put("messages", messages);
        if (streamMode) {
            body.put("stream", true);
            Map<String, Object> streamOptions = new HashMap<>();
            streamOptions.put("include_usage", false);
            body.put("stream_options", streamOptions);
        }

        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        if (streamMode) {
            String streamText = parseStreamResponse(response.getBody());
            if (!streamText.trim().isEmpty()) {
                return streamText;
            }
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (contentNode.isTextual()) {
            return contentNode.asText();
        }
        if (contentNode.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode n : contentNode) {
                String text = n.path("text").asText("");
                if (!text.isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(text);
                }
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * 解析流式（SSE）响应
     * <p>
     * OpenAI 流式 API 返回 Server-Sent Events 格式数据，
     * 每行以 "data: " 开头，包含 JSON 片段，最后以 "data: [DONE]" 结束。
     * 本方法遍历所有 data: 行，提取 delta.content 并拼接为完整文本。
     * </p>
     *
     * @param raw SSE 格式的原始响应字符串
     * @return 拼接后的完整文本内容
     * @throws Exception JSON 解析异常
     */
    private String parseStreamResponse(String raw) throws Exception {
        if (raw == null || raw.trim().isEmpty() || !raw.contains("data:")) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String[] lines = raw.split("\\r?\\n");
        for (String line : lines) {
            if (line == null) {
                continue;
            }
            String trimmed = line.trim();
            if (!trimmed.startsWith("data:")) {
                continue;
            }
            String data = trimmed.substring(5).trim();
            if (data.isEmpty() || "[DONE]".equals(data)) {
                continue;
            }
            JsonNode node = objectMapper.readTree(data);
            String delta = node.path("choices").path(0).path("delta").path("content").asText("");
            if (delta.isEmpty()) {
                JsonNode msgNode = node.path("choices").path(0).path("message").path("content");
                if (msgNode.isTextual()) {
                    delta = msgNode.asText("");
                }
            }
            if (!delta.isEmpty()) {
                sb.append(delta);
            }
        }
        return sb.toString();
    }

    /**
     * 调用备用端点 /v1/responses
     * <p>
     * 当主端点 /v1/chat/completions 不可用时，尝试调用 responses 端点。
     * 解析 output_text 或 output[].content[].text 字段获取回复文本。
     * </p>
     *
     * @param url     API 完整 URL
     * @param headers HTTP 请求头
     * @param prompt  构建好的提示词文本
     * @return AI 回复文本，失败或空时返回空字符串
     * @throws Exception 请求失败或解析异常时抛出
     */
    private String requestResponses(String url, HttpHeaders headers, String prompt) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("input", "You are a smart community meal planning and nutrition assistant.\n\n" + prompt);

        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        JsonNode root = objectMapper.readTree(response.getBody());

        JsonNode outputText = root.path("output_text");
        if (outputText.isTextual() && !outputText.asText("").trim().isEmpty()) {
            return outputText.asText();
        }

        JsonNode output = root.path("output");
        if (!output.isArray()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (JsonNode out : output) {
            JsonNode content = out.path("content");
            if (!content.isArray()) {
                continue;
            }
            for (JsonNode c : content) {
                String text = c.path("text").asText("");
                if (!text.isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(text);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 判断是否为 usage 类型兼容错误
     * <p>
     * 某些 API 实现（如部分代理网关）在返回 usage.prompt_tokens 时
     * 会遇到 "cannot unmarshal string" 的 JSON 解析错误。
     * 此时可通过 stream 模式绕过该问题。
     * </p>
     *
     * @param e HTTP 状态码异常
     * @return true 如果是 usage 类型兼容错误
     */
    private boolean isUsageTypeCompatError(HttpStatusCodeException e) {
        if (e == null) {
            return false;
        }
        String body = safeBody(e.getResponseBodyAsString());
        return body.contains("usage.prompt_tokens") && body.contains("cannot unmarshal string");
    }

    /**
     * 构建 /v1/chat/completions 完整 URL
     *
     * @return 主端点 URL
     */
    private String buildChatCompletionsUrl() {
        String base = normalizeBaseUrl(apiUrl);
        return base + "/v1/chat/completions";
    }

    /**
     * 构建 /v1/responses 完整 URL（备用端点）
     *
     * @return 备用端点 URL
     */
    private String buildResponsesUrl() {
        String base = normalizeBaseUrl(apiUrl);
        return base + "/v1/responses";
    }

    /**
     * 规范化 API 基础 URL
     * <p>
     * 从配置的 URL 中提取基础路径（去掉 /v1/... 部分），
     * 去除尾部斜杠。若配置为空则返回默认值。
     * </p>
     *
     * @param configuredUrl 配置的 API URL
     * @return 规范化的基础 URL
     */
    private String normalizeBaseUrl(String configuredUrl) {
        String configured = nvl(configuredUrl).trim();
        if (configured.isEmpty()) {
            return "https://api.openai.com";
        }

        int v1Index = configured.indexOf("/v1/");
        String base = v1Index > 0 ? configured.substring(0, v1Index) : configured;
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    /**
     * 创建一条 chat message
     *
     * @param role    角色（system / user / assistant）
     * @param content 消息内容
     * @return 包含 role 和 content 的 Map
     */
    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> map = new HashMap<>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }

    /**
     * 安全获取响应体文本（避免 null）
     *
     * @param v 原始响应体字符串
     * @return 非空字符串，null 或空时返回 "(empty response)"
     */
    private String safeBody(String v) {
        if (v == null || v.trim().isEmpty()) {
            return "(empty response)";
        }
        return v;
    }

    /**
     * null 安全转换：null → 空字符串
     *
     * @param v 原始字符串
     * @return 非 null 的字符串
     */
    private String nvl(String v) {
        return v == null ? "" : v;
    }

    /**
     * 本地兜底推荐（Local Fallback）
     * <p>
     * 当所有 API 端点都不可用时，使用简单规则生成推荐结果。
     * 算法：
     * <ul>
     *   <li>仅推荐上架且有库存的菜品</li>
     *   <li>关键词 "fat loss/low calorie/diet" → 按热量升序排列（低卡推荐）</li>
     *   <li>关键词 "muscle/high protein/protein" → 按蛋白质降序排列（高蛋白推荐）</li>
     *   <li>其他 → 直接取前 3 道菜</li>
     * </ul>
     * </p>
     *
     * @param question    用户问题文本
     * @param dishes      所有菜品列表
     * @param userProfile 用户画像（当前未使用，预留扩展）
     * @return 本地推荐文本
     */
    private String generateLocalAdvice(String question, List<DishInfo> dishes, User userProfile) {
        if (dishes == null || dishes.isEmpty()) {
            return "No dish data found in database yet.";
        }

        String q = question == null ? "" : question.toLowerCase(Locale.ROOT);
        List<DishInfo> available = dishes.stream()
                .filter(d -> d.getIsAvailable() == null || d.getIsAvailable() == 1)
                .filter(d -> d.getStockQty() == null || d.getStockQty() > 0)
                .collect(Collectors.toList());
        if (available.isEmpty()) {
            available = new ArrayList<>(dishes);
        }

        Comparator<DishInfo> byCalories = Comparator.comparing(d -> d.getCaloriesKcal() == null ? new BigDecimal("99999") : d.getCaloriesKcal());
        Comparator<DishInfo> byProtein = Comparator.comparing((DishInfo d) -> d.getProteinG() == null ? BigDecimal.ZERO : d.getProteinG()).reversed();

        List<DishInfo> picked;
        String title;
        if (q.contains("fat loss") || q.contains("low calorie") || q.contains("diet")) {
            picked = available.stream().sorted(byCalories).limit(3).collect(Collectors.toList());
            title = "Low-calorie suggestions:";
        } else if (q.contains("muscle") || q.contains("high protein") || q.contains("protein")) {
            picked = available.stream().sorted(byProtein).limit(3).collect(Collectors.toList());
            title = "High-protein suggestions:";
        } else {
            picked = available.stream().limit(3).collect(Collectors.toList());
            title = "Recommended dishes:";
        }

        StringBuilder sb = new StringBuilder(title).append("\n");
        for (int i = 0; i < picked.size(); i++) {
            DishInfo d = picked.get(i);
            sb.append(i + 1)
                    .append(". ").append(nvl(d.getDishName()))
                    .append(" | price: ").append(d.getDishPrice() == null ? "0.00" : d.getDishPrice())
                    .append(" | kcal: ").append(d.getCaloriesKcal() == null ? "N/A" : d.getCaloriesKcal())
                    .append(" | protein(g): ").append(d.getProteinG() == null ? "N/A" : d.getProteinG())
                    .append(" | stock: ").append(d.getStockQty() == null ? "N/A" : d.getStockQty())
                    .append("\n");
        }
        sb.append("Tip: combine vegetables + quality protein + whole grains for balanced meals.");
        return sb.toString();
    }
}
