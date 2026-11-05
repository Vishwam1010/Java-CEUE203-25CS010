import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Run {
}

class SampleTests {
    @Run
    public void testAddition() {
        System.out.println("testAddition ran");
    }

    @Run
    public void testMessage() {
        System.out.println("testMessage ran");
    }

    public void helperMethod() {
        System.out.println("This helper is not a test");
    }

    @Run
    public void testWithArgument(String value) {
        System.out.println("This method is skipped because it has an argument");
    }
}

class MiniTestRunner {
    public static int runTests(Object testObject)
            throws ReflectiveOperationException {
        int runCount = 0;

        for (Method method : testObject.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Run.class)
                    && method.getParameterCount() == 0) {
                method.setAccessible(true);
                method.invoke(testObject);
                runCount++;
            }
        }

        return runCount;
    }
}

public class MiniTestRunnerDemo {
    public static void main(String[] args)
            throws ReflectiveOperationException {
        SampleTests tests = new SampleTests();
        int count = MiniTestRunner.runTests(tests);

        System.out.println("Tests run: " + count);
    }
}