<template>
  <div class="supplier-container">
    <!-- 标题卡片 -->
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>🏢 供应商管理</h2>
          <p class="subtitle">管理供应商信息</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button v-if="canCreate('supplier')" type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增供应商
        </el-button>
      </div>
    </el-card>

    <!-- 搜索卡片 -->
    <el-card class="search-card">
      <el-form :model="searchForm" class="search-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="供应商名称">
              <el-input v-model="searchForm.supplierName" placeholder="请输入供应商名称" clearable @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="联系人">
              <el-input v-model="searchForm.contactPerson" placeholder="请输入联系人" clearable @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="联系电话">
              <el-input v-model="searchForm.contactPhone" placeholder="请输入联系电话" clearable @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="地址">
              <el-input v-model="searchForm.address" placeholder="请输入地址" clearable @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24" style="text-align: right;">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="supplierList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="supplierName" label="供应商名称" />
        <el-table-column prop="contactPerson" label="联系人" />
        <el-table-column prop="contactPhone" label="联系电话" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="canUpdate('supplier')" 
              type="primary" 
              link 
              @click="() => checkPermission('update', 'supplier', () => handleEdit(row))">
              编辑
            </el-button>
            <el-button 
              v-if="canDelete('supplier')" 
              type="danger" 
              link 
              @click="() => checkPermission('delete', 'supplier', () => handleDelete(row))">
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
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="form.supplierName" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注"
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
import supplierApi from '@/api/supplier'
import { canCreate, canUpdate, canDelete, checkPermission } from '@/utils/permission'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()

const supplierList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = ref({
  supplierName: '',
  contactPerson: '',
  contactPhone: '',
  address: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增供应商')
const formRef = ref(null)
const form = ref({
  id: null,
  supplierName: '',
  contactPerson: '',
  contactPhone: '',
  address: '',
  remark: ''
})

const rules = {
  supplierName: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' }
  ],
  contactPerson: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ]
}

const loadData = async () => {
  try {
    const { data } = await supplierApi.getList({
      current: pageNum.value,
      size: pageSize.value,
      supplierName: searchForm.value.supplierName,
      contactPerson: searchForm.value.contactPerson,
      contactPhone: searchForm.value.contactPhone,
      address: searchForm.value.address
    })
    supplierList.value = data.records
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
    supplierName: '',
    contactPerson: '',
    contactPhone: '',
    address: ''
  }
  handleSearch()
}

const handlePageChange = () => {
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增供应商'
  form.value = {
    id: null,
    supplierName: '',
    contactPerson: '',
    contactPhone: '',
    address: '',
    remark: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑供应商'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    const currentUserId = userStore.userInfo?.userId || userStore.userInfo?.id
    if (form.value.id) {
      await supplierApi.update(form.value, currentUserId)
      ElMessage.success('更新成功')
    } else {
      await supplierApi.create(form.value, currentUserId)
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
  await ElMessageBox.confirm('确定要删除该供应商吗？', '提示', {
    type: 'warning'
  })
  try {
    const currentUserId = userStore.userInfo?.userId || userStore.userInfo?.id
    await supplierApi.delete(row.id, currentUserId)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 应用路由参数
const applyRouteParams = () => {
  if (route.query.supplierName) {
    searchForm.value.supplierName = route.query.supplierName
  }
}

// 监听路由变化（包括时间戳）
watch(() => [route.query.supplierName, route.query._t], () => {
  applyRouteParams()
  loadData()
}, { immediate: false })

onMounted(() => {
  applyRouteParams()
  loadData()
})
</script>

<style scoped lang="scss">
.supplier-container {
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

  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
}
</style>


