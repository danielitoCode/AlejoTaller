<script lang="ts">
    import { Button } from "m3-svelte";
    import type { Category } from "../../../category/domain/entity/Category";

    export let categories: Category[] = [];
    export let selectedCategoryId: string | null = null;
    export let onCategorySelected: (categoryId: string | null) => void = () => {};
</script>

<div class="category-filter">
    <div class="filter-scroll">
        <button
            class={`filter-chip ${!selectedCategoryId ? "active" : ""}`}
            type="button"
            on:click={() => onCategorySelected(null)}
        >
            Todos
        </button>

        {#each categories as category}
            <button
                class={`filter-chip ${selectedCategoryId === category.id ? "active" : ""}`}
                type="button"
                on:click={() => onCategorySelected(category.id)}
            >
                {category.name}
            </button>
        {/each}
    </div>
</div>

<style>
    .category-filter {
        width: 100%;
        overflow: hidden;
    }

    .filter-scroll {
        display: flex;
        gap: 8px;
        overflow-x: auto;
        overflow-y: hidden;
        padding: 0;
        scroll-behavior: smooth;
        scrollbar-width: none;
        -ms-overflow-style: none;
    }

    .filter-scroll::-webkit-scrollbar {
        display: none;
    }

    .filter-chip {
        padding: 8px 16px;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 20px;
        background: var(--md-sys-color-surface);
        color: var(--md-sys-color-on-surface-variant);
        cursor: pointer;
        font-size: 0.9rem;
        font-weight: 500;
        white-space: nowrap;
        flex-shrink: 0;
        transition: all 0.2s ease;
    }

    .filter-chip:hover {
        background: var(--md-sys-color-surface-container);
        border-color: var(--md-sys-color-outline);
    }

    .filter-chip.active {
        background: var(--md-sys-color-primary-container);
        color: var(--md-sys-color-on-primary-container);
        border-color: var(--md-sys-color-primary);
    }

    .filter-chip:active {
        transform: scale(0.95);
    }
</style>
