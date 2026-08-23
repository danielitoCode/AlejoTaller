# AlejoTaller Customer MCP Server — Tools & JSON Schema Specification

Este documento contiene la especificación formal en formato **JSON Schema (Draft-07)** de cada una de las 16 herramientas expuestas por el servidor MCP de AlejoTaller.

---

## 📑 Resumen de Herramientas y Políticas de Seguridad

| Herramienta | Módulo | Permiso | Requiere Confirmación | Autenticación |
|---|---|---|---|---|
| `ping_customer_mcp` | Sistema | `READ` | ❌ No | ❌ No |
| `get_server_info` | Sistema | `READ` | ❌ No | ❌ No |
| `get_my_profile` | Cliente | `READ` | ❌ No | 🔒 Sí |
| `update_my_profile` | Cliente | `WRITE` | ❌ No | 🔒 Sí |
| `get_my_orders` | Pedidos | `READ` | ❌ No | 🔒 Sí |
| `get_order` | Pedidos | `READ` | ❌ No | 🔒 Sí |
| `create_order` | Pedidos | `WRITE` | ⚠️ **SÍ** | 🔒 Sí |
| `cancel_order` | Pedidos | `WRITE` | ⚠️ **SÍ** | 🔒 Sí |
| `list_products` | Catálogo | `READ` | ❌ No | ❌ No |
| `get_product` | Catálogo | `READ` | ❌ No | ❌ No |
| `list_categories` | Catálogo | `READ` | ❌ No | ❌ No |
| `get_category` | Catálogo | `READ` | ❌ No | ❌ No |
| `list_active_promotions` | Catálogo | `READ` | ❌ No | ❌ No |
| `get_my_support_threads` | Soporte | `READ` | ❌ No | 🔒 Sí |
| `get_support_thread` | Soporte | `READ` | ❌ No | 🔒 Sí |
| `get_thread_messages` | Soporte | `READ` | ❌ No | 🔒 Sí |
| `create_support_thread` | Soporte | `WRITE` | ❌ No | 🔒 Sí |
| `post_support_message` | Soporte | `WRITE` | ❌ No | 🔒 Sí |

---

## 1. Módulo de Sistema

### `ping_customer_mcp`
```json
{
  "name": "ping_customer_mcp",
  "description": "Comprueba la conectividad y estado de salud del servidor MCP de AlejoTaller.",
  "inputSchema": {
    "type": "object",
    "properties": {},
    "additionalProperties": false
  }
}
```

### `get_server_info`
```json
{
  "name": "get_server_info",
  "description": "Obtiene información general sobre las capacidades del servidor MCP de AlejoTaller y sus políticas de seguridad.",
  "inputSchema": {
    "type": "object",
    "properties": {},
    "additionalProperties": false
  }
}
```

---

## 2. Módulo de Perfil del Cliente

### `get_my_profile`
```json
{
  "name": "get_my_profile",
  "description": "Obtiene la información del perfil del cliente autenticado actual (nombre, email, teléfono, etc.). No requiere parámetros.",
  "inputSchema": {
    "type": "object",
    "properties": {},
    "additionalProperties": false
  }
}
```

### `update_my_profile`
```json
{
  "name": "update_my_profile",
  "description": "Actualiza la información permitida del perfil del cliente (nombre, teléfono, URL de foto).",
  "inputSchema": {
    "type": "object",
    "properties": {
      "name": {
        "type": "string",
        "description": "Nuevo nombre del cliente"
      },
      "phone": {
        "type": "string",
        "description": "Nuevo número de teléfono"
      },
      "photoUrl": {
        "type": "string",
        "format": "uri",
        "description": "URL de la foto de perfil"
      }
    },
    "additionalProperties": false
  }
}
```

---

## 3. Módulo de Pedidos y Ventas

### `get_my_orders`
```json
{
  "name": "get_my_orders",
  "description": "Obtiene la lista de pedidos u órdenes del cliente autenticado actual (tanto pendientes como confirmados o cancelados).",
  "inputSchema": {
    "type": "object",
    "properties": {},
    "additionalProperties": false
  }
}
```

### `get_order`
```json
{
  "name": "get_order",
  "description": "Obtiene los detalles de un pedido específico por su ID. Solo permite ver pedidos del cliente autenticado.",
  "inputSchema": {
    "type": "object",
    "properties": {
      "orderId": {
        "type": "string",
        "description": "Identificador único del pedido (ID de venta)"
      }
    },
    "required": ["orderId"],
    "additionalProperties": false
  }
}
```

### `cancel_order` *(Requiere confirmación previa)*
```json
{
  "name": "cancel_order",
  "description": "Cancela un pedido pendiente (UNVERIFIED) del cliente. Esta acción requiere confirmación previa por parte del usuario.",
  "inputSchema": {
    "type": "object",
    "properties": {
      "orderId": {
        "type": "string",
        "description": "ID del pedido a cancelar"
      }
    },
    "required": ["orderId"],
    "additionalProperties": false
  }
}
```

