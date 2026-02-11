<template>
  <div class="inventory-container glass-page">
    <!-- 标题卡片 -->
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>📊 库存管理</h2>
          <p class="subtitle">实时查看商品库存信息</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="warning" @click="handleRefresh" class="refresh-btn">
          <el-icon><Refresh /></el-icon>
          刷新库存
        </el-button>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 24px">
      <el-col :span="8">
        <el-card class="stat-card glass-card variant-blue">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalProducts }}</div>
              <div class="stat-label">商品总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card glass-card variant-pink">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value warning">{{ lowStockCount }}</div>
              <div class="stat-label">库存预警</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card glass-card variant-cyan">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalStock }}</div>
              <div class="stat-label">库存总量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card">
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form toolbar">
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.productName"
            placeholder="请输入商品名称"
            clearable
            @clear="loadData"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="searchForm.categoryId"
            placeholder="请选择分类"
            clearable
            @clear="loadData"
            style="width: 180px;"
          >
            <el-option
              v-for="category in categoryList"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select
            v-model="searchForm.stockStatus"
            placeholder="全部"
            clearable
            @clear="loadData"
            style="width: 140px;"
          >
            <el-option label="库存正常" value="normal" />
            <el-option label="低库存商品" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="glass-btn primary">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset" class="glass-btn plain">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="inventoryList" stripe style="width: 100%">
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="stock" label="当前库存" width="120">
          <template #default="{ row }">
            <span :class="{ 'stock-warning': row.stock <= row.minStock }">
              {{ row.stock }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="预警库存" width="120" />
        <el-table-column label="库存状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.stock <= row.minStock" type="warning" effect="light">低库存</el-tag>
            <el-tag v-else type="success" effect="light">库存正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleAdjust(row)">
              库存调整
            </el-button>
            <el-button type="success" link @click="handleHistory(row)">
              历史记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handlePageChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 库存调整对话框 -->
    <el-dialog
      v-model="adjustDialogVisible"
      title="库存调整"
      width="500px"
    >
      <el-form :model="adjustForm" :rules="adjustRules" ref="adjustFormRef" label-width="100px">
        <el-form-item label="商品名称">
          <el-input v-model="adjustForm.productName" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input v-model="adjustForm.currentStock" disabled />
        </el-form-item>
        <el-form-item label="调整类型" prop="type">
          <el-radio-group v-model="adjustForm.type">
            <el-radio v-if="canInbound('inventory')" :label="1">入库</el-radio>
            <el-radio v-if="canOutbound('inventory')" :label="2">出库</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调整数量" prop="quantity">
          <el-input-number v-model="adjustForm.quantity" :min="1" />
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input
            v-model="adjustForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入调整原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdjustSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 历史记录对话框 -->
    <el-dialog
      v-model="historyDialogVisible"
      :title="`${currentProductName} - 库存变动历史`"
      width="1000px"
    >
      <el-table 
        :data="historyList" 
        stripe 
        style="width: 100%"
        highlight-current-row
        @current-change="handleHistoryRowClick"
        :row-class-name="getHistoryRowClass"
      >
        <el-table-column prop="changeTypeName" label="变动类型" width="110">
          <template #default="{ row }">
            <el-tag :type="getChangeTypeTag(row.changeType)" size="small">
              {{ row.changeTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeQuantity" label="变动数量" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.changeQuantity > 0 ? '#10b981' : '#ef4444', fontWeight: 'bold' }">
              {{ row.changeQuantity > 0 ? '+' : '' }}{{ row.changeQuantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="beforeQuantity" label="变动前" width="90" />
        <el-table-column prop="afterQuantity" label="变动后" width="90" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip min-width="150" />
        <el-table-column prop="createTime" label="操作时间" width="160" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleHistoryDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="historyDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 历史记录详情对话框 -->
    <el-dialog
      v-model="historyDetailVisible"
      title="库存变动详情"
      width="600px"
    >
      <el-descriptions v-if="historyDetailData" :column="2" border>
        <el-descriptions-item label="商品名称" :span="2">
          <span style="font-weight: 600; color: #1e293b;">{{ historyDetailData.productName }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="变动类型">
          <el-tag :type="getChangeTypeTag(historyDetailData.changeType)">
            {{ historyDetailData.changeTypeName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="变动数量">
          <span :style="{ 
            color: historyDetailData.changeQuantity > 0 ? '#10b981' : '#ef4444',
            fontWeight: 'bold',
            fontSize: '16px'
          }">
            {{ historyDetailData.changeQuantity > 0 ? '+' : '' }}{{ historyDetailData.changeQuantity }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="变动前库存">
          <span style="font-size: 15px;">{{ historyDetailData.beforeQuantity }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="变动后库存">
          <span style="font-size: 15px; font-weight: 600; color: #3b82f6;">{{ historyDetailData.afterQuantity }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="订单号" :span="2">
          {{ historyDetailData.orderNo || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          <div style="white-space: pre-wrap; word-break: break-all;">
            {{ historyDetailData.remark || '无' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">
          <span style="color: #64748b;">
            <el-icon><Clock /></el-icon>
            {{ historyDetailData.createTime }}
          </span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="historyDetailVisible = false" class="glass-btn primary">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, Box, Warning, Goods, Search, Clock } from '@element-plus/icons-vue'
import inventoryApi from '@/api/inventory'
import categoryApi from '@/api/category'
import { canInbound, canOutbound } from '@/utils/permission'

const route = useRoute()
const router = useRouter()

const inventoryList = ref([])
const allInventoryList = ref([]) // 用于统计的全量数据
const categoryList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = ref({
  productName: '',
  categoryId: null,
  stockStatus: null
})

const adjustDialogVisible = ref(false)
const adjustFormRef = ref(null)
const adjustForm = ref({
  productId: null,
  productName: '',
  currentStock: 0,
  type: 1,
  quantity: 1,
  reason: ''
})

const adjustRules = {
  type: [
    { required: true, message: '请选择调整类型', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入调整数量', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入调整原因', trigger: 'blur' }
  ]
}

// 统计数据基于全量数据，不受筛选条件影响
const totalProducts = computed(() => allInventoryList.value.length)
const lowStockCount = computed(() => {
  return allInventoryList.value.filter(item => item.stock <= item.minStock).length
})
const totalStock = computed(() => {
  return allInventoryList.value.reduce((sum, item) => sum + item.stock, 0)
})

const loadData = async () => {
  try {
    const params = {
      current: pageNum.value,
      size: pageSize.value,
      productName: searchForm.value.productName,
      categoryId: searchForm.value.categoryId
    }
    
    // 处理库存状态筛选
    if (searchForm.value.stockStatus === 'low') {
      params.isWarning = true  // 低库存
    } else if (searchForm.value.stockStatus === 'normal') {
      params.isWarning = false  // 库存正常
    }
    
    const { data } = await inventoryApi.getList(params)
    
    // 使用后端返回的数据和总数
    inventoryList.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

// 加载全量数据用于统计（不受筛选条件影响）
const loadAllData = async () => {
  try {
    const { data } = await inventoryApi.getList({
      current: 1,
      size: 10000 // 获取所有数据
    })
    allInventoryList.value = data.records
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.value = {
    productName: '',
    categoryId: null,
    stockStatus: null
  }
  
  // 清除URL查询参数
  if (route.query.filter) {
    router.replace({ path: route.path, query: {} })
  }
  
  handleSearch()
}

const handlePageChange = () => {
  loadData()
}

const loadCategories = async () => {
  try {
    const { data } = await categoryApi.getAll()
    categoryList.value = data
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const handleRefresh = () => {
  loadData()
  loadAllData() // 刷新统计数据
  ElMessage.success('刷新成功')
}

const handleAdjust = (row) => {
  adjustForm.value = {
    productId: row.productId,
    productName: row.productName,
    currentStock: row.stock,
    type: 1,
    quantity: 1,
    reason: ''
  }
  adjustDialogVisible.value = true
}

const handleAdjustSubmit = async () => {
  await adjustFormRef.value.validate()
  try {
    const params = {
      productId: adjustForm.value.productId,
      quantity: adjustForm.value.quantity,
      remark: adjustForm.value.reason,
      operatorId: 1 // 默认操作人 ID
    }
    
    if (adjustForm.value.type === 1) {
      // 入库
      await inventoryApi.inbound(params)
      ElMessage.success('入库成功')
    } else if (adjustForm.value.type === 2) {
      // 出库
      await inventoryApi.outbound(params)
      ElMessage.success('出库成功')
    }
    
    adjustDialogVisible.value = false
    loadData()
    loadAllData() // 刷新统计数据
  } catch (error) {
    ElMessage.error('操作失败：' + (error.message || '请稍后重试'))
  }
}

const historyDialogVisible = ref(false)
const historyList = ref([])
const currentProductName = ref('')
const selectedHistoryRow = ref(null)
const historyDetailVisible = ref(false)
const historyDetailData = ref(null)

const handleHistory = async (row) => {
  try {
    currentProductName.value = row.productName
    const { data } = await inventoryApi.getLogs(row.productId)
    historyList.value = data
    historyDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取历史记录失败')
  }
}

const getChangeTypeTag = (type) => {
  const types = {
    1: 'success',
    2: 'warning',
    3: 'info'
  }
  return types[type] || 'info'
}

const handleHistoryRowClick = (row) => {
  selectedHistoryRow.value = row
}

const getHistoryRowClass = ({ row }) => {
  if (selectedHistoryRow.value && selectedHistoryRow.value.id === row.id) {
    return 'selected-history-row'
  }
  return ''
}

const handleHistoryDetail = (row) => {
  historyDetailData.value = row
  historyDetailVisible.value = true
}

// 应用路由筛选参数
const applyRouteFilter = () => {
  // 清除之前的筛选条件
  if (!route.query.filter && !route.query.productName) {
    searchForm.value.stockStatus = null
    searchForm.value.productName = ''
  }
  
  // 应用低库存筛选
  if (route.query.filter === 'low') {
    searchForm.value.stockStatus = 'low'
  }
  
  // 应用商品名称筛选（从最新动态跳转）
  if (route.query.productName) {
    searchForm.value.productName = route.query.productName
    searchForm.value.stockStatus = null // 清除库存状态筛选
  }
}

// 监听路由变化（监听filter、productName和时间戳_t）
watch(() => [route.query.filter, route.query.productName, route.query._t], () => {
  applyRouteFilter()
  loadData()
}, { immediate: false })

onMounted(async () => {
  await loadCategories()
  
  // 加载全量数据用于统计
  await loadAllData()
  
  // 检查路由参数，支持从仪表盘跳转时自动筛选
  applyRouteFilter()
  
  await loadData()
})
</script>

<style scoped lang="scss">
@use "@/styles/glass-theme.scss" as *;

.inventory-container {
  &.glass-page {
  }

  .page-header {
    margin-bottom: 24px;
    position: relative;
    border: none;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 255, 255, 0.7) 100%);

    .title-section {
      h2 {
        margin: 0 0 8px 0;
        font-size: 24px;
        font-weight: 800;
        color: #1e293b;
      }

      .subtitle {
        margin: 0;
        color: #64748b;
        font-size: 14px;
      }
    }

    .header-actions {
      position: absolute;
      top: 50%;
      right: 24px;
      transform: translateY(-50%);
    }
  }

  .refresh-btn {
    border-radius: 12px;
    font-weight: 600;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 16px -4px rgba(230, 162, 60, 0.3);
    }
  }

  /* 统计卡片玻璃感风格 */
  .stat-card.glass-card {
    min-height: 120px;
    display: flex;
    align-items: center;
    padding: 24px;
    border: 1px solid rgba(255, 255, 255, 0.8);
    
    &.variant-blue { background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.06) 100%); }
    &.variant-pink { background: linear-gradient(135deg, rgba(240, 147, 251, 0.1) 0%, rgba(245, 87, 108, 0.06) 100%); }
    &.variant-cyan { background: linear-gradient(135deg, rgba(79, 172, 254, 0.1) 0%, rgba(0, 242, 254, 0.06) 100%); }

    .stat-content {
      display: flex;
      align-items: center;
      gap: 20px;
      width: 100%;

      .stat-icon {
        width: 54px;
        height: 54px;
        border-radius: 14px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 26px;
        transition: all 0.3s ease;
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 26px;
          font-weight: 800;
          color: #1e293b;
          margin-bottom: 4px;
          letter-spacing: -0.5px;

          &.warning {
            color: #f59e0b;
          }
        }

        .stat-label {
          font-size: 13px;
          color: #64748b;
          font-weight: 500;
        }
      }
    }
  }

  .variant-blue .stat-icon { color: #3b82f6; background: rgba(59, 130, 246, 0.1); }
  .variant-pink .stat-icon { color: #ec4899; background: rgba(236, 72, 153, 0.1); }
  .variant-cyan .stat-icon { color: #06b6d4; background: rgba(6, 182, 212, 0.1); }

  .table-card {
    padding: 8px;
  }

  .stock-warning {
    color: #f59e0b;
    font-weight: 700;
  }
}

// 历史记录选中行样式
:deep(.selected-history-row) {
  background-color: rgba(59, 130, 246, 0.05) !important;
  
  td {
    background-color: transparent !important;
  }
}
</style>
