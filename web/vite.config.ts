import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'

// Auth0 Application (web-spa): Callback / Logout / Web Origins = http://localhost:5174/
// Dash backoffice usa :5173 — no mezclar orígenes.
export default defineConfig({
  plugins: [svelte()],
  server: {
    host: 'localhost',
    port: 5174,
    strictPort: true,
  },
  preview: {
    host: 'localhost',
    port: 5174,
    strictPort: true,
  },
})
