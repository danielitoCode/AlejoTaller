# AlejoTaller Customer MCP Server

Servidor **Model Context Protocol (MCP)** dedicado para el área de clientes de **AlejoTaller**, desarrollado con **TypeScript + Node.js + @modelcontextprotocol/sdk**.

Diseñado para ejecutarse localmente durante desarrollo (vía stdio o HTTP) y desplegarse como **Remote MCP Server en Cloudflare Workers mediante Streamable HTTP**.

---

## 1. Arquitectura y Principio de Diseño

El MCP actúa como la **capa de interacción entre Agentes de IA y el backend de AlejoTaller**.

```text
                        CLIENTE IA / AGENTE
                             │
                             │ MCP (Stdio / Streamable HTTP)
                             ▼
                 ┌───────────────────────┐
                 │ AlejoTaller MCP Server│
                 │                       │
                 │ Tools                 │
                 │ Resources             │
                 │ Prompts               │
                 └───────────┬───────────┘
                             │
                       Authentication Context
                             │
                       Authorization Policy
                             │
                             ▼
                          Services
                             │
                        Repositories
                             │
                             ▼
                      Appwrite Backend
```

### Separación de Responsabilidades:
1. **MCP Tool**: Expone la interfaz estructurada y anotada para la IA. No contiene lógica de negocio ni llamadas directas a la BD.
2. **Auth & Policies**: Determina la identidad del cliente autenticado y si la acción requiere confirmación previa.
3. **Services**: Aplican reglas de negocio (ej. verificar propiedad de un pedido o ticket).
4. **Repositories**: Encapsulan el acceso a Appwrite mediante `node-appwrite` (Server SDK con API Key).
5. **Appwrite**: Fuente de verdad persistente.

---

## 2. Herramientas MCP Disponibles (Tools)

> Para ver la especificación completa en formato **JSON Schema (Draft-07)** de cada función, consulta [docs/TOOLS_SPEC.md](docs/TOOLS_SPEC.md).

### Sistema
- `ping_customer_mcp`: Health check del servidor.
- `get_server_info`: Resumen de capacidades y políticas.

### Perfil de Cliente
- `get_my_profile`: Obtiene la información del perfil del cliente autenticado.
- `update_my_profile`: Actualiza nombre, teléfono o foto del cliente.

### Pedidos / Ventas
- `get_my_orders`: Lista las ventas/pedidos del cliente (`UNVERIFIED`, `VERIFIED`, `DELETED`).
- `get_order`: Consulta los detalles de un pedido específico por ID (verifica propiedad).
- `cancel_order`: Cancela un pedido pendiente (`requiresConfirmation: true`).
- `create_order`: Crea un nuevo pedido (`requiresConfirmation: true`).

### Productos y Catálogo
- `list_products`: Lista productos disponibles con stock vendible calculado.
- `get_product`: Consulta detalles de un producto por ID.
- `list_categories`: Lista categorías activas.
- `get_category`: Información de una categoría.
- `list_active_promotions`: Muestra las promociones vigentes.

### Soporte y Tickets
- `get_my_support_threads`: Lista los tickets de soporte del cliente.
- `get_support_thread`: Obtiene un ticket específico.
- `get_thread_messages`: Historial de mensajes de un ticket.
- `create_support_thread`: Crea una nueva consulta de soporte.
- `post_support_message`: Responde dentro de un ticket.

---

## 3. Instalación y Ejecución Local

```bash
cd mcp
npm install
```

### Configuración de Variables de Entorno:
Copia `.dev.vars.example` a `.dev.vars` y llena con tus credenciales de Appwrite:

```env
APPWRITE_ENDPOINT=https://cloud.appwrite.io/v1
APPWRITE_PROJECT_ID=tu_project_id
APPWRITE_API_KEY=tu_api_key
APPWRITE_DATABASE_ID=tu_database_id
```

### Ejecutar Localmente con MCP Inspector:
```bash
npm run dev
```

O conectar directamente MCP Inspector:
```bash
npx @modelcontextprotocol/inspector tsx src/index.ts
```

### Ejecutar Tests:
```bash
npm test
```

---

## 4. Despliegue en Cloudflare Workers

El servidor soporta **Streamable HTTP** mediante Web Standards nativos.

### Configurar secretos en Cloudflare:
```bash
npx wrangler secret put APPWRITE_ENDPOINT
npx wrangler secret put APPWRITE_PROJECT_ID
npx wrangler secret put APPWRITE_API_KEY
npx wrangler secret put APPWRITE_DATABASE_ID
```

### Desplegar:
```bash
npm run deploy
```

---

## 5. Estrategia de Autenticación e Identidad

En la **Fase 1 (Local / Dev)**, la identidad del cliente se resuelve desde la cabecera `X-Customer-Id` o desde las meta-propiedades del MCP context (`customerId`). **La IA jamás recibe ni especifica un `customerId` como argumento de las herramientas de usuario.**

### Transición a Producción:
En producción, el MCP Host enviará un token JWT obtenido tras autenticarse en Appwrite, el cual será validado server-side en `src/auth/resolver.ts`.
