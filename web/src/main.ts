import { mount } from 'svelte'
import './app.css'
import App from './App.svelte'
import { captureInitialDeepLink } from './core/infrastructure/presentation/navigation/initial-deep-link'

// Capture the URL hash before Svelte mounts, to prevent the router from clearing it
const initialDeepLink = (typeof window !== 'undefined' && window.location.hash) ? window.location.hash : null;

captureInitialDeepLink()

const app = mount(App, {
  target: document.getElementById('app')!,
  props: {
    initialDeepLink,
  }
})

export default app
