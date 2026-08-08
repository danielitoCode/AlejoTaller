import type { DeliveryType } from "../../domain/entity/enums";
import type {Sale} from "../../domain/entity/Sale";
import {saleFromDTO, saleToDTO} from "../mapper/Mappers";
import type {SaleRepository} from "../../domain/repository/SaleRepository";
import {SaleNetRepository} from "./sale.net.repository";
import {db} from "../../../../infrastructure/di/dexie.db";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";
import type { SaleDTO } from "../dto/SaleDTO";

function toPlainSaleDoc(raw: Record<string, unknown>): SaleDTO {
    const productsRaw = raw.products;
    let products: string;
    if (typeof productsRaw === "string") {
        products = productsRaw;
    } else if (productsRaw != null) {
        try {
            products = JSON.stringify(productsRaw);
        } catch {
            products = "[]";
        }
    } else {
        products = "[]";
    }

    const deliveryRaw = raw.delivery_address;
    let delivery_address: string | null = null;
    if (typeof deliveryRaw === "string") {
        delivery_address = deliveryRaw;
    } else if (deliveryRaw != null) {
        try {
            delivery_address = JSON.stringify(deliveryRaw);
        } catch {
            delivery_address = null;
        }
    }

    return {
        $id: String(raw.$id ?? raw.id ?? ""),
        $createdAt: String(raw.$createdAt ?? ""),
        $updatedAt: String(raw.$updatedAt ?? ""),
        date: String(raw.date ?? ""),
        amount: Number(raw.amount ?? 0) || 0,
        buy_state: String(raw.buy_state ?? raw.verified ?? "UNVERIFIED"),
        currency: String(raw.currency ?? "CUP"),
        products,
        user_id: String(raw.user_id ?? ""),
        delivery_type: (raw.delivery_type as string | null | undefined) ?? null,
        delivery_address,
        sale_type: (raw.sale_type as string | null | undefined) ?? null,
        stock_hold_applied: raw.stock_hold_applied === true
    } as SaleDTO;
}

export class SaleOfflineFirstRepository implements SaleRepository {
    constructor(
        private readonly net: SaleNetRepository) {}

    async getAllSales(): Promise<Sale[]> {
        try {
            const remote = await this.net.getAll()
            await db.sales.bulkPut(remote)
            return remote.map(saleFromDTO)
        } catch {
            const local = await db.sales.toArray()
            return local.map(saleFromDTO)
        }
    }

    async create(sale: Sale): Promise<Sale> {
        try {
            const created = await this.net.create(saleToDTO(sale));
            await db.sales.put(created);
            return saleFromDTO(created);
        } catch (error: any) {
            logger.error(
                `Error al crear venta en Appwrite: ${error?.message ?? "desconocido"}`,
                error?.stack
            );
            throw error;
        }
    }

    async getByUser(userId: string): Promise<Sale[]> {
        try {
            const remote = await this.net.getByUser(userId);
            await db.sales.bulkPut(remote);
            return remote.map(saleFromDTO);
        } catch {
            const local = await db.sales.where("user_id").equals(userId).toArray();
            return local.map(saleFromDTO);
        }
    }

    async updateVerified(id: string, verified: string): Promise<Sale> {
        try {
            const updated = await this.net.updateVerified(id, verified);
            await db.sales.put(updated);
            return saleFromDTO(updated);
        } catch (error: any) {
            logger.error(
                `Error al actualizar venta en Appwrite: ${error?.message ?? "desconocido"}`,
                error?.stack
            );
            throw error;
        }
    }

    async updateDeliveryType(id: string, deliveryType: DeliveryType): Promise<Sale> {
        try {
            const updated = await this.net.updateDeliveryType(id, deliveryType);
            await db.sales.put(updated);
            return saleFromDTO(updated);
        } catch (error: any) {
            logger.error(
                `Error al actualizar entrega en Appwrite: ${error?.message ?? "desconocido"}`,
                error?.stack
            );
            throw error;
        }
    }

    async updateStockHoldApplied(id: string, value: boolean): Promise<Sale> {
        try {
            const updated = await this.net.updateStockHoldApplied(id, value);
            await db.sales.put(updated);
            return saleFromDTO(updated);
        } catch (error: any) {
            logger.error(
                `Error al marcar stock_hold_applied: ${error?.message ?? "desconocido"}`,
                error?.stack
            );
            throw error;
        }
    }

    /** Realtime: escribe Dexie desde el documento Appwrite (sin red). */
    async applyLocalSnapshot(raw: Record<string, unknown>): Promise<Sale | null> {
        try {
            const plain = toPlainSaleDoc(raw);
            if (!plain.$id) return null;
            await db.sales.put(plain);
            return saleFromDTO(plain);
        } catch (error: any) {
            logger.error(
                `applyLocalSnapshot sale falló: ${error?.message ?? "desconocido"}`,
                error?.stack
            );
            return null;
        }
    }
}
