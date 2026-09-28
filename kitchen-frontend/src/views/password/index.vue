<template>
  <div class="app-container">
    <el-card class="profile-card" shadow="hover">
      <div slot="header" class="card-header">
        <span><i class="el-icon-user"></i> 个人信息管理</span>
      </div>

      <el-row :gutter="40">
        <el-col :xs="24" :sm="24" :md="12">
          <h3 class="section-title">基本信息</h3>
          <el-form ref="profileForm" :model="profileForm" label-width="100px">
            <el-form-item label="姓名">
              <el-input :value="name" disabled />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
            <el-form-item label="性别">
              <el-select v-model="profileForm.gender" placeholder="请选择性别" style="width: 100%;">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
            <el-form-item label="出生日期">
              <el-date-picker v-model="profileForm.birthday" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" style="width: 100%;" />
            </el-form-item>
            <el-form-item label="楼栋">
              <el-input v-model="profileForm.buildingNo" placeholder="请输入楼栋" />
            </el-form-item>
            <el-form-item label="单元">
              <el-input v-model="profileForm.unitNo" placeholder="请输入单元" />
            </el-form-item>
            <el-form-item label="房号">
              <el-input v-model="profileForm.roomNo" placeholder="请输入房号" />
            </el-form-item>
            <el-form-item label="饮食偏好">
              <el-input v-model="profileForm.dietaryTags" placeholder="如：少油、低盐、素食" />
            </el-form-item>
            <el-form-item label="健康提示">
              <el-input v-model="profileForm.healthNotes" type="textarea" :rows="2" placeholder="如：对海鲜过敏" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileLoading" icon="el-icon-check" @click="onProfileSubmit">保存信息</el-button>
            </el-form-item>
          </el-form>
        </el-col>

        <el-col :xs="24" :sm="24" :md="12">
          <h3 class="section-title">密码修改</h3>
          <el-form ref="form" :model="form" :rules="passwordRules" label-width="100px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入旧密码" prefix-icon="el-icon-key" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码" prefix-icon="el-icon-lock" />
            </el-form-item>
            <el-form-item label="确认密码" prop="repeat">
              <el-input v-model="form.repeat" type="password" show-password placeholder="请再次输入新密码" prefix-icon="el-icon-lock" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" icon="el-icon-check" @click="onSubmit">确认修改</el-button>
              <el-button icon="el-icon-refresh-right" @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>

          <div class="password-tips">
            <h4><i class="el-icon-info"></i> 安全提示</h4>
            <ul>
              <li>建议定期修改密码。</li>
              <li>密码长度建议不少于 6 位，并包含数字和字母。</li>
              <li>不要与其他网站使用相同密码。</li>
            </ul>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import { alterResidentPassword, updateResidentProfile, getResidentInfo } from "@/api/resident";

export default {
  name: "Profile",
  data() {
    const validateRepeat = (rule, value, callback) => {
      if (value !== this.form.newPassword) {
        callback(new Error("两次输入的密码不一致"));
      } else {
        callback();
      }
    };
    const validateNewPassword = (rule, value, callback) => {
      if (value === this.form.oldPassword) {
        callback(new Error("新密码不能与旧密码相同"));
      } else {
        callback();
      }
    };

    return {
      loading: false,
      profileLoading: false,
      form: {
        oldPassword: "",
        newPassword: "",
        repeat: ""
      },
      profileForm: {
        phone: "",
        gender: "",
        birthday: "",
        buildingNo: "",
        unitNo: "",
        roomNo: "",
        dietaryTags: "",
        healthNotes: ""
      },
      passwordRules: {
        oldPassword: [{ required: true, message: "请输入旧密码", trigger: "blur" }],
        newPassword: [
          { required: true, message: "请输入新密码", trigger: "blur" },
          { min: 6, message: "密码长度至少 6 位", trigger: "blur" },
          { validator: validateNewPassword, trigger: "blur" }
        ],
        repeat: [
          { required: true, message: "请再次输入新密码", trigger: "blur" },
          { validator: validateRepeat, trigger: "blur" }
        ]
      }
    };
  },
  computed: {
    ...mapGetters(["id", "name", "roles", "token"])
  },
  mounted() {
    this.fetchProfile();
  },
  methods: {
    fetchProfile() {
      getResidentInfo(this.token).then((res) => {
        console.log("getResidentInfo response:", res);
        if (res && res.status === 200 && res.data) {
          const user = res.data;
          this.profileForm = {
            phone: user.phone || "",
            gender: user.gender || "",
            birthday: user.birthday || "",
            buildingNo: user.buildingNo || "",
            unitNo: user.unitNo || "",
            roomNo: user.roomNo || "",
            dietaryTags: user.dietaryTags || "",
            healthNotes: user.healthNotes || ""
          };
          console.log("Profile loaded:", this.profileForm);
        }
      }).catch((err) => {
        console.error("Failed to fetch profile:", err);
      });
    },
    onProfileSubmit() {
      console.log("Saving profile - user id:", this.id);
      if (!this.id) {
        this.$message.error("未获取到用户ID，请重新登录");
        return;
      }

      this.profileLoading = true;

      const payload = {
        userid: this.id,
        phone: this.profileForm.phone,
        gender: this.profileForm.gender,
        birthday: this.profileForm.birthday,
        buildingNo: this.profileForm.buildingNo,
        unitNo: this.profileForm.unitNo,
        roomNo: this.profileForm.roomNo,
        dietaryTags: this.profileForm.dietaryTags,
        healthNotes: this.profileForm.healthNotes
      };
      console.log("Update profile payload:", payload);

      updateResidentProfile(payload)
        .then((res) => {
          console.log("Update profile response:", res);
          if (res === 1) {
            this.$message.success("个人信息保存成功");
          } else {
            this.$message.error("保存失败，请检查后端日志");
          }
        })
        .catch((err) => {
          console.error("Update profile error:", err);
          this.$message.error("保存失败: " + (err.message || "未知错误"));
        })
        .finally(() => {
          this.profileLoading = false;
        });
    },
    onSubmit() {
      this.$refs.form.validate((valid) => {
        if (!valid) return;
        this.loading = true;
        const isadmin = this.roles && this.roles[0] === "admin" ? 1 : 0;

        alterResidentPassword({
          userid: this.id,
          username: this.name,
          isadmin,
          oldPassword: this.form.oldPassword,
          newPassword: this.form.newPassword
        })
          .then((res) => {
            if (res === 1) {
              this.$message.success("密码修改成功");
              this.resetForm();
            } else {
              this.$message.error("旧密码不正确");
            }
          })
          .finally(() => {
            this.loading = false;
          });
      });
    },
    resetForm() {
      this.$refs.form.resetFields();
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.profile-card {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
}
.card-header {
  font-size: 18px;
  font-weight: 600;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  color: #303133;
}
.password-tips {
  margin-top: 30px;
  padding: 12px;
  background: #f7f9fc;
  border-radius: 6px;
}
.password-tips h4 {
  margin: 0 0 8px;
}
.password-tips ul {
  margin: 0;
  padding-left: 18px;
}
.password-tips li {
  line-height: 1.8;
  color: #606266;
}
</style>
