<template>
  <div class="tool-page art-full-height">
    <ElAlert
      class="mb-4"
      type="warning"
      :closable="false"
      show-icon
      title="当前项目仍未接入正式的表单构建器，本页先作为稳定入口和能力说明页。"
    />

    <ElRow :gutter="16" class="mb-4">
      <ElCol :xs="24" :lg="12">
        <ElCard shadow="never" class="tool-card">
          <template #header>
            <span>当前状态</span>
          </template>

          <ElDescriptions :column="1" border>
            <ElDescriptionsItem label="接入状态">未接入第三方表单设计器</ElDescriptionsItem>
            <ElDescriptionsItem label="为什么先保留菜单">
              若依基础菜单里已经包含“表单构建”，先保留可访问入口，避免动态路由出现组件缺失报错。
            </ElDescriptionsItem>
            <ElDescriptionsItem label="当前建议">
              如果你现在要推进资产模块，优先使用业务单据、盘点任务和代码生成管理页，而不是继续扩表单构建。
            </ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>
      </ElCol>

      <ElCol :xs="24" :lg="12">
        <ElCard shadow="never" class="tool-card">
          <template #header>
            <span>推荐替代入口</span>
          </template>

          <div class="action-list">
            <ElButton type="primary" @click="router.push('/tool/gen')">前往代码生成</ElButton>
            <ElButton @click="router.push('/tool/swagger')">查看系统接口</ElButton>
            <ElButton @click="router.push('/asset/order')">回到业务单据</ElButton>
          </div>

          <ElTimeline class="mt-4">
            <ElTimelineItem timestamp="现在" type="warning">
              本页承担的是“占位且可访问”的职责，不再让菜单点击后直接报错。
            </ElTimelineItem>
            <ElTimelineItem timestamp="下一阶段" type="primary">
              如果团队决定引入表单设计器，再补完整的设计、发布、渲染链路。
            </ElTimelineItem>
          </ElTimeline>
        </ElCard>
      </ElCol>
    </ElRow>

    <ElCard shadow="never" class="tool-card">
      <ElEmpty description="表单构建功能暂未启用" />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router'

  defineOptions({ name: 'ToolBuild' })

  const router = useRouter()
</script>

<style scoped lang="scss">
  .tool-page {
    padding: 16px;
    overflow: auto;
  }

  .tool-card {
    height: 100%;
  }

  .action-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }

  .mb-4 {
    margin-bottom: 16px;
  }

  .mt-4 {
    margin-top: 16px;
  }
</style>
