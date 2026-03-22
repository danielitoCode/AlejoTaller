import { defineConfig } from 'vite'
import { functionsMixins } from "vite-plugin-functions-mixins";
import { svelte } from '@sveltejs/vite-plugin-svelte'

// https://vite.dev/config/
export default defineConfig({
  plugins: [svelte(), functionsMixins({ deps: ["m3-svelte"] })],
})
