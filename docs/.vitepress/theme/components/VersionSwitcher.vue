<script setup lang="ts">
import { computed, ref } from 'vue'
import { useData, useRouter, withBase } from 'vitepress'

const router = useRouter()
const { page } = useData()

/** Pages the older versions have; they predate the Auto Uncrafting Table and the client config. */
const LEGACY_PAGES = ['getting-started', 'uncrafting-table', 'common-config', 'per-item-exp-config', 'ftb-quest-progression-config']

/** Mod versions with their own folder of pages. The latest version lives at the site root and has every page. */
const versions = [
  { label: 'Latest (v2.0.0+)', short: 'Latest', dir: '', pages: null },
  { label: 'v1.8.2+', short: 'v1.8.2+', dir: 'v1.8.2/', pages: LEGACY_PAGES },
  { label: 'v1.x.x', short: 'v1.x.x', dir: 'v1.x.x/', pages: LEGACY_PAGES },
]

const current = computed(
  () => versions.find((v) => v.dir !== '' && page.value.relativePath.startsWith(v.dir)) ?? versions[0],
)

const open = ref(false)

function onFocusOut(event: FocusEvent) {
  if (!(event.currentTarget as HTMLElement).contains(event.relatedTarget as Node | null)) open.value = false
}

/** Opens the same page in the chosen version, or its getting started page when that version has no such page. */
function select(version: (typeof versions)[number]) {
  open.value = false
  if (version === current.value) return
  const pageName = page.value.relativePath.slice(current.value.dir.length).replace(/(index)?\.md$/, '')
  const exists = pageName !== '' && (version.pages === null || version.pages.includes(pageName))
  router.go(withBase(`/${version.dir}${exists ? pageName : 'getting-started'}`))
}
</script>

<template>
  <!-- Opens on hover for mice and on click for touch and keyboard, like VitePress's own nav menus. -->
  <div class="version-switcher" :class="{ open }" @focusout="onFocusOut">
    <button class="button" type="button" :aria-expanded="open" aria-haspopup="true" @click="open = !open">
      <span class="text">{{ current.short }}</span>
      <svg class="icon" width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
    </button>

    <div class="menu">
      <button
        v-for="v in versions"
        :key="v.dir"
        type="button"
        class="item"
        :class="{ active: v === current }"
        @click="select(v)"
      >
        {{ v.label }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.version-switcher {
  position: relative;
  display: flex;
  align-items: center;
}

.button {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 12px;
  height: var(--vp-nav-height);
  font-size: 14px;
  font-weight: 500;
  color: var(--vp-c-text-1);
  transition: color 0.25s;
}

.button:hover {
  color: var(--vp-c-brand-1);
}

.icon {
  transition: transform 0.25s;
}

.open .icon,
.version-switcher:hover .icon {
  transform: rotate(180deg);
}

.menu {
  display: none;
  position: absolute;
  top: calc(var(--vp-nav-height) - 12px);
  right: 0;
  flex-direction: column;
  gap: 2px;
  min-width: 160px;
  padding: 8px;
  background: var(--vp-c-bg-elv);
  border: 1px solid var(--vp-c-divider);
  border-radius: 12px;
  box-shadow: var(--vp-shadow-3);
  z-index: 100;
}

.open .menu,
.version-switcher:hover .menu {
  display: flex;
}

.item {
  padding: 6px 12px;
  text-align: left;
  font-size: 14px;
  line-height: 20px;
  color: var(--vp-c-text-1);
  white-space: nowrap;
  border-radius: 6px;
  transition:
    background-color 0.25s,
    color 0.25s;
}

.item:hover {
  background-color: var(--vp-c-default-soft);
  color: var(--vp-c-brand-1);
}

.item.active {
  color: var(--vp-c-brand-1);
  font-weight: 600;
}

/* In the mobile nav screen the switcher sits in the page flow. */
@media (max-width: 767px) {
  .button {
    height: 48px;
    padding: 0;
  }

  .menu {
    top: 44px;
    left: 0;
    right: auto;
  }
}
</style>
