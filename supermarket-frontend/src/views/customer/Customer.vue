<template>
  <div class="customer-container glass-page">
    <!-- 标题卡片 -->
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>👥 客户管理</h2>
          <p class="subtitle">管理客户信息</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button
          v-if="canCreate('customer')"
          type="primary"
          @click="handleAdd"
          class="glass-btn primary"
        >
          <el-icon><Plus /></el-icon>
          新增客户
        </el-button>
      </div>
    </el-card>

    <!-- 搜索工具栏 -->
    <el-card class="toolbar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable @keyup.enter="handleSearch" style="width: 180px;" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable @keyup.enter="handleSearch" style="width: 180px;" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="searchForm.address" placeholder="请输入地址" clearable @keyup.enter="handleSearch" style="width: 200px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="glass-btn primary">查询</el-button>
          <el-button @click="handleReset" class="glass-btn plain">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="customerList" stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="80" :index="indexMethod" />
        <el-table-column prop="customerName" label="客户名称" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="canUpdate('customer')" 
              type="primary" 
              link 
              @click="() => checkPermission('update', 'customer', () => handleEdit(row))">
              编辑
            </el-button>
            <el-button 
              v-if="canDelete('customer')" 
              type="danger" 
              link 
              @click="() => checkPermission('delete', 'customer', () => handleDelete(row))">
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
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="form.customerName" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
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
import customerApi from '@/api/customer'
import { canCreate, canUpdate, canDelete, checkPermission } from '@/utils/permission'
import { useUserStore } from '@/store/user'

const route = useRoute()

const userStore = useUserStore()
const customerList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = ref({
  customerName: '',
  phone: '',
  address: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增客户')
const formRef = ref(null)
const form = ref({
  id: null,
  customerName: '',
  phone: '',
  address: ''
})

const rules = {
  customerName: [
    { required: true, message: '请输入客户名称', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const loadData = async () => {
  try {
    const { data } = await customerApi.getList({
      current: pageNum.value,
      size: pageSize.value,
      customerName: searchForm.value.customerName,
      phone: searchForm.value.phone,
      address: searchForm.value.address
    })
    customerList.value = [...data.records]
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
    customerName: '',
    phone: '',
    address: ''
  }
  handleSearch()
}

const handlePageChange = () => {
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增客户'
  form.value = {
    id: null,
    customerName: '',
    phone: '',
    address: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑客户'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    const currentUserId = userStore.userInfo?.userId || userStore.userInfo?.id
    if (form.value.id) {
      await customerApi.update(form.value, currentUserId)
      ElMessage.success('更新成功')
    } else {
      await customerApi.create(form.value, currentUserId)
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
  await ElMessageBox.confirm('确定要删除该客户吗？', '提示', {
    type: 'warning'
  })
  try {
    const currentUserId = userStore.userInfo?.userId || userStore.userInfo?.id
    await customerApi.delete(row.id, currentUserId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 计算序号
const indexMethod = (index) => {
  return (pageNum.value - 1) * pageSize.value + index + 1
}

// 应用路由参数
const applyRouteParams = () => {
  if (route.query.customerName) {
    searchForm.value.customerName = route.query.customerName
  }
}

// 监听路由变化（包括时间戳）
watch(() => [route.query.customerName, route.query._t], () => {
  applyRouteParams()
  loadData()
}, { immediate: false })

onMounted(() => {
  applyRouteParams()
  loadData()
})
</script>

<style scoped lang="scss">
@use "@/styles/glass-theme.scss" as *;

.customer-container {
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
}
</style>
