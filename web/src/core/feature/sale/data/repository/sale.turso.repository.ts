import type { SaleDTO } from "../dto/SaleDTO";
import type { Models } from "appwrite";
import { getTursoClient } from "../../../../infrastructure/turso/turso.client";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";

type SaleRow = {
    id: string;
    date_iso: string;
    amount: number;
    verified: string;
    currency: string;
    user_id: string;
    delivery_type: string | null;
    delivery_province: string | null;
    delivery_municipality: string | null;
    delivery_main_street: string | null;
    delivery_between_streets: string | null;
    delivery_phone: string | null;
    delivery_house_number: string | null;
    delivery_reference_name: string | null;
    sale_type: string | null;
    stock_hold_applied: number | null;
    products_json: string | null;
    created_at: string | null;
    updated_at: string | null;
};

function newId(prefix: string): string {
    return `${prefix}-${Math.random().toString(36).slice(2, 10)}${Date.now().toString(36).slice(-4)}`;
}

function deliveryJsonFromRow(row: SaleRow): string | null {
    if (!row.delivery_type || row.delivery_type === "PICKUP") {
        if (!row.delivery_province && !row.delivery_main_street) return null;
    }
    if (!row.delivery_province && !row.delivery_main_street && !row.delivery_phone) {
        return null;
    }
    return JSON.stringify({
        province: row.delivery_province ?? "",
        municipality: row.delivery_municipality ?? "",
        mainStreet: row.delivery_main_street ?? "",
        betweenStreets: row.delivery_between_streets ?? null,
        phone: row.delivery_phone ?? "",
        houseNumber: row.delivery_house_number ?? "",
        referenceName: row.delivery_reference_name ?? null,
    });
}

function rowToDto(row: SaleRow): SaleDTO {
    return {
        $id: row.id,
        $createdAt: row.created_at ?? "",
        $updatedAt: row.updated_at ?? "",
        $permissions: [],
        $databaseId: "turso",
        $collectionId: "sales",
        $sequence: 0,
        date: row.date_iso ?? "",
        amount: Number(row.amount ?? 0) || 0,
        buy_state: row.verified ?? "UNVERIFIED",
        currency: row.currency ?? "CUP",
        products: row.products_json ?? "[]",
        user_id: row.user_id ?? "",
        delivery_type: row.delivery_type,
        delivery_address: deliveryJsonFromRow(row),
        sale_type: row.sale_type,
        stock_hold_applied: row.stock_hold_applied === 1,
    } as SaleDTO;
}

function parseDelivery(raw: string | null | undefined): {
    province: string | null;
    municipality: string | null;
    main_street: string | null;
    between_streets: string | null;
    phone: string | null;
    house_number: string | null;
    reference_name: string | null;
} {
    const empty = {
        province: null as string | null,
        municipality: null as string | null,
        main_street: null as string | null,
        between_streets: null as string | null,
        phone: null as string | null,
        house_number: null as string | null,
        reference_name: null as string | null,
    };
    if (!raw) return empty;
    try {
        const o = JSON.parse(raw) as Record<string, unknown>;
        return {
            province: (o.province as string) ?? null,
            municipality: (o.municipality as string) ?? null,
            main_street: (o.mainStreet as string) ?? null,
            between_streets: (o.betweenStreets as string) ?? null,
            phone: (o.phone as string) ?? null,
            house_number: (o.houseNumber as string) ?? null,
            reference_name: (o.referenceName as string) ?? null,
        };
    } catch {
        return empty;
    }
}

const SELECT_SALES = `id, date_iso, amount, verified, currency, user_id, delivery_type,
    delivery_province, delivery_municipality, delivery_main_street, delivery_between_streets,
    delivery_phone, delivery_house_number, delivery_reference_name, sale_type,
    stock_hold_applied, products_json, created_at, updated_at`;

/**
 * Fuente remota Turso para ventas (CRUD cliente).
 * Sustituye SaleNetRepository (Appwrite) cuando VITE_DATA_PROVIDER=turso.
 */
export class SaleTursoRepository {
    async getAll(): Promise<SaleDTO[]> {
        logger.info("[turso] Cargando ventas (all)");
        const db = getTursoClient();
        const rs = await db.execute(
            `SELECT ${SELECT_SALES} FROM sales ORDER BY date_iso DESC, created_at DESC`,
        );
        return rs.rows.map((r) => rowToDto(r as unknown as SaleRow));
    }

