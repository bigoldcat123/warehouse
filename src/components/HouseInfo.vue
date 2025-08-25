<template>
    <div class=" bg-[rgb(249,250,252)] flex flex-col gap-y-1 text-xl max-w-72 min-w-72 p-5 ">
        <div>{{ house.houseName }} -> {{ house.id }}</div>
        <div>{{ house.houseType}}</div>
        <div>{{ house.x}}*{{house.y}}*{{house.z}}</div>
        <div>通风类型:{{ house.tongfengLx }}</div>
        <div>通风状态: {{ house.tongfengZt }}</div>
        <div class=" grid grid-cols-2 gap-x-2 gap-y-2">
            <button class=" bg-[rgb(63,157,251)] p-1" @click="show_tongfeng">通风图</button>
            <button class=" bg-[rgb(63,157,251)] p-1" @click="show_yuntu">云图</button>
            <button class=" bg-[rgb(63,157,251)] p-1" @click="show_quxian">曲线</button>
            <button class=" bg-[rgb(63,157,251)] p-1" @click="show_3D">3D图</button>
            <button class=" bg-[rgb(63,157,251)] p-1" @click="show_shuishi">水势图</button>
        </div>
        <div class=" grid grid-cols-2 gap-x-2 gap-y-2">
            <el-popconfirm title="确认删除?" @confirm="handle_delete">
                <template #reference>
                    <button class=" bg-[rgb(63,157,251)] p-1">删除</button>
                </template>
            </el-popconfirm>
            <button @click="emit('update',house)" class=" bg-[rgb(63,157,251)] p-1">编辑</button>
        </div>
    </div>
</template>
<script setup lang="ts">
import { api_PreFix } from '@/api';
import type { type_House } from '@/api/house';
import { ref } from 'vue'
const { house } = defineProps<{
    house:type_House
}>()
export type UrlInfo= { name: string, urls: Array<string> }
// const emit = defineEmits<{
//     // <eventName>: <expected arguments>
//     delete: [id:number] // named tuple syntax
//     update:[house:type_House],
//     show_image:[data:Array<A>]
// }>()
const emit = defineEmits<{
    (event: 'delete',id:number): void,
    (event: 'update',house:type_House): void
    (event: 'show_image', data: Array<UrlInfo>): void

}>()
const handle_delete =() => {
    emit('delete',house.id!)    
}
const map_fn = (x: string) => {
    if (x.startsWith('/') || x.startsWith('\\')) {
        return '/' + api_PreFix + '/static/' + x.substring(1).replace('\\', '/')
    } else {
        return '/' + api_PreFix + '/static/' + x.replace('\\', '/')
    }
};
const show_tongfeng = () => {
    console.log(house);
    
    const tongfengZt = house.tongfengTu?.split(',').map(map_fn) ?? []
    const tongFengSst = house.tongFengSst?.split(',').map(map_fn) ?? []

    emit('show_image',[
        {
            name:'通风状态图',
            urls: tongfengZt
        },
        {
            name: '通风水势图',
            urls: tongFengSst
        }
    ])
}
const show_yuntu = () => {
    const yuntu = house.yuntu?.split(",").map(map_fn) ?? []
    emit('show_image',[
        {
            name:'云图',
            urls:yuntu
        }
    ])
}
const show_quxian =() => {
    const quxian = house.quxian?.split(",").map(map_fn) ?? []
    emit('show_image', [
        {
            name: '曲线图',
            urls: quxian
        }
    ])
}
const show_3D = () => {
    const threeD = house.threeD?.split(",").map(map_fn) ?? []
    emit('show_image', [
        {
            name: '3D图',
            urls: threeD
        }
    ])
}
const show_shuishi = () => {
    const shuishi = house.valeWin?.split(",").map(map_fn) ?? []
    const moxing = house.tfmodeWin?.split(",").map(map_fn) ?? []
    emit('show_image', [
        {
            name: '水势图',
            urls: shuishi
        },
        {
            name: '通风窗口模型图',
            urls: moxing
        }
    ])
}
</script>
<style scoped>
</style>