### `create_order` *(Requiere confirmación previa)*
```json
{
  "name": "create_order",
  "description": "Crea un nuevo pedido de productos para el cliente autenticado. Esta acción requiere confirmación previa.",
  "inputSchema": {
    "type": "object",
    "properties": {
      "currency": {
        "type": "string",
        "enum": ["CUP", "USD", "MLC"],
        "description": "Moneda de pago"
      },
      "deliveryType": {
        "type": "string",
        "enum": ["PICKUP", "DELIVERY"],
        "description": "Tipo de entrega: PICKUP (recoger en taller) o DELIVERY (envío a domicilio)"
      },
      "items": {
        "type": "array",
        "minItems": 1,
        "description": "Lista de productos y cantidades",
        "items": {
          "type": "object",
          "properties": {
            "productId": {
              "type": "string",
              "description": "ID del producto"
            },
            "quantity": {
              "type": "integer",
              "minimum": 1,
              "description": "Cantidad deseada"
            }
          },
          "required": ["productId", "quantity"],
          "additionalProperties": false
        }
      },
      "deliveryAddress": {
        "type": "object",
        "description": "Dirección de entrega (requerida si deliveryType es DELIVERY)",
        "properties": {
          "province": { "type": "string" },
          "municipality": { "type": "string" },
          "mainStreet": { "type": "string" },
          "betweenStreets": { "type": ["string", "null"] },
          "phone": { "type": "string" },
          "houseNumber": { "type": "string" },
          "referenceName": { "type": ["string", "null"] }
        },
        "required": ["province", "municipality", "mainStreet", "phone", "houseNumber"],
        "additionalProperties": false
      }
    },
    "required": ["currency", "deliveryType", "items"],
    "additionalProperties": false
  }
}
```

---

## 4. Módulo de Catálogo y Promociones

### `list_products`
```json
{
  "name": "list_products",
  "description": "Consulta el catálogo de productos disponibles en AlejoTaller (precios, disponibilidad, fotos y categoría).",
  "inputSchema": {
    "type": "object",
    "properties": {
      "categoryId": {
        "type": "string",
        "description": "Filtrar productos opcionalmente por ID de categoría"
      }
    },
    "additionalProperties": false
  }
}
```

### `get_product`
```json
{
  "name": "get_product",
  "description": "Obtiene los detalles completos de un producto específico por su ID.",
  "inputSchema": {
    "type": "object",
    "properties": {
      "productId": {
        "type": "string",
        "description": "ID único del producto"
      }
    },
    "required": ["productId"],
    "additionalProperties": false
  }
}
```

### `list_categories`
```json
{
  "name": "list_categories",
  "description": "Obtiene la lista de categorías activas de productos y servicios de AlejoTaller.",
  "inputSchema": {
    "type": "object",
    "properties": {},
    "additionalProperties": false
  }
}
```

### `get_category`
```json
{
  "name": "get_category",
  "description": "Obtiene la información de una categoría específica por su ID.",
  "inputSchema": {
    "type": "object",
    "properties": {
      "categoryId": {
        "type": "string",
        "description": "ID de la categoría"
      }
    },
    "required": ["categoryId"],
    "additionalProperties": false
  }
}
```

### `list_active_promotions`
```json
{
  "name": "list_active_promotions",
  "description": "Obtiene la lista de promociones y ofertas vigentes de AlejoTaller (descuentos en productos y avisos especiales).",
  "inputSchema": {
    "type": "object",
    "properties": {},
    "additionalProperties": false
  }
}
```

---

## 5. Módulo de Soporte y Atención al Cliente

### `get_my_support_threads`
```json
{
  "name": "get_my_support_threads",
  "description": "Obtiene la lista de consultas o tickets de soporte creados por el cliente autenticado.",
  "inputSchema": {
    "type": "object",
    "properties": {},
    "additionalProperties": false
  }
}
```

### `get_support_thread`
```json
{
  "name": "get_support_thread",
  "description": "Obtiene la información y estado de un ticket o hilo de soporte específico.",
  "inputSchema": {
    "type": "object",
    "properties": {
      "threadId": {
        "type": "string",
        "description": "ID del hilo de soporte"
      }
    },
    "required": ["threadId"],
    "additionalProperties": false
  }
}
```

### `get_thread_messages`
```json
{
  "name": "get_thread_messages",
  "description": "Obtiene el historial completo de mensajes dentro de un hilo de soporte del cliente.",
  "inputSchema": {
    "type": "object",
    "properties": {
      "threadId": {
        "type": "string",
        "description": "ID del hilo de soporte"
      }
    },
    "required": ["threadId"],
    "additionalProperties": false
  }
}
```

### `create_support_thread`
```json
{
  "name": "create_support_thread",
  "description": "Abre una nueva consulta o ticket de soporte con el equipo de AlejoTaller.",
  "inputSchema": {
    "type": "object",
    "properties": {
      "reason": {
        "type": "string",
        "enum": ["soporte", "pregunta_tecnica", "facturacion", "otro"],
        "description": "Motivo de la consulta"
      },
      "subject": {
        "type": "string",
        "description": "Asunto del ticket"
      },
      "body": {
        "type": "string",
        "description": "Mensaje inicial describiendo el problema o duda"
      }
    },
    "required": ["reason", "subject", "body"],
    "additionalProperties": false
  }
}
```

### `post_support_message`
```json
{
  "name": "post_support_message",
  "description": "Envía una respuesta o mensaje adicional dentro de un ticket de soporte existente.",
  "inputSchema": {
    "type": "object",
    "properties": {
      "threadId": {
        "type": "string",
        "description": "ID del hilo de soporte"
      },
      "body": {
        "type": "string",
        "description": "Contenido del mensaje"
      }
    },
    "required": ["threadId", "body"],
    "additionalProperties": false
  }
}
```
