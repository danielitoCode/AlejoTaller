<script lang="ts">
    import { onMount } from "svelte";
    import { promotionStore } from "../viewmodel/promotion.store";
    import type { Promotion } from "../../domain/entity/Promotion";
    import { logger } from "../../../../infrastructure/presentation/util/logger.service";

    let selectedPromotion: Promotion | null = null;
    let showModal = false;

    onMount(async () => {
        try {
            logger.log("[PromotionList] Mounting - Loading promotions");
            await promotionStore.syncAll();
        } catch (error) {
            logger.error("[PromotionList] Error loading promotions:", error);
        }

        return () => {
            // Cleanup on unmount
            promotionStore.cleanup();
        };
    });

    function handleSelectPromotion(promo: Promotion) {
        selectedPromotion = promo;
        showModal = true;
    }

    function handleCloseModal() {
        selectedPromotion = null;
        showModal = false;
    }

    async function handleDeletePromotion(promotionId: string) {
        if (confirm("¿Estás seguro de que deseas eliminar esta promoción?")) {
            try {
                await promotionStore.deletePromotion(promotionId);
                logger.log("[PromotionList] Promotion deleted:", promotionId);
            } catch (error) {
                logger.error("[PromotionList] Error deleting promotion:", error);
            }
        }
    }

    function isPromotionActive(promo: Promotion): boolean {
        const now = Date.now();
        return now >= promo.validFromEpochMillis && now <= promo.validUntilEpochMillis;
    }

    function formatPrice(price: number | null | undefined): string {
        if (!price) return "-";
        return new Intl.NumberFormat("es-CO", {
            style: "currency",
            currency: "COP",
            minimumFractionDigits: 0
        }).format(price);
    }

    function formatDate(epochMillis: number): string {
        return new Date(epochMillis).toLocaleDateString("es-CO", {
            year: "numeric",
            month: "short",
            day: "numeric"
        });
    }
</script>

