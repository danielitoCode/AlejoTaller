<script lang="ts">
    import { onMount } from 'svelte';
    import { activeAlerts, saleAlertStore } from '../../../feature/sale/presentation/viewmodel/sale-alert.store';
    import { sessionStore } from '../../../feature/auth/presentation/viewmodel/session.store';
    import type { NavController } from '../../../../lib/navigation/NavController';
    import { reservationDetail } from '../navigation/nested.router';

    export let navController: NavController;

    let isAuthenticated = false;

    onMount(async () => {
        const user = await sessionStore.getCurrentUser().catch(() => null);
        isAuthenticated = !!user?.$id;
    });

    async function handleNavigateToSale(saleId: string) {
        const user = await sessionStore.getCurrentUser().catch(() => null);

        if (user?.$id) {
            // Usuario autenticado, navegar directamente
            navController.navigate(reservationDetail.path, { id: saleId });
        } else {
            // Usuario no autenticado, mostrar login (dejar implementar en app)
            // Aquí se podría show modal o dispatch evento
            console.log('Usuario debe autenticarse para ver pedido', saleId);
        }

        // Remover alerta después de procesar
        saleAlertStore.removeAlert(saleId);
    }

    function getDismissLabel(decision: string): string {
        return decision === 'confirmed' ? 'Cerrar' : 'Entendido';
    }

    function getStatusLabel(decision: string): string {
        return decision === 'confirmed' ? '✅ Confirmado' : '❌ Rechazado';
    }
</script>

<div class="alerts-container">
    {#each $activeAlerts as alert (alert.saleId)}
        <div
            class="alert-toast {alert.decision}"
            role="status"
            aria-live="polite"
            aria-label="{getStatusLabel(alert.decision)} - Pedido {alert.saleId}"
        >
            <div class="alert-content">
                <div class="alert-header">
                    <span class="alert-status">{getStatusLabel(alert.decision)}</span>
                </div>

                <p class="alert-message">
                    {#if alert.decision === 'confirmed'}
                        Tu pedido ha sido confirmado
                        {#if alert.amount}
                            por ${alert.amount.toFixed(2)} CUP
                        {/if}
                    {:else}
                        Tu pedido ha sido rechazado
                    {/if}
                </p>

                {#if alert.productCount}
                    <p class="alert-detail">{alert.productCount} producto{alert.productCount !== 1 ? 's' : ''}</p>
                {/if}
            </div>

            <div class="alert-actions">
                <button
                    type="button"
                    class="btn-view"
                    on:click={() => handleNavigateToSale(alert.saleId)}
                    aria-label="Ver detalles del pedido"
                >
                    Ver
                </button>
                <button
                    type="button"
                    class="btn-dismiss"
                    on:click={() => saleAlertStore.removeAlert(alert.saleId)}
                    aria-label="Cerrar notificación"
                >
                    ✕
                </button>
            </div>
        </div>
    {/each}
</div>

<style>
    .alerts-container {
        position: fixed;
        top: 16px;
        right: 16px;
        display: grid;
        gap: 12px;
        max-width: 360px;
        z-index: 2000;
        pointer-events: none;
    }

    .alert-toast {
        display: grid;
        grid-template-columns: 1fr auto;
        gap: 12px;
        align-items: center;
        padding: 14px 16px;
        border-radius: 12px;
        backdrop-filter: blur(4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
        pointer-events: auto;
        animation: slideIn 0.3s ease-out;
    }

    .alert-toast.confirmed {
        background: linear-gradient(135deg, rgba(34, 197, 94, 0.95), rgba(34, 197, 94, 0.85));
        border-left: 4px solid #22c55e;
        color: white;
    }

    .alert-toast.rejected {
        background: linear-gradient(135deg, rgba(239, 68, 68, 0.95), rgba(239, 68, 68, 0.85));
        border-left: 4px solid #ef4444;
        color: white;
    }

    .alert-content {
        display: grid;
        gap: 6px;
    }

    .alert-header {
        display: flex;
        gap: 8px;
        align-items: center;
    }

    .alert-status {
        font-weight: 700;
        font-size: 14px;
    }

    .alert-message {
        margin: 0;
        font-size: 14px;
        font-weight: 500;
        line-height: 1.3;
    }

    .alert-detail {
        margin: 0;
        font-size: 12px;
        opacity: 0.9;
    }

    .alert-actions {
        display: flex;
        gap: 8px;
    }

    .btn-view,
    .btn-dismiss {
        padding: 8px 12px;
        border: none;
        border-radius: 8px;
        font-size: 12px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s;
    }

    .btn-view {
        background: rgba(255, 255, 255, 0.25);
        color: white;
    }

    .btn-view:hover {
        background: rgba(255, 255, 255, 0.35);
    }

    .btn-dismiss {
        background: transparent;
        color: white;
        padding: 4px 8px;
        min-width: 28px;
        opacity: 0.7;
    }

    .btn-dismiss:hover {
        opacity: 1;
    }

    @keyframes slideIn {
        from {
            transform: translateX(400px);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }

    @media (max-width: 600px) {
        .alerts-container {
            left: 8px;
            right: 8px;
            max-width: none;
        }

        .alert-toast {
            grid-template-columns: 1fr;
        }

        .alert-actions {
            justify-self: end;
        }
    }
</style>
