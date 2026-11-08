import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface NotBlank {
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface MaxLength {
    int value();
}

class SignupForm {
    @NotBlank
    @MaxLength(30)
    private final String username;

    @NotBlank
    @MaxLength(50)
    private final String email;

    SignupForm(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}

class FormChecker {
    public static List<String> validate(Object object)
            throws IllegalAccessException {
        List<String> errors = new ArrayList<>();

        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object fieldValue = field.get(object);
            String fieldName = field.getName();

            if (field.isAnnotationPresent(NotBlank.class)
                    && (!(fieldValue instanceof String)
                    || ((String) fieldValue).trim().isEmpty())) {
                errors.add(fieldName + " must not be blank.");
            }

            MaxLength maxLength = field.getAnnotation(MaxLength.class);
            if (maxLength != null && fieldValue instanceof String) {
                String text = (String) fieldValue;

                if (text.length() > maxLength.value()) {
                    errors.add(fieldName + " must be at most "
                            + maxLength.value() + " characters.");
                }
            }
        }

        return errors;
    }
}

public class FormValidatorDemo {
    public static void main(String[] args) throws IllegalAccessException {
        SignupForm form = new SignupForm(
                "",
                "this-email-address-is-too-long-for-the-limit@example.com"
        );

        // Read the values through their getters.
        System.out.println("Username: " + form.getUsername());
        System.out.println("Email: " + form.getEmail());

        List<String> errors = FormChecker.validate(form);

        if (errors.isEmpty()) {
            System.out.println("Form is valid.");
        } else {
            System.out.println("Validation errors:");
            for (String error : errors) {
                System.out.println("- " + error);
            }
        }
    }
}