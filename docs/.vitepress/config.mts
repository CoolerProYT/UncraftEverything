import { defineConfig, type DefaultTheme } from 'vitepress'

// GitHub Pages serves a project site from /<repository>/. For a custom domain or a user site, build with DOCS_BASE=/.
const base = process.env.DOCS_BASE ?? '/UncraftEverything/'

/** Sidebar for a pre-2.0 version folder; those versions have no Auto Uncrafting Table or client config. */
function legacySidebar(prefix: string): DefaultTheme.SidebarItem[] {
  return [
    {
      text: 'Guide',
      items: [
        { text: 'Getting started', link: `${prefix}getting-started` },
        { text: 'Uncrafting Table', link: `${prefix}uncrafting-table` },
      ],
    },
    {
      text: 'Configuration',
      items: [
        { text: 'Common config', link: `${prefix}common-config` },
        { text: 'Per item exp config', link: `${prefix}per-item-exp-config` },
        { text: 'FTB Quests progression', link: `${prefix}ftb-quest-progression-config` },
      ],
    },
  ]
}

export default defineConfig({
  title: 'Uncraft Everything',
  description: 'Turn crafted items back into their ingredients. Wiki for the Uncraft Everything Minecraft mod.',
  base,
  cleanUrls: true,
  lastUpdated: true,
  srcExclude: ['README.md', 'scripts/**'],
  // `head` entries are not rewritten for the base path, unlike links, images and the theme logo.
  head: [['link', { rel: 'icon', type: 'image/png', href: `${base}icons/uncrafting_table.png` }]],
  themeConfig: {
    logo: { src: '/icons/uncrafting_table.png', alt: '' },
    nav: [
      { text: 'Guide', link: '/getting-started', activeMatch: '^/(getting-started|uncrafting-table|auto-uncrafting-table|changelog)' },
      { text: 'Configuration', link: '/common-config', activeMatch: '-config$' },
      { component: 'VersionSwitcher' },
    ],
    sidebar: {
      '/v1.8.2/': legacySidebar('/v1.8.2/'),
      '/v1.x.x/': legacySidebar('/v1.x.x/'),
      '/': [
        {
          text: 'Guide',
          items: [
            { text: 'Getting started', link: '/getting-started' },
            { text: 'Uncrafting Table', link: '/uncrafting-table' },
            { text: 'Auto Uncrafting Table', link: '/auto-uncrafting-table' },
            { text: 'Changelog', link: '/changelog' },
          ],
        },
        {
          text: 'Configuration',
          items: [
            { text: 'Client config', link: '/client-config' },
            { text: 'Common config', link: '/common-config' },
            { text: 'Per item exp config', link: '/per-item-exp-config' },
            { text: 'FTB Quests progression', link: '/ftb-quest-progression-config' },
          ],
        },
      ],
    },
    socialLinks: [
      {
        icon: {
          svg: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M4 19v-9q0-.475.213-.9t.587-.7l6-4.5q.525-.4 1.2-.4t1.2.4l6 4.5q.375.275.588.7T20 10v9q0 .825-.588 1.413T18 21h-3q-.425 0-.712-.288T14 20v-5q0-.425-.288-.712T13 14h-2q-.425 0-.712.288T10 15v5q0 .425-.288.713T9 21H6q-.825 0-1.412-.587T4 19"/></svg>',
        },
        link: 'https://coolerpromc.com/',
        ariaLabel: 'CoolerProMC website',
      },
      { icon: 'github', link: 'https://github.com/CoolerProYT/UncraftEverything' },
      { icon: 'discord', link: 'http://discord.gg/hvFfqsqQm8' },
    ],
    search: { provider: 'local' },
    outline: { level: [2, 3] },
    footer: { message: 'Released under CC0 1.0.' },
  },
})
