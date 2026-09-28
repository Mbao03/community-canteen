<template>
  <div class="dashboard-container">
    <div class="dashboard-welcome">
      <div class="welcome-content">
        <h1>欢迎使用智慧社区食堂管理系统</h1>
        <p>你好{{ name }}，祝你用餐愉快。</p>
        <div class="welcome-actions">
          <el-button type="primary" icon="el-icon-food" @click="navigateTo('/mealmanage/dishinfo')">浏览菜品</el-button>
          <el-button type="success" icon="el-icon-tickets" @click="navigateTo('/mealmanage/order')">订单管理</el-button>
          <el-button type="warning" icon="el-icon-s-opportunity" @click="navigateTo('/other/ai-assistant')">AI 营养助手</el-button>
        </div>
      </div>
    </div>

    <el-row :gutter="24" class="data-overview">
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-title">菜品总数</div>
          <div class="card-value">{{ dishCount }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-title">{{ isAdmin ? '订单总数' : '我的订单总数' }}</div>
          <div class="card-value">{{ orderCount }}</div>
        </el-card>
      </el-col>
      <el-col v-if="isAdmin" :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-title">居民总数</div>
          <div class="card-value">{{ residentCount }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-title">分类总数</div>
          <div class="card-value">{{ typeCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="content-section">
      <el-col :span="24">
        <el-card shadow="hover" class="dish-recommend">
          <div slot="header" class="card-header">
            <span>个性化推荐</span>
          </div>
          <div class="recommend-list">
            <div v-if="recommendedDishes.length === 0" class="empty-placeholder">暂无推荐，请先产生订单数据。</div>
            <div v-else>
              <div v-for="item in recommendedDishes" :key="item.dishId" class="dish-item">
                <div class="dish-info">
                  <div class="dish-name">{{ item.dishName }}</div>
                  <div class="dish-chef">{{ item.chefName || '未知厨师' }}</div>
                  <!-- <div class="dish-chef">推荐理由：{{ item.reason }}（{{ item.score }}分）</div> -->
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="chart-section">
      <el-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
        <el-card shadow="hover" class="chart-card">
          <div slot="header" class="card-header"><span>菜品分类分布</span></div>
          <div class="chart-container" ref="pieChartContainer"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
        <el-card shadow="hover" class="chart-card">
          <div slot="header" class="card-header"><span>可订状态分布</span></div>
          <div class="chart-container" ref="barChartContainer"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="24" :lg="8" :xl="8">
        <el-card shadow="hover" class="chart-card">
          <div slot="header" class="card-header"><span>热度最高排行</span></div>
          <div class="chart-container" ref="popularityChartContainer"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getDishCount } from '@/api/dish'
import { getOrderCount, queryOrdersByPage } from '@/api/order'
import { getResidentCount } from '@/api/resident'
import { getDishTypeCount } from '@/api/dishtype'
import { getDishTypeDistribution, getDishAvailabilityDistribution, getMostPopularDishes } from '@/api/dashboard'
import { getUserRecommendations } from '@/api/recommend'
import * as echarts from 'echarts'

export default {
  name: 'Dashboard',
  data() {
    return {
      dishCount: 0,
      orderCount: 0,
      residentCount: 0,
      typeCount: 0,
      recommendedDishes: [],
      chartData: {
        pieData: [],
        barData: { categories: [], values: [] },
        popularityData: { dishes: [], values: [] }
      },
      charts: {
        pieChart: null,
        barChart: null,
        popularityChart: null
      }
    }
  },
  computed: {
    ...mapGetters(['id', 'name', 'roles']),
    isAdmin() {
      return Array.isArray(this.roles) && this.roles.includes('admin')
    }
  },
  mounted() {
    this.fetchData()
    this.fetchRecommendations()
    this.$nextTick(() => {
      this.initCharts()
      this.fetchChartData()
    })
  },
  activated() {
    // 从其他页面返回时刷新数据
    this.fetchData()
    this.fetchRecommendations()
    this.fetchChartData()
  },
  methods: {
    async fetchData() {
      getDishCount().then(res => { this.dishCount = res || 0 }).catch(() => { this.dishCount = 0 })
      if (this.isAdmin) {
        getOrderCount().then(res => { this.orderCount = res || 0 }).catch(() => { this.orderCount = 0 })
      } else {
        queryOrdersByPage({ page: 1, limit: 1, userid: this.id }).then(res => {
          this.orderCount = (res && res.count) || 0
        }).catch(() => { this.orderCount = 0 })
      }
      if (this.isAdmin) {
        getResidentCount().then(res => { this.residentCount = res || 0 }).catch(() => { this.residentCount = 0 })
      } else {
        this.residentCount = 0
      }
      getDishTypeCount().then(res => { this.typeCount = res || 0 }).catch(() => { this.typeCount = 0 })
    },
    fetchRecommendations() {
      getUserRecommendations(this.id, 6).then((res) => {
        if (res && res.status === 200 && Array.isArray(res.data)) {
          this.recommendedDishes = res.data
        } else {
          this.recommendedDishes = []
        }
      }).catch(() => {
        this.recommendedDishes = []
      })
    },
    async fetchChartData() {
      try {
        this.chartData.pieData = await getDishTypeDistribution()
      } catch (e) {
        console.error('Pie chart error:', e)
      }
      try {
        this.chartData.barData = await getDishAvailabilityDistribution()
      } catch (e) {
        console.error('Bar chart error:', e)
      }
      try {
        this.chartData.popularityData = await getMostPopularDishes()
      } catch (e) {
        console.error('Popularity chart error:', e)
      }
      this.updatePieChart()
      this.updateBarChart()
      this.updatePopularityChart()
    },
    navigateTo(path) {
      this.$router.push(path)
    },
    initCharts() {
      this.charts.pieChart = echarts.init(this.$refs.pieChartContainer)
      this.charts.barChart = echarts.init(this.$refs.barChartContainer)
      this.charts.popularityChart = echarts.init(this.$refs.popularityChartContainer)
      window.addEventListener('resize', () => {
        this.charts.pieChart && this.charts.pieChart.resize()
        this.charts.barChart && this.charts.barChart.resize()
        this.charts.popularityChart && this.charts.popularityChart.resize()
      })
    },
    updatePieChart() {
      if (!this.charts.pieChart) return
      this.charts.pieChart.setOption({
        tooltip: { trigger: 'item' },
        series: [{ type: 'pie', radius: '60%', data: this.chartData.pieData }]
      })
    },
    updateBarChart() {
      if (!this.charts.barChart) return
      this.charts.barChart.setOption({
        xAxis: { type: 'category', data: this.chartData.barData.categories },
        yAxis: { type: 'value', name: '数量' },
        series: [{ type: 'bar', data: this.chartData.barData.values }]
      })
    },
    updatePopularityChart() {
      if (!this.charts.popularityChart) return
      this.charts.popularityChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: this.chartData.popularityData.dishes,
          axisLabel: { rotate: 30 }
        },
        yAxis: { type: 'value', name: '销售总量' },
        series: [{
          type: 'bar',
          data: this.chartData.popularityData.values,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#1890ff' },
              { offset: 1, color: '#69c0ff' }
            ])
          },
          barMaxWidth: 40
        }],
        grid: { bottom: 60 }
      })
    }
  }
}
</script>

<style scoped>
.dashboard-container { padding: 20px; }
.dashboard-welcome { margin-bottom: 20px; }
.welcome-content h1 { margin: 0 0 8px; }
.welcome-actions { margin-top: 12px; }
.data-overview { margin-bottom: 20px; }
.data-card { text-align: center; }
.card-title { color: #666; }
.card-value { font-size: 28px; font-weight: 600; color: #1e3a8a; }
.chart-container { width: 100%; height: 300px; }
.dish-item { padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.dish-name { font-weight: 600; }
.dish-chef { color: #666; font-size: 13px; }
.empty-placeholder { color: #999; }
</style>
