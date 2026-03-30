<template>
  <div class="real-estate-map-container mb-4">
    <div class="map-header mb-2 flex justify-between items-center">
      <div class="map-title flex items-center">
        <ElIcon class="mr-1"><Location /></ElIcon>
        <span>地理位置精确定位</span>
      </div>
      <div class="coordinate-inputs">
        <ElInputNumber
          v-model="internalLng"
          :precision="6"
          :step="0.000001"
          placeholder="经度"
          class="mr-2"
          @change="emitUpdate"
        />
        <ElInputNumber
          v-model="internalLat"
          :precision="6"
          :step="0.000001"
          placeholder="纬度"
          @change="emitUpdate"
        />
      </div>
    </div>

    <!-- 地图占位区域 -->
    <div class="map-canvas-wrapper relative">
      <div
        class="map-canvas bg-blue-50 rounded flex flex-col items-center justify-center p-8 border border-blue-100 min-h-[240px]"
      >
        <template v-if="hasCoordinates">
          <div class="coordinate-preview text-center">
            <ElIcon :size="48" color="#1890ff">
              <MapLocation />
            </ElIcon>
            <div class="text-blue-600 font-bold mt-2">坐标已准确定位</div>
            <div class="text-gray-500 text-sm">
              已记录点位：{{ internalLng }}, {{ internalLat }}
            </div>
            <p class="text-xs text-gray-400 mt-2 italic">
              * 集成生产级地图后，此处将渲染高德/百度/谷歌地图引擎
            </p>
          </div>
        </template>
        <template v-else>
          <ElIcon :size="48" color="#909399">
            <LocationFilled />
          </ElIcon>
          <div class="text-gray-400 mt-2 text-sm italic">等待坐标输入或从地图选点...</div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, watch, computed } from 'vue'
  import { Location, MapLocation, LocationFilled } from '@element-plus/icons-vue'

  defineOptions({ name: 'RealEstateMap' })

  const props = defineProps<{
    longitude?: number | string
    latitude?: number | string
  }>()

  const emit = defineEmits(['update:longitude', 'update:latitude'])

  const internalLng = ref<number | undefined>(props.longitude ? Number(props.longitude) : undefined)
  const internalLat = ref<number | undefined>(props.latitude ? Number(props.latitude) : undefined)

  const hasCoordinates = computed(() => {
    return internalLng.value !== undefined && internalLat.value !== undefined
  })

  watch(
    () => props.longitude,
    (val) => {
      internalLng.value = val ? Number(val) : undefined
    }
  )

  watch(
    () => props.latitude,
    (val) => {
      internalLat.value = val ? Number(val) : undefined
    }
  )

  const emitUpdate = () => {
    emit('update:longitude', internalLng.value)
    emit('update:latitude', internalLat.value)
  }
</script>

<style scoped>
  .map-canvas {
    transition: all 0.3s ease;
  }
  .map-canvas:hover {
    box-shadow: inset 0 0 10px rgba(24, 144, 255, 0.1);
  }
</style>
