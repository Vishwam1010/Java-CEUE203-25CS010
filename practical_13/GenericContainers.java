public class GenericContainers {
    static class Box<T> {
        private T value;

        public Box(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }

    static class Pair<A, B> {
        private final A first;
        private final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }
    }

    public static <T> T firstElement(T[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty.");
        }
        return array[0];
    }

    public static void main(String[] args) {
        Box<String> textBox = new Box<>("Hello");
        Box<Integer> numberBox = new Box<>(100);

        textBox.set("Java generics");
        numberBox.set(250);

        System.out.println("Text box: " + textBox.get());
        System.out.println("Number box: " + numberBox.get());

        Pair<String, Integer> student = new Pair<>("Vishwam", 25);
        System.out.println("Pair: " + student.getFirst()
                + ", " + student.getSecond());

        String[] names = {"Asha", "Ravi", "Meera"};
        Integer[] scores = {90, 85, 95};

        String firstName = firstElement(names);
        Integer firstScore = firstElement(scores);

        System.out.println("First name: " + firstName);
        System.out.println("First score: " + firstScore);
    }
}