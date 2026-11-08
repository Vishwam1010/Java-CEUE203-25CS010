import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;

public class SaveLoadDemo {
    private static final Path FILE = Path.of("people.dat");

    static class Person implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String name;
        private final int age;
        private final transient String password;

        Person(String name, int age, String password) {
            this.name = name;
            this.age = age;
            this.password = password;
        }

        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age
                    + ", password=" + password + "}";
        }
    }

    public static void main(String[] args) {
        Person[] peopleToSave = {
            new Person("Asha", 20, "secret123"),
            new Person("Ravi", 21, "mypassword")
        };

        // Write the array to a file.
        try (ObjectOutputStream output = new ObjectOutputStream(
                new FileOutputStream(FILE.toFile()))) {
            output.writeObject(peopleToSave);
            System.out.println("Saved " + peopleToSave.length + " people to " + FILE);
        } catch (IOException e) {
            System.err.println("Could not save the objects: " + e.getMessage());
            return;
        }

        // Read the array back into newly created objects.
        try (ObjectInputStream input = new ObjectInputStream(
                new FileInputStream(FILE.toFile()))) {
            Person[] loadedPeople = (Person[]) input.readObject();

            System.out.println("\nObjects loaded from file:");
            for (Person person : loadedPeople) {
                System.out.println(person);
            }

            System.out.println("\nNames and ages were restored.");
            System.out.println("The transient password values are null after loading.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not load the objects: " + e.getMessage());
        }
    }
}