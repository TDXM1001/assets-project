<template>
  <div class="real-estate-map">
    <ElRow :gutter="20">
      <ElCol :span="12">
        <ElFormItem label="经度 (Longitude)">
          <ElInputNumber
            v-model="internalLng"
            :precision="7"
            :step="0.000001"
            class="w-full"
            placeholder="请输入经度"
            @change="handleCoordChange"
          />
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem label="纬度 (Latitude)">
          <ElInputNumber
            v-model="internalLat"
            :precision="7"
            :step="0.000001"
            class="w-full"
            placeholder="请输入纬度"
            @change="handleCoordChange"
          />
        </ElFormItem>
      </ElCol>
    </ElRow>

    <!-- 地图占位区域，后续集成高德/百度地图 API -->
    <div class="real-estate-map__canvas">
      <div v-if="!hasCoords" class="real-estate-map__placeholder">
        <ElIcon :size="40"><Location /></ElIcon>
        <p>可在地图中拾取坐标 (待集成地图 SDK)</p>
      </div>
      <div v-else class="real-estate-map__marker-info">
        <ElTag type="success">已标记坐标: {{ internalLng }}, {{ internalLat }}</ElTag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, watch, computed } from 'vue'
  import { Location } from '@element-plus/icons-vue'

  const props = defineProps<{
    longitude?: number | string
    latitude?: number | string
  }>()

  const emit = defineEmits(['update:longitude', 'update:latitude'])

  const internalLng = ref<number | undefined>(
    props.longitude ? Number(props.longitude) : undefined
  )
  const internalLat = ref<number | undefined>(
    props.latitude ? Number(props.latitude) : undefined
  )

  const hasCoords = computed(() => internalLng.value !== undefined && internalLat.value !== undefined)

  watch(
    () => [props.longitude, props.latitude],
    ([newLng, newLat]) => {
      internalLng.value = newLng ? Number(newLng) : undefined
      internalLat.value = newLat ? Number(newLat) : undefined
    }
  )

  const handleCoordChange = () => {
    emit('update:longitude', internalLng.value)
    emit('update:latitude', internalLat.value)
  }
</script>

<style scoped>
  .real-estate-map__canvas {
    height: 200px;
    background-color: #f5f7fa;
    border: 1px dashed #dcdfe6;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 8px;
    color: #909399;
    flex-direction: column;
  }

  .real-estate-map__placeholder p {
    margin-top: 10px;
    font-size: 13px;
  }

  .real-estate-map__marker-info {
    text-align: center;
  }
</style>
