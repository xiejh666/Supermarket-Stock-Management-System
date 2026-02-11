<template>
  <div class="category-container">
    <!-- 标题卡片 -->
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>📂 分类管理</h2>
          <p class="subtitle">管理商品分类信息</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button
          v-if="canCreate('category')"
          type="primary"
          @click="handleAdd"
          class="glass-btn primary"
        >
          <el-icon><Plus /></el-icon>
          新增分类
        </el-button>
      </div>
    </el-card>

    <!-- 搜索工具栏 -->
    <el-card class="toolbar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="分类名称">
          <el-input v-model="searchForm.categoryName" placeholder="请输入分类名称" clearable @keyup.enter="handleSearch" style="width: 200px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="glass-btn primary">查询</el-button>
          <el-button @click="handleReset" class="glass-btn plain">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="categoryList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="categoryName" label="分类名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="canUpdate('category')" 
              type="primary" 
              link 
              @click="() => checkPermission('update', 'category', () => handleEdit(row))">
              编辑
            </el-button>
            <el-button 
              v-if="canDelete('category')" 
              type="danger" 
              link 
              @click="() => checkPermission('delete', 'category', () => handleDelete(row))">
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import categoryApi from '@/api/category'
import { canCreate, canUpdate, canDelete, checkPermission } from '@/utils/permission'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()

const categoryList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = ref({
  categoryName: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const formRef = ref(null)
const form = ref({
  id: null,
  categoryName: '',
  description: ''
})

const rules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ]
}

const loadData = async () => {
  try {
    const { data } = await categoryApi.getList({
      current: pageNum.value,
      size: pageSize.value,
      categoryName: searchForm.value.categoryName
    })
    categoryList.value = data.records
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
    categoryName: ''
  }
  handleSearch()
}

const handlePageChange = () => {
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增分类'
  form.value = {
    id: null,
    categoryName: '',
    description: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑分类'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    const currentUserId = userStore.userInfo?.userId || userStore.userInfo?.id
    if (form.value.id) {
      await categoryApi.update(form.value, currentUserId)
      ElMessage.success('更新成功')
    } else {
      await categoryApi.create(form.value, currentUserId)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    const errorMessage = error?.response?.data?.message || error?.message || '操作失败，请联系管理员'
    ElMessage.error(errorMessage)
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
    type: 'warning'
  })
  try {
    const currentUserId = userStore.userInfo?.userId || userStore.userInfo?.id
    await categoryApi.delete(row.id, currentUserId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 应用路由参数
const applyRouteParams = () => {
  if (route.query.categoryName) {
    searchForm.value.categoryName = route.query.categoryName
  }
}

// 监听路由变化（包括时间戳）
watch(() => [route.query.categoryName, route.query._t], () => {
  applyRouteParams()
  loadData()
}, { immediate: false })

onMounted(() => {
  applyRouteParams()
  loadData()
})
</script>

<style scoped lang="scss">
.category-container {
  padding: 0;

  :deep(.el-card) {
    border-radius: 20px;
    border: 1px solid rgba(226, 232, 240, 0.8);
    box-shadow: 0 8px 32px -12px rgba(15, 23, 42, 0.08);
    backdrop-filter: blur(10px);
    background: rgba(255, 255, 255, 0.8);
  }

  .page-header {
    margin-bottom: 24px;
    position: relative;
    border: none;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 255, 255, 0.7) 100%);

    .header-content {
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
    padding: 4px;
    
    :deep(.el-form-item) {
      margin-bottom: 0;
      margin-right: 18px;
    }

    :deep(.el-input__wrapper) {
      border-radius: 12px;
      background: rgba(248, 250, 252, 0.8);
      box-shadow: none !important;
      border: 1px solid #e2e8f0;
    }
  }

  .table-card {
    padding: 8px;
    
    :deep(.el-table) {
      --el-table-border-color: #f1f5f9;
      --el-table-header-bg-color: #f8fafc;
      border-radius: 12px;
      overflow: hidden;
    }
  }
}

/* 玻璃感按钮样式 */
.glass-btn {
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  backdrop-filter: blur(4px);
  padding: 8px 20px;
  height: 38px;

  &.primary {
    background: rgba(59, 130, 246, 0.1) !important;
    border-color: rgba(59, 130, 246, 0.2) !important;
    color: #2563eb !important;

    &:hover {
      background: rgba(59, 130, 246, 0.18) !important;
      transform: translateY(-2px);
      box-shadow: 0 8px 16px -4px rgba(59, 130, 246, 0.2);
    }
  }

  &.plain {
    background: rgba(241, 245, 249, 0.8) !important;
    border-color: #e2e8f0 !important;
    color: #475569 !important;

    &:hover {
      background: #f1f5f9 !important;
      transform: translateY(-2px);
      box-shadow: 0 8px 16px -4px rgba(0, 0, 0, 0.05);
    }
  }
}
</style>


