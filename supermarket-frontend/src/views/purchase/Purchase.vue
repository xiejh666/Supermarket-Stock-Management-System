<template>
  <div class="purchase-container">
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>📦 采购管理</h2>
          <p class="subtitle">管理采购订单信息</p>
        </div>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增采购单
        </el-button>
      </div>
    </el-card>

    <el-card class="table-card">
      <el-table :data="purchaseList" stripe style="width: 100%">
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
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import purchaseApi, { createPurchaseOrder, confirmInbound } from '@/api/purchase'
import supplierApi from '@/api/supplier'
import productApi from '@/api/product'
import { useUserStore } from '@/store/user'

const purchaseList = ref([])
const supplierList = ref([])
const productList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

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
    const { data } = await purchaseApi.getList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    purchaseList.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
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
    // 使用具名导出的函数
    await createPurchaseOrder(form.value, 1) // applicantId 默认为 1
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
    await purchaseApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadData()
  loadSuppliers()
  loadProducts()
})
</script>

<style scoped lang="scss">
.purchase-container {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .title-section {
        h2 {
          margin: 0 0 8px 0;
          font-size: 24px;
          font-weight: 600;
        }

        .subtitle {
          margin: 0;
          color: #909399;
          font-size: 14px;
        }
      }
    }
  }

  .table-card {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }

  .amount {
    color: #f56c6c;
    font-weight: bold;
  }
}
</style>

