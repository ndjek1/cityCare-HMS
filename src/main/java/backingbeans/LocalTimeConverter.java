package backingbeans;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@FacesConverter(forClass = LocalTime.class, value = "localTimeConverter")
public class LocalTimeConverter implements Converter<LocalTime> {

    // This formatter MUST match what appointmentBookingBean.formatLocalTimeForValue() produces
    // AND what the itemValue in f:selectItems will be after conversion.
    private static final DateTimeFormatter PARSE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm"); // e.g., 09:00

    @Override
    public LocalTime getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            System.out.println("LocalTimeConverter: Attempting to parse string value: '" + value + "'"); // Debug
            LocalTime parsedTime = LocalTime.parse(value, PARSE_FORMATTER);
            System.out.println("LocalTimeConverter: Successfully parsed to LocalTime: " + parsedTime); // Debug
            return parsedTime;
        } catch (DateTimeParseException e) {
            System.err.println("LocalTimeConverter: Failed to parse string value: '" + value + "'. Error: " + e.getMessage()); // Debug
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Time Format", "Please use HH:mm format (e.g., 09:00). Original: " + value), e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, LocalTime value) {
        if (value == null) {
            return ""; // Return empty string for null, which is typical for <f:selectItem itemValue="#{null}">
        }
        // This method is used to convert the LocalTime object (from backing bean property)
        // back to a String if needed for display, OR crucially, to match against the submitted String value
        // if JSF is doing comparisons.
        String formattedString = value.format(PARSE_FORMATTER);
        System.out.println("LocalTimeConverter: Converting LocalTime " + value + " to String: '" + formattedString + "'"); // Debug
        return formattedString;
    }
}