package edu.udes.algoritmos.u9;

import java.util.Objects;

/**
 * Representa una solicitud de pago.
 */
public class PaymentRequest {
    private final String customerId;
    private final String currency;
    private final long amountCents;

    public PaymentRequest(String customerId, String currency, long amountCents) {
        this.customerId = Objects.requireNonNull(customerId, "customerId es obligatorio");
        this.currency = Objects.requireNonNull(currency, "currency es obligatorio");
        if (amountCents <= 0) {
            throw new IllegalArgumentException("amountCents debe ser mayor a cero");
        }
        this.amountCents = amountCents;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCurrency() {
        return currency;
    }

    public long getAmountCents() {
        return amountCents;
    }
}
