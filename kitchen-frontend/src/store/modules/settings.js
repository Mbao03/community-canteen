
import defaultSettings from '@/settings'

const { showSettings, fixedHeader, sidebarLogo } = defaultSettings
const apiHost = (process.env.VUE_APP_API_BASE_URL || 'http://localhost:9111').replace(/\/+$/, '')
const apiContext = (process.env.VUE_APP_API_CONTEXT_PATH || '/MealManager').replace(/\/+$/, '')
const baseApi = `${apiHost}${apiContext}`

const state = {
  baseApi,
  showSettings: showSettings,
  fixedHeader: fixedHeader,
  sidebarLogo: sidebarLogo
}

const mutations = {
  CHANGE_SETTING: (state, { key, value }) => {
    // eslint-disable-next-line no-prototype-builtins
    if (state.hasOwnProperty(key)) {
      state[key] = value
    }
  }
}

const actions = {
  changeSetting({ commit }, data) {
    commit('CHANGE_SETTING', data)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