<div class="promotion-list-container">
    <!-- Header -->
    <div class="promotion-header">
        <h1>Promociones</h1>
        <div class="stats">
            <span>Total: <strong>{$promotionStore.items.length}</strong></span>
            <span>Activas: <strong>{$activePromotions.length}</strong></span>
        </div>
    </div>

    <!-- Loading State -->
    {#if $promotionStore.loading}
        <div class="loading">
            <p>Cargando promociones...</p>
        </div>
    {/if}

    <!-- Error State -->
    {#if $promotionStore.error}
        <div class="error">
            <p>Error: {$promotionStore.error}</p>
            <button on:click={() => promotionStore.syncAll()}>
                Reintentar
            </button>
        </div>
    {/if}

    <!-- Empty State -->
    {#if !$promotionStore.loading && $promotionStore.items.length === 0}
        <div class="empty-state">
            <p>No hay promociones disponibles</p>
        </div>
    {/if}

    <!-- Promotions Grid -->
    {#if $promotionStore.items.length > 0}
        <div class="promotions-grid">
            {#each $promotionStore.items as promo (promo.id)}
                <div class={`promotion-card ${isPromotionActive(promo) ? "active" : "inactive"}`}>
                    <!-- Image -->
                    {#if promo.imageUrl}
                        <div class="promo-image">
                            <img src={promo.imageUrl} alt={promo.title} />
                            {#if isPromotionActive(promo)}
                                <span class="badge active-badge">Activa</span>
                            {/if}
                        </div>
                    {/if}

                    <!-- Content -->
                    <div class="promo-content">
                        <h3>{promo.title}</h3>
                        <p class="description">{promo.message}</p>

                        <!-- Prices -->
                        <div class="prices">
                            {#if promo.oldPrice && promo.oldPrice > 0}
                                <span class="old-price">
                                    <del>{formatPrice(promo.oldPrice)}</del>
                                </span>
                            {/if}
                            <span class="current-price">
                                {formatPrice(promo.currentPrice)}
                            </span>
                        </div>

                        <!-- Validity Period -->
                        <div class="validity">
                            <span class="validity-date">
                                {formatDate(promo.validFromEpochMillis)} - {formatDate(promo.validUntilEpochMillis)}
                            </span>
                        </div>

                        <!-- Actions -->
                        <div class="actions">
                            <button
                                class="btn-detail"
                                on:click={() => handleSelectPromotion(promo)}
                            >
                                Ver Detalles
                            </button>
                            <button
                                class="btn-delete"
                                on:click={() => handleDeletePromotion(promo.id)}
                            >
                                Eliminar
                            </button>
                        </div>
                    </div>
                </div>
            {/each}
        </div>
    {/if}

    <!-- Detail Modal -->
    {#if showModal && selectedPromotion}
        <div class="modal-overlay" on:click={handleCloseModal}>
            <div class="modal" on:click|stopPropagation>
                <div class="modal-header">
                    <h2>{selectedPromotion.title}</h2>
                    <button class="close-btn" on:click={handleCloseModal}>×</button>
                </div>

                <div class="modal-body">
                    {#if selectedPromotion.imageUrl}
                        <img src={selectedPromotion.imageUrl} alt={selectedPromotion.title} class="modal-image" />
                    {/if}

                    <p class="modal-message">{selectedPromotion.message}</p>

                    <div class="modal-info">
                        <div class="info-item">
                            <span class="label">Precio Original:</span>
                            <span class="value">{formatPrice(selectedPromotion.oldPrice)}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Precio Promoción:</span>
                            <span class="value">{formatPrice(selectedPromotion.currentPrice)}</span>
                        </div>
                        {#if selectedPromotion.oldPrice && selectedPromotion.currentPrice}
                            <div class="info-item">
                                <span class="label">Descuento:</span>
                                <span class="value discount">
                                    {Math.round(((selectedPromotion.oldPrice - selectedPromotion.currentPrice) / selectedPromotion.oldPrice) * 100)}%
                                </span>
                            </div>
                        {/if}
                        <div class="info-item">
                            <span class="label">Válida desde:</span>
                            <span class="value">{formatDate(selectedPromotion.validFromEpochMillis)}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Válida hasta:</span>
                            <span class="value">{formatDate(selectedPromotion.validUntilEpochMillis)}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Estado:</span>
                            <span class="value {isPromotionActive(selectedPromotion) ? 'active' : 'inactive'}">
                                {isPromotionActive(selectedPromotion) ? "Activa" : "Inactiva"}
                            </span>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn-close" on:click={handleCloseModal}>
                        Cerrar
                    </button>
                </div>
            </div>
        </div>
    {/if}
</div>

<style>
    .promotion-list-container {
        padding: 20px;
        max-width: 1200px;
        margin: 0 auto;
    }

    .promotion-header {
        margin-bottom: 30px;
    }

    .promotion-header h1 {
        margin: 0 0 10px 0;
        font-size: 28px;
        color: #333;
    }

    .stats {
        display: flex;
        gap: 20px;
        font-size: 14px;
        color: #666;
    }

    .loading,
    .error,
    .empty-state {
        padding: 40px 20px;
        text-align: center;
        background: #f5f5f5;
        border-radius: 8px;
        margin: 20px 0;
    }

    .error {
        background: #fee;
        color: #c33;
    }

    .error button {
        margin-top: 15px;
        padding: 8px 16px;
        background: #c33;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
    }

    .promotions-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 20px;
        margin: 20px 0;
    }

    .promotion-card {
        background: white;
        border: 1px solid #ddd;
        border-radius: 8px;
        overflow: hidden;
        transition: all 0.3s ease;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    .promotion-card:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        transform: translateY(-2px);
    }

    .promotion-card.inactive {
        opacity: 0.7;
    }

    .promo-image {
        position: relative;
        width: 100%;
        height: 180px;
        overflow: hidden;
        background: #f0f0f0;
    }

    .promo-image img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    .badge {
        position: absolute;
        top: 10px;
        right: 10px;
        padding: 4px 12px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: bold;
    }

    .active-badge {
        background: #4caf50;
        color: white;
    }

    .promo-content {
        padding: 15px;
    }

    .promo-content h3 {
        margin: 0 0 8px 0;
        font-size: 16px;
        color: #333;
    }

    .description {
        margin: 0 0 12px 0;
        font-size: 13px;
        color: #666;
        line-height: 1.4;
    }

    .prices {
        display: flex;
        gap: 10px;
        margin: 12px 0;
        align-items: center;
    }

    .old-price {
        color: #999;
        font-size: 12px;
    }

    .current-price {
        font-size: 18px;
        font-weight: bold;
        color: #2196f3;
    }

    .validity {
        margin: 10px 0;
    }

    .validity-date {
        font-size: 12px;
        color: #666;
    }

    .actions {
        display: flex;
        gap: 8px;
        margin-top: 12px;
    }

    .btn-detail,
    .btn-delete {
        flex: 1;
        padding: 8px 12px;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 12px;
        font-weight: 500;
        transition: all 0.2s ease;
    }

    .btn-detail {
        background: #2196f3;
        color: white;
    }

    .btn-detail:hover {
        background: #1976d2;
    }

    .btn-delete {
        background: #f44336;
        color: white;
    }

    .btn-delete:hover {
        background: #d32f2f;
    }

    /* Modal Styles */
    .modal-overlay {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1000;
    }

    .modal {
        background: white;
        border-radius: 8px;
        max-width: 500px;
        width: 90%;
        max-height: 80vh;
        overflow-y: auto;
    }

    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px;
        border-bottom: 1px solid #eee;
    }

    .modal-header h2 {
        margin: 0;
        font-size: 20px;
        color: #333;
    }

    .close-btn {
        background: none;
        border: none;
        font-size: 28px;
        cursor: pointer;
        color: #666;
    }

    .modal-body {
        padding: 20px;
    }

    .modal-image {
        width: 100%;
        height: 250px;
        object-fit: cover;
        border-radius: 4px;
        margin-bottom: 15px;
    }

    .modal-message {
        margin: 0 0 20px 0;
        line-height: 1.6;
        color: #666;
    }

    .modal-info {
        display: grid;
        gap: 12px;
    }

    .info-item {
        display: flex;
        justify-content: space-between;
        padding: 10px 0;
        border-bottom: 1px solid #f0f0f0;
    }

    .info-item .label {
        font-weight: 500;
        color: #666;
    }

    .info-item .value {
        color: #333;
        font-weight: 500;
    }

    .info-item .value.active {
        color: #4caf50;
    }

    .info-item .value.inactive {
        color: #999;
    }

    .info-item .value.discount {
        color: #f44336;
        font-weight: bold;
    }

    .modal-footer {
        padding: 15px 20px;
        border-top: 1px solid #eee;
        text-align: right;
    }

    .btn-close {
        padding: 10px 20px;
        background: #2196f3;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-weight: 500;
    }

    .btn-close:hover {
        background: #1976d2;
    }

    @media (max-width: 768px) {
        .promotions-grid {
            grid-template-columns: 1fr;
        }

        .modal {
            width: 95%;
            max-height: 90vh;
        }
    }
</style>

