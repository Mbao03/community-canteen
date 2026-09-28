import { residentLogin, residentLogout, getResidentInfo } from '@/api/resident'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { resetRouter } from '@/router'

const getDefaultState = () => {
  return {
    token: getToken(),
    id: 0,
    name: '',
    avatar: '',
    roles: []
  }
}

const state = getDefaultState()

const mutations = {
  RESET_STATE: (state) => {
    Object.assign(state, getDefaultState())
  },
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_ID: (state, id) => {
    state.id = id
  },
  SET_NAME: (state, name) => {
    state.name = name
  },
  SET_AVATAR: (state, avatar) => {
    state.avatar = avatar
  },
  SET_ROLES: (state, roles) => {
    state.roles = roles
  }
}

const actions = {
  // user login
  login({ commit }, userInfo) {
    const { username, password, isadmin } = userInfo
    return new Promise((resolve, reject) => {
      residentLogin({ username: username.trim(), userpassword: password, isadmin: isadmin }).then(response => {
        const { status, message, data } = response

        if(status !== 200) {
          reject(message)
        }

        commit('SET_TOKEN', data.token)
        setToken(data.token)
        resolve()
      }).catch(error => {
        reject(error)
      })
    })
  },

  // get user info
  getInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      getResidentInfo(state.token).then(response => {
        const { data } = response

        if (!data) {
          reject('登录状态失效，请重新登录')
        }

        data['avatar'] = '/pic/02.jpg'
        if (data.isadmin === 1){
          data['roles'] = ['admin']
          data['avatar'] = '/pic/05.jpg'
        }
        else data['roles'] = ['resident']


        const { userid, roles, username, avatar } = data

        // roles must be a non-empty array
        if (!roles || roles.length <= 0) {
          reject('获取角色失败，请重新登录')
        }

        commit('SET_ID', userid)
        commit('SET_ROLES', roles)
        commit('SET_NAME', username)
        commit('SET_AVATAR', avatar)
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  // user logout
  logout({ commit, state }) {
    return new Promise((resolve, reject) => {
      residentLogout(state.token).then(() => {
        removeToken() // must remove  token  first
        resetRouter()
        commit('RESET_STATE')
        resolve()
      }).catch(error => {
        reject(error)
      })
    })
  },

  // remove token
  resetToken({ commit }) {
    return new Promise(resolve => {
      removeToken() // must remove  token  first
      commit('RESET_STATE')
      resolve()
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
