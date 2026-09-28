package com.example.kitchen.dto;

import java.util.List;
import java.util.Map;

/**
 * AI 聊天请求体 DTO
 * <p>
 * 封装前端发送给 /ai/chat 接口的请求数据，
 * 包含用户消息、用户画像和历史对话记录。
 * </p>
 */
public class AiChatRequest {

    /** 用户当前输入的文本消息 */
    private String message;

    /** 用户画像（含个人基本信息和饮食偏好） */
    private UserProfile userProfile;

    /**
     * 历史对话记录
     * <p>
     * 每项为一个 Map，包含：
     * <ul>
     *   <li>"role" — 角色："user" 或 "assistant"</li>
     *   <li>"text" — 对应角色的消息文本</li>
     * </ul>
     * 用于保持多轮对话的上下文记忆。
     * </p>
     */
    private List<Map<String, String>> history;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public UserProfile getUserProfile() { return userProfile; }
    public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }
    public List<Map<String, String>> getHistory() { return history; }
    public void setHistory(List<Map<String, String>> history) { this.history = history; }

    /**
     * 用户画像 — 前端传入的个人信息与饮食偏好
     * <p>
     * 包含用户基本信息（姓名、性别、生日、联系方式、住址）
     * 以及个性化饮食标签和健康备注，用于 AI 做个性化推荐。
     * </p>
     */
    public static class UserProfile {
        /** 用户 ID */
        private Integer userid;
        /** 用户姓名 */
        private String username;
        /** 联系电话 */
        private String phone;
        /** 性别 */
        private String gender;
        /** 生日（字符串格式 "yyyy-MM-dd"） */
        private String birthday;
        /** 楼栋号 */
        private String buildingNo;
        /** 单元号 */
        private String unitNo;
        /** 房间号 */
        private String roomNo;
        /** 饮食偏好标签（如 "低脂,高蛋白"） */
        private String dietaryTags;
        /** 健康备注（如 "高血压,糖尿病"） */
        private String healthNotes;

        public Integer getUserid() { return userid; }
        public void setUserid(Integer userid) { this.userid = userid; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public String getBirthday() { return birthday; }
        public void setBirthday(String birthday) { this.birthday = birthday; }
        public String getBuildingNo() { return buildingNo; }
        public void setBuildingNo(String buildingNo) { this.buildingNo = buildingNo; }
        public String getUnitNo() { return unitNo; }
        public void setUnitNo(String unitNo) { this.unitNo = unitNo; }
        public String getRoomNo() { return roomNo; }
        public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
        public String getDietaryTags() { return dietaryTags; }
        public void setDietaryTags(String dietaryTags) { this.dietaryTags = dietaryTags; }
        public String getHealthNotes() { return healthNotes; }
        public void setHealthNotes(String healthNotes) { this.healthNotes = healthNotes; }
    }
}
