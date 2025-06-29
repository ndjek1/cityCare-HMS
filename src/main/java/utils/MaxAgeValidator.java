package utils;


import annotations.MaxAge;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

// Links the logic to the @MaxAge annotation and specifies it works on Date fields
public class MaxAgeValidator implements ConstraintValidator<MaxAge, Date> {

    private int maximumAge;

    @Override
    public void initialize(MaxAge constraintAnnotation) {
        // Get the maximum age value from the annotation
        this.maximumAge = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Date dateOfBirth, ConstraintValidatorContext context) {
        // Let @NotNull handle null values
        if (dateOfBirth == null) {
            return true;
        }

        // Convert java.util.Date to java.time.LocalDate for safe calculation
        LocalDate birthDateLocal = dateOfBirth.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate today = LocalDate.now();

        // Calculate the age
        int calculatedAge = Period.between(birthDateLocal, today).getYears();

        // The validation check: is the calculated age less than or equal to the required maximum?
        return calculatedAge <= this.maximumAge;
    }
}
