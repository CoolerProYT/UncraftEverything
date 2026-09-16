import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import ItemSlot from './components/ItemSlot.vue'
import RecipeCard from './components/RecipeCard.vue'
import StatusColors from './components/StatusColors.vue'
import VersionSwitcher from './components/VersionSwitcher.vue'
import './style.css'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('ItemSlot', ItemSlot)
    app.component('RecipeCard', RecipeCard)
    app.component('StatusColors', StatusColors)
    app.component('VersionSwitcher', VersionSwitcher)
  },
} satisfies Theme
