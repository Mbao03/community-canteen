<template>
  <div class="app-container">
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="queryParam" size="small">
        <el-form-item label="居民姓名">
          <el-input v-model="queryParam.username" clearable placeholder="输入居民姓名" @keyup.enter.native="handleFilter" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleFilter">搜索</el-button>
          <el-button type="success" icon="el-icon-plus" @click="handleCreate">新增居民</el-button>
          <el-button type="danger" icon="el-icon-delete" :disabled="selectedRows.length === 0" @click="handleDeleteSome">批量删除</el-button>
          <el-button icon="el-icon-refresh" @click="handleShowAll">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table ref="multipleTable" :data="tableData" border stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" align="center" />
      <el-table-column prop="userid" label="ID" width="70" align="center" />
      <el-table-column prop="username" label="居民姓名" min-width="120" />
      <el-table-column prop="userpassword" label="登录密码" min-width="120" show-overflow-tooltip />
      <el-table-column label="角色" width="90" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isadmin === 1 ? 'warning' : 'success'">{{ scope.row.isadmin === 1 ? '管理员' : '居民' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" min-width="120" />
      <el-table-column label="住址" min-width="180">
        <template slot-scope="scope">{{ formatAddress(scope.row) }}</template>
      </el-table-column>
      <el-table-column prop="dietaryTags" label="饮食偏好" min-width="130" show-overflow-tooltip />
      <el-table-column prop="healthNotes" label="健康备注" min-width="160" show-overflow-tooltip />
      <el-table-column label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isActive === 0 ? 'info' : 'success'">{{ scope.row.isActive === 0 ? '停用' : '启用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="140" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" icon="el-icon-edit" @click="handleUpdate(scope.row)" circle />
          <el-button size="mini" type="danger" icon="el-icon-delete" @click="handleDelete(scope.row)" circle />
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 16px; text-align: right">
      <el-pagination
        background
        :current-page.sync="queryParam.page"
        :page-size="queryParam.limit"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="recordTotal"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog :title="formType === 0 ? '新增居民' : '编辑居民'" :visible.sync="dialogFormVisible" width="660px" :close-on-click-modal="false">
      <el-form ref="ruleForm" :model="form" :rules="rules" label-width="90px" size="small">
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="姓名" prop="username"><el-input v-model="form.username" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="密码" prop="userpassword"><el-input v-model="form.userpassword" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="角色" prop="isadmin"><el-radio-group v-model="form.isadmin"><el-radio :label="1">管理员</el-radio><el-radio :label="0">居民</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="楼栋"><el-input v-model="form.buildingNo" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="单元"><el-input v-model="form.unitNo" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="房号"><el-input v-model="form.roomNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="性别"><el-select v-model="form.gender" clearable style="width:100%"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-radio-group v-model="form.isActive"><el-radio :label="1">启用</el-radio><el-radio :label="0">停用</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="饮食偏好"><el-input v-model="form.dietaryTags" placeholder="如：少盐、低脂、无辣" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="健康备注"><el-input type="textarea" :rows="3" v-model="form.healthNotes" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import waves from "@/directive/waves";
import { getCount, queryResidentsByPage, addResident, deleteResident, deleteResidents, updateResident } from "@/api/resident";

const emptyResident = () => ({
  userid: null,
  username: "",
  userpassword: "",
  isadmin: 0,
  phone: "",
  gender: "",
  buildingNo: "",
  unitNo: "",
  roomNo: "",
  dietaryTags: "",
  healthNotes: "",
  isActive: 1
});

export default {
  name: "ResidentIndex",
  directives: { waves },
  data() {
    return {
      tableData: [],
      recordTotal: 0,
      selectedRows: [],
      queryParam: { page: 1, limit: 10, username: null },
      dialogFormVisible: false,
      formType: 0,
      form: emptyResident(),
      rules: {
        username: [{ required: true, message: "请输入居民姓名", trigger: "blur" }],
        userpassword: [{ required: true, message: "请输入登录密码", trigger: "blur" }],
        isadmin: [{ required: true, message: "请选择角色", trigger: "change" }]
      }
    };
  },
  created() {
    this.fetchList();
  },
  methods: {
    fetchList() {
      queryResidentsByPage(this.queryParam).then((res) => {
        this.tableData = res.data || [];
        this.recordTotal = res.count || 0;
      });
    },
    formatAddress(row) {
      return [row.buildingNo, row.unitNo, row.roomNo].filter(Boolean).join("-") || "-";
    },
    handleSelectionChange(rows) {
      this.selectedRows = rows;
    },
    handleSizeChange(limit) {
      this.queryParam.limit = limit;
      this.fetchList();
    },
    handleCurrentChange(page) {
      this.queryParam.page = page;
      this.fetchList();
    },
    handleFilter() {
      this.queryParam.page = 1;
      this.fetchList();
    },
    handleShowAll() {
      this.queryParam.page = 1;
      this.queryParam.username = null;
      this.fetchList();
    },
    handleCreate() {
      this.formType = 0;
      this.form = emptyResident();
      this.dialogFormVisible = true;
    },
    handleUpdate(row) {
      this.formType = 1;
      this.form = { ...emptyResident(), ...row };
      this.dialogFormVisible = true;
    },
    submitForm() {
      this.$refs.ruleForm.validate((valid) => {
        if (!valid) return;
        const submitter = this.formType === 0 ? addResident : updateResident;
        submitter(this.form).then((res) => {
          if (res === 1 || res === 0) {
            this.$message.success(this.formType === 0 ? "新增成功" : "更新成功");
            if (this.formType === 0) {
              getCount().then(() => this.fetchList());
            } else {
              this.fetchList();
            }
            this.dialogFormVisible = false;
          } else {
            this.$message.error(this.formType === 0 ? "新增失败" : "更新失败");
          }
        });
      });
    },
    handleDelete(row) {
      this.$confirm("确认删除该居民吗？", "提示", { type: "warning" }).then(() => {
        deleteResident(row).then((res) => {
          if (res === 1) {
            this.$message.success("删除成功");
            this.fetchList();
          } else if (res === -1) {
            this.$message.error("该居民存在订单记录，无法删除");
          } else if (res === -2) {
            this.$message.error("管理员账号不可删除");
          } else {
            this.$message.error("删除失败");
          }
        });
      });
    },
    handleDeleteSome() {
      this.$confirm("确认批量删除选中居民吗？", "提示", { type: "warning" }).then(() => {
        deleteResidents(this.selectedRows).then((res) => {
          if (res > 0) {
            this.$message.success(`已删除 ${res} 条记录`);
            this.fetchList();
          } else {
            this.$message.error("批量删除失败");
          }
        });
      });
    }
  }
};
</script>
