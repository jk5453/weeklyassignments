import java.util.*;

    class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        Map<String, Integer> sentences = new HashMap<>();
    }

    public class AutocompleteSystem {

        private TrieNode root;
        private Map<String, Integer> frequencyMap;
        private String currentInput;

        public AutocompleteSystem() {
            root = new TrieNode();
            frequencyMap = new HashMap<>();
            currentInput = "";
        }


        private void insert(String query, int freq) {

            TrieNode node = root;

            for (char c : query.toCharArray()) {

                node.children.putIfAbsent(c, new TrieNode());
                node = node.children.get(c);

                node.sentences.put(query,
                        node.sentences.getOrDefault(query, 0) + freq);
            }
        }


        public void updateFrequency(String query) {

            int freq = frequencyMap.getOrDefault(query, 0) + 1;
            frequencyMap.put(query, freq);

            insert(query, 1);
        }


        public List<String> search(String prefix) {

            TrieNode node = root;

            for (char c : prefix.toCharArray()) {

                if (!node.children.containsKey(c))
                    return new ArrayList<>();

                node = node.children.get(c);
            }

            PriorityQueue<Map.Entry<String, Integer>> pq =
                    new PriorityQueue<>(
                            (a, b) -> a.getValue() == b.getValue()
                                    ? b.getKey().compareTo(a.getKey())
                                    : a.getValue() - b.getValue()
                    );

            for (Map.Entry<String, Integer> entry : node.sentences.entrySet()) {

                pq.offer(entry);

                if (pq.size() > 10)
                    pq.poll();
            }

            List<String> result = new ArrayList<>();

            while (!pq.isEmpty())
                result.add(pq.poll().getKey());

            Collections.reverse(result);

            return result;
        }


        public static void main(String[] args) {

            AutocompleteSystem system = new AutocompleteSystem();

            system.updateFrequency("java tutorial");
            system.updateFrequency("javascript");
            system.updateFrequency("java download");
            system.updateFrequency("java tutorial");
            system.updateFrequency("java 21 features");

            System.out.println(system.search("jav"));
        }
    }

