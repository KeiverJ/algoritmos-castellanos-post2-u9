package edu.udes.algoritmos.u9;

import java.util.Objects;

/**
 * Representa una solicitud de pago.
 */
public class PaymentRequest {
    private final String customerId;
    private final String currency;
    private final long amountCents;

    /**
     * Crea una solicitud de pago.
     *
     * @param customerId identificador del cliente
     * @param currency moneda del pago
     * @param amountCents monto en centavos
     */
    public PaymentRequest(String customerId, String currency, long amountCents) {
        this.customerId = Objects.requireNonNull(customerId, "customerId es obligatorio");
        this.currency = Objects.requireNonNull(currency, "currency es obligatorio");
        if (amountCents <= 0) {
            throw new IllegalArgumentException("amountCents debe ser mayor a cero");
        }
        this.amountCents = amountCents;
    }

    /**
     * Retorna el identificador del cliente.
     *
     * @return id del cliente
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Retorna la moneda del pago.
     *
     * @return moneda
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Retorna el monto en centavos.
     *
     * @return monto en centavos
     */
    public long getAmountCents() {
        return amountCents;
    }
}
