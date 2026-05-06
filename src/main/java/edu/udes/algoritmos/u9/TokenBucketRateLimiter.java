package edu.udes.algoritmos.u9;

/**
 * Implementa un rate limiter tipo Token Bucket con sincronizacion interna.
 * Thread-safety: secciones criticas sincronizadas con un lock dedicado.
 */
public class TokenBucketRateLimiter {
    private final long capacity;
    private final long tokensPerMs;
    private final Object lock = new Object();
    private long availableTokens;
    private long lastRefillTime;

    /**
     * Crea un rate limiter con capacidad por segundo.
     *
     * @param capacityPerSecond tokens permitidos por segundo
     */
    public TokenBucketRateLimiter(long capacityPerSecond) {
        if (capacityPerSecond <= 0) {
            throw new IllegalArgumentException("capacityPerSecond debe ser mayor a cero");
        }
        this.capacity = capacityPerSecond;
        this.tokensPerMs = Math.max(1, capacityPerSecond / 1000);
        this.availableTokens = capacityPerSecond;
        this.lastRefillTime = System.currentTimeMillis();
    }

    /**
     * Solicita un token.
     *
     * @return true si se otorgo el token
     */
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    /**
     * Solicita una cantidad de tokens.
     *
     * @param tokens cantidad solicitada
     * @return true si se otorgan todos los tokens
     */
    public boolean tryAcquire(long tokens) {
        if (tokens <= 0) {
            throw new IllegalArgumentException("tokens debe ser mayor a cero");
        }
        synchronized (lock) {
            refill();
            if (availableTokens >= tokens) {
                availableTokens -= tokens;
                return true;
            }
            return false;
        }
    }

    /**
     * Retorna los tokens disponibles actuales.
     *
     * @return tokens disponibles
     */
    public long getAvailableTokens() {
        synchronized (lock) {
            refill();
            return availableTokens;
        }
    }

    long getTokensPerMs() {
        return tokensPerMs;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTime;
        if (elapsed <= 0) {
            return;
        }
        long tokensToAdd = elapsed * tokensPerMs;
        availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
        lastRefillTime = now;
    }
}
