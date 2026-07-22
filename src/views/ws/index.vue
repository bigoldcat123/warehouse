<template>
  <div class="w-full h-screen min-w-[1000px] min-h-[800px] bg-[#2c3e50] overflow-auto">
      <!-- Header -->
      <div class="absolute z-50 top-[17px] text-[rgb(6,142,206)]"
          :style="{ left: 'max(500px, 50%)', transform: 'translateX(-50%)' }">
          {{ wareHouseName }}
      </div>
      <div class="absolute z-50 top-[50px] text-[rgb(6,142,206)]"
          :style="{ left: 'max(500px, 50%)', transform: 'translateX(-50%)' }">
          {{ houseName }}
      </div>
      
      <!-- Back Button -->
      <div @click="$router.back()" class="absolute opacity-0 z-50 top-[25px] text-[rgb(6,142,206)] size-[20px] cursor-pointer"
          :style="{ left: 'max(968px, 97.3%)', transform: 'translateX(-50%)' }">
          <svg viewBox="0 0 24 24" class="w-[24px] h-[24px] stroke-current stroke-[2.2] fill-none">
              <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
      </div>

      <div class="relative w-full h-full min-w-[1000px] min-h-[800px]">
          <!-- Corners -->
          <div class="absolute top-0 left-0 w-[500px] h-[118px] z-20 overflow-hidden">
              <img src="/show_pic/left_top.png" alt="左上角图片" class="w-full h-full object-cover" />
          </div>

          <div class="absolute top-0 right-0 w-[500px] h-[118px] z-20 overflow-hidden">
              <img src="/show_pic/right_top.png" alt="右上角图片" class="w-full h-full object-cover" />
          </div>

          <div class="absolute bottom-0 left-0 w-[500px] h-[62px] z-20 overflow-hidden">
              <img src="/show_pic/left_bot.png" alt="左下角图片" class="w-full h-full object-cover" />
          </div>

          <div class="absolute bottom-0 right-0 w-[500px] h-[62px] z-20 overflow-hidden">
              <img src="/show_pic/right_bot.png" alt="右下角图片" class="w-full h-full object-cover" />
          </div>

          <!-- Top / Bottom panels -->
          <div class="absolute top-0 left-[500px] right-[500px] h-[118px] z-10 border-0" :style="{
              backgroundImage: `url('/show_pic/center_top_bg.png')`,
              backgroundRepeat: 'repeat-x'
          }" />

          <div class="absolute bottom-0 left-[500px] right-[500px] h-[62px] z-10 border-0" :style="{
              backgroundImage: `url('/show_pic/center_bot_bg.png')`,
              backgroundRepeat: 'repeat-x'
          }" />

          <!-- Side panels -->
          <div class="absolute top-[118px] bottom-[62px] left-0 w-[167px] z-10" :style="{
              backgroundImage: `url('/show_pic/left_center.png')`,
              backgroundRepeat: 'repeat-y'
          }" />

          <div class="absolute top-[118px] bottom-[62px] right-0 w-[167px] z-10" :style="{
              backgroundImage: `url('/show_pic/right_center.png')`,
              backgroundRepeat: 'repeat-y'
          }" />

          <!-- Center content area: Gauges -->
          <div
              class="absolute top-[118px] bottom-[62px] left-[167px] right-[167px] z-30 overflow-auto flex flex-col bg-[#011437] backdrop-blur">
              
              <!-- Gauge Container -->
              <div class="w-full max-w-[1400px] mx-auto my-4 px-4 flex-1 min-h-0 flex flex-col items-center justify-center">
                  
                  <!-- Single Row: Three Gauges -->
                  <div class="flex flex-wrap justify-center items-center gap-6 w-full">
                      <Gauge 
                          unit="kWh" 
                          name="空调每小时用电量" 
                          :value="data?.airHourElec ?? 0" 
                          :max="50"
                          class="gauge-item flex-1 min-w-[180px] max-w-[320px]"
                      />
                      <Gauge 
                          unit="kWh" 
                          name="小时耗电量" 
                          :value="data?.meterElec ?? 0" 
                          :max="100"
                          class="gauge-item flex-1 min-w-[180px] max-w-[320px]"
                      />
                      <Gauge 
                          unit="kWh" 
                          name="光伏每小时发电量" 
                          :value="data?.pvElec ?? 0" 
                          :max="500"
                          class="gauge-item flex-1 min-w-[180px] max-w-[320px]"
                      />
                  </div>

                  <!-- Data Update Status -->
                  <!-- <div class="flex-shrink-0 w-full max-w-[1200px] mx-auto px-4 pb-4 pt-6 flex items-center justify-between gap-4 flex-wrap">
                      <div class="flex gap-2.5 items-center">
                          <span class="badge">实时更新</span>
                          <span class="badge text-xs opacity-70">刷新间隔: 1s</span>
                      </div>
                      <div class="flex gap-2.5 items-center">
                          <span class="badge text-xs opacity-70">
                              {{ currentTime }}
                          </span>
                      </div>
                  </div> -->
              </div>
          </div>
      </div>
  </div>
