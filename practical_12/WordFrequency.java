import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordFrequency {
    public static void main(String[] args) {
        String paragraph = "Java is easy to learn. Java is useful, and Java is popular.";

        Map<String, Integer> frequency = new HashMap<>();

        String[] words = paragraph.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .trim()
                .split("\\s+");

        for (String word : words) {
            if (!word.isEmpty()) {
                frequency.put(word, frequency.getOrDefault(word, 0) + 1);
            }
        }

        List<Map.Entry<String, Integer>> sortedWords =
                new ArrayList<>(frequency.entrySet());

        sortedWords.sort(
                Comparator.<Map.Entry<String, Integer>>comparingInt(
                        Map.Entry::getValue
                ).reversed().thenComparing(Map.Entry::getKey)
        );

        System.out.println("Words sorted by frequency:");
        for (Map.Entry<String, Integer> entry : sortedWords) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}