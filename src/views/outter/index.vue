<template>
    <div class="depot-system">
      <div class="button-group">
        <button @click="$router.replace('/panel')">返回主页</button>
        <button @click="navigate(5)">库区总览</button>
        <button @click="navigate(1)">仓房检测</button>
        <button @click="navigate(2)">检测记录</button>
        <button @click="navigate(6)">预警记录</button>
        <button @click="navigate(3)">空仓标定</button>
        <button @click="navigate(4)">仓门预警</button>
      </div>
      <iframe
        ref="mainIframe"
        name="mainIframe"
        :src="iframeSrc"
        frameborder="0"
        class="main-iframe"
      ></iframe>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted } from 'vue';
  
  interface User {
    username: string;
    password: string;
  }
  
  interface LoginResponse {
    data: {
      token: string;
    };
  }
  
  const myLoginUrl = 'http://zzgdla.com:12345/';
  const depotId = 3;
  
  const user: User = {
    username: 'admin',
    password: 'admin@123',
  };
  
  const thirdToken = ref<string>('');
  const iframeSrc = ref<string>('');
  const mainIframe = ref<HTMLIFrameElement | null>(null);
  
  // 登录获取token
  const login = async (): Promise<void> => {
    try {
      const response = await fetch(`${myLoginUrl}login/v1/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(user),
      });
  
      if (response.ok) {
        const res: LoginResponse = await response.json();
        console.log(res);
        thirdToken.value = res.data.token;
        console.log('Token:', thirdToken.value);
        
        // 登录成功后加载默认页面（总览）
        navigate(5);
      } else {
        console.error('登录失败:', response.statusText);
      }
    } catch (error) {
      console.error('登录请求错误:', error);
    }
  };
  
  // 导航到不同页面
  const navigate = (index: number): void => {
    if (!thirdToken.value) {
      console.warn('Token未获取,无法导航');
      return;
    }
  
    const baseUrl = `${myLoginUrl}graam/newqian/index.html?depotid=${depotId}&thirdToken=${thirdToken.value}`;
    const overviewUrl = `${myLoginUrl}graam/zonglan/index.html?depotid=${depotId}&thirdToken=${thirdToken.value}`;
  
    const routes: Record<number, string> = {
      1: `${baseUrl}#/kz`,
      2: `${baseUrl}#/lsjl`,
      3: `${baseUrl}#/kcbd`,
      4: `${baseUrl}#/cmyj`,
      6: `${baseUrl}#/measure_yujing`,
      5: overviewUrl,
    };
  
    iframeSrc.value = routes[index] || '';
  };
  
  // 组件挂载时执行登录
  onMounted(() => {
    login();
  });
  </script>
  
  <style scoped>
  .depot-system {
    width: 100vw;
    height: 100vh;
    display: flex;
    flex-direction: column;
  }
  
  .button-group {
    padding: 10px;
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
  
  .button-group button {
    padding: 8px 16px;
    cursor: pointer;
    border: 1px solid #ddd;
    background-color: #f0f0f0;
    border-radius: 4px;
    transition: background-color 0.3s;
  }
  
  .button-group button:hover {
    background-color: #e0e0e0;
  }
  
  .button-group button:active {
    background-color: #d0d0d0;
  }
  
  .main-iframe {
    width: 95vw;
    height: 90vh;
    padding: 0;
    border: 1px solid red;
    flex: 1;
  }
  </style>