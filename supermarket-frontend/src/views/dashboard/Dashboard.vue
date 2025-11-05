<template>
  <div class="dashboard-container page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><TrendCharts /></el-icon>
        数据仪表盘
      </h1>
      <el-button type="primary" :icon="Refresh" @click="refreshData" :loading="loading">
        刷新数据
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="24" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6" v-for="(stat, index) in stats" :key="index">
        <div 
          class="stat-card card hover-lift" 
          :class="`fade-in-up delay-${index + 1}`"
          :style="{ background: stat.gradient }"
        >
          <div class="stat-icon">
            <component :is="stat.icon" />
          </div>
          <div class="stat-content">
            <div class="stat-value number-roll">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-trend" :class="stat.trend">
            <el-icon><CaretTop v-if="stat.trend === 'up'" /><CaretBottom v-else /></el-icon>
            <span>{{ stat.change }}%</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="24" class="charts-row">
      <!-- 销售趋势图 -->
      <el-col :xs="24" :sm="24" :md="16">
        <div class="card chart-card fade-in-up delay-2">
          <div class="chart-header">
            <h3 class="chart-title">
              <el-icon><TrendCharts /></el-icon>
              销售趋势分析
            </h3>
            <el-radio-group v-model="salesPeriod" size="small" @change="updateSalesChart">
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
              <el-radio-button label="year">本年</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="salesChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <!-- 销售占比饼图 -->
      <el-col :xs="24" :sm="24" :md="8">
        <div class="card chart-card fade-in-up delay-3">
          <div class="chart-header">
            <h3 class="chart-title">
              <el-icon><PieChart /></el-icon>
              销售占比
            </h3>
          </div>
          <div ref="categoryChartRef" class="chart-container" style="height: 320px;"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 第三行图表 -->
    <el-row :gutter="24" class="charts-row">
      <!-- 库存预警 -->
      <el-col :xs="24" :sm="24" :md="12">
        <div class="card chart-card fade-in-up delay-4">
          <div class="chart-header">
            <h3 class="chart-title">
              <el-icon><Warning /></el-icon>
              库存预警
            </h3>
          </div>
          <div ref="inventoryChartRef" class="chart-container" style="height: 320px;"></div>
        </div>
      </el-col>

      <!-- 采购统计 -->
      <el-col :xs="24" :sm="24" :md="12">
        <div class="card chart-card fade-in-up delay-5">
          <div class="chart-header">
            <h3 class="chart-title">
              <el-icon><ShoppingCart /></el-icon>
              采购统计
            </h3>
          </div>
          <div ref="purchaseChartRef" class="chart-container" style="height: 320px;"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 最新动态 -->
    <el-row :gutter="24">
      <el-col :xs="24" :sm="24" :md="12">
        <div class="card fade-in-up delay-6">
          <div class="chart-header">
            <h3 class="chart-title">
              <el-icon><Bell /></el-icon>
              最新动态
            </h3>
          </div>
          <el-timeline class="activity-timeline">
            <el-timeline-item
              v-for="(activity, index) in activities"
              :key="index"
              :timestamp="activity.time"
              :type="activity.type"
              placement="top"
            >
              <p>{{ activity.content }}</p>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-col>

      <!-- 待办事项 -->
      <el-col :xs="24" :sm="24" :md="12">
        <div class="card fade-in-up delay-7">
          <div class="chart-header">
            <h3 class="chart-title">
              <el-icon><List /></el-icon>
              待办事项
            </h3>
          </div>
          <div class="todo-list">
            <div v-for="(todo, index) in todos" :key="index" class="todo-item">
              <el-checkbox v-model="todo.done" :label="todo.text" />
              <el-tag :type="todo.priority === 'high' ? 'danger' : 'info'" size="small">
                {{ todo.priority === 'high' ? '紧急' : '普通' }}
              </el-tag>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStatistics } from '@/api/statistics'
