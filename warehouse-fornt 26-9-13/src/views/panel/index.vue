<template>
  <div class="panel-container">
    <!-- 标题图片 -->
    <div class="title-section">
      <img src="/panel/banner.png" alt="标题" class="title-image" />
    </div>

    <!-- 四个可点击图片 -->
    <div class="images-section">
        <div class=" flex justify-between">
            <img src="/panel/yuntu.png" alt="图片4" class="nav-image" @click="navigateTo('/house')" />
            <img src="/panel/shuliang.png" alt="图片3" class="nav-image" @click="navigateTo('/outter')"  />
        </div>
        <div class=" flex justify-between mt-3">
            <img src="/panel/neihuanliu.png" alt="图片2" class="nav-image" @click="navigateTo('/nhl')" />
            <img src="/panel/guangfu.png" alt="图片1" class="nav-image" @click="navigateTo('/gfkt')"  />
        </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.panel-container {
  min-height: 100vh;
  background: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.title-section {
  margin-bottom: 40px;
}

.title-image {
  max-width: 100%;
  width: 50vw;
  height: auto;
}

.images-section {
  width: 66vw;
}

.nav-image {
  cursor: pointer;
  width: 22vw;
  height: auto;
  transition: transform 0.3s ease;
}

.nav-image:hover {
  transform: scale(1.05);
}
</style>
