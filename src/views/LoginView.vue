<template>
    <div id="login">

        <div class="container">
            <div class="c1">
                <component :is="authList[currentAuth]"></component>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
// 66 133 244
import { ref } from 'vue'
import UserNamePasswordAuth from '@/components/Login/UserNamePasswordAuth.vue';
import MailAuth from '@/components/Login/MailAuth.vue';
const authList = [UserNamePasswordAuth, MailAuth]
const currentAuth = ref(0)
</script>
<style scoped>
.container {

width: 350px;
height:350px;
position: absolute;
left: 50%;
top: 50%;
transform: translate(-50%,-50%);
/* background-color: rgba(75, 81, 95, 0.7);
box-shadow: 7px 7px 17px rgba(52, 56, 66, 0.5); */
background-color: rgba(204, 206, 210, 1);
box-shadow: 7px 7px 17px rgba(52, 56, 66, 0.5);
border-radius: 5px;
display: flex;
}
.bottombtns {
    width: 400px;
    margin: 0 auto;
}
.c1{

width:300px;
position: absolute;
left: 50%;
top: 50%;
transform: translate(-50%,-50%);
display: flex;
}
#login{
    background:url("../assets/image/login_bg1.jpg");
    width:100%;
    height:100%;
    position:fixed;
    background-size:100% 100%;
    display:flex;
    justify-content: center;
    align-items: center;
}
</style>
