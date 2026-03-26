import QRCode from 'qrcode';
import type {Sale} from '../../../feature/sale/domain/entity/Sale';

/**
 * Genera datos comprimidos para QR de una venta/reservación
 * Formato: id|userId|amount|items (cada item: productId:qty:price)
 * 
 * Ejemplo: abc123|user456|99.99|p1:2:50|p2:1:99.99
 * 
 * Esto minimiza el tamaño del QR para escaneo rápido
 */
export function generateSaleQRData(sale: Sale): string {
    const itemsData = sale.products
        .map(item => `${item.productId}:${item.quantity}:${item.price}`)
        .join('|');
    
    return `${sale.id}|${sale.userId}|${sale.amount}|${itemsData}`;
}

/**
 * Genera la imagen SVG del QR desde los datos de venta
 */
export async function generateSaleQRSvg(sale: Sale): Promise<string> {
    const data = generateSaleQRData(sale);
    return QRCode.toString(data, { type: 'svg', width: 200 });
}

/**
 * Genera la imagen PNG del QR como Data URL
 */
export async function generateSaleQRDataUrl(sale: Sale): Promise<string> {
    const data = generateSaleQRData(sale);
    return QRCode.toDataURL(data, { width: 200 });
}

/**
 * Parsea los datos del QR de vuelta a un objeto legible
 */
export function parseSaleQRData(data: string): {
    saleId: string;
    userId: string;
    amount: number;
    items: Array<{ productId: string; quantity: number; price: number }>;
} {
    const parts = data.split('|');
    const saleId = parts[0];
    const userId = parts[1];
    const amount = parseFloat(parts[2]);
    const items = parts.slice(3).map(item => {
        const [productId, quantity, price] = item.split(':');
        return {
            productId,
            quantity: parseInt(quantity),
            price: parseFloat(price)
        };
    });
    
    return { saleId, userId, amount, items };
}
