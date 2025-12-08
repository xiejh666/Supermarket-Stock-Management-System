<template>
  <div class="category-container">
    <el-card class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>📂 分类管理</h2>
          <p class="subtitle">管理商品分类信息</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button v-if="canCreate('category')" type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增分类
        </el-button>
      </div>
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
        @size-change="loadData"
        @current-change="loadData"
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import categoryApi from '@/api/category'
import { canCreate, canUpdate, canDelete, checkPermission } from '@/utils/permission'

const categoryList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

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
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    categoryList.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
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
    if (form.value.id) {
      await categoryApi.update(form.value)
      ElMessage.success('更新成功')
    } else {
      await categoryApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
    type: 'warning'
  })
  try {
    await categoryApi.delete(row.id)
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
.category-container {
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

  .table-card {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
}
</style>


