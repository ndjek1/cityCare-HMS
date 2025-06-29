package annotations;



import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import utils.MaxAgeValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxAgeValidator.class) // Link to the validator logic
public @interface MaxAge {

    String message() default "Age exceeds the maximum limit.";

    // The maximum age allowed (e.g., 100)
    int value();

    // Standard boilerplate
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}