<script lang="ts">
    import type { Promotion } from "../../domain/entity/Promotion";
    import { isPromotionActive } from "../../domain/entity/Promotion";

    export let promotion: Promotion;
    export let onSelect: ((promo: Promotion) => void) | null = null;
    export let onDelete: ((id: string) => void) | null = null;

    function handleClick() {
        if (onSelect) {
            onSelect(promotion);
        }
    }

    function handleDelete(event: MouseEvent) {
        event.stopPropagation();
        if (onDelete && confirm("¿Eliminar esta promoción?")) {
            onDelete(promotion.id);
        }
    }

    function formatPrice(price: number | null | undefined): string {
        if (!price) return "-";
        return new Intl.NumberFormat("es-CO", {
            style: "currency",
            currency: "COP",
            minimumFractionDigits: 0
        }).format(price);
    }

    const isActive = isPromotionActive(promotion, Date.now());
    const discount = promotion.oldPrice && promotion.currentPrice
        ? Math.round(((promotion.oldPrice - promotion.currentPrice) / promotion.oldPrice) * 100)
        : 0;
</script>

<div class={`promotion-item ${isActive ? "active" : "inactive"}`} on:click={handleClick} role="button" tabindex="0">
    {#if promotion.imageUrl}
        <div class="item-image">
            <img src={promotion.imageUrl} alt={promotion.title} />
            {#if discount > 0}
                <span class="discount-badge">-{discount}%</span>
            {/if}
        </div>
    {/if}

    <div class="item-content">
        <h4>{promotion.title}</h4>
        <p class="item-message">{promotion.message}</p>

        <div class="item-prices">
            {#if promotion.oldPrice && promotion.oldPrice > 0}
                <span class="old-price">
                    <del>{formatPrice(promotion.oldPrice)}</del>
                </span>
            {/if}
            <span class="new-price">{formatPrice(promotion.currentPrice)}</span>
        </div>

        {#if onDelete}
            <button class="delete-btn" on:click={handleDelete} title="Eliminar">
                🗑️
            </button>
        {/if}
    </div>
</div>

<style>
    .promotion-item {
        display: flex;
        gap: 12px;
        padding: 12px;
        background: white;
        border: 1px solid #ddd;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.2s ease;
        position: relative;
    }

    .promotion-item:hover {
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        transform: translateY(-1px);
    }

    .promotion-item.inactive {
        opacity: 0.6;
    }

    .item-image {
        position: relative;
        width: 80px;
        height: 80px;
        flex-shrink: 0;
        overflow: hidden;
        border-radius: 4px;
        background: #f0f0f0;
    }

    .item-image img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    .discount-badge {
        position: absolute;
        top: 4px;
        right: 4px;
        background: #f44336;
        color: white;
        padding: 2px 6px;
        border-radius: 3px;
        font-size: 11px;
        font-weight: bold;
    }

    .item-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        min-width: 0;
    }

    .item-content h4 {
        margin: 0 0 4px 0;
        font-size: 14px;
        color: #333;
        font-weight: 600;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .item-message {
        margin: 0;
        font-size: 12px;
        color: #666;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 1;
        -webkit-box-orient: vertical;
    }

    .item-prices {
        display: flex;
        gap: 8px;
        align-items: center;
        margin-top: 4px;
    }

    .old-price {
        font-size: 11px;
        color: #999;
    }

    .new-price {
        font-size: 14px;
        font-weight: bold;
        color: #2196f3;
    }

    .delete-btn {
        position: absolute;
        top: 4px;
        right: 4px;
        background: #f44336;
        color: white;
        border: none;
        width: 24px;
        height: 24px;
        border-radius: 50%;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        opacity: 0;
        transition: opacity 0.2s ease;
    }

    .promotion-item:hover .delete-btn {
        opacity: 1;
    }

    .delete-btn:hover {
        background: #d32f2f;
    }
</style>

