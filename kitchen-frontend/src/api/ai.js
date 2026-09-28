/**
 * AI 聊天 API 客户端
 *
 * 提供与后端 AI 聊天的接口调用，包含：
 * - chatWithUserProfile: 携带用户画像的聊天请求（AiChat.vue 使用）
 * - chatWithOpenAI: 不带用户画像的聊天请求（兼容旧版）
 * - tryDecodeMojibake: 自动检测并修复中文乱码
 *
 * 乱码修复说明：
 * 部分 AI 网关在返回中文时可能出现 UTF-8 字节被误解码为 Latin1 的乱码。
 * 检测机制：查找 Latin1 字符集中常见的 UTF-8 多字节标记字符，
 * 然后按字节重新解码为 UTF-8。
 */

import request from '@/utils/request'

/**
 * 检测文本是否为 UTF-8 乱码（Mojibake）
 *
 * 原理：检测文本中是否包含 UTF-8 多字节编码被误解码后产生的
 * Latin1 字符（如 Ã、Â、â、å 等），且不含 CJK 汉字。
 *
 * @param {string} text 待检测文本
 * @returns {boolean} true 表示可能是乱码
 */
function looksLikeMojibake(text) {
  if (!text || typeof text !== 'string') return false
  // 常见的 UTF-8 多字节被误解码为 Latin1 的标记字符
  const hasMarkers = /[\u00C3\u00C2\u00E2\u00E5\u00E6\u00E7\u00E9\u00E8\u00EF\u00F0]/.test(text)
  // 是否包含 CJK 汉字
  const hasCjk = /[\u4e00-\u9fff]/.test(text)
  // 有 Latin1 标记字符但不含汉字 → 很可能是乱码
  return hasMarkers && !hasCjk
}

/**
 * 尝试修复 UTF-8 乱码
 *
 * 将字符串的每个字符取其低 8 位（charCodeAt & 0xff）组成字节数组，
 * 然后用 TextDecoder 以 UTF-8 重新解码。
 *
 * @param {string} text 可能乱码的文本
 * @returns {string} 修复后的文本，不是乱码则原样返回
 */
function tryDecodeMojibake(text) {
  if (!looksLikeMojibake(text)) return text
  try {
    const bytes = new Uint8Array(Array.from(text).map(ch => ch.charCodeAt(0) & 0xff))
    return new TextDecoder('utf-8').decode(bytes)
  } catch (e) {
    return text
  }
}

/**
 * 发送 AI 聊天请求（带用户画像）
 *
 * 将用户消息、用户画像和历史对话记录发送给后端 /ai/chat 接口，
 * 后端会将这些信息构建为提示词发送给大模型。
 * 收到回复后自动检测并修复可能的乱码。
 *
 * @param {string} message     用户当前消息文本
 * @param {Object} userProfile 用户画像对象（含 userid、username、gender 等）
 * @param {Array}  history     历史对话记录 [{ role, text }]
 * @returns {string} AI 回复文本
 * @throws API 请求失败时抛出异常
 */
export async function chatWithUserProfile(message, userProfile, history = []) {
  const res = await request({
    url: '/ai/chat',
    method: 'post',
    data: {
      message,
      history,
      userProfile
    }
  })

  if (!res || res.status !== 200) {
    throw new Error((res && res.message) || 'AI backend request failed.')
  }

  return tryDecodeMojibake(res.data || '')
}

/**
 * 发送 AI 聊天请求（不带用户画像，兼容旧版）
 *
 * @param {string} message 用户当前消息文本
 * @param {Array}  history 历史对话记录 [{ role, text }]
 * @returns {string} AI 回复文本
 * @throws API 请求失败时抛出异常
 */
export async function chatWithOpenAI(message, history = []) {
  const res = await request({
    url: '/ai/chat',
    method: 'post',
    data: {
      message,
      history
    }
  })

  if (!res || res.status !== 200) {
    throw new Error((res && res.message) || 'AI backend request failed.')
  }

  return tryDecodeMojibake(res.data || '')
}
