<template>
  <div class="customer-container">
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>👥 客户管理</h2>
          <p class="subtitle">管理客户信息</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button v-if="canCreate('customer')" type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增客户
        </el-button>
      </div>
      
      <el-divider style="margin: 15px 0;" />
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="customerList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
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
        @size-change="loadData"
        @current-change="loadData"
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import customerApi from '@/api/customer'
import { canCreate, canUpdate, canDelete, checkPermission } from '@/utils/permission'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const customerList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = ref({
  customerName: '',
  phone: ''
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
      phone: searchForm.value.phone
    })
    customerList.value = data.records
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
    phone: ''
  }
  handleSearch()
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
    ElMessage.error('操作失败')
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

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.customer-container {
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
  
  .search-form {
    margin-top: 20px;
  }

  .table-card {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
}
</style>
