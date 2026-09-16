<script setup lang="ts">
import { data } from '../uncrafteverything'

/** CSS color for an AARRGGBB hex string. */
function css(argb: string): string {
  return `#${argb.slice(2)}${argb.slice(0, 2)}`
}
</script>

<template>
  <table class="ue-status">
    <thead>
      <tr>
        <th>Setting</th>
        <th>Default</th>
        <th>Used when</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="color in data.statusColors" :key="color.key">
        <td><code>{{ color.key }}</code></td>
        <td>
          <span class="default">
            <span class="swatch" :style="{ background: css(color.argb) }" aria-hidden="true" />
            <span>
              <code>{{ color.value }}</code>
              <span class="hex">#{{ color.argb.toUpperCase() }}</span>
            </span>
          </span>
        </td>
        <td>{{ color.status }}</td>
      </tr>
    </tbody>
  </table>
</template>

<style scoped>
.default {
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}

.swatch {
  width: 22px;
  height: 22px;
  flex: none;
  border-radius: 4px;
  box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.25);
}

.hex {
  display: block;
  font-family: var(--vp-font-family-mono);
  font-size: 11px;
  color: var(--vp-c-text-2);
}
</style>
