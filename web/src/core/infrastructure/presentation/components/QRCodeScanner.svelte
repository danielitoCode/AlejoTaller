<script lang="ts">
    import { parseSaleQRData } from '../util/qr.service';

    let qrInput: string = '';
    let parsedData: ReturnType<typeof parseSaleQRData> | null = null;
    let error: string | null = null;

    function handleScan() {
        try {
            error = null;
            if (!qrInput.trim()) {
                error = 'Ingresa un código QR válido';
                return;
            }
            parsedData = parseSaleQRData(qrInput);
        } catch (err) {
            error = err instanceof Error ? err.message : 'Error al procesar el QR';
            parsedData = null;
        }
    }

    function handleKeyDown(e: KeyboardEvent) {
        if (e.key === 'Enter') {
            handleScan();
        }
    }
</script>

<div class="qr-scanner">
    <div class="input-group">
        <input
            type="text"
            value={qrInput}
            on:input={(e) => (qrInput = e.currentTarget.value)}
            on:keydown={handleKeyDown}
            placeholder="Escanea o pega el código QR aquí"
            class="qr-input"
            spellcheck="false"
            autocomplete="off"
        />
        <button type="button" on:click={handleScan} class="scan-btn">Procesar</button>
    </div>

    {#if error}
        <div class="error-message">
            <span>⚠️ {error}</span>
        </div>
    {/if}

    {#if parsedData}
        <div class="result-card">
            <div class="result-section">
                <h3>ID de Venta</h3>
                <code>{parsedData.saleId}</code>
            </div>

            <div class="result-section">
                <h3>Monto Total</h3>
                <strong>${parsedData.amount.toFixed(2)} CUP</strong>
            </div>

            <div class="result-section">
                <h3>Productos ({parsedData.items.length})</h3>
                <div class="items-list">
                    {#each parsedData.items as item (item.productId)}
                        <div class="item-card">
                            <div>
                                <span class="product-id">Producto: {item.productId}</span>
                                <span class="quantity">Cantidad: {item.quantity}</span>
                            </div>
                            <span class="price">${item.price.toFixed(2)}</span>
                        </div>
                    {/each}
                </div>
            </div>
        </div>
    {/if}
</div>

<style>
    .qr-scanner {
        display: grid;
        gap: 16px;
        padding: 16px;
    }

    .input-group {
        display: grid;
        grid-template-columns: 1fr auto;
        gap: 8px;
    }

    .qr-input {
        padding: 12px 14px;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 12px;
        font-family: monospace;
        font-size: 14px;
        background: var(--md-sys-color-surface-container);
        color: var(--md-sys-color-on-surface);
    }

    .qr-input:focus {
        outline: none;
        border-color: var(--md-sys-color-primary);
        box-shadow: 0 0 0 3px color-mix(in srgb, var(--md-sys-color-primary) 20%, transparent);
    }

    .scan-btn {
        padding: 12px 24px;
        border: none;
        border-radius: 12px;
        background: var(--md-sys-color-primary);
        color: var(--md-sys-color-on-primary);
        font-weight: 600;
        cursor: pointer;
        transition: background 0.2s;
    }

    .scan-btn:hover {
        background: var(--md-sys-color-primary-container);
    }

    .error-message {
        padding: 12px 16px;
        background: color-mix(in srgb, var(--md-sys-color-error) 14%, transparent);
        color: var(--md-sys-color-error);
        border-radius: 12px;
        font-size: 14px;
    }

    .result-card {
        display: grid;
        gap: 16px;
        padding: 16px;
        background: var(--md-sys-color-surface-container);
        border-radius: 16px;
        border: 1px solid var(--md-sys-color-outline-variant);
    }

    .result-section {
        display: grid;
        gap: 8px;
    }

    .result-section h3 {
        margin: 0;
        font-size: 12px;
        font-weight: 600;
        text-transform: uppercase;
        color: var(--md-sys-color-on-surface-variant);
        letter-spacing: 0.04em;
    }

    code {
        padding: 8px 12px;
        background: var(--md-sys-color-surface);
        border-radius: 8px;
        font-family: 'Courier New', monospace;
        font-size: 13px;
        color: var(--md-sys-color-primary);
        word-break: break-all;
    }

    strong {
        font-size: 18px;
        color: var(--md-sys-color-primary);
    }

    .items-list {
        display: grid;
        gap: 10px;
    }

    .item-card {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px;
        background: var(--md-sys-color-surface);
        border-radius: 12px;
        border: 1px solid var(--md-sys-color-outline-variant);
    }

    .item-card div {
        display: grid;
        gap: 4px;
    }

    .product-id {
        font-weight: 600;
        color: var(--md-sys-color-on-surface);
    }

    .quantity {
        font-size: 13px;
        color: var(--md-sys-color-on-surface-variant);
    }

    .price {
        font-weight: 700;
        color: var(--md-sys-color-primary);
    }
</style>

