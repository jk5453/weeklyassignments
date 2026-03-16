import java.util.concurrent.*;
import java.util.*;

    class TokenBucket {

        private int tokens;
        private final int maxTokens;
        private final double refillRate; // tokens per second
        private long lastRefillTime;

        public TokenBucket(int maxTokens, double refillRate) {
            this.tokens = maxTokens;
            this.maxTokens = maxTokens;
            this.refillRate = refillRate;
            this.lastRefillTime = System.currentTimeMillis();
        }

        // refill tokens based on elapsed time
        private void refill() {
            long now = System.currentTimeMillis();

            double tokensToAdd =
                    ((now - lastRefillTime) / 1000.0) * refillRate;

            if (tokensToAdd > 0) {
                tokens = Math.min(maxTokens,
                        tokens + (int) tokensToAdd);
                lastRefillTime = now;
            }
        }

        public synchronized boolean allowRequest() {
            refill();

            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        public int getTokens() {
            return tokens;
        }

        public int getMaxTokens() {
            return maxTokens;
        }
    }

    public class RateLimiter {

        // clientId → TokenBucket
        private final Map<String, TokenBucket> clientBuckets =
                new ConcurrentHashMap<>();

        private static final int MAX_REQUESTS = 1000;


        private static final double REFILL_RATE =
                MAX_REQUESTS / 3600.0;

        public boolean checkRateLimit(String clientId) {

            clientBuckets.putIfAbsent(
                    clientId,
                    new TokenBucket(MAX_REQUESTS, REFILL_RATE)
            );

            TokenBucket bucket = clientBuckets.get(clientId);

            boolean allowed = bucket.allowRequest();

            if (allowed) {

                System.out.println(
                        "Allowed (" +
                                bucket.getTokens() +
                                " requests remaining)"
                );

            } else {

                long retryAfter =
                        (long)((1 / REFILL_RATE));

                System.out.println(
                        "Denied (0 requests remaining, retry after "
                                + retryAfter + "s)"
                );
            }

            return allowed;
        }

        public Map<String, Object> getRateLimitStatus(String clientId) {

            TokenBucket bucket = clientBuckets.get(clientId);

            if (bucket == null) {
                return Map.of(
                        "used", 0,
                        "limit", MAX_REQUESTS,
                        "reset", System.currentTimeMillis()
                );
            }

            int remaining = bucket.getTokens();
            int used = MAX_REQUESTS - remaining;

            Map<String, Object> status = new HashMap<>();

            status.put("used", used);
            status.put("limit", MAX_REQUESTS);
            status.put("remaining", remaining);

            return status;
        }


        public static void main(String[] args) {

            RateLimiter limiter = new RateLimiter();

            for (int i = 0; i < 5; i++) {
                limiter.checkRateLimit("abc123");
            }

            System.out.println(
                    limiter.getRateLimitStatus("abc123")
            );
        }
    }
