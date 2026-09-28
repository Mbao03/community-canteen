<template>
  <div class="register-container">
    <div class="register-card">
      <h2>注册账号</h2>
      <p>加入智慧社区食堂系统</p>
      <el-form ref="form" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="repeat">
          <el-input v-model="form.repeat" type="password" show-password placeholder="请再次输入密码" @keyup.enter.native="submit" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">确认注册</el-button>
          <el-button @click="goLogin">返回登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { registerResident } from "@/api/resident";

export default {
  name: "Register",
  data() {
    const validateRepeat = (rule, value, callback) => {
      if (value !== this.form.password) {
        callback(new Error("两次输入的密码不一致"));
      } else {
        callback();
      }
    };
    return {
      loading: false,
      form: {
        username: "",
        password: "",
        repeat: ""
      },
      rules: {
        username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
        repeat: [
          { required: true, message: "请再次输入密码", trigger: "blur" },
          { validator: validateRepeat, trigger: "blur" }
        ]
      }
    };
  },
  methods: {
    submit() {
      this.$refs.form.validate((valid) => {
        if (!valid) return;
        this.loading = true;
        registerResident({
          username: this.form.username,
          password: this.form.password
        })
          .then((res) => {
            if (res === 0) {
              this.$message.error("注册失败，用户名可能已存在");
            } else {
              this.$message.success("注册成功");
              this.goLogin();
            }
          })
          .finally(() => {
            this.loading = false;
          });
      });
    },
    goLogin() {
      this.$router.push("/login");
    }
  }
};
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.register-card {
  width: 420px;
  padding: 24px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}
.register-card h2 {
  margin: 0 0 8px;
}
.register-card p {
  color: #909399;
  margin: 0 0 20px;
}
</style>
