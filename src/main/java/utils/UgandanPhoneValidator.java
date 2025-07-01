package utils; // Or any package you prefer

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@FacesValidator("custom.ugandanPhoneValidator") // This is the ID we will use in the XHTML
@Named
@ViewScoped
public class UgandanPhoneValidator implements Validator<String>, Serializable {

    private static final List<String> UGANDAN_MOBILE_PREFIXES = Arrays.asList(
            "070", "071", "074", "075", "076", "077", "078", "079"
    );
    private static final int EXPECTED_LENGTH = 10;

    @Override
    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if (value == null || value.trim().isEmpty()) {
            return; // Don't validate empty values, let @NotEmpty handle that.
        }

        String phoneNumber = value.replaceAll("\\s", "").trim();

        // 1. Check if it consists of only digits
        if (!phoneNumber.matches("^\\d+$")) {
            System.out.println(phoneNumber);
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Format", "Phone number must contain only digits."));
        }

        // 2. Check length
        if (phoneNumber.length() != EXPECTED_LENGTH) {
            System.out.println(phoneNumber);
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Length", "Phone number must be exactly " + EXPECTED_LENGTH + " digits long."));
        }

        // 3. Check for valid Ugandan mobile prefixes
        String prefix = phoneNumber.substring(0, 3);
        if (!UGANDAN_MOBILE_PREFIXES.contains(prefix)) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Prefix", "Phone number must start with a valid Ugandan prefix (e.g., 077, 075, 070, etc.)."));
        }
    }
}