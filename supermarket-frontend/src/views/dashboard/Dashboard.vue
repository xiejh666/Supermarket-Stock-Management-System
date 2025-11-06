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

// 最新动态 - 初始化为空数组，从后端动态获取
const activities = ref([])

// 待办事项 - 初始化为空数组，从后端动态获取
const todos = ref([])

// 格式化时间为相对时间
const formatRelativeTime = (dateStr) => {
  if (!dateStr) return '刚刚'
  const date = new Date(dateStr)
  const now = new Date()
  const diff = Math.floor((now - date) / 1000) // 秒
  
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)} 小时前`
  if (diff < 604800) return `${Math.floor(diff / 86400)} 天前`
  return date.toLocaleDateString()
}

// 获取最新动态
const fetchActivities = async () => {
  try {
    // 从统计数据中提取最新动态
    const activityList = []
    
    // 这里可以从多个接口获取最新数据，暂时使用模拟数据
    // TODO: 后续可以添加专门的活动日志接口
    activityList.push(
      { time: formatRelativeTime(new Date(Date.now() - 2 * 60 * 1000)), content: '系统运行正常', type: 'success' },
      { time: formatRelativeTime(new Date(Date.now() - 15 * 60 * 1000)), content: '已同步最新数据', type: 'info' },
      { time: formatRelativeTime(new Date(Date.now() - 60 * 60 * 1000)), content: '数据备份完成', type: 'success' }
    )
    
    activities.value = activityList
  } catch (error) {
    console.error('获取最新动态失败:', error)
    activities.value = [
      { time: '刚刚', content: '欢迎使用系统', type: 'info' }
    ]
  }
}

// 获取待办事项
const fetchTodos = async () => {
  try {
    const { data } = await getDashboardStatistics()
    const todoList = []
    
    // 根据统计数据生成待办事项
    if (data.pendingPurchaseOrders > 0) {
      todoList.push({
        text: `待审核采购订单 ${data.pendingPurchaseOrders} 个`,
        done: false,
        priority: 'high'
      })
    }
    
    if (data.lowStockProducts > 0) {
      todoList.push({
        text: `处理低库存商品 ${data.lowStockProducts} 个`,
        done: false,
        priority: 'high'
      })
    }
    
    todoList.push({
      text: '查看本周销售报表',
      done: false,
      priority: 'normal'
    })
    
    todoList.push({
      text: '更新商品价格',
      done: false,
      priority: 'normal'
    })
    
    todos.value = todoList
  } catch (error) {
    console.error('获取待办事项失败:', error)
    todos.value = [
      { text: '暂无待办事项', done: false, priority: 'normal' }
    ]
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    loading.value = true
    const { data } = await getDashboardStatistics()
    
    console.log('Dashboard statistics data:', data)
    
    // 更新统计卡片 - 使用数据或默认值0
    stats.value[0].value = `¥${(data.todaySales || 0).toFixed(2)}`
    stats.value[1].value = `¥${(data.monthSales || 0).toFixed(2)}`
    stats.value[2].value = data.lowStockProducts || 0
    stats.value[3].value = data.pendingPurchaseOrders || 0
    
    // 如果数据确实为0，显示提示信息
    if (data.todaySales === 0 && data.monthSales === 0) {
      console.log('提示：当前暂无销售数据，这可能是因为：1.数据库中没有销售记录 2.今日/本月还没有销售订单')
    }
    
  } catch (error) {
    console.error('获取统计数据失败:', error)
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
  if (!salesChart) return
  
  let xAxisData = []
  let salesData = []
  let orderData = []
  
  // 根据不同时间周期设置不同的数据
  if (salesPeriod.value === 'week') {
    xAxisData = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    salesData = [3200, 4500, 3800, 5200, 4800, 6200, 7500]
    orderData = [45, 62, 55, 75, 68, 88, 105]
  } else if (salesPeriod.value === 'month') {
    xAxisData = ['1日', '5日', '10日', '15日', '20日', '25日', '30日']
    salesData = [12500, 15800, 18200, 22300, 25600, 28900, 32100]
    orderData = [180, 220, 265, 310, 355, 398, 445]
  } else if (salesPeriod.value === 'year') {
    xAxisData = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
    salesData = [85000, 92000, 108000, 125000, 142000, 158000, 175000, 188000, 196000, 205000, 218000, 235000]
    orderData = [1200, 1350, 1480, 1620, 1750, 1890, 2050, 2180, 2290, 2400, 2560, 2720]
  }
  
  // 更新图表配置
  salesChart.setOption({
    xAxis: {
      data: xAxisData
    },
    series: [
      {
        name: '销售额',
        data: salesData
      },
      {
        name: '订单数',
        data: orderData
      }
    ]
  })
  
  const periodText = salesPeriod.value === 'week' ? '本周' : salesPeriod.value === 'month' ? '本月' : '本年'
  console.log(`已切换到${periodText}数据`)
}

// 刷新数据
const refreshData = async () => {
  await fetchStatistics()
  await fetchActivities()
  await fetchTodos()
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
  await fetchActivities()
  await fetchTodos()
  
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
