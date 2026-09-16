// @ts-ignore
import raw from '../data/data.json'

export interface Recipe {
  id: string
  type: string
  result: { id: string; count: number }
  pattern?: string[]
  key?: Record<string, string>
  ingredients?: string[]
}

export interface StatusColor {
  /** Key in the [StatusColor] section of the client config. */
  key: string
  /** Default color as eight hex digits, AARRGGBB. */
  argb: string
  /** Default color as the signed integer written to the config file. */
  value: number
  status: string
}

export const data = raw as unknown as {
  names: Record<string, string>
  textures: Record<string, string>
  recipes: Recipe[]
  statusColors: StatusColor[]
}

const TAG_NAMES: Record<string, string> = {
  '#minecraft:planks': 'Any Planks',
}

/** Items a tag ingredient cycles through, like a recipe viewer does. */
export const TAG_MEMBERS: Record<string, string[]> = {
  '#minecraft:planks': [
    'minecraft:oak_planks',
    'minecraft:spruce_planks',
    'minecraft:birch_planks',
    'minecraft:jungle_planks',
    'minecraft:acacia_planks',
    'minecraft:dark_oak_planks',
    'minecraft:mangrove_planks',
    'minecraft:cherry_planks',
    'minecraft:pale_oak_planks',
    'minecraft:bamboo_planks',
    'minecraft:crimson_planks',
    'minecraft:warped_planks',
  ],
}

/** Mod items use their in-game name; vanilla ids are turned into readable names. */
export function itemName(id: string): string {
  if (data.names[id]) return data.names[id]
  if (TAG_NAMES[id]) return TAG_NAMES[id]
  const path = id.replace(/^#/, '').split(':').pop() ?? id
  return path
    .split('_') // @ts-ignore
    .map((word) => (['of', 'the'].includes(word) ? word : word.charAt(0).toUpperCase() + word.slice(1)))
    .join(' ')
}

/** Hosted renders of vanilla items, one PNG per item id. Mojang's textures are not bundled here. */
const VANILLA_ICONS = 'https://storage.googleapis.com/coolerpromc/textures'

/**
 * Where to load an item's icon from. Mod blocks use the renders committed in public/icons (a site path);
 * vanilla items use the hosted renders.
 */
export function itemIcon(id: string): { src: string; local: boolean } | null {
  if (data.textures[id]) return { src: data.textures[id], local: true }
  // @ts-ignore
  const [namespace, path] = id.includes(':') ? id.split(':') : ['minecraft', id]
  if (namespace !== 'minecraft') return null
  return { src: `${VANILLA_ICONS}/${namespace}/${path}.png`, local: false }
}

export function findRecipe(id: string): Recipe | undefined {
  return data.recipes.find((r) => r.id === id || r.id === `uncrafteverything:${id}`)
}

/** Nine cells for the crafting grid, left to right, top to bottom. */
export function recipeGrid(recipe: Recipe): (string | null)[] {
  if (recipe.pattern && recipe.key) {
    const cells: (string | null)[] = []
    for (let row = 0; row < 3; row++) {
      for (let col = 0; col < 3; col++) {
        const symbol = recipe.pattern[row]?.[col] ?? ' '
        cells.push(symbol === ' ' ? null : recipe.key[symbol] ?? null)
      }
    }
    return cells
  }
  return Array.from({ length: 9 }, (_, i) => recipe.ingredients?.[i] ?? null)
}
