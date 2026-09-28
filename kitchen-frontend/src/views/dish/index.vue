<template>
  <div class="app-container">
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="queryParam" size="small">
        <el-form-item label="菜品名称">
          <el-input v-model="queryParam.dishName" clearable placeholder="输入菜品名称" @keyup.enter.native="handleFilter" />
        </el-form-item>
        <el-form-item label="厨师/窗口">
          <el-input v-model="queryParam.chefName" clearable placeholder="输入厨师名" @keyup.enter.native="handleFilter" />
        </el-form-item>
        <el-form-item label="菜品分类">
          <el-select v-model="queryParam.dishTypeId" clearable filterable placeholder="选择分类">
            <el-option v-for="item in typeData" :key="item.dishTypeId" :label="item.dishTypeName" :value="item.dishTypeId" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleFilter">搜索</el-button>

          <el-button v-permission="['admin']" type="success" icon="el-icon-plus" @click="handleCreate">新增菜品</el-button>
          <el-button
            v-permission="['admin']"
            type="danger"
            icon="el-icon-delete"
            :disabled="selectedRows.length === 0"
            @click="handleDeleteSome"
          >批量删除</el-button>
        </el-form-item>
        <el-button icon="el-icon-refresh" @click="handleShowAll">重置</el-button>
      </el-form>
    </el-card>

    <el-table
      ref="multipleTable"
      v-loading="tableLoading"
      :data="tableData"
      border
      stripe
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="48" align="center" />
      <el-table-column prop="dishId" label="ID" width="70" align="center" />
      <el-table-column label="图片" width="90" align="center">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.dishImg"
            :src="$store.state.settings.baseApi + scope.row.dishImg"
            style="width: 52px; height: 52px; border-radius: 6px"
            fit="cover"
            :preview-src-list="[$store.state.settings.baseApi + scope.row.dishImg]"
          >
            <div slot="error" style="width: 52px; height: 52px; background: #fef2e8; border-radius: 6px; display: flex; align-items: center; justify-content: center;">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M17 3C18.1 3 19 3.9 19 5V9C19 10.1 18.1 11 17 11V21C17 21.55 16.55 22 16 22C15.45 22 15 21.55 15 21V11H13V7C13 5.9 13.9 5 15 5V3C15 2.45 15.45 2 16 2C16.55 2 17 2.45 17 3ZM7 3C8.1 3 9 3.9 9 5V9C9 10.1 8.1 11 7 11V21C7 21.55 6.55 22 6 22C5.45 22 5 21.55 5 21V11H3V5C3 3.9 3.9 3 5 3V2C5 1.45 5.45 1 6 1C6.55 1 7 1.45 7 2V3Z" fill="#e8a87c"/></svg>
            </div>
          </el-image>
          <div v-else style="width: 52px; height: 52px; background: #fef2e8; border-radius: 6px; display: flex; align-items: center; justify-content: center;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M11 9H9V2H7V9H5V2H3V9C3 11.12 4.66 12.84 6.75 12.97V22H9.25V12.97C11.34 12.84 13 11.12 13 9V2H11V9ZM16 6V14H18.5V22H21V14H23V6C23 3.24 20.76 1 18 1C15.24 1 13 3.24 13 6H16Z" fill="#e8a87c"/></svg>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="dishName" label="菜品名称" min-width="130" show-overflow-tooltip />
      <el-table-column prop="chefName" label="厨师" min-width="90" />
      <el-table-column prop="dishTypeName" label="分类" min-width="100" />
      <el-table-column label="单价" width="90" align="right">
        <template slot-scope="scope">¥{{ scope.row.dishPrice }}</template>
      </el-table-column>
      <el-table-column prop="stockQty" label="库存" width="80" align="center" />
      <el-table-column label="上架" width="80" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isAvailable === 1 ? 'success' : 'info'" size="mini">{{ scope.row.isAvailable === 1 ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="caloriesKcal" label="热量(kcal)" width="100" align="center" />
      <el-table-column prop="dishDesc" label="描述" min-width="180" show-overflow-tooltip />
      <el-table-column fixed="right" label="操作" width="190" align="center">
        <template slot-scope="scope">
          <el-button v-permission="['admin']" size="mini" type="primary" icon="el-icon-edit" @click="handleUpdate(scope.row)" circle />
          <el-button v-permission="['admin']" size="mini" type="danger" icon="el-icon-delete" @click="handleDelete(scope.row)" circle />
          <el-button
            size="mini"
            type="success"
            icon="el-icon-shopping-cart-full"
            :disabled="!canOrder(scope.row)"
            @click="handleOrder(scope.row)"
            circle
          />
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

    <el-dialog :title="formType === 0 ? '新增菜品' : '编辑菜品'" :visible.sync="dialogFormVisible" width="720px" :close-on-click-modal="false">
      <el-form ref="ruleForm" :model="form" :rules="rules" label-width="95px" size="small">
        <el-row :gutter="14">
          <el-col :span="12"><el-form-item label="菜品名称" prop="dishName"><el-input v-model="form.dishName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="厨师" prop="chefName"><el-input v-model="form.chefName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="单价" prop="dishPrice"><el-input-number v-model="form.dishPrice" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="分类" prop="dishTypeId"><el-select v-model="form.dishTypeId" style="width:100%"><el-option v-for="item in typeData" :key="item.dishTypeId" :label="item.dishTypeName" :value="item.dishTypeId" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="厨房窗口"><el-input v-model="form.kitchenName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="库存" prop="stockQty"><el-input-number v-model="form.stockQty" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="上架"><el-radio-group v-model="form.isAvailable"><el-radio :label="1">是</el-radio><el-radio :label="0">否</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预估制作(分)"><el-input-number v-model="form.prepMinutes" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="辣度(0-5)"><el-input-number v-model="form.spiceLevel" :min="0" :max="5" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="热量(kcal)"><el-input-number v-model="form.caloriesKcal" :min="0" :precision="1" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="蛋白(g)"><el-input-number v-model="form.proteinG" :min="0" :precision="1" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="脂肪(g)"><el-input-number v-model="form.fatG" :min="0" :precision="1" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="碳水(g)"><el-input-number v-model="form.carbG" :min="0" :precision="1" style="width:100%" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="过敏原"><el-input v-model="form.allergens" placeholder="如：花生、甲壳类" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="描述" prop="dishDesc"><el-input type="textarea" :rows="3" v-model="form.dishDesc" /></el-form-item></el-col>
          <el-col :span="24">
            <el-form-item label="菜品图片">
              <el-upload
                class="avatar-uploader"
                :action="$store.state.settings.baseApi + '/upload/uploadImg'"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <img v-if="form.dishImg" :src="$store.state.settings.baseApi + form.dishImg" class="avatar" @error="form.dishImg = ''" />
                <i v-else class="el-icon-plus avatar-uploader-icon" />
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="提交订餐" :visible.sync="dialogOrderVisible" width="500px">
      <el-form :model="orderForm" label-width="90px" size="small">
        <el-form-item label="居民">
          <el-select v-model="orderForm.userid" filterable style="width:100%" :disabled="!roleIsAdmin">
            <el-option v-for="item in residentData" :key="item.userid" :label="item.username" :value="item.userid" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量"><el-input-number v-model="orderForm.quantity" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="餐段">
          <el-select v-model="orderForm.mealSlot" clearable style="width:100%">
            <el-option label="早餐" value="BREAKFAST" />
            <el-option label="午餐" value="LUNCH" />
            <el-option label="晚餐" value="DINNER" />
          </el-select>
        </el-form-item>
        <el-form-item label="取餐方式">
          <el-radio-group v-model="orderForm.deliveryType">
            <el-radio label="PICKUP">自取</el-radio>
            <el-radio label="DELIVERY">配送</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系电话"><el-input v-model="orderForm.contactPhone" /></el-form-item>
        <el-form-item v-if="orderForm.deliveryType === 'DELIVERY'" label="配送地址"><el-input v-model="orderForm.deliveryAddress" /></el-form-item>
        <el-form-item label="备注"><el-input type="textarea" :rows="2" v-model="orderForm.remark" /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogOrderVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder">确定下单</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import permission from "@/directive/permission/index.js";
