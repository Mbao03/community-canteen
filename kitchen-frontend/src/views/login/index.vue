<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="title">智慧社区食堂系统</h2>
      <p class="subtitle">健康饮食，便捷订餐</p>

      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" autocomplete="on" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            :type="passwordType"
            v-model="loginForm.password"
            placeholder="请输入密码"
            autocomplete="on"
            @keyup.enter.native="handleLogin"
          >
            <i
              slot="suffix"
              :class="passwordType === 'password' ? 'el-icon-view' : 'el-icon-hide'"
              class="pwd-toggle"
              @click="togglePwd"
            />
          </el-input>
        </el-form-item>

        <el-form-item label="登录身份" prop="isadmin">
          <el-select v-model="loginForm.isadmin" style="width: 100%">
            <el-option :value="0" label="居民" />
            <el-option :value="1" label="管理员" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin">登录</el-button>
        </el-form-item>
        <el-form-item>
          <el-button plain style="width: 100%" @click="handleRegister">注册账号</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      loading: false,
      passwordType: "password",
      loginForm: {
        username: "",
        password: "",
        isadmin: 0
      },
      loginRules: {
        username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
        isadmin: [{ required: true, message: "请选择登录身份", trigger: "change" }]
      }
    };
  },
  methods: {
    togglePwd() {
      this.passwordType = this.passwordType === "password" ? "text" : "password";
    },
    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (!valid) return;
        this.loading = true;
        this.$store
          .dispatch("user/login", this.loginForm)
          .then(() => {
            this.$router.push({ path: "/" });
          })
          .catch((e) => {
            const msg = e && e.response && e.response.data ? e.response.data : (e.message || "登录失败");
            this.$message.error(msg);
          })
          .finally(() => {
            this.loading = false;
          });
      });
    },
    handleRegister() {
      this.$router.push({ path: "/register" });
    }
  }
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #dbeafe, #eff6ff);
}
.login-card {
  width: 420px;
  padding: 28px;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}
.title {
  margin: 0;
  font-size: 26px;
  color: #1e3a8a;
  text-align: center;
}
.subtitle {
  margin: 8px 0 18px;
  text-align: center;
  color: #64748b;
}
.pwd-toggle {
  cursor: pointer;
}
</style>