</template>

<script setup lang="ts">
import Gauge from '@/components/charts/Gauge.vue'
import { useCurrentUserStore } from '@/stores/currentUser'
import { useCurrentWareHouse } from '@/stores/currentWareHouse'
import { onMounted, onUnmounted, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import gf_kt, { type GfktItem } from '@/api/gf_kt'

const ware_house = useCurrentWareHouse()
const store = useCurrentUserStore()
const route = useRoute()

const house_no = route.query.houseno as string
const wareHouseName = computed(() => ware_house.getWareHouse().wareHouseNO)
const houseName = computed(() => route.query.houseName as string || '光伏信息')

const data = ref<GfktItem | null>(null)

const fetch_data = async () => {
  try {
      const res = (await gf_kt.getOneByHouseNo(house_no)).data.value
      data.value = res
  } catch (error) {
      console.error('获取数据失败:', error)
  }
}

const interval = ref<number | null>(null)
const currentTime = ref('')

// Update time every second
const updateTime = () => {
  currentTime.value = new Date().toLocaleTimeString()
}

onMounted(() => {
  fetch_data()
  interval.value = setInterval(() => {
      fetch_data()
      updateTime()
  }, 1000) as unknown as number
  updateTime()
})

onUnmounted(() => {
  if (interval.value) {
      clearInterval(interval.value)
  }
})
</script>

<style scoped>
/* Gauge item styling to match the theme */
.gauge-item {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(4px);
  border: 1px solid rgba(6, 142, 206, 0.15);
  border-radius: 16px;
  padding: 16px 20px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gauge-item:hover {
  border-color: rgba(6, 142, 206, 0.4);
  box-shadow: 0 8px 30px rgba(6, 142, 206, 0.1);
  transform: translateY(-2px);
}

/* Badge styling */
.badge {
  font-size: 0.85rem;
  background: rgba(6, 142, 206, 0.15);
  padding: 6px 14px;
  border-radius: 40px;
  color: rgb(6, 142, 206);
  letter-spacing: 0.3px;
  border: 1px solid rgba(6, 142, 206, 0.3);
  font-variant-numeric: tabular-nums;
}

/* Scrollbar styling for the content area */
.absolute.top-\[118px\].bottom-\[62px\].left-\[167px\].right-\[167px\]::-webkit-scrollbar {
  width: 6px;
}

.absolute.top-\[118px\].bottom-\[62px\].left-\[167px\].right-\[167px\]::-webkit-scrollbar-track {
  background: rgba(6, 142, 206, 0.05);
  border-radius: 3px;
}

.absolute.top-\[118px\].bottom-\[62px\].left-\[167px\].right-\[167px\]::-webkit-scrollbar-thumb {
  background: rgba(6, 142, 206, 0.3);
  border-radius: 3px;
}

.absolute.top-\[118px\].bottom-\[62px\].left-\[167px\].right-\[167px\]::-webkit-scrollbar-thumb:hover {
  background: rgba(6, 142, 206, 0.5);
}

/* Responsive adjustments */
@media (max-width: 1200px) {
  .gauge-item {
      min-width: 150px;
      max-width: 280px;
  }
}

@media (max-width: 768px) {
  .gauge-item {
      min-width: 140px;
      max-width: 100%;
      flex: 1 1 100%;
  }
}
</style>