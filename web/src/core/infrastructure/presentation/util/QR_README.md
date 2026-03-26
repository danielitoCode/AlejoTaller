# QR Code para Reservaciones

## Descripción General

Se implementó un sistema de generación de QR codes para las reservaciones que incluye los detalles de los productos de forma comprimida. Esto permite escaneo rápido sin necesidad de decodificar grandes volúmenes de datos.

## Formato del QR

El formato es: `id|amount|items`

Donde:
- `id`: ID único de la venta/reservación
- `amount`: Monto total de la compra
- `items`: Lista de productos separados por `|` en formato `productId:quantity:price`

**Ejemplo:**
```
abc123|99.99|p1:2:50|p2:1:99.99
```

Este formato es muy comprimido y se reduce considerablemente el tamaño del QR, permitiendo:
- Escaneo rápido
- Mejor tolerancia a daño
- Menores requisitos de espacio

## Uso

### Generar QR desde una Venta

```typescript
import { generateSaleQRDataUrl } from '@/core/infrastructure/presentation/util/qr.service';
import type { Sale } from '@/core/feature/sale/domain/entity/Sale';

const sale: Sale = { /* ... */ };
const qrDataUrl = await generateSaleQRDataUrl(sale);
// qrDataUrl es un Data URL PNG que puede usarse en <img src={qrDataUrl} />
```

### Parsear QR Escaneado

```typescript
import { parseSaleQRData } from '@/core/infrastructure/presentation/util/qr.service';

const scannedData = "abc123|99.99|p1:2:50";
const parsed = parseSaleQRData(scannedData);

console.log(parsed);
// {
//   saleId: 'abc123',
//   amount: 99.99,
//   items: [
//     { productId: 'p1', quantity: 2, price: 50 }
//   ]
// }
```

## Componentes

### QRCodeDisplay.svelte

Componente reutilizable que muestra el QR de una venta:

```svelte
<script lang="ts">
  import QRCodeDisplay from '@/core/infrastructure/presentation/components/QRCodeDisplay.svelte';
  import type { Sale } from '@/core/feature/sale/domain/entity/Sale';

  let sale: Sale;
</script>

<QRCodeDisplay {sale} />
```

El componente:
- Genera automáticamente el QR al montarse
- Maneja estados de carga y error
- Es responsive y se adapta bien en dispositivos móviles

## Ventajas del Enfoque

1. **Tamaño Comprimido**: Los QR son pequeños (hasta V4 = 33x33 pixels)
2. **Información Completa**: Contiene todos los datos necesarios de la compra
3. **Validación**: El ID de venta permite verificar integridad al procesar
4. **Rápido**: Sin necesidad de hacer llamadas API para decodificar
5. **Offline**: Funciona sin conexión a internet

## Rutas Modificadas

- **InternalReservationDetailScreen.svelte**: Ahora muestra un QR code funcional con los productos incluidos
- **InternalBuyConfirmScreen.svelte**: Después de crear la venta, navega automáticamente a la pantalla de detalle donde se muestra el QR

## Dependencias Nuevas

- `qrcode@1.5.4`: Generador de QR codes
- `@types/qrcode@1.5.6`: Type definitions para TypeScript

