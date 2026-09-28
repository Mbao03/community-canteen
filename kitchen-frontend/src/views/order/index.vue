<template>
  <div class="app-container">
    <el-card shadow="never" style="margin-bottom: 16px">
      <el-form :inline="true" :model="queryParam" size="small">
        <el-form-item v-if="roleIsAdmin" label="居民姓名">
          <el-input v-model="queryParam.username" clearable placeholder="输入居民姓名" @keyup.enter.native="handleFilter" />
        </el-form-item>
        <el-form-item label="菜品名称">
          <el-input v-model="queryParam.dishName" clearable placeholder="输入菜品名称" @keyup.enter.native="handleFilter" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleFilter">搜索</el-button>
          <el-button v-permission="['admin']" type="danger" icon="el-icon-delete" :disabled="selectedRows.length === 0" @click="handleDeleteSome">批量删除</el-button>
          <el-button icon="el-icon-refresh" @click="handleShowAll">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table ref="multipleTable" :data="tableData" border stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" align="center" />
      <el-table-column prop="orderId" label="订单ID" width="80" align="center" />
      <el-table-column prop="residentName" label="居民" min-width="110" />
      <el-table-column prop="dishName" label="菜品" min-width="120" />
      <el-table-column prop="quantity" label="数量" width="70" align="center" />
      <el-table-column prop="unitPrice" label="单价" width="90" align="right">
        <template slot-scope="scope">¥{{ scope.row.unitPrice || 0 }}</template>
      </el-table-column>
      <el-table-column prop="totalPrice" label="总价" width="90" align="right">
        <template slot-scope="scope">¥{{ scope.row.totalPrice || 0 }}</template>
      </el-table-column>
      <el-table-column label="餐段" width="90" align="center">
        <template slot-scope="scope">{{ mealSlotLabel(scope.row.mealSlot) }}</template>
      </el-table-column>
      <el-table-column label="配送" width="90" align="center">
        <template slot-scope="scope">{{ deliveryTypeLabel(scope.row.deliveryType) }}</template>
      </el-table-column>
      <el-table-column prop="contactPhone" label="联系电话" min-width="120" />
      <el-table-column prop="deliveryAddress" label="配送地址" min-width="160" show-overflow-tooltip />
      <el-table-column label="下单时间" min-width="160">
        <template slot-scope="scope">{{ scope.row.orderTimeStr || '-' }}</template>
      </el-table-column>
      <el-table-column label="完成时间" min-width="160">
        <template slot-scope="scope">{{ scope.row.completeTimeStr || '-' }}</template>
      </el-table-column>
      <el-table-column label="订单状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="statusTag(scope.row.orderStatus)">{{ statusLabel(scope.row.orderStatus, scope.row.completeTimeStr) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="190" align="center">
        <template slot-scope="scope">
          <el-button v-permission="['admin']" size="mini" type="danger" icon="el-icon-delete" @click="handleDelete(scope.row)" circle />
          <el-button
            v-permission="['admin']"
            size="mini"
            type="success"
            icon="el-icon-check"
            :disabled="scope.row.orderStatus === 'COMPLETED' || scope.row.orderStatus === 'CANCELLED'"
            @click="handleComplete(scope.row)"
            circle
          />
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="tableData.length === 0" description="暂无订单数据">
      <el-button type="primary" @click="$router.push('/mealmanage/dishinfo')">去订餐</el-button>
      <el-button style="margin-left: 8px" @click="$router.push('/other/ai-assistant')">AI 营养助手</el-button>
    </el-empty>

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
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import permission from "@/directive/permission/index.js";
import waves from "@/directive/waves";
import { queryOrdersByPage, deleteOrder, deleteOrders, completeOrder } from "@/api/order";

export default {
  name: "OrderIndex",
  directives: { waves, permission },
  data() {
    return {
      tableData: [],
      recordTotal: 0,
      selectedRows: [],
      queryParam: {
        page: 1,
        limit: 10,
        userid: null,
        username: null,
        dishName: null
      }
    };
  },
  computed: {
    ...mapGetters(["id", "roles"]),
    roleIsAdmin() {
      return this.roles && this.roles[0] === "admin";
    }
  },
  watch: {
    id: {
      immediate: true,
      handler() {
        this.queryParam.userid = this.roleIsAdmin ? null : this.id;
        if (this.roleIsAdmin || Number(this.id) > 0) {
          this.fetchList();
        }
      }
    },
    roles: {
      immediate: true,
      handler() {
        this.queryParam.userid = this.roleIsAdmin ? null : this.id;
        if (this.roleIsAdmin || Number(this.id) > 0) {
          this.fetchList();
        }
      }
    }
  },
  created() {
    if (this.roleIsAdmin || Number(this.id) > 0) {
      this.fetchList();
    }
  },
  methods: {
    fetchList() {
      this.queryParam.userid = this.roleIsAdmin ? null : this.id;
      queryOrdersByPage(this.queryParam).then((res) => {
        this.tableData = res.data || [];
        this.recordTotal = res.count || 0;
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
      this.queryParam.page = 1;
      this.queryParam.username = null;
      this.queryParam.dishName = null;
      this.fetchList();
    },
    handleSelectionChange(rows) {
      this.selectedRows = rows;
    },
    handleDelete(row) {
      this.$confirm("确认删除该订单吗？", "提示", { type: "warning" }).then(() => {
        deleteOrder(row).then((res) => {
          if (res === 1) {
            this.$message.success("删除成功");
            this.fetchList();
          } else {
            this.$message.error("删除失败");
          }
        });
      });
    },
    handleDeleteSome() {
      this.$confirm("确认批量删除选中订单吗？", "提示", { type: "warning" }).then(() => {
        deleteOrders(this.selectedRows).then((res) => {
          if (res > 0) {
            this.$message.success(`已删除 ${res} 条记录`);
            this.fetchList();
          } else {
            this.$message.error("批量删除失败");
          }
        });
      });
    },
    handleComplete(row) {
      this.$confirm("确认将此订单标记为已完成吗？", "提示", { type: "warning" }).then(() => {
        completeOrder(row.orderId, row.dishId).then((res) => {
          if (res === 1) {
            this.$message.success("订单已完成");
            this.fetchList();
          } else {
            this.$message.error("处理失败");
          }
        });
      });
    },
    mealSlotLabel(value) {
      if (value === "BREAKFAST") return "早餐";
      if (value === "LUNCH") return "午餐";
      if (value === "DINNER") return "晚餐";
      return value || "-";
    },
    deliveryTypeLabel(value) {
      if (value === "PICKUP") return "自取";
      if (value === "DELIVERY") return "配送";
      return value || "-";
    },
    statusLabel(status, completeTimeStr) {
      if (status === "COMPLETED" || completeTimeStr) return "已完成";
      if (status === "CANCELLED") return "已取消";
      if (status === "CONFIRMED") return "已确认";
      return "待处理";
    },
    statusTag(status) {
      if (status === "COMPLETED") return "success";
      if (status === "CANCELLED") return "info";
      if (status === "CONFIRMED") return "warning";
      return "danger";
    }
  }
};
</script>
