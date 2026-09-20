<template>
    <div class="w-full h-screen min-w-[1000px] min-h-[800px] bg-[#2c3e50] overflow-auto">
        <!-- 顶部标题 -->
        <div v-if="$slots.title" class="absolute z-50 top-[17px] text-[rgb(6,142,206)]"
            :style="{ left: 'max(500px, 50%)', transform: 'translateX(-50%)' }">
            <slot name="title" />
        </div>
        <div v-if="$slots.subtitle" class="absolute z-50 top-[50px] text-[rgb(6,142,206)]"
            :style="{ left: 'max(500px, 50%)', transform: 'translateX(-50%)' }">
            <slot name="subtitle" />
        </div>
        <!-- 右上角操作(返回等) -->
        <div v-if="$slots.actions" class="absolute z-50 top-[25px] text-[rgb(6,142,206)]"
            :style="{ left: 'max(968px, 97.3%)', transform: 'translateX(-50%)' }">
            <slot name="actions" />
        </div>

        <div class="relative w-full h-full min-w-[1000px] min-h-[800px]">
            <!-- 四角 -->
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

            <!-- 上/下中段背景(repeat-x) -->
            <div class="absolute top-0 left-[500px] right-[500px] h-[118px] z-10 border-0" :style="{
                backgroundImage: `url('/show_pic/center_top_bg.png')`,
                backgroundRepeat: 'repeat-x'
            }" />
            <div class="absolute bottom-0 left-[500px] right-[500px] h-[62px] z-10 border-0" :style="{
                backgroundImage: `url('/show_pic/center_bot_bg.png')`,
                backgroundRepeat: 'repeat-x'
            }" />

            <!-- 左/右中段背景(repeat-y) -->
            <div class="absolute top-[118px] bottom-[62px] left-0 w-[167px] z-10" :style="{
                backgroundImage: `url('/show_pic/left_center.png')`,
                backgroundRepeat: 'repeat-y'
            }" />
            <div class="absolute top-[118px] bottom-[62px] right-0 w-[167px] z-10" :style="{
                backgroundImage: `url('/show_pic/right_center.png')`,
                backgroundRepeat: 'repeat-y'
            }" />

            <!-- 中央内容区 -->
            <div class="absolute top-[118px] bottom-[62px] left-[167px] right-[167px] z-30">
                <slot />
            </div>
        </div>
    </div>
</template>
