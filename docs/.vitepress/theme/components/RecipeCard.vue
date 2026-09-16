<script setup lang="ts">
import { computed } from 'vue'
import { findRecipe, itemName, recipeGrid } from '../uncrafteverything'
import ItemSlot from './ItemSlot.vue'

// Either the id of a recipe synced from the mod, or an explicit grid for recipes the current mod no longer has.
const props = defineProps<{
  id?: string
  grid?: (string | null)[]
  result?: string
  count?: number
}>()

const recipe = computed(() => (props.id ? findRecipe(props.id) : undefined))

const cells = computed<(string | null)[]>(() => {
  if (recipe.value) return recipeGrid(recipe.value)
  return Array.from({ length: 9 }, (_, i) => props.grid?.[i] || null)
})
const output = computed(() =>
  recipe.value ? recipe.value.result : props.result ? { id: props.result, count: props.count ?? 1 } : null,
)
const shape = computed(() => (recipe.value?.ingredients ? 'Shapeless' : 'Shaped'))
</script>

<template>
  <figure v-if="output" class="ue-recipe">
    <figcaption class="title">
      <span>Crafting</span>
      <span class="shape">{{ shape }}</span>
    </figcaption>
    <div class="body">
      <div class="grid">
        <ItemSlot v-for="(cell, i) in cells" :id="cell" :key="i" />
      </div>
      <svg class="arrow" viewBox="0 0 22 15" aria-hidden="true" shape-rendering="crispEdges">
        <path d="M0 5h14V0h1v1h1v1h1v1h1v1h1v1h1v1h1v1h-1v1h-1v1h-1v1h-1v1h-1v1h-1v1h-1V10H0z" />
      </svg>
      <ItemSlot :id="output.id" :count="output.count" size="large" />
    </div>
    <div class="result">{{ output.count > 1 ? `${output.count} × ` : '' }}{{ itemName(output.id) }}</div>
  </figure>
  <p v-else class="ue-muted">Recipe {{ id }} not found.</p>
</template>

<style scoped>
.ue-recipe {
  display: table;
  margin: 16px 0;
  padding: 10px 16px 12px;
  background: var(--ue-gui-bg);
  border: 3px solid;
  border-color: var(--ue-gui-light) var(--ue-gui-dark) var(--ue-gui-dark) var(--ue-gui-light);
  border-radius: 4px;
  box-shadow:
    0 0 0 2px var(--ue-gui-outline),
    var(--vp-shadow-2);
}

.title {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ue-gui-text);
}

.shape {
  font-size: 10px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  opacity: 0.7;
}

.body {
  display: flex;
  align-items: center;
  gap: 16px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, 36px);
}

.arrow {
  width: 44px;
  height: 30px;
  fill: var(--ue-gui-arrow);
}

.result {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid var(--ue-gui-dark);
  font-size: 14px;
  font-weight: 600;
  color: var(--ue-gui-text);
}

@media (max-width: 400px) {
  .body {
    gap: 8px;
  }

  .arrow {
    width: 28px;
    height: 19px;
  }
}
</style>
