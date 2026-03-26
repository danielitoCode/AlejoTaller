<script lang="ts">
    import { onMount } from 'svelte';
    import type { Sale } from '../../../feature/sale/domain/entity/Sale';
    import { generateSaleQRDataUrl } from '../util/qr.service';

    export let sale: Sale;

    let qrImage: string = '';
    let loading = true;
    let error: string | null = null;

    onMount(async () => {
        try {
            qrImage = await generateSaleQRDataUrl(sale);
        } catch (err) {
            error = err instanceof Error ? err.message : 'Error generando QR';
            console.error('QR generation error:', err);
        } finally {
            loading = false;
        }
    });
</script>

<div class="qr-container">
    {#if loading}
        <div class="qr-box loading">
            <span>Generando QR...</span>
        </div>
    {:else if error}
        <div class="qr-box error">
            <span>{error}</span>
        </div>
    {:else if qrImage}
        <img src={qrImage} alt="QR Code for sale {sale.id}" class="qr-image" />
    {/if}
</div>

<style>
    .qr-container {
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .qr-box {
        min-height: 200px;
        min-width: 200px;
        border-radius: 16px;
        background: white;
        color: #111827;
        display: grid;
        place-items: center;
        gap: 8px;
        border: 2px solid #e5e7eb;
    }

    .qr-box.loading {
        background: #f9fafb;
        color: #6b7280;
    }

    .qr-box.error {
        background: #fef2f2;
        color: #dc2626;
        border-color: #fca5a5;
    }

    .qr-image {
        width: 200px;
        height: 200px;
        image-rendering: pixelated;
    }
</style>

