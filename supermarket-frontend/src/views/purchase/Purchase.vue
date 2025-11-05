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
        <el-table-column prop="purchaseDate" label="采购日期" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button
              v-if="row.status === 0"
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

    <!-- 新增采购单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
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
          <el-table-column label="商品" width="200">
            <template #default="{ row }">
              <el-select v-model="row.productId" placeholder="选择商品" @change="handleProductChange(row)">
                <el-option
                  v-for="product in productList"
                  :key="product.id"
                  :label="product.productName"
                  :value="product.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="采购数量" width="150">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" @change="calculateTotal" />
            </template>
          </el-table-column>
          <el-table-column label="采购单价" width="150">
            <template #default="{ row }">
              <el-input-number v-model="row.purchasePrice" :min="0" :precision="2" @change="calculateTotal" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              ¥{{ (row.quantity * row.purchasePrice).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
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
import { Plus } from '@element-plus/icons-vue'
import purchaseApi, { createPurchaseOrder, confirmInbound } from '@/api/purchase'
import supplierApi from '@/api/supplier'
import productApi from '@/api/product'

const purchaseList = ref([])
const supplierList = ref([])
const productList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('新增采购单')
const formRef = ref(null)
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
    return sum + (item.quantity * item.purchasePrice)
  }, 0).toFixed(2)
})

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待入库', 1: '已入库', 2: '已取消' }
  return texts[status] || '未知'
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

const handleView = (row) => {
  ElMessage.info('查看详情功能开发中...')
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

