# Uncraft Everything wiki

VitePress site for the mod. Recipes, item names and the default status colors are read from the mod, so the wiki never drifts from the game.

```bash
cd docs
npm install
npm run dev     # syncs data, then serves http://localhost:5173
npm run build   # syncs data, then builds to .vitepress/dist
```

`npm run sync` (run automatically by `dev` and `build`) writes `.vitepress/data/data.json`, which is git-ignored. Vanilla textures are not copied: vanilla items load from the hosted renders at `https://storage.googleapis.com/coolerpromc/textures/`, set in `.vitepress/theme/uncrafteverything.ts`. The mod's blocks have no flat item texture, so their icons are rendered from the block model and committed in `public/icons/`.

Pages for older mod versions live in `v1.8.2/` and `v1.x.x/` and are picked with the version switcher in the nav bar.