    async getByUser(userId: string): Promise<SaleDTO[]> {
        logger.info(`[turso] Cargando ventas user=${userId.slice(0, 12)}…`);
        const db = getTursoClient();
        const rs = await db.execute({
            sql: `SELECT ${SELECT_SALES} FROM sales WHERE user_id = ? ORDER BY date_iso DESC, created_at DESC`,
            args: [userId],
        });
        return rs.rows.map((r) => rowToDto(r as unknown as SaleRow));
    }

    async create(
        data: Omit<SaleDTO, keyof Models.Document> | Record<string, unknown>,
    ): Promise<SaleDTO> {
        const db = getTursoClient();
        const id = String((data as any).$id || (data as any).id || newId("sale"));
        const dateIso = String((data as any).date ?? new Date().toISOString());
        const amount = Number((data as any).amount ?? 0) || 0;
        const verified = String((data as any).buy_state ?? "UNVERIFIED");
        const currency = String((data as any).currency ?? "CUP");
        const userId = String((data as any).user_id ?? "");
        const deliveryType = ((data as any).delivery_type as string | null) ?? null;
        const saleType = ((data as any).sale_type as string | null) ?? null;
        const stockHold = (data as any).stock_hold_applied === true ? 1 : 0;
        let productsJson = (data as any).products;
        if (typeof productsJson !== "string") {
            productsJson = JSON.stringify(productsJson ?? []);
        }
        const del = parseDelivery((data as any).delivery_address as string | null);

        logger.info(`[turso] Creando venta id=${id}`);
        await db.execute({
            sql: `INSERT INTO sales (
                id, date_iso, amount, verified, currency, user_id, delivery_type,
                delivery_province, delivery_municipality, delivery_main_street,
                delivery_between_streets, delivery_phone, delivery_house_number,
                delivery_reference_name, sale_type, stock_hold_applied, products_json,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'))`,
            args: [
                id,
                dateIso,
                amount,
                verified,
                currency,
                userId,
                deliveryType,
                del.province,
                del.municipality,
                del.main_street,
                del.between_streets,
                del.phone,
                del.house_number,
                del.reference_name,
                saleType,
                stockHold,
                productsJson,
            ],
        });

        try {
            const items = JSON.parse(String(productsJson)) as Array<Record<string, unknown>>;
            if (Array.isArray(items)) {
                let order = 0;
                for (const it of items) {
                    const productId = String(it.productId ?? it.product_id ?? "");
                    if (!productId) continue;
                    const qty = Math.max(1, Math.floor(Number(it.quantity ?? 1)));
                    const unit = Number(it.unitPrice ?? it.price ?? 0) || 0;
                    const list = it.listUnitPrice != null ? Number(it.listUnitPrice) : null;
                    await db.execute({
                        sql: `INSERT INTO sale_items (
                            id, sale_id, product_id, product_name, quantity,
                            unit_price, list_unit_price, price_legacy, line_order
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
                        args: [
                            newId("si"),
                            id,
                            productId,
                            (it.productName as string) ?? null,
                            qty,
                            unit,
                            list,
                            unit,
                            order++,
                        ],
                    });
                }
            }
        } catch (e) {
            logger.warn(`[turso] sale_items partial fail: ${e instanceof Error ? e.message : e}`);
        }

        return this.getById(id);
    }

    async getById(id: string): Promise<SaleDTO> {
        const db = getTursoClient();
        const rs = await db.execute({
            sql: `SELECT ${SELECT_SALES} FROM sales WHERE id = ? LIMIT 1`,
            args: [id],
        });
        const row = rs.rows[0] as unknown as SaleRow | undefined;
        if (!row) throw new Error(`Sale not found: ${id}`);
        return rowToDto(row);
    }

    async updateVerified(id: string, verified: string): Promise<SaleDTO> {
        const db = getTursoClient();
        await db.execute({
            sql: `UPDATE sales SET verified = ?, updated_at = datetime('now') WHERE id = ?`,
            args: [verified, id],
        });
        return this.getById(id);
    }

    async updateDeliveryType(id: string, deliveryType: string): Promise<SaleDTO> {
        const db = getTursoClient();
        await db.execute({
            sql: `UPDATE sales SET delivery_type = ?, updated_at = datetime('now') WHERE id = ?`,
            args: [deliveryType, id],
        });
        return this.getById(id);
    }

    async updateStockHoldApplied(id: string, value: boolean): Promise<SaleDTO> {
        const db = getTursoClient();
        await db.execute({
            sql: `UPDATE sales SET stock_hold_applied = ?, updated_at = datetime('now') WHERE id = ?`,
            args: [value ? 1 : 0, id],
        });
        return this.getById(id);
    }
}
