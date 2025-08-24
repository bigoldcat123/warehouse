<script setup lang="ts">
import { ref } from 'vue'
import { useCurrentUserStore } from '@/stores/currentUser';
import { useRouter } from 'vue-router';
import auth from '@/api/auth';
import server from '@/api';
import {
    Menu as IconMenu,
    Location,
    Setting,
    Avatar,
    Bell,
    Box,
    Flag,
    EditPen,
    CloseBold
} from '@element-plus/icons-vue'
import ChangePasswd from './ChangePasswd.vue';
import { ElTag } from 'element-plus';
import warehouse from '@/api/warehouse';
import { useCurrentWareHouse } from '@/stores/currentWareHouse';
const router = useRouter()
const currentWareHouse = useCurrentWareHouse()
const currentUser = useCurrentUserStore()
const kv = ref<any[]>([{
    value: '分公司',
    key: -1
}])
warehouse.kv().then(res => {
    kv.value.push(...res.data.value)
})
function logout() {
    auth.logout().then(res => {
        currentUser.logout()
        currentWareHouse.clear()
        router.push({ path: '/login' })
    })
}
const v = ref(false)
</script>

<template>
    <div class=" flex w-full" style="overflow-x:auto;">
        <div class="flex flex-col h-screen" style="width:180px;background-color: #ddd;">
            <div style="font-size:30px;border:1px solid #ddd;height:100px;vertical-align: middle;text-align: center;">
                <img src="../assets/image/title_1.png"></div>
            <el-menu class=" flex-1 el-menu-vertical-demo" :default-active="currentUser.isMainComp()?'/alarmarg':'/alarm'" :router="true">

                <el-menu-item v-if="currentUser.isMainComp()"  index="/alarmarg">
                    <el-icon>
                        <Flag />
                    </el-icon>
                    <span>报警统计</span>
                </el-menu-item>
                <el-menu-item  index="/alarm">
                    <el-icon>
                        <Bell />
                    </el-icon>
                    <span>报警信息</span>
                </el-menu-item>

                <el-menu-item index="/house">
                    <el-icon>
                        <Location />
                    </el-icon>
                    <span>仓房管理</span>
                </el-menu-item>

                <el-menu-item  index="/data">
                    <el-icon>
                        <IconMenu />
                    </el-icon>
                    <span>数据查看</span>
                </el-menu-item>
                <el-menu-item  index="/entry">
                    <el-icon>
                        <setting />
                    </el-icon>
                    <span>仓房入库管理</span>
                </el-menu-item>
                <el-menu-item v-if="currentUser.getUserDetail()?.username == 'admin'"
                    index="/warehouse">
                    <el-icon>
                        <Box />
                    </el-icon>
                    <span>仓库管理</span>
                </el-menu-item>


                <el-menu-item v-if="currentUser.getUserDetail()?.username == 'admin'"
                    index="/user">
                    <el-icon>
                        <Avatar />
                    </el-icon>
                    <span>用户管理</span>
                </el-menu-item>


            </el-menu>
        </div>
        <div class=" flex-1 flex flex-col items-end">
            <div class="p-8" style="height:100px;border:1px;width:100%;position: relative;background-color:  rgb(66,133,244);">
                <div style="height:75px;width:600px;position: absolute;left: 50px;top:10px;"><img src="/banner2.png">
                </div>
                <div style="height:75px;border:1px;width:350px;position: absolute;right: 0px;">
                    <ElTag type="success">{{
                        kv.filter(x => x.key == currentUser.getUserDetail()!.companyID)[0]?.value
                        }}</ElTag>
                    <ElTag>{{ currentUser.getUserDetail()?.username }}</ElTag><span
                        style="font-size:small;">您好！&emsp;</span>

                    <el-button type="primary" size="small" @click="v = true"><el-icon><EditPen/></el-icon> 修改密码</el-button>
                    <el-button type="danger" size="small" @click="logout"><el-icon><CloseBold/></el-icon> 退出</el-button>
                </div>
            </div>
            <div>&nbsp;</div>
            <div class="px-8 w-full">
                <RouterView />
            </div>
        </div>
    </div>
    <ChangePasswd :dialog-visible="v" @close="v = false"></ChangePasswd>
</template>
<!-- <style>
.p-8{
    width:300px;
position: absolute;
left: 50%;
top: 50%;
transform: translate(-50%,-50%);
display: flex;

}
</style> -->
