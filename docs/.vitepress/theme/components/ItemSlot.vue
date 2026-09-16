<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { withBase } from 'vitepress'
import { TAG_MEMBERS, itemIcon, itemName } from '../uncrafteverything'

const props = withDefaults(
  defineProps<{ id?: string | null; count?: number; label?: boolean; size?: 'normal' | 'large' }>(),
  { id: null, count: 1, label: false, size: 'normal' },
)

const members = computed(() => (props.id ? TAG_MEMBERS[props.id] : undefined))

// Tag ingredients cycle through their members once a second; the first member is shown until the page hydrates.
const tick = ref(0)
let timer: ReturnType<typeof setInterval> | undefined
onMounted(() => {
  if (!members.value) return
  // Preloads every member so the icon doesn't blank out while the next one loads.
  for (const member of members.value) {
    const preload = itemIcon(member)
    if (preload && !preload.local) new Image().src = preload.src
  }
  timer = setInterval(() => tick.value++, 1000)
})
onUnmounted(() => clearInterval(timer))

const shown = computed(() => (members.value ? members.value[tick.value % members.value.length] : props.id))

const name = computed(() => (props.id ? itemName(props.id) : ''))
const title = computed(() => (members.value && shown.value ? `${name.value} (${itemName(shown.value)})` : name.value))
const icon = computed(() => (shown.value ? itemIcon(shown.value) : null))
const src = computed(() => (icon.value ? (icon.value.local ? withBase(icon.value.src) : icon.value.src) : null))

// Falls back to initials when an item has no icon or the hosted icon fails to load.
const failed = ref(false)
watch(src, () => (failed.value = false))
const initials = computed(() =>
  name.value
    .split(' ')
    .filter((word) => /^[A-Z]/.test(word))
    .slice(0, 2)
    .map((word) => word[0])
    .join(''),
)
</script>

<template>
  <span class="ue-item" :class="size">
    <span class="ue-slot" :title="title" :aria-label="title || 'Empty slot'" role="img">
      <img v-if="src && !failed" :src="src" alt="" loading="lazy" @error="failed = true" />
      <span v-else-if="id" class="ue-initials">{{ initials }}</span>
      <span v-if="members" class="ue-tag" aria-hidden="true">#</span>
      <span v-if="count > 1" class="ue-count">{{ count }}</span>
    </span>
    <span v-if="label && id" class="ue-label">{{ name }}</span>
  </span>
</template>

<style scoped>
.ue-item {
  --slot: 36px;
  --icon: 32px;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  vertical-align: middle;
}

.ue-item.large {
  --slot: 52px;
  --icon: 44px;
}

.ue-slot {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: var(--slot);
  height: var(--slot);
  flex: none;
  background: var(--ue-slot-bg);
  border: 2px solid;
  border-color: var(--ue-slot-dark) var(--ue-slot-light) var(--ue-slot-light) var(--ue-slot-dark);
}

.ue-slot:hover {
  background: var(--ue-slot-hover);
}

.ue-slot img {
  width: var(--icon);
  height: var(--icon);
  object-fit: contain;
}

.ue-initials,
.ue-count,
.ue-tag {
  font: 700 12px/1 var(--vp-font-family-mono);
  color: #fff;
  text-shadow: 1px 1px 0 #3f3f3f;
}

.large .ue-count {
  font-size: 15px;
}

.ue-count {
  position: absolute;
  right: 1px;
  bottom: -1px;
}

.ue-tag {
  position: absolute;
  left: 2px;
  top: 1px;
  font-size: 10px;
  color: #ffe16b;
}

.ue-label {
  font-weight: 600;
}
</style>
