<script lang="ts">
    /**
     * Ejemplo de integración de PromotionList en página principal
     * Demuestra cómo usar el sistema completo de promociones realtime
     */
    import PromotionList from "../feature/notification/presentation/screens/PromotionList.svelte";
    import { promotionStore } from "../feature/notification/presentation/viewmodel/promotion.store";
    import { logger } from "../infrastructure/presentation/util/logger.service";
    import { onMount } from "svelte";

    let statsText = "";

    onMount(async () => {
        logger.log("[PromotionsPage] Loading...");

        // Auto-cargar y suscribirse a eventos realtime
        try {
            await promotionStore.syncAll();
        } catch (error) {
            logger.error("[PromotionsPage] Error loading promotions:", error);
        }

        // Log de cambios en tiempo real
        const unsubscribe = promotionStore.subscribe((state) => {
            statsText = `
Totales: ${state.items.length}
Cargando: ${state.loading ? "Sí" : "No"}
Error: ${state.error || "Ninguno"}
            `.trim();
        });

        // Cleanup al desmontar
        return () => {
            unsubscribe();
            promotionStore.cleanup();
        };
    });
</script>

<div class="promotions-page">
    <!-- Header -->
    <header class="page-header">
        <h1>🎉 Promociones en Tiempo Real</h1>
        <p>Eventos síncronos desde Appwrite vía Pusher</p>
    </header>

    <!-- Status Bar -->
    <div class="status-bar">
        <pre>{statsText}</pre>
    </div>

    <!-- Main Content -->
    <main class="page-content">
        <PromotionList />
    </main>

    <!-- Footer Info -->
    <footer class="page-footer">
        <p>
            💡 Las promociones se actualizan automáticamente cuando se crean,
            editan o eliminan en el backend.
            La suscripción a Pusher se activa/desactiva según sea necesario.
        </p>
    </footer>
</div>

<style>
    .promotions-page {
        min-height: 100vh;
        background: #f5f5f5;
    }

    .page-header {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        padding: 30px 20px;
        text-align: center;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    .page-header h1 {
        margin: 0 0 10px 0;
        font-size: 32px;
    }

    .page-header p {
        margin: 0;
        font-size: 14px;
        opacity: 0.9;
    }

    .status-bar {
        background: white;
        padding: 15px 20px;
        border-bottom: 1px solid #ddd;
        font-family: monospace;
        font-size: 12px;
        color: #666;
        overflow-x: auto;
    }

    .status-bar pre {
        margin: 0;
        white-space: pre-wrap;
        word-wrap: break-word;
    }

    .page-content {
        padding: 20px;
        max-width: 1200px;
        margin: 0 auto;
    }

    .page-footer {
        padding: 30px 20px;
        text-align: center;
        background: white;
        border-top: 1px solid #ddd;
        font-size: 14px;
        color: #666;
        margin-top: 40px;
    }

    .page-footer p {
        margin: 0;
        line-height: 1.6;
    }

    @media (max-width: 768px) {
        .page-header h1 {
            font-size: 24px;
        }

        .page-header p {
            font-size: 12px;
        }
    }
</style>

