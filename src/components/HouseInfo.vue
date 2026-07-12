<template>
    <div class=" bg-[rgb(249,250,252)] flex flex-col gap-y-1 text-[15px] w-[253px] p-5 ">
        <div>{{ house.houseNo }} 号仓房（ {{ house.houseName }} ）</div>
        <div>种类:{{ house.breed }}</div>
        <div>水分: {{ house.water }}</div>
        <div>入库时间: {{ house.entryTime }}</div>
        <div class=" grid grid-cols-2 gap-x-2 gap-y-2">
            <button v-if="isWind" class=" bg-[rgb(63,157,251)] p-1" @click="show_tongfeng_water">通风水势图</button>
            <button style="display:none" v-if="isWind"  class=" bg-[rgb(63,157,251)] p-1" @click="show_tongfeng_window">通风窗口图</button>

            <button v-if="!isWind" class=" bg-[rgb(63,157,251)] p-1" @click="show_yuntu">云图</button>
            <button v-if="!isWind" class=" bg-[rgb(63,157,251)] p-1" @click="show_quxian">曲线</button>
            <button v-if="!isWind" class=" bg-[rgb(63,157,251)] p-1" @click="show_3D">3D图</button>
            <!-- //TODO -->
            <button v-if="!isWind" class=" bg-[rgb(63,157,251)] p-1" @click="show_shuishi">水势图</button>
            <button style="display:none" v-if="!isWind"  class=" bg-[rgb(63,157,251)] p-1" @click="show_window">窗口图</button>
            <button v-if="!isWind && house.has_kt" @click="router.push('/wsss?houseno=' + house.houseNo)" class=" bg-[rgb(63,157,251)] p-1">光伏</button>

            <button v-if="!isWind" @click="emit('update', house)" class=" bg-[rgb(63,157,251)] p-1">编辑</button>

        </div>
        <div class=" grid grid-cols-2 gap-x-2 gap-y-2">
            <!-- <el-popconfirm title="确认删除?" @confirm="handle_delete">
                <template #reference>
                    <button class=" bg-[rgb(63,157,251)] p-1">删除</button>
                </template>
</el-popconfirm> -->
        </div>
    </div>
</template>
<script setup lang="ts">
import { api_PreFix } from '@/api';
import { useRouter } from 'vue-router';
import type { type_House } from '@/api/house';
const { house, isWind } = defineProps<{
    house: type_House
    isWind?: boolean
}>()
const router = useRouter()

export type UrlInfo = { name: string, urls: Array<string>, houseName?: string,is_yuntu_model?:boolean }
// const emit = defineEmits<{
//     // <eventName>: <expected arguments>
//     delete: [id:number] // named tuple syntax
//     update:[house:type_House],
//     show_image:[data:Array<A>]
// }>()
const emit = defineEmits<{
    (event: 'delete', id: number): void,
    (event: 'update', house: type_House): void
    (event: 'show_image', data: Array<UrlInfo>): void

}>()
const handle_delete = () => {
    emit('delete', house.id!)
}
const map_fn = (x: string) => {
    if (x.startsWith('/') || x.startsWith('\\')) {
        return '/' + api_PreFix + '/static/' + x.substring(1).replace('\\', '/')
    } else {
        return '/' + api_PreFix + '/static/' + x.replace('\\', '/')
    }
};
const show_tongfeng_window = () => {

    const tongfengZt = house.tongfengTu?.split(',').map(map_fn) ?? []

    router.push({
        path: '/show',
        query: {
            name: '通风窗口图',
            urls: tongfengZt,
            houseName: house.houseName
        }
    })
}
const show_tongfeng_water = () => {

    const tongFengSst = house.tongFengSst?.split(',').map(map_fn) ?? []


    router.push({
        path: '/show',
        query: {
            name: '通风水势图',
            urls: tongFengSst,
            houseName: house.houseName
        }
    })
}
const show_yuntu = () => {
    const yuntu = house.yuntu?.split(",").map(map_fn) ?? []

    router.push({
        path: '/show',
        query: {
            name: '云图',
            urls: yuntu,
            houseName: house.houseName
        }
    })
}
const show_quxian = () => {
    const quxian = house.quxian?.split(",").map(map_fn) ?? []

    router.push({
        path: '/show',
        query: {
            name: '曲线图',
            urls: quxian,
            houseName: house.houseName
        }
    })
}
const show_3D = () => {
    const threeD = house.threeD?.split(",").map(map_fn) ?? []

    router.push({
        path: '/show',
        query: {
            name: '3D图',
            urls: threeD,
            houseName: house.houseName
        }
    })
}
const show_shuishi = () => {
    const shuishi = house.tfModSst?.split(",").map(map_fn) ?? []

    router.push({
        path: '/show',
        query: {
            name: '水势图',
            urls: shuishi,
            houseName: house.houseName
        }
    })
}
const show_window = () => {
  const shuishi = house.tfmodeWin?.split(",").map(map_fn) ?? []

  router.push({
      path: '/show',
      query: {
          name: '窗口图',
          urls: shuishi,
          houseName: house.houseName
      }
  })
}
</script>
<style scoped></style>
