import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UniqueVisitors {
    public static void main(String[] args) {
        List<String> loginNames = Arrays.asList(
                "asha", "ravi", "meera", "asha",
                "karan", "ravi", "neha", "meera"
        );

        Set<String> uniqueUsers = new HashSet<>(loginNames);
        Set<String> firstSeenOrder = new LinkedHashSet<>(loginNames);

        System.out.println("Unique user count: " + uniqueUsers.size());

        System.out.println("First three distinct users:");
        int printed = 0;

        for (String name : firstSeenOrder) {
            if (printed == 3) {
                break;
            }

            System.out.println(name);
            printed++;
        }
    }
}