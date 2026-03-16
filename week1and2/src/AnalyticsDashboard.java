
    import java.util.*;
import java.util.concurrent.*;

    class PageEvent {
        String url;
        String userId;
        String source;

        public PageEvent(String url, String userId, String source) {
            this.url = url;
            this.userId = userId;
            this.source = source;
        }
    }

    public class AnalyticsDashboard {


        private Map<String, Integer> pageViews = new ConcurrentHashMap<>();


        private Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();


        private Map<String, Integer> trafficSources = new ConcurrentHashMap<>();

        public AnalyticsDashboard() {
            startDashboardUpdates();
        }


        public void processEvent(PageEvent event) {


            pageViews.merge(event.url, 1, Integer::sum);


            uniqueVisitors
                    .computeIfAbsent(event.url, k -> ConcurrentHashMap.newKeySet())
                    .add(event.userId);


            trafficSources.merge(event.source, 1, Integer::sum);
        }


        private List<Map.Entry<String, Integer>> getTopPages() {

            PriorityQueue<Map.Entry<String, Integer>> pq =
                    new PriorityQueue<>(Map.Entry.comparingByValue());

            for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {
                pq.offer(entry);
                if (pq.size() > 10) pq.poll();
            }

            List<Map.Entry<String, Integer>> result = new ArrayList<>();
            while (!pq.isEmpty()) result.add(pq.poll());

            Collections.reverse(result);
            return result;
        }


        public void getDashboard() {

            System.out.println("\n=== REAL-TIME DASHBOARD ===");

            System.out.println("\nTop Pages:");

            int rank = 1;
            for (Map.Entry<String, Integer> entry : getTopPages()) {

                int unique = uniqueVisitors
                        .getOrDefault(entry.getKey(), new HashSet<>())
                        .size();

                System.out.println(
                        rank++ + ". " + entry.getKey()
                                + " - " + entry.getValue()
                                + " views (" + unique + " unique)"
                );
            }

            int total = trafficSources.values()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            System.out.println("\nTraffic Sources:");

            for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {

                double percent = (entry.getValue() * 100.0) / total;

                System.out.println(
                        entry.getKey() + ": "
                                + String.format("%.1f", percent) + "%"
                );
            }
        }


        private void startDashboardUpdates() {

            ScheduledExecutorService scheduler =
                    Executors.newSingleThreadScheduledExecutor();

            scheduler.scheduleAtFixedRate(
                    this::getDashboard,
                    5,
                    5,
                    TimeUnit.SECONDS
            );
        }


        public static void main(String[] args) throws Exception {

            AnalyticsDashboard dashboard = new AnalyticsDashboard();

            dashboard.processEvent(new PageEvent(
                    "/article/breaking-news", "user_123", "google"));

            dashboard.processEvent(new PageEvent(
                    "/article/breaking-news", "user_456", "facebook"));

            dashboard.processEvent(new PageEvent(
                    "/sports/championship", "user_789", "direct"));

            dashboard.processEvent(new PageEvent(
                    "/sports/championship", "user_101", "google"));

            Thread.sleep(15000);
        }
    }

