import request from '@/utils/request'

function normalizeDish(item = {}) {
  return {
    ...item,
    dishId: item.dishId ?? null,
    dishName: item.dishName ?? '',
    chefName: item.chefName ?? '',
    dishPrice: item.dishPrice ?? 0,
    dishTypeId: item.dishTypeId ?? null,
    dishTypeName: item.dishTypeName ?? '',
    dishDesc: item.dishDesc ?? '',
    dishImg: item.dishImg ?? '',
    isReserved: item.isReserved ?? 0,
    stockQty: item.stockQty ?? 0,
    isAvailable: item.isAvailable ?? 1
  }
}

function toBackendDishPayload(data = {}) {
  return { ...data }
}

function toBackendDishQuery(params = {}) {
  return { ...params }
}

export function getDishCount() {
  return request({
    url: '/dishInfo/getCount',
    method: 'get'
  })
}
export const getCount = getDishCount

export function queryDishes() {
  return request({
    url: '/dishInfo/queryDishes',
    method: 'get'
  }).then(res => (res || []).map(normalizeDish))
}

export function queryDishesByPage(params) {
  return request({
    url: '/dishInfo/queryDishesByPage',
    method: 'get',
    params: toBackendDishQuery(params)
  }).then(res => ({
    ...(res || {}),
    data: ((res && res.data) || []).map(normalizeDish)
  }))
}

export function addDish(data) {
  return request({
    url: '/dishInfo/addDish',
    method: 'post',
    data: toBackendDishPayload(data)
  })
}

export function deleteDish(data) {
  return request({
    url: '/dishInfo/deleteDish',
    method: 'delete',
    data: toBackendDishPayload(data)
  })
}

export function deleteDishes(data) {
  return request({
    url: '/dishInfo/deleteDishes',
    method: 'delete',
    data: (data || []).map(toBackendDishPayload)
  })
}
export const deleteDishs = deleteDishes

export function updateDish(data) {
  return request({
    url: '/dishInfo/updateDish',
    method: 'put',
    data: toBackendDishPayload(data)
  })
}
