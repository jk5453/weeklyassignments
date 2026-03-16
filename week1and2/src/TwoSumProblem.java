
    import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

    class Transaction {
        int id;
        int amount;
        String merchant;
        String account;
        LocalDateTime time;

        public Transaction(int id, int amount, String merchant, String account, String timeStr) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            this.time = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
    }

    public class TwoSumProblem {

        private List<Transaction> transactions;

        public TwoSumProblem(List<Transaction> transactions) {
            this.transactions = transactions;
        }

        public List<int[]> findTwoSum(int target) {
            Map<Integer, Transaction> complementMap = new HashMap<>();
            List<int[]> result = new ArrayList<>();

            for (Transaction t : transactions) {
                if (complementMap.containsKey(t.amount)) {
                    result.add(new int[]{complementMap.get(t.amount).id, t.id});
                } else {
                    complementMap.put(target - t.amount, t);
                }
            }
            return result;
        }

        public List<int[]> findTwoSumWithTimeWindow(int target, int windowMinutes) {
            Map<Integer, List<Transaction>> complementMap = new HashMap<>();
            List<int[]> result = new ArrayList<>();

            for (Transaction t : transactions) {

                if (complementMap.containsKey(t.amount)) {
                    for (Transaction other : complementMap.get(t.amount)) {
                        long diff = Math.abs(Duration.between(other.time, t.time).toMinutes());
                        if (diff <= windowMinutes) {
                            result.add(new int[]{other.id, t.id});
                        }
                    }
                }

                complementMap.computeIfAbsent(target - t.amount, k -> new ArrayList<>()).add(t);
            }

            return result;
        }

        public List<List<Integer>> findKSum(int k, int target) {
            List<List<Integer>> result = new ArrayList<>();
            transactions.sort(Comparator.comparingInt(a -> a.amount));
            kSumHelper(transactions, k, 0, target, new ArrayList<>(), result);
            return result;
        }

        private void kSumHelper(List<Transaction> arr, int k, int start, int target,
                                List<Integer> path, List<List<Integer>> result) {
            if (k == 2) {
                int left = start, right = arr.size() - 1;
                while (left < right) {
                    int sum = arr.get(left).amount + arr.get(right).amount;
                    if (sum == target) {
                        List<Integer> temp = new ArrayList<>(path);
                        temp.add(arr.get(left).id);
                        temp.add(arr.get(right).id);
                        result.add(temp);
                        left++;
                        right--;
                    } else if (sum < target) left++;
                    else right--;
                }
            } else {
                for (int i = start; i < arr.size(); i++) {
                    if (i > start && arr.get(i).amount == arr.get(i-1).amount) continue; // skip duplicates
                    path.add(arr.get(i).id);
                    kSumHelper(arr, k-1, i+1, target - arr.get(i).amount, path, result);
                    path.remove(path.size()-1);
                }
            }
        }

        public List<Map<String, Object>> detectDuplicates() {
            Map<String, Map<Integer, Set<String>>> merchantAmountMap = new HashMap<>();
            List<Map<String, Object>> result = new ArrayList<>();

            for (Transaction t : transactions) {
                merchantAmountMap
                        .computeIfAbsent(t.merchant, k -> new HashMap<>())
                        .computeIfAbsent(t.amount, k -> new HashSet<>())
                        .add(t.account);
            }

            for (String merchant : merchantAmountMap.keySet()) {
                for (Integer amount : merchantAmountMap.get(merchant).keySet()) {
                    Set<String> accounts = merchantAmountMap.get(merchant).get(amount);
                    if (accounts.size() > 1) {
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("merchant", merchant);
                        entry.put("amount", amount);
                        entry.put("accounts", accounts);
                        result.add(entry);
                    }
                }
            }
            return result;
        }

        public static void main(String[] args) {
            List<Transaction> txs = Arrays.asList(
                    new Transaction(1, 500, "Store A", "acc1", "2026-03-16 10:00"),
                    new Transaction(2, 300, "Store B", "acc2", "2026-03-16 10:15"),
                    new Transaction(3, 200, "Store C", "acc3", "2026-03-16 10:30"),
                    new Transaction(4, 500, "Store A", "acc2", "2026-03-16 11:00")
            );

            TwoSumProblem problem = new TwoSumProblem(txs);

            System.out.println("Two-Sum target 500: " + problem.findTwoSum(500));
            System.out.println("Two-Sum with 60 min window: " + problem.findTwoSumWithTimeWindow(500, 60));
            System.out.println("K-Sum (k=3, target=1000): " + problem.findKSum(3, 1000));
            System.out.println("Duplicate detection: " + problem.detectDuplicates());
        }
    }

