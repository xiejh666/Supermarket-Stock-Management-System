<template>
  <div class="inventory-container">
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>📊 库存管理</h2>
          <p class="subtitle">实时查看商品库存信息</p>
        </div>
        <el-button type="warning" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新库存
        </el-button>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
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
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
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
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
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
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.productName"
            placeholder="请输入商品名称"
            clearable
            @clear="loadData"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="searchForm.categoryId"
            placeholder="请选择分类"
            clearable
            @clear="loadData"
          >
            <el-option
              v-for="category in categoryList"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
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
            <el-tag v-if="row.stock <= 0" type="danger">无库存</el-tag>
            <el-tag v-else-if="row.stock <= row.minStock" type="warning">库存不足</el-tag>
            <el-tag v-else type="success">库存正常</el-tag>
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
        @size-change="loadData"
        @current-change="loadData"
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
            <el-radio :label="1">入库</el-radio>
            <el-radio :label="2">出库</el-radio>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Box, Warning, Goods, Search } from '@element-plus/icons-vue'
import inventoryApi from '@/api/inventory'
import categoryApi from '@/api/category'

const inventoryList = ref([])
const categoryList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = ref({
  productName: '',
  categoryId: null
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

const totalProducts = computed(() => inventoryList.value.length)
const lowStockCount = computed(() => {
  return inventoryList.value.filter(item => item.stock <= item.minStock).length
})
const totalStock = computed(() => {
  return inventoryList.value.reduce((sum, item) => sum + item.stock, 0)
})

const loadData = async () => {
  try {
    const { data } = await inventoryApi.getList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchForm.value
    })
    inventoryList.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
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
    await inventoryApi.adjust({
      productId: adjustForm.value.productId,
      type: adjustForm.value.type,
      quantity: adjustForm.value.quantity,
      reason: adjustForm.value.reason
    })
    ElMessage.success('调整成功')
    adjustDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('调整失败')
  }
}

const handleHistory = (row) => {
  ElMessage.info('查看历史记录功能开发中...')
}

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

<style scoped lang="scss">
.inventory-container {
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

  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 20px;

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 28px;
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
          margin-bottom: 4px;

          &.warning {
            color: #e6a23c;
          }
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .table-card {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

    .search-form {
      margin-bottom: 20px;
    }
  }

  .stock-warning {
    color: #e6a23c;
    font-weight: bold;
  }
}
</style>


