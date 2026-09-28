import request from '@/utils/request'

export function getUserRecommendations(userid, limit = 5) {
  return request({
    url: '/recommend/user',
    method: 'get',
    params: {
      userid,
      limit
    }
  })
}
