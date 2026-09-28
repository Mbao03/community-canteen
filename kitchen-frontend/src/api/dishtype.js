import request from '@/utils/request'

function normalizeDishType(item = {}) {
  return {
    ...item,
    dishTypeId: item.dishTypeId ?? null,
    dishTypeName: item.dishTypeName ?? '',
    dishTypeDesc: item.dishTypeDesc ?? ''
  }
}

function toBackendDishTypePayload(data = {}) {
  return { ...data }
}

function toBackendDishTypeQuery(params = {}) {
  return { ...params }
}

export function getDishTypeCount() {
  return request({
    url: '/dishType/getCount',
    method: 'get'
  })
}
export const getCount = getDishTypeCount

export function queryDishTypes() {
  return request({
    url: '/dishType/queryDishTypes',
    method: 'get'
  }).then(res => (res || []).map(normalizeDishType))
}

export function queryDishTypesByPage(params) {
  return request({
    url: '/dishType/queryDishTypesByPage',
    method: 'get',
    params: toBackendDishTypeQuery(params)
  }).then(res => ({
    ...(res || {}),
    data: ((res && res.data) || []).map(normalizeDishType)
  }))
}

export function addDishType(data) {
  return request({
    url: '/dishType/addDishType',
    method: 'post',
    data: toBackendDishTypePayload(data)
  })
}

export function deleteDishType(data) {
  return request({
    url: '/dishType/deleteDishType',
    method: 'delete',
    data: toBackendDishTypePayload(data)
  })
}

export function deleteDishTypes(data) {
  return request({
    url: '/dishType/deleteDishTypes',
    method: 'delete',
    data: (data || []).map(toBackendDishTypePayload)
  })
}

export function updateDishType(data) {
  return request({
    url: '/dishType/updateDishType',
    method: 'put',
    data: toBackendDishTypePayload(data)
  })
}
