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
        <div class="info-panel">
          <!-- 最新动态 -->
          <el-card class="activity-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Bell /></el-icon>
                <span>最新动态</span>
              </div>
            </template>
            <div class="activity-list">
              <div 
                v-for="(activity, index) in activities" 
                :key="`${activity.id}-${index}`" 
                class="activity-item"
                :class="`activity-${activity.type}`"
              >
                <div class="activity-icon">
                  <el-icon :class="`icon-${activity.type}`">
                    <component :is="getActivityIcon(activity.type)" />
                  </el-icon>
                </div>
                <div class="activity-content">
                  <div class="activity-text">
                    <span class="activity-title">{{ activity.title }}</span>
                    <span class="activity-desc">{{ activity.content }}</span>
                  </div>
                  <div class="activity-meta">
                    <span class="activity-time">{{ activity.time }}</span>
                    <span v-if="activity.badge" class="activity-badge" :class="`badge-${activity.type}`">
                      {{ activity.badge }}
                    </span>
                  </div>
                </div>
              </div>
              <div v-if="activities.length === 0" class="activity-empty">
                <el-icon class="empty-icon"><InfoFilled /></el-icon>
                <span>暂无最新动态</span>
              </div>
            </div>
          </el-card>
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
  List,
  InfoFilled,
  SuccessFilled,
  CircleCheck,
  CircleClose,
  Clock
} from '@element-plus/icons-vue'

const loading = ref(false)
const salesPeriod = ref('week')

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
    // 从统计数据生成动态信息
    const activityList = []
    const now = Date.now()
    
    if (statisticsData.value) {
      const data = statisticsData.value
      
      // 销售动态
      if (data.todaySales > 0) {
        const changeText = data.todaySalesChange >= 0 ? '上升' : '下降'
        activityList.push({
          id: 'sales-today',
          title: '销售业绩',
          content: `今日销售额 ¥${data.todaySales.toFixed(2)}，较昨日${changeText} ${Math.abs(data.todaySalesChange || 0).toFixed(1)}%`,
          time: formatRelativeTime(new Date(now - 5 * 60 * 1000)),
          type: data.todaySalesChange >= 0 ? 'success' : 'warning',
          badge: changeText
        })
      }
      
      // 库存预警动态
      if (data.inventoryWarning && data.inventoryWarning.length > 0) {
        const warningItems = data.inventoryWarning.filter(item => item.warningLevel > 0)
        if (warningItems.length > 0) {
          const criticalCount = warningItems.filter(item => item.warningLevel === 3).length
          const warningCount = warningItems.filter(item => item.warningLevel === 2).length
          
          let warningText = ''
          if (criticalCount > 0) {
            warningText = `${criticalCount}个商品严重缺货`
          } else if (warningCount > 0) {
            warningText = `${warningCount}个商品库存偏低`
          }
          
          activityList.push({
            id: 'inventory-warning',
            title: '库存预警',
            content: `${warningText}，请及时补货`,
            time: formatRelativeTime(new Date(now - 10 * 60 * 1000)),
            type: criticalCount > 0 ? 'error' : 'warning',
            badge: criticalCount > 0 ? '紧急' : '注意'
          })
        }
      }
      
      // 采购订单动态
      if (data.purchaseStatistics && data.purchaseStatistics.statusStatistics) {
        const pendingOrders = data.purchaseStatistics.statusStatistics.find(s => s.statusValue === 0)
        if (pendingOrders && pendingOrders.orderCount > 0) {
          activityList.push({
            id: 'purchase-pending',
            title: '采购管理',
            content: `有 ${pendingOrders.orderCount} 个采购订单待审核`,
            time: formatRelativeTime(new Date(now - 15 * 60 * 1000)),
            type: 'info',
            badge: '待处理'
          })
        }
      }
      
      // 月度业绩动态
      if (data.monthSales > 0) {
        const changeText = data.monthSalesChange >= 0 ? '增长' : '下降'
        activityList.push({
          id: 'sales-month',
          title: '月度统计',
          content: `本月销售额 ¥${data.monthSales.toFixed(2)}，较上月${changeText} ${Math.abs(data.monthSalesChange || 0).toFixed(1)}%`,
          time: formatRelativeTime(new Date(now - 30 * 60 * 1000)),
          type: data.monthSalesChange >= 0 ? 'success' : 'info',
          badge: '月报'
        })
      }
      
      // 系统状态动态
      activityList.push({
        id: 'system-status',
        title: '系统状态',
        content: '数据同步完成，所有服务运行正常',
        time: formatRelativeTime(new Date(now - 45 * 60 * 1000)),
        type: 'success',
        badge: '正常'
      })
    }
    
    // 如果没有业务动态，添加默认信息
    if (activityList.length === 0) {
      activityList.push({
        id: 'welcome',
        title: '系统欢迎',
        content: '欢迎使用超市管理系统',
        time: '刚刚',
        type: 'info',
        badge: '欢迎'
      })
    }
    
    activities.value = activityList.slice(0, 6) // 最多显示6条
    console.log('最新动态已更新:', activities.value.length, '条')
  } catch (error) {
    console.error('获取最新动态失败:', error)
    activities.value = [{
      id: 'error',
      title: '系统提示',
      content: '动态数据加载失败',
      time: '刚刚',
      type: 'error'
    }]
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
  await fetchStatistics('week')
  await fetchActivities()
  
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

.activity-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #9ca3af;
  font-size: 14px;
}

.empty-icon {
  font-size: 24px;
  margin-bottom: 8px;
  color: #d1d5db;
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
