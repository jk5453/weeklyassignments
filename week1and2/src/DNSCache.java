
    import java.util.*;
import java.util.concurrent.*;

    class DNSEntry {
        String domain;
        String ipAddress;
        long timestamp;
        long expiryTime;

        public DNSEntry(String domain, String ipAddress, int ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.timestamp = System.currentTimeMillis();
            this.expiryTime = this.timestamp + ttlSeconds * 1000L;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    public class DNSCache {

        private final int capacity;


        private final Map<String, DNSEntry> cache;

        private int hits = 0;
        private int misses = 0;
        private long totalLookupTime = 0;
        private int lookupCount = 0;

        public DNSCache(int capacity) {
            this.capacity = capacity;

            cache = new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                    return size() > DNSCache.this.capacity;
                }
            };

            startCleanupThread();
        }

        public synchronized String resolve(String domain) {
            long start = System.nanoTime();

            DNSEntry entry = cache.get(domain);

            if (entry != null && !entry.isExpired()) {
                hits++;
                recordTime(start);
                return "Cache HIT → " + entry.ipAddress;
            }

            if (entry != null && entry.isExpired()) {
                cache.remove(domain);
                System.out.println("Cache EXPIRED for " + domain);
            }

            misses++;

            String ip = queryUpstreamDNS(domain);
            int ttl = 300;

            DNSEntry newEntry = new DNSEntry(domain, ip, ttl);
            cache.put(domain, newEntry);

            recordTime(start);

            return "Cache MISS → " + ip + " (TTL: " + ttl + "s)";
        }

        private void recordTime(long start) {
            long end = System.nanoTime();
            totalLookupTime += (end - start);
            lookupCount++;
        }


        private String queryUpstreamDNS(String domain) {
            try {
                Thread.sleep(100); // simulate network delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return "172.217." + new Random().nextInt(255) + "." + new Random().nextInt(255);
        }

        public synchronized void getCacheStats() {
            int total = hits + misses;
            double hitRate = total == 0 ? 0 : (hits * 100.0) / total;
            double avgLookupMs = (lookupCount == 0) ? 0 : (totalLookupTime / 1_000_000.0) / lookupCount;

            System.out.println("Hit Rate: " + hitRate + "%");
            System.out.println("Average Lookup Time: " + avgLookupMs + " ms");
            System.out.println("Cache Size: " + cache.size());
        }


        private void startCleanupThread() {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            scheduler.scheduleAtFixedRate(() -> {
                synchronized (DNSCache.this) {
                    Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, DNSEntry> entry = it.next();
                        if (entry.getValue().isExpired()) {
                            it.remove();
                        }
                    }
                }
            }, 30, 30, TimeUnit.SECONDS);
        }


        public static void main(String[] args) throws Exception {

            DNSCache dnsCache = new DNSCache(5);

            System.out.println(dnsCache.resolve("google.com"));
            System.out.println(dnsCache.resolve("google.com"));

            Thread.sleep(2000);

            System.out.println(dnsCache.resolve("openai.com"));
            System.out.println(dnsCache.resolve("google.com"));

            dnsCache.getCacheStats();
        }
    }

