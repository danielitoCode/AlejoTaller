package com.elitec.alejotaller.feature.sale.domain.repository

/**
 * Puerto de dominio que abstrae la pasarela de pago.
 * La capa de dominio depende de esta interfaz, nunca de la implementación concreta.
 */
interface PaymentGateway {
    /**
     * Crea un link de pago externo para una venta.
     *
     * @param saleId      Identificador único de la venta.
     * @param amount      Monto a cobrar.
     * @param description Descripción del pedido para mostrar al pagador.
     * @return URL de checkout donde el usuario completa el pago.
     */
    suspend fun createCheckoutUrl(
        saleId: String,
        amount: Double,
        description: String
    ): Result<String>
}