<template>
  <div class="app-container">
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="queryParam" size="small">
        <el-form-item label="分类名称">
          <el-input v-model="queryParam.dishTypeName" clearable placeholder="输入分类名称" @keyup.enter.native="handleFilter" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleFilter">搜索</el-button>
          <el-button type="success" icon="el-icon-plus" @click="handleCreate">新增分类</el-button>
          <el-button type="danger" icon="el-icon-delete" :disabled="selectedRows.length === 0" @click="handleDeleteSome">批量删除</el-button>
          <el-button icon="el-icon-refresh" @click="handleShowAll">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table ref="multipleTable" :data="tableData" border stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" align="center" />
      <el-table-column prop="dishTypeId" label="ID" width="80" align="center" />
      <el-table-column prop="dishTypeName" label="分类名称" min-width="140" />
      <el-table-column prop="dishTypeDesc" label="分类描述" min-width="220" show-overflow-tooltip />
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

    <el-dialog :title="formType === 0 ? '新增分类' : '编辑分类'" :visible.sync="dialogFormVisible" width="520px">
      <el-form ref="ruleForm" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类名称" prop="dishTypeName">
          <el-input v-model="form.dishTypeName" />
        </el-form-item>
        <el-form-item label="分类描述" prop="dishTypeDesc">
          <el-input type="textarea" :rows="4" v-model="form.dishTypeDesc" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import waves from "@/directive/waves";
import { getCount, queryDishTypesByPage, addDishType, deleteDishType, deleteDishTypes, updateDishType } from "@/api/dishtype";

const emptyForm = () => ({
  dishTypeId: null,
  dishTypeName: "",
  dishTypeDesc: ""
});

export default {
  name: "DishTypeIndex",
  directives: { waves },
  data() {
    return {
      tableData: [],
      recordTotal: 0,
      selectedRows: [],
      queryParam: {
        page: 1,
        limit: 10,
        dishTypeName: null
      },
      dialogFormVisible: false,
      formType: 0,
      form: emptyForm(),
      rules: {
        dishTypeName: [{ required: true, message: "请输入分类名称", trigger: "blur" }],
        dishTypeDesc: [{ required: true, message: "请输入分类描述", trigger: "blur" }]
      }
    };
  },
  computed: {
    ...mapGetters(["id", "name", "roles"])
  },
  created() {
    this.fetchList();
  },
  methods: {
    fetchList() {
      queryDishTypesByPage(this.queryParam).then((res) => {
        this.tableData = res.data || [];
        this.recordTotal = res.count || 0;
      });
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
      this.queryParam.dishTypeName = null;
      this.fetchList();
    },
    handleCreate() {
      this.formType = 0;
      this.form = emptyForm();
      this.dialogFormVisible = true;
    },
    handleUpdate(row) {
      this.formType = 1;
      this.form = { ...row };
      this.dialogFormVisible = true;
    },
    submitForm() {
      this.$refs.ruleForm.validate((valid) => {
        if (!valid) return;
        const submitter = this.formType === 0 ? addDishType : updateDishType;
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
      this.$confirm("确认删除该分类吗？", "提示", { type: "warning" }).then(() => {
        deleteDishType(row).then((res) => {
          if (res === 1) {
            this.$message.success("删除成功");
            this.fetchList();
          } else if (res === -1) {
            this.$message.error("该分类下仍有关联菜品，无法删除");
          } else {
            this.$message.error("删除失败");
          }
        });
      });
    },
    handleDeleteSome() {
      this.$confirm("确认批量删除选中分类吗？", "提示", { type: "warning" }).then(() => {
        deleteDishTypes(this.selectedRows).then((res) => {
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
