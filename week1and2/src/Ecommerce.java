import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Ecommerce {


        private ConcurrentHashMap<String, AtomicInteger> inventory;


        private ConcurrentHashMap<String, Queue<Integer>> waitingList;

        public Ecommerce() {
            inventory = new ConcurrentHashMap<>();
            waitingList = new ConcurrentHashMap<>();
        }


        public void addProduct(String productId, int stock) {
            inventory.put(productId, new AtomicInteger(stock));
            waitingList.put(productId, new ConcurrentLinkedQueue<>());
        }


        public String checkStock(String productId) {
            AtomicInteger stock = inventory.get(productId);

            if (stock == null) {
                return "Product not found";
            }

            return stock.get() + " units available";
        }


        public String purchaseItem(String productId, int userId) {

            AtomicInteger stock = inventory.get(productId);

            if (stock == null) {
                return "Product not found";
            }


            while (true) {
                int currentStock = stock.get();

                if (currentStock <= 0) {
                    Queue<Integer> queue = waitingList.get(productId);
                    queue.add(userId);
                    return "Added to waiting list, position #" + queue.size();
                }

                if (stock.compareAndSet(currentStock, currentStock - 1)) {
                    return "Success, " + (currentStock - 1) + " units remaining";
                }
            }
        }


        public void showWaitingList(String productId) {
            Queue<Integer> queue = waitingList.get(productId);

            if (queue == null || queue.isEmpty()) {
                System.out.println("Waiting list empty");
                return;
            }

            System.out.println("Waiting list for " + productId + ": " + queue);
        }


        public static void main(String[] args) throws InterruptedException {

            Ecommerce system = new Ecommerce();
            String product = "IPHONE15_256GB";

            system.addProduct(product, 100);

            System.out.println(system.checkStock(product));

            int users = 50000;

            ExecutorService executor = Executors.newFixedThreadPool(100);

            for (int i = 1; i <= users; i++) {
                int userId = i;

                executor.submit(() -> {
                    String result = system.purchaseItem(product, userId);
                    System.out.println("User " + userId + " → " + result);
                });
            }

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);

            System.out.println("Final Stock: " + system.checkStock(product));
            system.showWaitingList(product);
        }
    }

