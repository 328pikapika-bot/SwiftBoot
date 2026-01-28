<template>
  <div>
    <template v-for="(item, index) in options">
      <template v-if="values.includes(item.value)">
        <span
          v-if="(item.elTagType == 'default' || item.elTagType == '') && (item.elTagClass == '' || item.elTagClass == null)"
          :key="item.value"
          :index="index"
          :class="item.elTagClass"
        >{{ item.label + ' ' }}</span>
        <el-tag
          v-else
          :disable-transitions="true"
          :key="item.value + ''"
          :index="index"
          :type="item.elTagType === 'primary' ? '' : item.elTagType"
          :class="item.elTagClass"
        >{{ item.label + ' ' }}</el-tag>
      </template>
    </template>
    <template v-if="unmatch && showValue">
      {{ unmatchArray | handleArray }}
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = defineProps({
  // 数据
  options: {
    type: Array as any,
    default: null,
  },
  // 当前的值
  value: [Number, String, Array],
  // 当未找到匹配的数据时，显示value
  showValue: {
    type: Boolean,
    default: true,
  },
  separator: {
    type: String,
    default: ",",
  }
});

const values = computed(() => {
  if (props.value === null || typeof props.value === 'undefined' || props.value === '') return [];
  return Array.isArray(props.value) ? props.value.map(item => '' + item) : String(props.value).split(props.separator);
});

const unmatch = computed(() => {
  if (props.options?.length == 0 || props.value === null || typeof props.value === 'undefined' || props.value === '' || props.options === null || typeof props.options === 'undefined') return false;
  // 传入的值在options中找不到时，返回true
  return values.value.some(val => !props.options.some((opt: any) => opt.value == val));
});

const unmatchArray = computed(() => {
  if (props.options?.length == 0 || props.value === null || typeof props.value === 'undefined' || props.value === '' || props.options === null || typeof props.options === 'undefined') return [];
  // 传入的值在options中找不到时，返回找到的值
  return values.value.filter(val => !props.options.some((opt: any) => opt.value == val));
});

const handleArray = (array: any[]) => {
  if (array.length === 0) return "";
  return array.reduce((pre, cur) => {
    return pre + " " + cur;
  });
};
</script>

<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}
</style>
