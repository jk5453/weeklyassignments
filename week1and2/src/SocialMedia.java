import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class SocialMedia {


    // Stores username -> userId
    private ConcurrentHashMap<String, Integer> usernameToUserId;

    // Stores username -> number of attempts
    private ConcurrentHashMap<String, Integer> usernameAttempts;

    public SocialMedia() {
        usernameToUserId = new ConcurrentHashMap<>();
        usernameAttempts = new ConcurrentHashMap<>();
    }

    // Register a user
    public void registerUser(String username, int userId) {
        usernameToUserId.put(username, userId);
    }

    // Check if username is available
    public boolean checkAvailability(String username) {

        // Track attempt frequency
        usernameAttempts.merge(username, 1, Integer::sum);

        return !usernameToUserId.containsKey(username);
    }

    // Suggest alternative usernames
    public List<String> suggestAlternatives(String username) {

        List<String> suggestions = new ArrayList<>();

        // Append numbers
        for (int i = 1; i <= 5; i++) {
            String candidate = username + i;

            if (!usernameToUserId.containsKey(candidate)) {
                suggestions.add(candidate);
            }
        }

        // Replace underscore with dot
        if (username.contains("_")) {
            String dotVersion = username.replace("_", ".");
            if (!usernameToUserId.containsKey(dotVersion)) {
                suggestions.add(dotVersion);
            }
        }

        // Random suffix suggestion
        for (int i = 0; i < 3; i++) {
            int rand = new Random().nextInt(1000);
            String randomCandidate = username + rand;

            if (!usernameToUserId.containsKey(randomCandidate)) {
                suggestions.add(randomCandidate);
            }
        }

        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {

        String mostAttempted = "";
        int maxAttempts = 0;

        for (Map.Entry<String, Integer> entry : usernameAttempts.entrySet()) {

            if (entry.getValue() > maxAttempts) {
                maxAttempts = entry.getValue();
                mostAttempted = entry.getKey();
            }
        }

        return mostAttempted + " (" + maxAttempts + " attempts)";
    }

    // Display registered users
    public void printUsers() {
        System.out.println("Registered Users:");
        for (Map.Entry<String, Integer> entry : usernameToUserId.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }

    // Main method for testing
    public static void main(String[] args) {

        SocialMedia system = new SocialMedia();

        // Pre-register some users
        system.registerUser("john_doe", 1001);
        system.registerUser("alex99", 1002);
        system.registerUser("admin", 1003);

        system.printUsers();

        System.out.println();

        // Username checks
        System.out.println("Check username availability:");

        System.out.println("john_doe → " + system.checkAvailability("john_doe"));
        System.out.println("jane_smith → " + system.checkAvailability("jane_smith"));
        System.out.println("admin → " + system.checkAvailability("admin"));

        // Simulate multiple attempts
        system.checkAvailability("admin");
        system.checkAvailability("admin");
        system.checkAvailability("admin");
        system.checkAvailability("john_doe");
        system.checkAvailability("john_doe");

        System.out.println();

        // Suggestions
        System.out.println("Suggestions for 'john_doe':");

        List<String> suggestions = system.suggestAlternatives("john_doe");

        for (String s : suggestions) {
            System.out.println(s);
        }

        System.out.println();

        // Most attempted username
        System.out.println("Most attempted username:");
        System.out.println(system.getMostAttempted());
    }
}