import { ElMessage } from 'element-plus'
import {
  TrendCharts,
  Refresh,
  Money,
  ShoppingBag,
  Box,
  UserFilled,
  CaretTop,
  CaretBottom,
  PieChart,
  Warning,
  ShoppingCart,
  Bell,
  List
} from '@element-plus/icons-vue'

const loading = ref(false)
const salesPeriod = ref('month')

// 统计数据
const stats = ref([
  {
    label: '今日销售额',
    value: '¥0',
    change: '+12.5',
    trend: 'up',
    icon: Money,
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    label: '本月销售额',
    value: '¥0',
    change: '+8.3',
    trend: 'up',
    icon: ShoppingBag,
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    label: '低库存商品',
    value: '0',
    change: '-2.1',
    trend: 'down',
    icon: Box,
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    label: '待审核订单',
    value: '0',
    change: '+5.4',
    trend: 'up',
    icon: UserFilled,
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
])

// 图表引用
const salesChartRef = ref(null)
const categoryChartRef = ref(null)
const inventoryChartRef = ref(null)
const purchaseChartRef = ref(null)

let salesChart, categoryChart, inventoryChart, purchaseChart

// 最新动态
const activities = ref([
  { time: '2 分钟前', content: '用户张三创建了新的采购订单 #2024001', type: 'primary' },
  { time: '15 分钟前', content: '商品"可口可乐"库存低于预警值', type: 'warning' },
  { time: '1 小时前', content: '采购订单 #2024001 已审核通过', type: 'success' },
  { time: '2 小时前', content: '完成销售订单 #SO202411050001', type: 'success' },
  { time: '3 小时前', content: '新增供应商"某某公司"', type: 'info' }
])

// 待办事项
const todos = ref([
  { text: '审核采购订单 #2024001', done: false, priority: 'high' },
  { text: '处理低库存预警商品', done: false, priority: 'high' },
  { text: '查看本周销售报表', done: true, priority: 'normal' },
  { text: '联系供应商补货', done: false, priority: 'normal' }
])

// 获取统计数据
const fetchStatistics = async () => {
  try {
    loading.value = true
    const { data } = await getDashboardStatistics()
    
    // 更新统计卡片
    stats.value[0].value = `¥${data.todaySales.toFixed(2)}`
    stats.value[1].value = `¥${data.monthSales.toFixed(2)}`
    stats.value[2].value = data.lowStockProducts
    stats.value[3].value = data.pendingPurchaseOrders
    
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 初始化销售趋势图
const initSalesChart = () => {
  if (!salesChartRef.value) return
  
  salesChart = echarts.init(salesChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['销售额', '订单数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '销售额(元)',
        position: 'left'
      },
      {
        type: 'value',
        name: '订单数',
        position: 'right'
      }
    ],
    series: [
      {
        name: '销售额',
        type: 'bar',
        data: [3200, 4500, 3800, 5200, 4800, 6200, 7500],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
        },
        barWidth: '40%'
      },
      {
        name: '订单数',
        type: 'line',
        yAxisIndex: 1,
        data: [45, 62, 55, 75, 68, 88, 105],
        smooth: true,
        itemStyle: {
          color: '#f5576c'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 87, 108, 0.3)' },
            { offset: 1, color: 'rgba(245, 87, 108, 0.05)' }
          ])
        }
      }
    ]
  }
  
  salesChart.setOption(option)
}

// 初始化分类占比图
const initCategoryChart = () => {
  if (!categoryChartRef.value) return
  
  categoryChart = echarts.init(categoryChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center'
    },
    series: [
      {
        name: '销售占比',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 1548, name: '饮料', itemStyle: { color: '#667eea' } },
          { value: 735, name: '零食', itemStyle: { color: '#f093fb' } },
          { value: 580, name: '日用品', itemStyle: { color: '#4facfe' } },
          { value: 484, name: '生鲜', itemStyle: { color: '#43e97b' } },
          { value: 300, name: '其他', itemStyle: { color: '#fa709a' } }
        ]
      }
    ]
  }
  
  categoryChart.setOption(option)
}

