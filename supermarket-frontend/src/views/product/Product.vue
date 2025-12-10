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
      </div>
      <div class="header-actions">
        <el-button v-if="canCreate('product')" type="primary" :icon="Plus" @click="handleAdd">新增商品</el-button>
      </div>
    </el-card>

    <!-- 搜索工具栏 -->
    <el-card class="toolbar">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="商品名称">
          <el-input v-model="queryForm.productName" placeholder="请输入商品名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="商品编码">
          <el-input v-model="queryForm.productCode" placeholder="请输入商品编码" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="queryForm.categoryId" placeholder="请选择分类" clearable style="width: 180px;">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px;">
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
        <el-table-column label="序号" width="60">
          <template #default="{ $index }">
            {{ (pagination.current - 1) * pagination.size + $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
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
            <el-button 
              v-if="canUpdate('product')" 
              link 
              type="primary" 
              :icon="Edit" 
              @click="() => checkPermission('update', 'product', () => handleEdit(row))">
              编辑
            </el-button>
            <el-button 
              v-if="canDelete('product')" 
              link 
              type="danger" 
              :icon="Delete" 
              @click="() => checkPermission('delete', 'product', () => handleDelete(row))">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handlePageChange"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="商品详情"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="商品编码">
          {{ viewData.productCode }}
        </el-descriptions-item>
        <el-descriptions-item label="商品名称">
          {{ viewData.productName }}
        </el-descriptions-item>
        <el-descriptions-item label="商品分类">
          {{ getCategoryName(viewData.categoryId) }}
        </el-descriptions-item>
        <el-descriptions-item label="规格">
          {{ viewData.specification || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="单位">
          {{ viewData.unit || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="成本价">
          <span class="price">¥{{ viewData.costPrice }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="零售价">
          <span class="price primary">¥{{ viewData.retailPrice }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="库存">
          <el-tag :type="viewData.stock <= viewData.warningStock ? 'danger' : 'success'">
            {{ viewData.stock }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预警库存">
          {{ viewData.warningStock }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === 1 ? 'success' : 'info'">
            {{ viewData.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商品图片" :span="2">
          <el-image
            v-if="viewData.imageUrl"
            :src="viewData.imageUrl"
            style="width: 100px; height: 100px"
            fit="cover"
            :preview-src-list="[viewData.imageUrl]"
          />
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="商品描述" :span="2">
          {{ viewData.description || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatTime(viewData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">
          {{ formatTime(viewData.updateTime) }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
        <el-button v-if="canUpdate('product')" type="primary" @click="handleEditFromView">编辑</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      @open="handleDialogOpen"
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
          <el-upload
            class="product-image-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :on-error="handleImageError"
            :before-upload="beforeImageUpload"
            accept="image/*"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="product-image" />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持jpg、png格式，大小不超过5MB</div>
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
        <el-button v-if="canCreate('product') || canUpdate('product')" type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
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
  getProductDetail,
  createProduct,
  updateProduct,
  deleteProduct,
  updateProductStatus
} from '@/api/product'
import { getAllCategories } from '@/api/category'
import { canCreate, canUpdate, canDelete, checkPermission } from '@/utils/permission'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()

const productList = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogTitle = ref('新增商品')
const formRef = ref(null)
const viewData = ref({})
const loading = ref(false)
const submitLoading = ref(false)

const queryForm = reactive({
  productName: '',
  productCode: '',
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
  costPrice: [
    { required: true, message: '请输入成本价', trigger: 'blur' }
  ],
  retailPrice: [
    { required: true, message: '请输入零售价', trigger: 'blur' }
  ]
}

// 图片上传配置
const uploadUrl = ref('/api/upload/product')
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${getToken()}`
}))

// 上传前验证
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 上传成功
const handleImageSuccess = (response) => {
  if (response.code === 200) {
    form.imageUrl = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

// 上传失败
const handleImageError = () => {
  ElMessage.error('图片上传失败，请重试')
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

// 获取分类列表（使用扁平化列表）
const fetchCategories = async () => {
  try {
    const { data } = await getAllCategories()
    categories.value = data
  } catch (error) {
    ElMessage.error('获取分类列表失败')
  }
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.categoryName : '-'
}

// 格式化时间
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

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

// 分页变化
const handlePageChange = () => {
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(queryForm, {
    productName: '',
    productCode: '',
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
const handleEdit = async (row) => {
  try {
    dialogTitle.value = '编辑商品'
    // 调用详情接口获取完整信息（包括库存预警）
    const { data } = await getProductDetail(row.id)
    Object.assign(form, data)
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取商品详情失败')
  }
}

// 查看
const handleView = (row) => {
  viewData.value = { ...row }
  viewDialogVisible.value = true
}

// 从查看对话框跳转到编辑
const handleEditFromView = () => {
  viewDialogVisible.value = false
  dialogTitle.value = '编辑商品'
  Object.assign(form, viewData.value)
  dialogVisible.value = true
}

// 对话框打开时刷新分类列表
const handleDialogOpen = () => {
  fetchCategories()
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const operatorId = userStore.userInfo?.userId || userStore.userInfo?.id
    await deleteProduct(row.id, operatorId)
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
    const operatorId = userStore.userInfo?.userId || userStore.userInfo?.id
    await updateProductStatus(row.id, row.status, operatorId)
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
    
    const operatorId = userStore.userInfo?.userId || userStore.userInfo?.id
    
    if (form.id) {
      await updateProduct(form, operatorId)
      ElMessage.success('更新成功')
    } else {
      await createProduct(form, operatorId)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    if (error !== false) {
      // 显示后端返回的具体错误信息
      const errorMessage = error?.response?.data?.message || error?.message || '操作失败，请联系管理员'
      ElMessage.error(errorMessage)
    }
  } finally {
    submitLoading.value = false
  }
}

// 应用路由参数
const applyRouteParams = () => {
  // 如果有路由参数，先清空表单
  if (route.query.productName || route.query.productCode) {
    queryForm.productName = ''
    queryForm.productCode = ''
    queryForm.categoryId = null
    queryForm.status = null
  }
  
  // 应用路由参数
  if (route.query.productName) {
    queryForm.productName = route.query.productName
  }
  if (route.query.productCode) {
    queryForm.productCode = route.query.productCode
  }
}

// 监听路由变化（包括时间戳）
watch(() => [route.query.productName, route.query.productCode, route.query._t], () => {
  applyRouteParams()
  fetchData()
}, { immediate: false })

onMounted(() => {
  applyRouteParams()
  fetchData()
  fetchCategories()
})
</script>

<style scoped lang="scss">
.product-container {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;
    position: relative;

    .header-content {
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

    .header-actions {
      position: absolute;
      top: 50%;
      right: 20px;
      transform: translateY(-50%);
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

  // 商品图片上传样式
  .product-image-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: all 0.3s;
      width: 178px;
      height: 178px;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        border-color: #409eff;
      }
    }

    .product-image {
      width: 178px;
      height: 178px;
      display: block;
      object-fit: cover;
    }

    .image-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      text-align: center;
    }
  }

  .upload-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
  }
}
</style>