import waves from "@/directive/waves";
import { getCount, queryDishesByPage, addDish, deleteDish, deleteDishs, updateDish } from "@/api/dish";
import { queryDishTypes } from "@/api/dishtype";
import { placeOrder } from "@/api/order";
import { queryResidents } from "@/api/resident";

const emptyDishForm = () => ({
  dishId: null,
  dishName: "",
  chefName: "",
  dishPrice: 0,
  dishTypeId: null,
  dishDesc: "",
  isReserved: 0,
  dishImg: "",
  kitchenName: "",
  stockQty: 0,
  isAvailable: 1,
  prepMinutes: 0,
  spiceLevel: 0,
  allergens: "",
  caloriesKcal: null,
  proteinG: null,
  fatG: null,
  carbG: null
});

export default {
  name: "DishIndex",
  directives: { waves, permission },
  data() {
    return {
      tableLoading: false,
      tableData: [],
      recordTotal: 0,
      selectedRows: [],
      typeData: [],
      residentData: [],
      queryParam: { page: 1, limit: 10, dishName: null, chefName: null, dishTypeId: null },
      dialogFormVisible: false,
      dialogOrderVisible: false,
      formType: 0,
      form: emptyDishForm(),
      orderForm: {
        userid: null,
        dishId: null,
        quantity: 1,
        mealSlot: "LUNCH",
        deliveryType: "PICKUP",
        contactPhone: "",
        deliveryAddress: "",
        remark: ""
      },
      rules: {
        dishName: [{ required: true, message: "请输入菜品名称", trigger: "blur" }],
        chefName: [{ required: true, message: "请输入厨师名称", trigger: "blur" }],
        dishPrice: [{ required: true, message: "请输入单价", trigger: "change" }],
        dishTypeId: [{ required: true, message: "请选择分类", trigger: "change" }],
        stockQty: [{ required: true, message: "请输入库存", trigger: "change" }],
        dishDesc: [{ required: true, message: "请输入描述", trigger: "blur" }]
      }
    };
  },
  computed: {
    ...mapGetters(["id", "roles"]),
    roleIsAdmin() {
      return this.roles && this.roles[0] === "admin";
    }
  },
  created() {
    this.fetchTypes();
    this.fetchResidents();
    this.fetchList();
  },
  methods: {
    fetchTypes() {
      queryDishTypes().then((res) => {
        this.typeData = res || [];
      });
    },
    fetchResidents() {
      queryResidents().then((res) => {
        this.residentData = res || [];
      });
    },
    fetchList() {
      this.tableLoading = true;
      queryDishesByPage(this.queryParam)
        .then((res) => {
          this.tableData = res.data || [];
          this.recordTotal = res.count || 0;
        })
        .finally(() => {
          this.tableLoading = false;
        });
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
      this.queryParam = { page: 1, limit: this.queryParam.limit, dishName: null, chefName: null, dishTypeId: null };
      this.fetchList();
    },
    handleSelectionChange(rows) {
      this.selectedRows = rows;
    },
    handleAvatarSuccess(res) {
      if (res && res.code === 0) {
        this.form.dishImg = res.data;
        this.$message.success("上传成功");
      } else {
        this.$message.error("上传失败");
      }
    },
    beforeAvatarUpload(file) {
      const ok = ["image/jpeg", "image/jpg", "image/png"].includes(file.type);
      const sizeOk = file.size / 1024 / 1024 < 2;
      if (!ok) this.$message.error("仅支持 JPG/PNG 格式");
      if (!sizeOk) this.$message.error("图片不能超过 2MB");
      return ok && sizeOk;
    },
    handleCreate() {
      this.formType = 0;
      this.form = emptyDishForm();
      this.dialogFormVisible = true;
    },
    handleUpdate(row) {
      this.formType = 1;
      this.form = { ...emptyDishForm(), ...row };
      this.dialogFormVisible = true;
    },
    submitForm() {
      this.$refs.ruleForm.validate((valid) => {
        if (!valid) return;
        const submitter = this.formType === 0 ? addDish : updateDish;
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
      this.$confirm("确认删除该菜品吗？", "提示", { type: "warning" }).then(() => {
        deleteDish(row).then((res) => {
          if (res === 1) {
            this.$message.success("删除成功");
            this.fetchList();
          } else if (res === -1) {
            this.$message.error("该菜品存在订单记录，无法删除");
          } else {
            this.$message.error("删除失败");
          }
        });
      });
    },
    handleDeleteSome() {
      this.$confirm("确认批量删除选中菜品吗？", "提示", { type: "warning" }).then(() => {
        deleteDishs(this.selectedRows).then((res) => {
          if (res > 0) {
            this.$message.success(`已删除 ${res} 条记录`);
            this.fetchList();
          } else {
            this.$message.error("批量删除失败");
          }
        });
      });
    },
    handleOrder(row) {
      if (!this.canOrder(row)) {
        this.$message.warning("该菜品暂不可选购，请检查上架状态和库存");
        return;
      }
      this.orderForm = {
        userid: this.roleIsAdmin ? null : this.id,
        dishId: row.dishId,
        quantity: 1,
        mealSlot: "LUNCH",
        deliveryType: "PICKUP",
        contactPhone: "",
        deliveryAddress: "",
        remark: ""
      };
      if (!this.roleIsAdmin) {
        this.orderForm.userid = this.id;
      }
      this.dialogOrderVisible = true;
    },
    submitOrder() {
      if (!this.orderForm.userid) {
        this.$message.warning("请选择居民");
        return;
      }
      placeOrder(this.orderForm.userid, this.orderForm.dishId, {
        quantity: this.orderForm.quantity,
        mealSlot: this.orderForm.mealSlot,
        deliveryType: this.orderForm.deliveryType,
        contactPhone: this.orderForm.contactPhone,
        deliveryAddress: this.orderForm.deliveryAddress,
        remark: this.orderForm.remark
      }).then((res) => {
        if (res === 1) {
          this.$message.success("下单成功");
          this.dialogOrderVisible = false;
          this.fetchList();
        } else {
          this.$message.error("下单失败，请检查库存或参数");
        }
      }).catch((e) => {
        const msg = e && e.message ? e.message : "下单失败";
        this.$message.error(msg);
      });
    },
    canOrder(row) {
      const isAvailable = row.isAvailable !== 0;
      const stock = Number(row.stockQty || 0);
      return isAvailable && stock > 0;
    }
  }
};
</script>

<style scoped>
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
}
.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}
.avatar-uploader-icon {
  font-size: 24px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}
.avatar {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  object-fit: cover;
}
</style>