// 初始化库存预警图
const initInventoryChart = () => {
  if (!inventoryChartRef.value) return
  
  inventoryChart = echarts.init(inventoryChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: ['可口可乐', '薯片', '面包', '牛奶', '矿泉水']
    },
    series: [
      {
        name: '库存数量',
        type: 'bar',
        data: [8, 12, 15, 9, 11],
        itemStyle: {
          color: (params) => {
            const colors = ['#ef4444', '#f59e0b', '#f59e0b', '#10b981', '#10b981']
            return colors[params.dataIndex]
          },
          borderRadius: [0, 4, 4, 0]
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{c} 件'
        }
      }
    ]
  }
  
  inventoryChart.setOption(option)
}

// 初始化采购统计图
const initPurchaseChart = () => {
  if (!purchaseChartRef.value) return
  
  purchaseChart = echarts.init(purchaseChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: '5%',
      left: 'center'
    },
    series: [
      {
        name: '采购状态',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '60%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}\n({d}%)'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: [
          { value: 5, name: '待审核', itemStyle: { color: '#f59e0b' } },
          { value: 12, name: '已通过', itemStyle: { color: '#10b981' } },
          { value: 2, name: '已拒绝', itemStyle: { color: '#ef4444' } },
          { value: 8, name: '已入库', itemStyle: { color: '#3b82f6' } }
        ]
      }
    ]
  }
  
  purchaseChart.setOption(option)
}

// 更新销售趋势图
const updateSalesChart = () => {
  // 根据选择的时间周期更新数据
  ElMessage.success(`切换到${salesPeriod.value === 'week' ? '本周' : salesPeriod.value === 'month' ? '本月' : '本年'}数据`)
}

// 刷新数据
const refreshData = async () => {
  await fetchStatistics()
  ElMessage.success('数据已刷新')
}

// 窗口大小改变时重新渲染图表
const handleResize = () => {
  salesChart?.resize()
  categoryChart?.resize()
  inventoryChart?.resize()
  purchaseChart?.resize()
}

onMounted(async () => {
  await fetchStatistics()
  
  await nextTick()
  
  // 初始化所有图表
  initSalesChart()
  initCategoryChart()
  initInventoryChart()
  initPurchaseChart()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

// 组件卸载时清理
import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  salesChart?.dispose()
  categoryChart?.dispose()
  inventoryChart?.dispose()
  purchaseChart?.dispose()
})
</script>

<style scoped>
.dashboard-container {
  min-height: 100%;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  position: relative;
  padding: 24px;
  color: white;
  border-radius: var(--radius-lg);
  overflow: hidden;
  height: 140px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.stat-icon {
  font-size: 48px;
  opacity: 0.3;
  position: absolute;
  right: 20px;
  top: 20px;
}

.stat-content {
  z-index: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  z-index: 1;
}

.stat-trend.up {
  color: #fff;
}

.stat-trend.down {
  color: #fed7d7;
}

/* 图表区域 */
.charts-row {
  margin-bottom: 24px;
}

.chart-card {
  padding: 24px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--border-light);
}

.chart-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-title i {
  color: var(--primary-color);
}

.chart-container {
  height: 400px;
  width: 100%;
}

/* 活动时间线 */
.activity-timeline {
  padding: 16px 0;
}

:deep(.el-timeline-item__timestamp) {
  color: var(--text-secondary);
  font-size: 12px;
}

/* 待办事项 */
.todo-list {
  padding: 16px 0;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-light);
  transition: var(--transition-fast);
}

.todo-item:hover {
  background: var(--bg-color);
  margin: 0 -16px;
  padding: 12px 16px;
}

.todo-item:last-child {
  border-bottom: none;
}

/* 响应式 */
@media (max-width: 768px) {
  .stat-value {
    font-size: 24px;
  }
  
  .chart-container {
    height: 300px !important;
  }
  
  .stat-card {
    height: 120px;
  }
  
  .stat-icon {
    font-size: 36px;
  }
}
</style>
