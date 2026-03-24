# AGENTS.md

## Overview
This codebase consists of two main projects:
1. **Web Frontend**: A Svelte + TypeScript + Vite application located in `web/`.
2. **Dashboard Backend**: A Svelte + TypeScript + Vite dashboard with supporting services and workers located in `dash_alejo_taller/`.

Both projects follow modular structures with clear separation of concerns. The backend integrates with Appwrite, Render, and Cloudflare services.

---

## Architecture

### Web Frontend (`web/`)
- **Purpose**: Provides the main user interface for the application.
- **Structure**:
  - `src/core/feature/`: Contains domain-specific features (e.g., `auth`, `product`, `sale`).
  - `src/core/infrastructure/`: Handles environment configurations and data access.
  - `src/demo/`: Demonstrates reusable UI components.
  - `src/lib/`: Contains shared libraries (e.g., `motion`, `navigation`).
- **Build Tool**: Vite is used for development and production builds.

### Dashboard Backend (`dash_alejo_taller/`)
- **Purpose**: Provides administrative tools and backend services.
- **Structure**:
  - `functions/`: Contains serverless functions (e.g., `password_reset`).
  - `services/`: Node.js services for specific tasks (e.g., `password_reset_render`).
  - `workers/`: Cloudflare Workers for infrastructure monitoring.
  - `src/core/feature/`: Similar to the frontend, organizes domain-specific features.

---

## Developer Workflows

### Building and Running
- **Frontend**:
  ```bash
  pnpm install
  pnpm dev
  ```
- **Backend**:
  - Functions and services have their own `package.json` files. Install dependencies and run them individually.

### Testing
- No explicit testing framework is mentioned. Add tests under `test/` directories.

### Debugging
- Use `.env` files to configure environment variables for local development.
- For Cloudflare Workers, use `wrangler` CLI.

---

## Conventions and Patterns

### State Management
- Use Svelte stores for shared state. Example:
  ```ts
  import { writable } from 'svelte/store';
  export const count = writable(0);
  ```

### API Integration
- Appwrite is the primary backend. Use environment variables for configuration.
- Example for password reset:
  - Request code: `{ "action": "request" }`
  - Confirm code: `{ "action": "confirm", "code": "123456", "newPassword": "NuevaPassSegura123" }`

### Environment Variables
- `.env` files are used extensively. Key variables include:
  - `APPWRITE_FUNCTION_API_ENDPOINT`
  - `GMAIL_FROM`
  - `PASSWORD_RESET_TTL_SECONDS`

---

## Key Files and Directories

### Frontend
- `src/core/feature/`: Domain-specific features.
- `src/demo/`: UI component demos.
- `vite.config.ts`: Vite configuration.

### Backend
- `functions/password_reset/`: Appwrite function for password reset.
- `services/password_reset_render/`: Node.js service for password reset.
- `workers/infra_status/`: Cloudflare Worker for infrastructure monitoring.

---

## External Dependencies
- **Appwrite**: Backend-as-a-service for user management.
- **Render**: Hosting for backend services.
- **Cloudflare**: Workers for serverless functions and Pages for static hosting.

---

## Notes for AI Agents
- Follow the modular structure when adding new features.
- Use existing patterns for state management and API integration.
- Ensure environment variables are documented and used securely.
- Refer to `README.md` files in subdirectories for detailed instructions on specific components.
