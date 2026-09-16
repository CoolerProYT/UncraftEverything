// Pulls wiki data straight from the mod so the docs never drift from the game:
// recipes and item names from the mod's resources, and the status overlay colors from the client config.
import { existsSync, mkdirSync, readdirSync, readFileSync, writeFileSync } from 'node:fs'
import { basename, dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const MOD_ID = 'uncrafteverything'
const docs = join(dirname(fileURLToPath(import.meta.url)), '..')
const root = join(docs, '..')
const clientConfig = join(root, `common/src/main/java/com/coolerpromc/${MOD_ID}/config/UncraftEverythingClientConfig.java`)
const resources = join(root, 'common/src/main/resources')
const assets = join(resources, `assets/${MOD_ID}`)
const data = join(resources, `data/${MOD_ID}`)

if (!existsSync(resources)) {
  console.error(`No mod resources at ${resources}.`)
  process.exit(1)
}

const readJson = (file) => JSON.parse(readFileSync(file, 'utf8'))
const jsonFiles = (dir) => (existsSync(dir) ? readdirSync(dir).filter((f) => f.endsWith('.json')).sort() : [])

const lang = readJson(join(assets, 'lang/en_us.json'))

// Item names for mod items; vanilla names are prettified on the page.
const names = {}
for (const [key, value] of Object.entries(lang)) {
  const match = key.match(new RegExp(`^(item|block)\.${MOD_ID}\.([a-z0-9_]+)$`))
  if (match) names[`${MOD_ID}:${match[2]}`] = value
}

// Block items have no flat texture, so their icons are rendered from the block model and committed in public/icons/.
const textures = {}
const renderedIcons = join(docs, 'public/icons')
for (const file of existsSync(renderedIcons) ? readdirSync(renderedIcons).filter((f) => f.endsWith('.png')) : []) {
  textures[`${MOD_ID}:${basename(file, '.png')}`] = `/icons/${file}`
}

const ingredient = (value) => {
  if (typeof value === 'string') return value
  if (Array.isArray(value)) return ingredient(value[0])
  if (value?.item) return value.item
  if (value?.tag) return `#${value.tag}`
  return '?'
}

const recipes = jsonFiles(join(data, 'recipe')).map((file) => {
  const json = readJson(join(data, 'recipe', file))
  const recipe = { id: `${MOD_ID}:${basename(file, '.json')}`, type: json.type, result: { id: json.result?.id, count: json.result?.count ?? 1 } }
  if (json.type === 'minecraft:crafting_shaped') {
    recipe.pattern = json.pattern
    recipe.key = Object.fromEntries(Object.entries(json.key).map(([symbol, value]) => [symbol, ingredient(value)]))
  } else if (json.type === 'minecraft:crafting_shapeless') {
    recipe.ingredients = json.ingredients.map(ingredient)
  }
  return recipe
})

// Status overlay colors, read from their definitions: defineInt("StatusColor.<key>", 0xAARRGGBB, min, max, "Overlay color for <status>").
const statusColors = [
  ...readFileSync(clientConfig, 'utf8').matchAll(/defineInt\("StatusColor\.(\w+)",\s*0x([0-9a-fA-F]{8}),[^"]*"Overlay color for ([^"]+)"\)/g),
].map(([, key, argb, status]) => ({ key, argb: argb.toLowerCase(), value: Number.parseInt(argb, 16) | 0, status }))
if (statusColors.length === 0) {
  console.error(`No status colors found in ${clientConfig}.`)
  process.exit(1)
}

mkdirSync(join(docs, '.vitepress/data'), { recursive: true })
writeFileSync(join(docs, '.vitepress/data/data.json'), JSON.stringify({ names, textures, recipes, statusColors }, null, 2))
console.log(
  `Synced ${recipes.length} recipes, ${Object.keys(names).length} names, ${Object.keys(textures).length} textures, ${statusColors.length} status colors.`,
)
