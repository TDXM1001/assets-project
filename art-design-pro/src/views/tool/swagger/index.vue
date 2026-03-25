<template>
  <div class="swagger-page art-full-height">
    <ElAlert
      class="mb-4"
      type="success"
      :closable="false"
      show-icon
      title="本页通过 iframe 桥接后端 Swagger UI，优先解决动态菜单可访问和接口文档可查看。"
    />

    <ElCard shadow="never" class="swagger-toolbar mb-4">
      <div class="toolbar-content">
        <div>
          <div class="toolbar-title">接口文档地址</div>
          <div class="toolbar-url">{{ swaggerUrl }}</div>
        </div>
        <div class="toolbar-actions">
          <ElButton type="primary" @click="reloadSwagger">刷新文档</ElButton>
          <ElButton @click="openSwagger">新窗口打开</ElButton>
        </div>
      </div>
    </ElCard>

    <div class="swagger-frame" v-loading="loading">
      <iframe
        :key="frameKey"
        :src="swaggerUrl"
        frameborder="0"
        width="100%"
        height="100%"
        scrolling="auto"
        @load="loading = false"
      ></iframe>
    </div>
  </div>
</template>

<script setup lang="ts">
  defineOptions({ name: 'ToolSwagger' })

  const loading = ref(true)
  const frameKey = ref(1)
  const baseUrl = import.meta.env.VITE_API_URL || '/api'
  const swaggerUrl = `${baseUrl}/swagger-ui.html`

  function reloadSwagger() {
    loading.value = true
    frameKey.value += 1
  }

  function openSwagger() {
    window.open(swaggerUrl, '_blank', 'noopener,noreferrer')
  }

  onMounted(() => {
    setTimeout(() => {
      loading.value = false
    }, 10000)
  })
</script>

<style scoped lang="scss">
  .swagger-page {
    height: 100%;
    padding: 16px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }

  .swagger-toolbar {
    flex-shrink: 0;
  }

  .toolbar-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  }

  .toolbar-title {
    font-size: 14px;
    color: var(--art-text-gray-600);
  }

  .toolbar-url {
    margin-top: 6px;
    font-size: 14px;
    color: var(--art-text-gray-900);
    word-break: break-all;
  }

  .toolbar-actions {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }

  .swagger-frame {
    flex: 1;
    overflow: hidden;
    background-color: var(--art-bg-color);
    border-radius: 8px;
  }

  .mb-4 {
    margin-bottom: 16px;
  }
</style>
