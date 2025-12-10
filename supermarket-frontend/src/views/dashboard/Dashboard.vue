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
          :style="{ background: stat.gradient, cursor: stat.clickable ? 'pointer' : 'default' }"
          @click="handleStatCardClick(stat)"
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

    <!-- 最新动态 - 全宽显示 -->
    <el-row :gutter="24">
      <el-col :span="24">
        <div class="card chart-card activity-panel fade-in-up delay-6">
          <div class="chart-header activity-header">
            <div class="header-left">
              <h3 class="chart-title">
                <el-icon><Bell /></el-icon>
                最新动态
              </h3>
              <span class="activity-count">
                共 {{ activities.length }} 条动态
                <span v-if="activities.length > 0" style="color: #9ca3af; margin-left: 8px;">
                  (第 {{ (activityPagination.current - 1) * activityPagination.size + 1 }}-{{ Math.min(activityPagination.current * activityPagination.size, activities.length) }} 条)
                </span>
              </span>
            </div>
            <div class="header-right">
              <el-radio-group v-model="activityType" size="small" @change="fetchActivities">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="purchase">采购</el-radio-button>
                <el-radio-button label="sale">销售</el-radio-button>
                <el-radio-button label="inventory">库存</el-radio-button>
              </el-radio-group>
              <el-button 
                :icon="Refresh" 
                size="small" 
                @click="fetchActivities"
                :loading="activityLoading"
                style="margin-left: 12px;"
              >
                刷新
              </el-button>
            </div>
          </div>
          
          <div class="activity-list-container" v-loading="activityLoading">
            <div class="activity-grid">
              <div 
                v-for="(activity, index) in paginatedActivities" 
                :key="activity.id" 
                class="activity-card-item"
                :class="[`activity-type-${activity.type}`, `activity-icon-${activity.icon}`]"
                @click="handleActivityClick(activity)"
              >
                <div class="activity-card-icon">
                  <el-icon>
                    <component :is="getActivityIconComponent(activity.icon)" />
                  </el-icon>
                </div>
                <div class="activity-card-content">
                  <div class="activity-card-header">
                    <span class="activity-card-title">{{ activity.title }}</span>
                    <el-tag 
                      :type="getActivityTagType(activity.icon)" 
                      size="small"
                      effect="plain"
                    >
                      {{ activity.badge }}
                    </el-tag>
                  </div>
                  <div class="activity-card-desc">{{ activity.content }}</div>
                  <div class="activity-card-footer">
                    <span class="activity-card-time">
                      <el-icon><Clock /></el-icon>
                      {{ activity.time }}
                    </span>
                    <span class="activity-card-action">
                      点击查看详情
                      <el-icon><ArrowRight /></el-icon>
                    </span>
                  </div>
                </div>
              </div>
            </div>
            
            <div v-if="activities.length === 0" class="activity-empty-state">
              <el-icon class="empty-icon"><InfoFilled /></el-icon>
              <p class="empty-text">暂无最新动态</p>
              <p class="empty-desc">系统会实时展示采购、销售、库存等业务动态</p>
            </div>
            
            <!-- 分页 -->
            <div v-if="activities.length > 0" class="activity-pagination">
              <el-pagination
                v-model:current-page="activityPagination.current"
                v-model:page-size="activityPagination.size"
                :total="activities.length"
                :page-sizes="[6, 12, 18, 24]"
                layout="total, sizes, prev, pager, next, jumper"
                background
                @size-change="handleActivityPageChange"
                @current-change="handleActivityPageChange"
              />
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getDashboardStatistics } from '@/api/statistics'
import { getRecentActivities } from '@/api/activity'
import { useUserStore } from '@/store/user'
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
  List,
  InfoFilled,
  SuccessFilled,
  CircleCheck,
  CircleClose,
  Clock,
  ArrowRight,
  DocumentChecked,
  ShoppingTrolley,
  Goods
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const salesPeriod = ref('week')
const activityType = ref('all')
const activityLoading = ref(false)
let activityTimer = null

// 分页配置
const activityPagination = reactive({
  current: 1,
  size: 6
})

