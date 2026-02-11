<template>
  <div class="purchase-container glass-page">
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>📦 采购管理</h2>
          <p class="subtitle">管理采购订单信息</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button
          v-if="canCreate('purchase')"
          type="primary"
          @click="handleAdd"
          class="glass-btn primary"
        >
          <el-icon><Plus /></el-icon>
          新增采购单
        </el-button>
      </div>
    </el-card>

    <!-- 搜索工具栏 -->
    <el-card class="toolbar">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="采购单号">
          <el-input v-model="queryForm.orderNo" placeholder="请输入采购单号" clearable style="width: 200px;" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="queryForm.supplierName" placeholder="请输入供应商名称" clearable style="width: 180px;" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 140px;">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
            <el-option label="已入库" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="采购时间">
          <el-date-picker
            v-model="queryForm.createTimeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 260px;"
            clearable
          />
        </el-form-item>
        <el-form-item label="入库时间">
          <el-date-picker
            v-model="queryForm.inboundTimeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 260px;"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch" class="glass-btn primary">查询</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset" class="glass-btn plain">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="displayPurchaseList" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="采购单号" width="180" />
        <el-table-column prop="supplierName" label="供应商" />
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="采购时间" width="180" />
        <el-table-column prop="inboundTime" label="入库时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button
              v-if="row.status === 0 && isAdmin"
              type="warning"
              link
              @click="handleAudit(row)"
            >
              审核
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="success"
              link
              @click="handleConfirm(row)"
            >
              确认入库
            </el-button>
            <el-button 
              v-if="canDelete('purchase')" 
              type="danger" 
              link 
              @click="() => checkPermission('delete', 'purchase', () => handleDelete(row))">
              删除
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

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="采购订单详情"
      width="900px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="采购单号">
          {{ viewData.orderNo }}
        </el-descriptions-item>
        <el-descriptions-item label="供应商">
          {{ viewData.supplierName }}
        </el-descriptions-item>
        <el-descriptions-item label="采购时间">
          {{ formatTime(viewData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="总金额">
          <span class="amount">¥{{ viewData.totalAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(viewData.status)">
            {{ getStatusText(viewData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入库时间">
          {{ formatTime(viewData.inboundTime) }}
        </el-descriptions-item>
      </el-descriptions>
      
      <el-divider content-position="left">采购明细</el-divider>
      
      <el-table :data="viewData.items" border>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="quantity" label="采购数量" width="120" />
        <el-table-column label="采购单价" width="120">
          <template #default="{ row }">
            ¥{{ row.unitPrice }}
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            ¥{{ row.totalPrice }}
          </template>
        </el-table-column>
      </el-table>
      
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增采购单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="1100px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" placeholder="请选择供应商" style="width: 100%">
                <el-option
                  v-for="supplier in supplierList"
                  :key="supplier.id"
                  :label="supplier.supplierName"
                  :value="supplier.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采购日期" prop="purchaseDate">
              <el-date-picker
                v-model="form.purchaseDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider content-position="left">采购明细</el-divider>
        
        <el-button type="success" @click="handleAddItem" style="margin-bottom: 10px">
          添加商品
        </el-button>
        
        <el-table :data="form.items" border style="width: 100%">
          <el-table-column label="商品" width="280">
            <template #default="{ row }">
              <el-select v-model="row.productId" placeholder="选择商品" style="width: 100%" @change="handleProductChange(row)">
                <el-option
                  v-for="product in productList"
                  :key="product.id"
                  :label="product.productName"
                  :value="product.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="采购数量" width="180">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" style="width: 100%" @change="calculateTotal" />
            </template>
          </el-table-column>
          <el-table-column label="采购单价" width="180">
            <template #default="{ row }">
              <el-input-number v-model="row.purchasePrice" :min="0" :precision="2" style="width: 100%" @change="calculateTotal" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="150">
            <template #default="{ row }">
              ¥{{ ((row.quantity || 0) * (row.purchasePrice || 0)).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ $index }">
              <el-button type="danger" link @click="handleRemoveItem($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div style="margin-top: 20px; text-align: right; font-size: 18px; font-weight: bold;">
          总金额: <span style="color: #f56c6c;">¥{{ totalAmount }}</span>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft, Plus } from '@element-plus/icons-vue'
import purchaseApi, { createPurchaseOrder, confirmInbound } from '@/api/purchase'
import supplierApi from '@/api/supplier'
import productApi from '@/api/product'
import { useUserStore } from '@/store/user'
import { canCreate, canDelete, canAudit, checkPermission } from '@/utils/permission'

const route = useRoute()
const router = useRouter()

const purchaseList = ref([])
const displayPurchaseList = ref([]) // 显示的列表
const supplierList = ref([])
const productList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选表单
const queryForm = reactive({
  orderNo: '',
  supplierName: '',
  status: null,
  createTimeRange: null,
  inboundTimeRange: null
})

// 执行筛选
const applyFilter = () => {
  let list = purchaseList.value
  
  // 按采购单号筛选
  if (queryForm.orderNo) {
    list = list.filter(item => item.orderNo && item.orderNo.includes(queryForm.orderNo))
  }
  
  // 按供应商名称筛选
  if (queryForm.supplierName) {
    list = list.filter(item => item.supplierName && item.supplierName.includes(queryForm.supplierName))
  }
  
  // 按状态筛选
  if (queryForm.status !== null && queryForm.status !== undefined && queryForm.status !== '') {
    list = list.filter(item => item.status === queryForm.status)
  }
  
  // 按采购时间筛选
  if (queryForm.createTimeRange && queryForm.createTimeRange.length === 2) {
    const [start, end] = queryForm.createTimeRange
    list = list.filter(item => {
      if (!item.createTime) return false
      const createTime = new Date(item.createTime)
      return createTime >= start && createTime <= end
    })
  }
  
  // 按入库时间筛选
  if (queryForm.inboundTimeRange && queryForm.inboundTimeRange.length === 2) {
    const [start, end] = queryForm.inboundTimeRange
    list = list.filter(item => {
      if (!item.inboundTime) return false
      const inboundTime = new Date(item.inboundTime)
      return inboundTime >= start && inboundTime <= end
    })
  }
  
  // 更新总数为筛选后的数据量
  total.value = list.length
  
  // 前端分页：只显示当前页的数据
  const startIndex = (pageNum.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  displayPurchaseList.value = list.slice(startIndex, endIndex)
}

const userStore = useUserStore()
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('新增采购单')
const formRef = ref(null)
const viewData = ref({})

// 从用户信息中获取角色，判断是否为管理员
const isAdmin = computed(() => {
  return userStore.userInfo?.username === 'admin'
})

const form = ref({
  supplierId: null,
  purchaseDate: '',
  items: []
})

const rules = {
  supplierId: [
    { required: true, message: '请选择供应商', trigger: 'change' }
  ],
  purchaseDate: [
    { required: true, message: '请选择采购日期', trigger: 'change' }
  ]
}

const totalAmount = computed(() => {
  return form.value.items.reduce((sum, item) => {
    return sum + ((item.quantity || 0) * (item.purchasePrice || 0))
  }, 0).toFixed(2)
})

// 查询
const handleSearch = () => {
  pageNum.value = 1  // 重置到第一页
  applyFilter()
}

// 重置
const handleReset = () => {
  queryForm.orderNo = ''
  queryForm.supplierName = ''
  queryForm.status = null
  queryForm.createTimeRange = null
  queryForm.inboundTimeRange = null
  
  // 清除URL查询参数
  if (route.query.filter) {
    router.replace({ path: route.path, query: {} })
  }
  
  // 重置页码到第一页
  pageNum.value = 1
  
  // 重置后重新应用筛选（此时无筛选条件，显示所有数据）
  applyFilter()
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'primary',
    2: 'danger',
    3: 'success'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '待入库',
    2: '已拒绝',
    3: '已入库'
  }
  return statusMap[status] || '未知'
}

const loadData = async () => {
  try {
    // 加载所有数据，不分页（前端筛选需要所有数据）
    const { data } = await purchaseApi.getList({
      current: 1,
      size: 10000 // 获取所有数据
    })
    purchaseList.value = data.records
    total.value = data.total
    // 加载完数据后应用筛选条件（会自动分页）
    applyFilter()
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const handlePageChange = () => {
  // 翻页时重新应用筛选，显示当前页的数据
  applyFilter()
}

const loadSuppliers = async () => {
  try {
    const { data } = await supplierApi.getAll()
    supplierList.value = data
  } catch (error) {
    console.error('加载供应商失败', error)
  }
}

const loadProducts = async () => {
  try {
    const { data } = await productApi.getAll()
    productList.value = data
  } catch (error) {
    console.error('加载商品失败', error)
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增采购单'
  form.value = {
    supplierId: null,
    purchaseDate: new Date().toISOString().split('T')[0],
    items: []
  }
  dialogVisible.value = true
}

const handleAddItem = () => {
  form.value.items.push({
    productId: null,
    quantity: 1,
    purchasePrice: 0
  })
}

const handleRemoveItem = (index) => {
  form.value.items.splice(index, 1)
}

const handleProductChange = (row) => {
  const product = productList.value.find(p => p.id === row.productId)
  if (product) {
    row.purchasePrice = product.purchasePrice || 0
  }
  calculateTotal()
}

const calculateTotal = () => {
  // 触发计算属性更新
}

const handleSubmit = async () => {
  if (form.value.items.length === 0) {
    ElMessage.warning('请至少添加一个商品')
    return
  }
  
  await formRef.value.validate()
  try {
    // 使用具名导出的函数，使用当前登录用户的ID作为申请人ID
    const currentUserId = userStore.userInfo?.userId || userStore.userInfo?.id
    await createPurchaseOrder(form.value, currentUserId)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleView = async (row) => {
  try {
    const { data } = await purchaseApi.getDetail(row.id)
    viewData.value = data
    viewDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

const handleAudit = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入审核备注（可选）', '审核采购订单', {
      confirmButtonText: '通过',
      cancelButtonText: '拒绝',
      distinguishCancelAndClose: true,
      inputPlaceholder: '请输入审核备注'
    })
    
    // 通过审核
    await purchaseApi.audit(row.id, 1, value || '', 1) // auditorId 默认为 1
    ElMessage.success('审核通过')
    loadData()
  } catch (error) {
    if (error === 'cancel') {
      // 拒绝审核
      try {
        const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPlaceholder: '请输入拒绝原因'
        })
        
        await purchaseApi.audit(row.id, 2, value || '审核不通过', 1)
        ElMessage.success('已拒绝')
        loadData()
      } catch (err) {
        if (err !== 'cancel' && err !== 'close') {
          ElMessage.error('操作失败')
        }
      }
    } else if (error !== 'close') {
      ElMessage.error('操作失败')
    }
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const handleConfirm = async (row) => {
  await ElMessageBox.confirm('确定要确认入库吗？', '提示', {
    type: 'warning'
  })
  try {
    await confirmInbound(row.id, 1) // operatorId 默认为 1
    ElMessage.success('入库成功')
    loadData()
  } catch (error) {
    ElMessage.error('入库失败')
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该采购单吗？', '提示', {
    type: 'warning'
  })
  try {
    const currentUserId = userStore.userInfo?.userId || userStore.userInfo?.id
    await purchaseApi.delete(row.id, currentUserId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 应用路由筛选条件
const applyRouteFilter = () => {
  // 清除之前的筛选条件
  if (!route.query.filter && !route.query.orderNo) {
    queryForm.status = null
    queryForm.orderNo = ''
  }
  
  // 应用待审核筛选
  if (route.query.filter === 'pending') {
    queryForm.status = 0 // 0 表示待审核
    pageNum.value = 1 // 重置页码
  }
  
  // 应用订单号筛选（从消息通知跳转）
  if (route.query.orderNo) {
    queryForm.orderNo = route.query.orderNo
    queryForm.status = null // 清除状态筛选
    queryForm.supplierName = '' // 清除供应商筛选
    queryForm.createTimeRange = null // 清除时间筛选
    queryForm.inboundTimeRange = null
    pageNum.value = 1 // 重置页码
  }
}

// 监听路由变化（监听filter、orderNo和时间戳_t）
watch(() => [route.query.filter, route.query.orderNo, route.query._t], async () => {
  applyRouteFilter()
  await loadData() // loadData 会自动调用 applyFilter
}, { immediate: false })

onMounted(async () => {
  loadSuppliers()
  loadProducts()
  
  // 检查路由参数，支持从仪表盘跳转时自动筛选待审核订单
  applyRouteFilter()
  
  await loadData() // loadData 会自动调用 applyFilter
})
</script>

<style scoped lang="scss">
@use "@/styles/glass-theme.scss" as *;

.purchase-container {
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

  .toolbar {
    margin-bottom: 24px;
  }

  .table-card {
    padding: 8px;
  }

  .amount {
    color: #ef4444;
    font-weight: 700;
  }
}
</style>
