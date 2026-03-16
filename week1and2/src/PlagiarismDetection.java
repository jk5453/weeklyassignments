
    import java.util.*;

    class Document {
        String id;
        String text;

        public Document(String id, String text) {
            this.id = id;
            this.text = text;
        }
    }

    public class PlagiarismDetection {


        private Map<String, Set<String>> index = new HashMap<>();


        private Map<String, Set<String>> documentNgrams = new HashMap<>();

        private int N = 5;

        public PlagiarismDetection(int n) {
            this.N = n;
        }


        public void addDocument(Document doc) {
            Set<String> ngrams = generateNgrams(doc.text);

            documentNgrams.put(doc.id, ngrams);

            for (String gram : ngrams) {
                index.computeIfAbsent(gram, k -> new HashSet<>()).add(doc.id);
            }
        }


        public void analyzeDocument(String docId, String text) {

            Set<String> newDocNgrams = generateNgrams(text);

            System.out.println("Extracted " + newDocNgrams.size() + " n-grams");

            Map<String, Integer> matchCounts = new HashMap<>();

            for (String gram : newDocNgrams) {

                if (index.containsKey(gram)) {
                    for (String existingDoc : index.get(gram)) {
                        matchCounts.put(existingDoc,
                                matchCounts.getOrDefault(existingDoc, 0) + 1);
                    }
                }
            }

            for (String doc : matchCounts.keySet()) {

                int matches = matchCounts.get(doc);

                double similarity =
                        (matches * 100.0) / newDocNgrams.size();

                System.out.println("Found " + matches +
                        " matching n-grams with \"" + doc + "\"");

                System.out.println("Similarity: " +
                        String.format("%.2f", similarity) + "%");

                if (similarity > 60) {
                    System.out.println("PLAGIARISM DETECTED\n");
                } else if (similarity > 15) {
                    System.out.println("Suspicious similarity\n");
                }
            }
        }


        private Set<String> generateNgrams(String text) {

            String[] words = text.toLowerCase()
                    .replaceAll("[^a-z ]", "")
                    .split("\\s+");

            Set<String> ngrams = new HashSet<>();

            for (int i = 0; i <= words.length - N; i++) {

                StringBuilder sb = new StringBuilder();

                for (int j = 0; j < N; j++) {
                    sb.append(words[i + j]).append(" ");
                }

                ngrams.add(sb.toString().trim());
            }

            return ngrams;
        }


        public static void main(String[] args) {

            PlagiarismDetection detector = new PlagiarismDetection(5);

            Document doc1 = new Document("essay_089",
                    "machine learning is transforming the world of artificial intelligence and data science");

            Document doc2 = new Document("essay_092",
                    "artificial intelligence and data science are transforming modern machine learning applications");

            detector.addDocument(doc1);
            detector.addDocument(doc2);

            String newEssay =
                    "machine learning and artificial intelligence are transforming the world of data science";

            detector.analyzeDocument("essay_123", newEssay);
        }
    }


