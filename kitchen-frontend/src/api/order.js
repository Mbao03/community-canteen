import request from '@/utils/request'

function normalizeOrder(item = {}) {
  return {
    ...item,
    orderId: item.orderId ?? null,
    residentId: item.residentId ?? null,
    residentName: item.residentName ?? '',
    dishId: item.dishId ?? null,
    dishName: item.dishName ?? '',
    orderTimeStr: item.orderTimeStr ?? '',
    completeTimeStr: item.completeTimeStr ?? ''
  }
}

function toBackendOrderPayload(data = {}) {
  return { ...data }
}

function toBackendOrderQuery(params = {}) {
  return { ...params }
}

export function getOrderCount() {
  return request({
    url: '/order/getCount',
    method: 'get'
  })
}

export function queryOrdersByPage(params) {
  return request({
    url: '/order/queryOrdersByPage',
    method: 'get',
    params: toBackendOrderQuery(params)
  }).then(res => ({
    ...(res || {}),
    data: ((res && res.data) || []).map(normalizeOrder)
  }))
}

export function addOrder(data) {
  return request({
    url: '/order/addOrder',
    method: 'post',
    data: toBackendOrderPayload(data)
  })
}

export function deleteOrder(data) {
  return request({
    url: '/order/deleteOrder',
    method: 'delete',
    data: toBackendOrderPayload(data)
  })
}

export function deleteOrders(data) {
  return request({
    url: '/order/deleteOrders',
    method: 'delete',
    data: (data || []).map(toBackendOrderPayload)
  })
}

export function updateOrder(data) {
  return request({
    url: '/order/updateOrder',
    method: 'put',
    data: toBackendOrderPayload(data)
  })
}

export function placeOrder(residentId, dishId, extra = {}) {
  return request({
    url: '/order/placeOrder',
    method: 'post',
    params: {
      residentId,
      dishId,
      ...extra
    }
  })
}

export function completeOrder(orderId, dishId) {
  return request({
    url: '/order/completeOrder',
    method: 'post',
    params: {
      orderId,
      dishId
    }
  })
}
