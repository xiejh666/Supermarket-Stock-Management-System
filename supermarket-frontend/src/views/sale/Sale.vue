<template>
  <div class="sale-container glass-page">
    <!-- 标题卡片 -->
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>💰 销售管理</h2>
          <p class="subtitle">管理销售订单信息</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button
          v-if="canCreate('sale')"
          type="primary"
          @click="handleAdd"
          class="glass-btn primary"
        >
          <el-icon><Plus /></el-icon>
          新增销售单
        </el-button>
      </div>
    </el-card>

    <!-- 搜索工具栏 -->
    <el-card class="toolbar">
      <el-form :model="searchForm">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="订单号">
              <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="客户名称">
              <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 100%;">
                <el-option label="待支付" :value="0" />
                <el-option label="已完成" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="销售日期">
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 100%;"
                @change="handleSearch"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24" style="text-align: right;">
            <el-button type="primary" @click="handleSearch" class="glass-btn primary">查询</el-button>
            <el-button @click="handleReset" class="glass-btn plain">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="saleList" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="销售单号" width="180" />
        <el-table-column prop="customerName" label="客户名称" />
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
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button 
              v-if="canDelete('sale')" 
              type="danger" 
              link 
              @click="() => checkPermission('delete', 'sale', () => handleDelete(row))">
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
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 新增销售单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="1100px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="客户名称" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" clearable filterable style="width: 100%" @change="handleCustomerChange">
                <el-option
                  v-for="customer in customerList"
                  :key="customer.id"
                  :label="customer.customerName"
                  :value="customer.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="手机号">
              <el-input v-model="form.customerPhone" placeholder="选择客户后自动显示" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="地址">
              <el-input v-model="form.customerAddress" placeholder="选择客户后自动显示" readonly />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider content-position="left">销售明细</el-divider>
        
        <el-button type="success" @click="handleAddItem" style="margin-bottom: 10px">
          添加商品
        </el-button>
        
        <el-table :data="form.items" border style="width: 100%">
          <el-table-column label="商品" width="280">
            <template #default="{ row }">
              <el-select v-model="row.productId" placeholder="选择商品" @change="handleProductChange(row)" style="width: 100%">
                <el-option
                  v-for="product in productList"
                  :key="product.id"
                  :label="product.productName"
                  :value="product.id"
                  :disabled="product.status === 0"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="销售数量" width="180">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" @change="calculateTotal" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="销售单价" width="180">
            <template #default="{ row }">
              <el-input-number v-model="row.salePrice" :min="0" :precision="2" @change="calculateTotal" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="150">
            <template #default="{ row }">
              <span style="color: #67c23a; font-weight: bold;">¥{{ (row.quantity * row.salePrice).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ $index }">
              <el-button type="danger" link @click="handleRemoveItem($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div style="margin-top: 20px; text-align: right; font-size: 18px; font-weight: bold;">
          总金额: <span style="color: #67c23a;">¥{{ totalAmount }}</span>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="销售订单详情"
      width="800px"
    >
      <div v-if="detailForm" class="detail-content">
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="订单编号">{{ detailForm.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="客户名称">{{ detailForm.customerName || '无' }}</el-descriptions-item>
          <el-descriptions-item label="客户手机号">{{ detailForm.customerPhone || '无' }}</el-descriptions-item>
          <el-descriptions-item label="客户地址">{{ detailForm.customerAddress || '无' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailForm.createTime }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detailForm.status)">
              {{ getStatusText(detailForm.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总金额">
             <span class="amount">¥{{ detailForm.totalAmount }}</span>
          </el-descriptions-item>
           <el-descriptions-item label="收银员">{{ detailForm.cashierName }}</el-descriptions-item>
           <el-descriptions-item v-if="detailForm.status === 2" label="取消原因" :span="2">
              {{ detailForm.cancelReason }}
           </el-descriptions-item>
        </el-descriptions>

        <div class="section-title">商品明细</div>
        <el-table :data="detailForm.items" border stripe>
          <el-table-column prop="productCode" label="商品编码" width="150" />
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="unitPrice" label="单价" width="120">
             <template #default="{ row }">¥{{ row.unitPrice }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column prop="totalPrice" label="总价" width="120">
             <template #default="{ row }">¥{{ row.totalPrice }}</template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <div class="dialog-footer">
           <el-button @click="detailVisible = false">关闭</el-button>
           <template v-if="detailForm && detailForm.status === 0">
              <el-button type="danger" @click="handleOpenCancel">取消支付</el-button>
              <el-button type="success" @click="handlePay">确认支付</el-button>
           </template>
        </div>
      </template>
    </el-dialog>

    <!-- 取消原因对话框 -->
    <el-dialog
      v-model="cancelVisible"
      title="取消订单"
      width="400px"
    >
       <el-form :model="cancelForm" label-width="80px">
          <el-form-item label="取消原因" required>
             <el-input 
                v-model="cancelForm.reason" 
                type="textarea" 
                :rows="3" 
                placeholder="请输入取消原因"
             />
          </el-form-item>
       </el-form>
       <template #footer>
          <el-button @click="cancelVisible = false">返回</el-button>
          <el-button type="primary" @click="handleCancel">确认取消</el-button>
       </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import saleApi, { createSaleOrder } from '@/api/sale'
import productApi from '@/api/product'
import customerApi from '@/api/customer'
import { canCreate, canDelete, checkPermission } from '@/utils/permission'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const saleList = ref([])
const productList = ref([])
const customerList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = ref({
  orderNo: '',
  customerName: '',
  status: null,
  dateRange: []
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增销售单')
const formRef = ref(null)
const form = ref({
  customerId: null,
  customerPhone: '',
  customerAddress: '',
  items: []
})

// 详情相关
const detailVisible = ref(false)
const detailForm = ref(null)

// 取消相关
const cancelVisible = ref(false)
const cancelForm = ref({
    reason: ''
})
const currentOrderId = ref(null)

const rules = {
  customerId: [
    { required: false, message: '请选择客户', trigger: 'change' }
  ]
}

const totalAmount = computed(() => {
  return form.value.items.reduce((sum, item) => {
    return sum + (item.quantity * item.salePrice)
  }, 0).toFixed(2)
})

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'info' } // 0:待支付(warning), 1:已支付(success), 2:已取消(info)
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待支付', 1: '已支付', 2: '已取消' }
  return texts[status] || '未知'
}

const loadData = async () => {
  try {
    const params = {
      current: pageNum.value,
      size: pageSize.value,
      orderNo: searchForm.value.orderNo,
      customerName: searchForm.value.customerName
    }
    
    // 添加状态筛选
    if (searchForm.value.status !== null && searchForm.value.status !== undefined && searchForm.value.status !== '') {
      params.status = searchForm.value.status
    }
    
    if (searchForm.value.dateRange && searchForm.value.dateRange.length === 2) {
        params.startDate = searchForm.value.dateRange[0]
        params.endDate = searchForm.value.dateRange[1]
    }

    const { data } = await saleApi.getList(params)
    saleList.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.value = {
    orderNo: '',
    customerName: '',
    status: null,
    dateRange: []
  }
  
  // 清除URL查询参数
  if (route.query.orderNo) {
    router.replace({ path: route.path, query: {} })
  }
  
  handleSearch()
}

// 应用路由筛选参数
const applyRouteFilter = () => {
  // 清除之前的筛选条件
  if (!route.query.orderNo) {
    searchForm.value.orderNo = ''
  }
  
  // 应用订单号筛选（从最新动态跳转）
  if (route.query.orderNo) {
    searchForm.value.orderNo = route.query.orderNo
  }
}

// 监听路由变化（包括时间戳）
watch(() => [route.query.orderNo, route.query._t], async () => {
  applyRouteFilter()
  await loadData()
}, { immediate: false })

const loadProducts = async () => {
  try {
    const { data } = await productApi.getAll()
    productList.value = data
  } catch (error) {
    console.error('加载商品失败', error)
  }
}

const loadCustomers = async () => {
  try {
    const { data } = await customerApi.getAll()
    customerList.value = data
  } catch (error) {
    console.error('加载客户失败', error)
  }
}

const handleAdd = async () => {
  dialogTitle.value = '新增销售单'
  form.value = {
    customerId: null,
    customerPhone: '',
    customerAddress: '',
    items: []
  }
  // 打开对话框时重新加载客户列表，确保获取最新数据
  await loadCustomers()
  dialogVisible.value = true
}

const handleAddItem = () => {
  form.value.items.push({
    productId: null,
    quantity: 1,
    salePrice: 0
  })
}

const handleRemoveItem = (index) => {
  form.value.items.splice(index, 1)
}

const handleProductChange = (row) => {
  const product = productList.value.find(p => p.id === row.productId)
  if (product) {
    row.salePrice = product.price || 0 // 注意：商品API返回的售价字段可能是price
  }
  calculateTotal()
}

const calculateTotal = () => {
  // 触发计算属性更新
}

const handleCustomerChange = (customerId) => {
  if (customerId) {
    const customer = customerList.value.find(c => c.id === customerId)
    if (customer) {
      form.value.customerPhone = customer.phone || ''
      form.value.customerAddress = customer.address || ''
    }
  } else {
    // 清空选择时，清空手机号和地址
    form.value.customerPhone = ''
    form.value.customerAddress = ''
  }
}

const handleSubmit = async () => {
  if (form.value.items.length === 0) {
    ElMessage.warning('请至少添加一个商品')
    return
  }
  
  await formRef.value.validate()
  try {
    const currentUserId = userStore.userInfo?.userId || userStore.userInfo?.id
    await createSaleOrder(form.value, currentUserId, currentUserId) // cashierId 和 operatorId 都使用当前用户ID
    ElMessage.success('创建成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleView = async (row) => {
  try {
      const { data } = await saleApi.getDetail(row.id)
      detailForm.value = data
      detailVisible.value = true
      currentOrderId.value = row.id
  } catch (error) {
      ElMessage.error('获取详情失败')
  }
}

const handlePay = async () => {
    try {
        await ElMessageBox.confirm('确定要确认支付吗？库存将被扣减。', '提示', {
            type: 'warning'
        })
        await saleApi.pay(currentOrderId.value)
        ElMessage.success('支付成功')
        detailVisible.value = false
        loadData()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('支付失败')
        }
    }
}

const handleOpenCancel = () => {
    cancelForm.value.reason = ''
    cancelVisible.value = true
}

const handleCancel = async () => {
    if (!cancelForm.value.reason.trim()) {
        ElMessage.warning('请输入取消原因')
        return
    }
    try {
        await saleApi.cancel(currentOrderId.value, cancelForm.value.reason)
        ElMessage.success('取消成功')
        cancelVisible.value = false
        detailVisible.value = false
        loadData()
    } catch (error) {
        ElMessage.error('取消失败')
    }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该销售单吗？', '提示', {
    type: 'warning'
  })
  try {
    await saleApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(async () => {
  loadProducts()
  loadCustomers()
  
  // 检查路由参数，支持从仪表盘跳转时自动筛选
  applyRouteFilter()
  
  await loadData()
})
</script>

<style scoped lang="scss">
@use "@/styles/glass-theme.scss" as *;

.sale-container {
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
    color: #67c23a;
    font-weight: 700;
  }
  
  .detail-content {
      padding: 10px;
  }
  
  .section-title {
      margin: 24px 0 12px;
      font-weight: 700;
      font-size: 16px;
      border-left: 4px solid #3b82f6;
      padding-left: 12px;
      color: #1e293b;
  }
  
  .dialog-footer {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
  }
}
</style>
