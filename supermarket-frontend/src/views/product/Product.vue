<template>
  <div class="product-container">
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>
            <el-icon><Goods /></el-icon>
            商品管理
          </h2>
          <p class="subtitle">管理商品信息</p>
        </div>
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增商品</el-button>
      </div>
    </el-card>

    <!-- 搜索工具栏 -->
    <el-card class="toolbar">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="商品名称">
          <el-input v-model="queryForm.productName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="queryForm.categoryId" placeholder="请选择分类" clearable>
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 商品列表 -->
    <el-card class="table-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#fafafa', fontWeight: '600' }"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="150">
          <template #default="{ row }">
            <div class="product-info">
              <el-avatar
                :size="40"
                :src="row.imageUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                shape="square"
              />
              <span class="product-name">{{ row.productName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="specification" label="规格" width="100" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="costPrice" label="成本价" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.costPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="retailPrice" label="零售价" width="100">
          <template #default="{ row }">
            <span class="price primary">¥{{ row.retailPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100">
          <template #default="{ row }">
            <el-tag :type="row.stock <= row.warningStock ? 'danger' : 'success'">
              {{ row.stock }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品编码" prop="productCode">
          <el-input v-model="form.productCode" placeholder="请输入商品编码" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规格" prop="specification">
              <el-input v-model="form.specification" placeholder="如: 500ml" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="form.unit" placeholder="如: 瓶" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="成本价" prop="costPrice">
              <el-input-number
                v-model="form.costPrice"
                :precision="2"
                :step="0.1"
                :min="0"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="零售价" prop="retailPrice">
              <el-input-number
                v-model="form.retailPrice"
                :precision="2"
                :step="0.1"
                :min="0"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="预警库存" prop="warningStock">
          <el-input-number
            v-model="form.warningStock"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-input v-model="form.imageUrl" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Goods,
  Plus,
  Search,
  RefreshLeft,
  View,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import {
  getProductList,
  createProduct,
  updateProduct,
  deleteProduct,
  updateProductStatus
} from '@/api/product'
import { getCategoryTree } from '@/api/category'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增商品')
const formRef = ref(null)

const queryForm = reactive({
  productName: '',
  categoryId: null,
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const categories = ref([])

const form = reactive({
  id: null,
  productName: '',
  productCode: '',
  categoryId: null,
  specification: '',
  unit: '',
  costPrice: 0,
  retailPrice: 0,
  warningStock: 10,
  imageUrl: '',
  description: '',
  status: 1
})

const rules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  productCode: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  costPrice: [{ required: true, message: '请输入成本价', trigger: 'blur' }],
  retailPrice: [{ required: true, message: '请输入零售价', trigger: 'blur' }]
}

// 获取商品列表
const fetchData = async () => {
  try {
    loading.value = true
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...queryForm
    }
    const { data } = await getProductList(params)
    tableData.value = data.records
    pagination.total = data.total
  } catch (error) {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const { data } = await getCategoryTree()
    categories.value = data
  } catch (error) {
    ElMessage.error('获取分类列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(queryForm, {
    productName: '',
    categoryId: null,
    status: null
  })
  handleSearch()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增商品'
  Object.assign(form, {
    id: null,
    productName: '',
    productCode: '',
    categoryId: null,
    specification: '',
    unit: '',
    costPrice: 0,
    retailPrice: 0,
    warningStock: 10,
    imageUrl: '',
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑商品'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 查看
const handleView = (row) => {
  ElMessage.info('查看详情功能开发中...')
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 修改状态
const handleStatusChange = async (row) => {
  try {
    await updateProductStatus(row.id, row.status)
    ElMessage.success(row.status === 1 ? '已上架' : '已下架')
  } catch (error) {
    ElMessage.error('操作失败')
    row.status = row.status === 1 ? 0 : 1 // 恢复状态
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    if (form.id) {
      await updateProduct(form)
      ElMessage.success('更新成功')
    } else {
      await createProduct(form)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchCategories()
})
</script>

<style scoped lang="scss">
.product-container {
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
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .subtitle {
          margin: 0;
          color: #909399;
          font-size: 14px;
        }
      }
    }
  }

  .toolbar {
    margin-bottom: 20px;
  }

  .table-card {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }

  .product-info {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .product-name {
    font-weight: 500;
  }

  .price {
    font-weight: 600;
    color: #606266;
    
    &.primary {
      color: #409eff;
      font-size: 16px;
    }
  }

  :deep(.el-pagination) {
    margin-top: 24px;
    justify-content: center;
  }
}
</style>



