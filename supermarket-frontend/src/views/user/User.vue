<template>
  <div class="user-container glass-page">
    <!-- 标题卡片 -->
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>👤 用户管理</h2>
          <p class="subtitle">系统用户及权限管理</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button
          v-if="canCreate('user')"
          type="primary"
          @click="handleAdd"
          class="glass-btn primary"
        >
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>
    </el-card>

    <!-- 搜索卡片 -->
    <el-card class="search-card toolbar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable @keyup.enter="handleSearch" style="width: 180px;" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入姓名" clearable @keyup.enter="handleSearch" style="width: 180px;" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.roleCode" placeholder="全部" clearable style="width: 150px;">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="收银员" value="CASHIER" />
            <el-option label="库管员" value="WAREHOUSE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="glass-btn primary">查询</el-button>
          <el-button @click="handleReset" class="glass-btn plain">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="userList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="roleName" label="角色" />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
              :disabled="row.roleCode === 'ADMIN'"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="canUpdate('user')" 
              type="primary" 
              link 
              @click="() => checkPermission('update', 'user', () => handleEdit(row))">
              编辑
            </el-button>
            <el-button 
              v-if="canUpdate('user')" 
              type="warning" 
              link 
              @click="handleResetPwd(row)">
              重置密码
            </el-button>
            <el-button 
              v-if="canDelete('user') && row.roleCode !== 'ADMIN'" 
              type="danger" 
              link 
              @click="() => checkPermission('delete', 'user', () => handleDelete(row))">
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="form.roleCode" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="收银员" value="CASHIER" />
            <el-option label="库管员" value="WAREHOUSE" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
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
import userApi from '@/api/user'
import { canCreate, canUpdate, canDelete, checkPermission } from '@/utils/permission'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const userList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = ref({
  username: '',
  realName: '',
  roleCode: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref(null)
const form = ref({
  id: null,
  username: '',
  realName: '',
  roleCode: '',
  phone: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const loadData = async () => {
  try {
    const { data } = await userApi.getList({
      current: pageNum.value,
      size: pageSize.value,
      username: searchForm.value.username,
      realName: searchForm.value.realName,
      roleCode: searchForm.value.roleCode
    })
    userList.value = data.records
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
    username: '',
    realName: '',
    roleCode: ''
  }
  handleSearch()
}

const handlePageChange = () => {
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  form.value = {
    id: null,
    username: '',
    realName: '',
    roleCode: '',
    phone: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleStatusChange = async (row) => {
  try {
    const currentUserId = userStore.userInfo?.userId || userStore.userInfo?.id
    await userApi.updateStatus(row.id, row.status, currentUserId)
    ElMessage.success('操作成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('操作失败')
  }
}

const handleResetPwd = async (row) => {
  try {
    await ElMessageBox.confirm('确定要重置该用户的密码为 123456 吗？', '提示', {
      type: 'warning'
    })
    const currentUserId = userStore.userInfo?.userId || userStore.userInfo?.id
    await userApi.resetPassword(row.id, currentUserId)
    ElMessage.success('密码已重置为 123456')
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    const currentUserId = userStore.userInfo?.userId || userStore.userInfo?.id
    if (form.value.id) {
      await userApi.update(form.value, currentUserId)
      ElMessage.success('更新成功')
    } else {
      await userApi.create(form.value, currentUserId)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      type: 'warning'
    })
    const currentUserId = userStore.userInfo?.userId || userStore.userInfo?.id
    await userApi.delete(row.id, currentUserId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
@use "@/styles/glass-theme.scss" as *;

.user-container {
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

  .search-card {
    margin-bottom: 24px;
  }

  .table-card {
    padding: 8px;
  }
}
</style>
