import request from '@/utils/request'

// 登录
export function residentLogin(data) {
  return request({
    url: '/resident/login',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getResidentInfo(token) {
  return request({
    url: '/resident/info',
    method: 'get',
    params: { token }
  })
}

// 登出
export function residentLogout(token) {
  return request({
    url: '/resident/logout',
    method: 'post',
    params: { token }
  })
}

// 注册
export function registerResident(params) {
  return request({
    url: '/resident/register',
    method: 'post',
    params
  })
}

// 更新个人信息
export function updateResidentProfile(data) {
  return request({
    url: '/resident/updateProfile',
    method: 'post',
    data
  })
}

// 修改密码
export function alterResidentPassword(params) {
  return request({
    url: '/resident/alterPassword',
    method: 'post',
    params
  })
}

// 获取用户数量
export function getResidentCount() {
  return request({
    url: '/resident/getCount',
    method: 'get'
  })
}
export const getCount = getResidentCount

// 查询所有用户信息
export function queryResidents() {
  return request({
    url: '/resident/queryResidents',
    method: 'get'
  })
}

// 分页查询用户信息
export function queryResidentsByPage(params) {
  return request({
    url: '/resident/queryResidentsByPage',
    method: 'get',
    params
  })
}

// 添加用户信息
export function addResident(data) {
  return request({
    url: '/resident/addResident',
    method: 'post',
    data
  })
}

// 删除用户信息
export function deleteResident(data) {
  return request({
    url: '/resident/deleteResident',
    method: 'delete',
    data
  })
}

// 删除一些用户信息
export function deleteResidents(data) {
  return request({
    url: '/resident/deleteResidents',
    method: 'delete',
    data
  })
}

// 更新用户信息
export function updateResident(data) {
  return request({
    url: '/resident/updateResident',
    method: 'put',
    data
  })
}
