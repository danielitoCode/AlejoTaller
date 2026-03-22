import {PromotionNetRepository} from "./promotion.net.repository";
import type {PromotionRepository} from "../../domain/repository/PromotionRepository";
import type {Promotion} from "../../domain/entity/Promotion";
import {db} from "../../../../infrastructure/di/dexie.db";
import {promotionFromDTO, promotionToDTO} from "../mapper/Mappers";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";

export class PromotionOfflineFirstRepository implements PromotionRepository {
    constructor(private readonly net: PromotionNetRepository) {}

    async create(promotion: Promotion): Promise<void> {
        try {
            const created = await this.net.create(promotionToDTO(promotion))
            await db.promotions.put(created)
        } catch (error: any) {
            logger.error(
                `Error al crear promoción en Appwrite: ${error?.message ?? "desconocido"}`,
                error?.stack
            );
            throw error;
        }
    }

    async delete(id: string): Promise<void> {
        try {
            await this.net.delete(id)
            await db.promotions.delete(id)
        } catch (error: any) {
            logger.error(
                `Error al eliminar promoción en Appwrite: ${error?.message ?? "desconocido"}`,
                error?.stack
            );
            throw error;
        }
    }

    async getAll(): Promise<Promotion[]> {
        try {
            const remote = await this.net.getAll()
            await db.promotions.bulkPut(remote)
            return remote.map(promotionFromDTO)
        } catch {
            const local = await db.promotions.toArray()
            return local.map(promotionFromDTO)
        }
    }

    async getActive(now: number): Promise<Promotion[]> {
        try {
            const remote = await this.net.getActive(now)
            await db.promotions.bulkPut(remote)
            return remote.map(promotionFromDTO)
        } catch {
            const local = await db.promotions
                .filter((it) => it.validFromEpochMillis <= now && it.validUntilEpochMillis >= now)
                .toArray()
            return local.map(promotionFromDTO)
        }
    }

    async sync(): Promise<void> {
        const remote = await this.net.getAll()
        await db.promotions.clear()
        await db.promotions.bulkPut(remote)
    }
}
