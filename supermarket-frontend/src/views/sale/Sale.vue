<template>
  <div class="sale-container">
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>💰 销售管理</h2>
          <p class="subtitle">管理销售订单信息</p>
        </div>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增销售单
        </el-button>
      </div>
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
        <el-table-column prop="saleDate" label="销售日期" width="120" />
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
              确认出库
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

    <!-- 新增销售单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerName">
              <el-input v-model="form.customerName" placeholder="请输入客户名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售日期" prop="saleDate">
              <el-date-picker
                v-model="form.saleDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider content-position="left">销售明细</el-divider>
        
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
          <el-table-column label="销售数量" width="150">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" @change="calculateTotal" />
            </template>
          </el-table-column>
          <el-table-column label="销售单价" width="150">
            <template #default="{ row }">
              <el-input-number v-model="row.salePrice" :min="0" :precision="2" @change="calculateTotal" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              ¥{{ (row.quantity * row.salePrice).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import saleApi, { createSaleOrder } from '@/api/sale'
import productApi from '@/api/product'

const saleList = ref([])
const productList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('新增销售单')
const formRef = ref(null)
const form = ref({
  customerName: '',
  saleDate: '',
  items: []
})

const rules = {
  customerName: [
    { required: true, message: '请输入客户名称', trigger: 'blur' }
  ],
  saleDate: [
    { required: true, message: '请选择销售日期', trigger: 'change' }
  ]
}

const totalAmount = computed(() => {
  return form.value.items.reduce((sum, item) => {
    return sum + (item.quantity * item.salePrice)
  }, 0).toFixed(2)
})

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待出库', 1: '已出库', 2: '已取消' }
  return texts[status] || '未知'
}

const loadData = async () => {
  try {
    const { data } = await saleApi.getList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    saleList.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
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
  dialogTitle.value = '新增销售单'
  form.value = {
    customerName: '',
    saleDate: new Date().toISOString().split('T')[0],
    items: []
  }
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
    row.salePrice = product.salePrice || 0
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
    await createSaleOrder(form.value, 1) // cashierId 默认为 1
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
  await ElMessageBox.confirm('确定要确认出库吗？', '提示', {
    type: 'warning'
  })
  try {
    await saleApi.confirm(row.id)
    ElMessage.success('出库成功')
    loadData()
  } catch (error) {
    ElMessage.error('出库失败')
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

onMounted(() => {
  loadData()
  loadProducts()
})
</script>

<style scoped lang="scss">
.sale-container {
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
    color: #67c23a;
    font-weight: bold;
  }
}
</style>