// 统计数据
const stats = ref([
  {
    label: '今日销售额',
    value: '¥0',
    change: '+12.5',
    trend: 'up',
    icon: Money,
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    clickable: false
  },
  {
    label: '本月销售额',
    value: '¥0',
    change: '+8.3',
    trend: 'up',
    icon: ShoppingBag,
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    clickable: false
  },
  {
    label: '低库存商品',
    value: '0',
    change: '-2.1',
    trend: 'down',
    icon: Box,
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    clickable: true,
    route: '/inventory',
    query: { filter: 'low' }
  },
  {
    label: '待审核订单',
    value: '0',
    change: '+5.4',
    trend: 'up',
    icon: UserFilled,
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    clickable: true,
    route: '/purchase',
    query: { filter: 'pending' }
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

// 分页后的动态列表
const paginatedActivities = computed(() => {
  const start = (activityPagination.current - 1) * activityPagination.size
  const end = start + activityPagination.size
  return activities.value.slice(start, end)
})

// 统计数据
const statisticsData = ref(null)

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

// 获取库存预警颜色
const getInventoryColor = (warningLevel) => {
  switch (warningLevel) {
    case 3: return '#ef4444' // 红色 - 严重不足
    case 2: return '#f59e0b' // 黄色 - 偏低
    case 0: return '#10b981' // 绿色 - 充足
    default: return '#6b7280' // 灰色 - 默认
  }
}

// 获取采购状态颜色
const getPurchaseStatusColor = (statusValue) => {
  switch (statusValue) {
    case 0: return '#f59e0b' // 黄色 - 待审核
    case 1: return '#10b981' // 绿色 - 已通过
    case 2: return '#ef4444' // 红色 - 已拒绝
    case 3: return '#3b82f6' // 蓝色 - 已入库
    default: return '#6b7280' // 灰色 - 默认
  }
}

// 获取活动图标
const getActivityIcon = (type) => {
  switch (type) {
    case 'success': return SuccessFilled
    case 'warning': return Warning
    case 'error': return CircleClose
    case 'info': return InfoFilled
    default: return InfoFilled
  }
}

// 获取最新动态
const fetchActivities = async () => {
  try {
    activityLoading.value = true
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    
    const { data } = await getRecentActivities({
      userId,
      type: activityType.value,
      limit: 100 // 获取更多数据，前端分页
    })
    
    activities.value = data || []
    // 重置到第一页
    activityPagination.current = 1
    console.log('获取最新动态成功:', activities.value.length, '条')
  } catch (error) {
    console.error('获取最新动态失败:', error)
    activities.value = []
  } finally {
    activityLoading.value = false
  }
}

// 处理分页变化
const handleActivityPageChange = () => {
  // 滚动到动态面板顶部
  const activityPanel = document.querySelector('.activity-panel')
  if (activityPanel) {
    activityPanel.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 获取活动图标组件
const getActivityIconComponent = (iconType) => {
  switch (iconType) {
    case 'success': return SuccessFilled
    case 'warning': return Warning
    case 'info': return InfoFilled
    case 'error': return CircleClose
    default: return DocumentChecked
  }
}

// 获取活动标签类型
const getActivityTagType = (iconType) => {
  switch (iconType) {
    case 'success': return 'success'
    case 'warning': return 'warning'
    case 'info': return ''
    case 'error': return 'danger'
    default: return 'info'
  }
}

// 处理统计卡片点击
const handleStatCardClick = (stat) => {
  if (!stat.clickable) return
  
  if (stat.route) {
    router.push({
      path: stat.route,
      query: stat.query || {}
    })
  }
}

// 处理活动点击
const handleActivityClick = (activity) => {
  console.log('点击活动:', activity)
  
  // 根据活动类型跳转到对应页面，并传递查询参数
  switch (activity.type) {
    case 'purchase':
      // 跳转到采购管理，传递订单号
      router.push({
        path: '/purchase',
        query: { orderNo: activity.orderNo }
      })
      break
    case 'sale':
      // 跳转到销售管理，传递订单号
      router.push({
        path: '/sale',
        query: { orderNo: activity.orderNo }
      })
      break
    case 'inventory':
      // 跳转到库存管理，传递商品名称
      router.push({
        path: '/inventory',
        query: { productName: activity.productName }
      })
      break
    default:
      console.log('未知活动类型:', activity.type)
  }
}

// 启动自动刷新
const startActivityAutoRefresh = () => {
  // 每30秒自动刷新一次
  activityTimer = setInterval(() => {
    fetchActivities()
  }, 30000)
}

// 停止自动刷新
const stopActivityAutoRefresh = () => {
  if (activityTimer) {
    clearInterval(activityTimer)
    activityTimer = null
  }
}


// 获取统计数据
const fetchStatistics = async (period = 'week') => {
  try {
    loading.value = true
    console.log('开始获取统计数据，周期:', period)
    
    const response = await getDashboardStatistics(period)
    console.log('API响应:', response)
    
    if (response.code === 200) {
      const data = response.data
      statisticsData.value = data
      console.log('后端返回的数据结构:', data)
      
      // 更新统计卡片 - 添加安全的数据处理
      const todaySalesValue = data.todaySales || 0
      const monthSalesValue = data.monthSales || 0
      
      stats.value[0].value = `¥${typeof todaySalesValue === 'number' ? todaySalesValue.toFixed(2) : todaySalesValue}`
      stats.value[1].value = `¥${typeof monthSalesValue === 'number' ? monthSalesValue.toFixed(2) : monthSalesValue}`
      stats.value[2].value = (data.lowStockProducts || 0).toString()
      stats.value[3].value = (data.pendingPurchaseOrders || 0).toString()
      
      // 更新动态比较数据
      stats.value[0].change = Math.abs(data.todaySalesChange || 0).toFixed(1)
      stats.value[0].trend = (data.todaySalesChange || 0) >= 0 ? 'up' : 'down'
      
      stats.value[1].change = Math.abs(data.monthSalesChange || 0).toFixed(1)
      stats.value[1].trend = (data.monthSalesChange || 0) >= 0 ? 'up' : 'down'
      
      stats.value[2].change = Math.abs(data.lowStockChange || 0).toFixed(1)
      stats.value[2].trend = (data.lowStockChange || 0) >= 0 ? 'up' : 'down'
      
      stats.value[3].change = Math.abs(data.pendingOrdersChange || 0).toFixed(1)
      stats.value[3].trend = (data.pendingOrdersChange || 0) >= 0 ? 'up' : 'down'
      
      console.log('统计数据更新成功')
    } else {
      console.error('API返回错误:', response)
      ElMessage.error(response.message || '获取统计数据失败')
    }

    // 数据获取后更新图表和最新动态
    updateChartsWithData()
    await fetchActivities() // 刷新最新动态
    
  } catch (error) {
    console.error('获取统计数据失败详细错误:', error)
    console.error('错误堆栈:', error.stack)
    ElMessage.error(`获取统计数据失败: ${error.message}`)
  } finally {
    loading.value = false
  }
}

// 使用真实数据更新图表
const updateChartsWithData = () => {
  if (!statisticsData.value) {
    console.log('统计数据为空，跳过图表更新')
    return
  }
  
  console.log('开始更新图表，数据:', statisticsData.value)
  
  // 更新销售趋势图
  if (salesChart && statisticsData.value.salesTrend) {
    try {
      const trendData = statisticsData.value.salesTrend
      console.log('销售趋势数据:', trendData)
      
      const option = {
        xAxis: {
          type: 'category',
          data: trendData.labels || [],
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
            data: trendData.salesData || [],
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
            data: trendData.orderData || [],
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
      salesChart.setOption(option, true) // 第二个参数true表示不合并，完全替换
      console.log('销售趋势图更新成功')
    } catch (error) {
      console.error('更新销售趋势图失败:', error)
    }
  } else {
    console.log('销售图表未初始化或无趋势数据')
  }
  
  // 更新分类占比图
  if (categoryChart && statisticsData.value.categoryRatio) {
    try {
      console.log('分类占比数据:', statisticsData.value.categoryRatio)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
        },
        legend: {
          orient: 'horizontal',
          bottom: 10,
          left: 'center',
          itemGap: 20,
          itemWidth: 14,
          itemHeight: 14,
          textStyle: {
            fontSize: 12
          }
        },
        series: [{
          name: '销售占比',
          type: 'pie',
          radius: ['35%', '65%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false
          },
          labelLine: {
            show: false
          },
          emphasis: {
            label: {
              show: false  // 悬停时也不显示标签，只显示tooltip
            },
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          data: statisticsData.value.categoryRatio.map((item, index) => {
            const colors = ['#667eea', '#f093fb', '#4facfe', '#43e97b', '#fa709a', '#ffeaa7', '#74b9ff']
            return {
              name: item.name,
              value: item.value,
              itemStyle: { color: colors[index % colors.length] }
            }
          })
        }]
      }
      categoryChart.setOption(option, true)
      console.log('分类占比图更新成功，数据条数:', statisticsData.value.categoryRatio.length)
    } catch (error) {
      console.error('更新分类占比图失败:', error)
    }
  } else {
    console.log('分类占比图未初始化或无数据')
  }
  
  // 更新库存预警图
  if (inventoryChart && statisticsData.value.inventoryWarning) {
    try {
      const warningData = statisticsData.value.inventoryWarning
      console.log('库存预警数据:', warningData)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(params) {
            const item = warningData[params[0].dataIndex]
            if (item.warningLevel === 0) {
              return `<div style="padding: 8px;">
                <div style="font-weight: bold; color: #10b981;">${item.productName}</div>
                <div style="margin-top: 4px; color: #6b7280;">所有商品库存充足</div>
              </div>`
            }
            return `<div style="padding: 8px;">
              <div style="font-weight: bold;">${item.productName}</div>
              <div style="margin-top: 4px;">当前库存: ${item.currentStock}</div>
              <div>预警数量: ${item.warningStock}</div>
              <div style="margin-top: 4px; color: ${item.warningLevel === 3 ? '#ef4444' : '#f59e0b'};">
                ${item.warningLevel === 3 ? '库存严重不足' : '库存偏低'}
              </div>
            </div>`
          }
        },
        grid: {
          left: '20%',
          right: '10%',
          top: '10%',
          bottom: '10%'
        },
        xAxis: {
          type: 'value',
          axisLabel: {
            fontSize: 12
          }
        },
        yAxis: {
          type: 'category',
          data: warningData.map(item => {
            // 限制标签长度，避免超出
            const name = item.productName
            return name.length > 6 ? name.substring(0, 6) + '...' : name
          }),
          axisLabel: {
            fontSize: 12,
            width: 80,
            overflow: 'truncate',
            ellipsis: '...'
          }
        },
        series: [
          {
            name: '当前库存',
            type: 'bar',
            data: warningData.map((item, index) => ({
              value: item.currentStock,
              itemStyle: {
                color: getInventoryColor(item.warningLevel)
              }
            })),
            barWidth: '60%',
            label: {
              show: true,
              position: 'right',
              fontSize: 11,
              formatter: function(params) {
                const item = warningData[params.dataIndex]
                return item.warningLevel === 0 ? '充足' : params.value.toString()
              }
            }
          }
        ]
      }
      inventoryChart.setOption(option, true)
      console.log('库存预警图更新成功')
    } catch (error) {
      console.error('更新库存预警图失败:', error)
    }
  }
  
  // 更新采购统计图
  if (purchaseChart && statisticsData.value.purchaseStatistics) {
    try {
      const statusData = statisticsData.value.purchaseStatistics.statusStatistics || []
      console.log('采购状态数据:', statusData)
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'horizontal',
          bottom: 10,
          left: 'center',
          itemGap: 15,
          textStyle: {
            fontSize: 12
          }
        },
        series: [
          {
            name: '采购状态',
            type: 'pie',
            radius: ['35%', '65%'],
            center: ['50%', '45%'],
            avoidLabelOverlap: true,
            itemStyle: {
              borderRadius: 8,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false
            },
            labelLine: {
              show: false
            },
            emphasis: {
              label: {
                show: false
              },
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            },
            data: statusData.map(item => ({
              value: item.orderCount,
              name: item.statusName,
              itemStyle: { 
                color: getPurchaseStatusColor(item.statusValue)
              }
            }))
          }
        ]
      }
      purchaseChart.setOption(option, true)
      console.log('采购统计图更新成功')
    } catch (error) {
      console.error('更新采购统计图失败:', error)
    }
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
      formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 10,
      left: 'center',
      itemGap: 20,
      itemWidth: 14,
      itemHeight: 14,
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '销售占比',
        type: 'pie',
        radius: ['35%', '65%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        labelLine: {
          show: false
        },
        emphasis: {
          label: {
            show: false  // 悬停时也不显示标签，只显示tooltip
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
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
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 10,
      left: 'center',
      itemGap: 15,
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '采购状态',
        type: 'pie',
        radius: ['35%', '65%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        labelLine: {
          show: false
        },
        emphasis: {
          label: {
            show: false
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
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
const updateSalesChart = async () => {
  // 重新获取对应时间周期的数据
  await fetchStatistics(salesPeriod.value)
  
  const periodText = salesPeriod.value === 'week' ? '本周' : salesPeriod.value === 'month' ? '本月' : '本年'
  console.log(`已切换到${periodText}数据`)
}

// 刷新数据
const refreshData = async () => {
  await fetchStatistics(salesPeriod.value)
  await fetchActivities()
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
  // 先初始化图表（使用默认配置）
  await nextTick()
  initSalesChart()
  initCategoryChart()
  initInventoryChart()
  initPurchaseChart()
  
  // 然后获取真实数据并更新图表
  await fetchStatistics('week')
  await fetchActivities()
  
  // 启动自动刷新
  startActivityAutoRefresh()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

// 组件卸载时清理
import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  stopActivityAutoRefresh()
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
  transition: all 0.3s ease;
}

.stat-card[style*="cursor: pointer"]:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2);
}

.stat-card[style*="cursor: pointer"]:active {
  transform: translateY(-2px) scale(1.01);
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

/* 最新动态样式 */
.activity-list {
  max-height: 400px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.activity-item:hover {
  background-color: #fafafa;
  border-radius: 8px;
  margin: 0 -12px;
  padding: 16px 12px;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  margin-right: 12px;
  margin-top: 2px;
}

.activity-icon .el-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.icon-success {
  background-color: #f0f9ff;
  color: #10b981;
}

.icon-warning {
  background-color: #fffbeb;
  color: #f59e0b;
}

.icon-error {
  background-color: #fef2f2;
  color: #ef4444;
}

.icon-info {
  background-color: #f0f9ff;
  color: #3b82f6;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-text {
  margin-bottom: 6px;
}

.activity-title {
  font-weight: 600;
  color: #1f2937;
  font-size: 14px;
  display: block;
  margin-bottom: 4px;
}

.activity-desc {
  color: #6b7280;
  font-size: 13px;
  line-height: 1.4;
}

.activity-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.activity-time {
  color: #9ca3af;
  font-size: 12px;
}

.activity-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
}

.badge-success {
  background-color: #dcfce7;
  color: #166534;
}

.badge-warning {
  background-color: #fef3c7;
  color: #92400e;
}

.badge-error {
  background-color: #fee2e2;
  color: #991b1b;
}

.badge-info {
  background-color: #dbeafe;
  color: #1e40af;
}

/* 最新动态面板 */
.activity-panel {
  margin-top: 24px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.activity-count {
  font-size: 13px;
  color: #6b7280;
  background: #f3f4f6;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.activity-list-container {
  min-height: 300px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  padding: 20px 0;
}

.activity-card-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.activity-card-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: #3b82f6;
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.activity-card-item:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  transform: translateY(-2px);
}

.activity-card-item:hover::before {
  transform: scaleY(1);
}

.activity-type-purchase::before {
  background: #8b5cf6;
}

.activity-type-sale::before {
  background: #10b981;
}

.activity-type-inventory::before {
  background: #f59e0b;
}

.activity-card-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 24px;
  transition: all 0.3s ease;
}

.activity-icon-success .activity-card-icon {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #16a34a;
}

.activity-icon-warning .activity-card-icon {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
}

.activity-icon-info .activity-card-icon {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #2563eb;
}

.activity-icon-error .activity-card-icon {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
}

.activity-card-item:hover .activity-card-icon {
  transform: scale(1.1) rotate(5deg);
}

.activity-card-content {
  flex: 1;
  min-width: 0;
}

.activity-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 8px;
}

.activity-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  line-height: 1.4;
}

.activity-card-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.activity-card-time {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #9ca3af;
}

.activity-card-time .el-icon {
  font-size: 14px;
}

.activity-card-action {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #3b82f6;
  font-weight: 500;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.activity-card-item:hover .activity-card-action {
  opacity: 1;
}

.activity-card-action .el-icon {
  font-size: 14px;
}

.activity-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.activity-empty-state .empty-icon {
  font-size: 48px;
  color: #d1d5db;
  margin-bottom: 16px;
}

.activity-empty-state .empty-text {
  font-size: 16px;
  font-weight: 500;
  color: #6b7280;
  margin: 0 0 8px 0;
}

.activity-empty-state .empty-desc {
  font-size: 13px;
  color: #9ca3af;
  margin: 0;
}

/* 分页器样式 */
.activity-pagination {
  display: flex;
  justify-content: center;
  padding: 24px 0 12px 0;
  border-top: 1px solid #f3f4f6;
  margin-top: 20px;
}

.activity-pagination :deep(.el-pagination) {
  gap: 8px;
}

.activity-pagination :deep(.el-pagination.is-background .btn-prev),
.activity-pagination :deep(.el-pagination.is-background .btn-next),
.activity-pagination :deep(.el-pagination.is-background .el-pager li) {
  border-radius: 6px;
  font-weight: 500;
}

.activity-pagination :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}


/* 响应式 */
@media (max-width: 768px) {
  .stat-value {
    font-size: 20px;
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
