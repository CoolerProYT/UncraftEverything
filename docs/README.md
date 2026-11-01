# Uncraft Everything wiki

VitePress site for the mod. Recipes, item names and the default status colors are read from the mod, for the current branch. The Minecraft 1.20.1 guides document that release separately and do not use the current branch’s generated recipe or color data.

```bash
cd docs
npm install
npm run dev     # syncs data, then serves http://localhost:5173
npm run build   # syncs data, then builds to .vitepress/dist
```

`npm run sync` (run automatically by `dev` and `build`) writes `.vitepress/data/data.json`, which is git-ignored. Item icons are not bundled: they load from the 1024x1024 textures hosted at `https://storage.googleapis.com/coolerpromc/textures/`, under `minecraft/` for vanilla and `uncrafteverything/` for the mod. The mod's blocks have no flat item texture, so their icons are renders of the block model. `public/icons/` keeps small copies for the site logo and favicon, and its file names decide which hosted icons the sync script links. When you add a block, upload its icon to `gs://coolerpromc/textures/uncrafteverything/<name>.png` and put a copy in `public/icons/`.

Minecraft 1.20.1 / 20.1.0 pages live in `1.20.1/`, including the Fabric upgrade and Forge TaCZ guides. Pages for older mod versions live in `v1.8.2/` and `v1.x.x/` and are picked with the version switcher in the nav bar.
