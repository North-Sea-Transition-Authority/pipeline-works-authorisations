package uk.co.ogauthority.pwa.util.forminputs.decimal;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.ogauthority.pwa.exception.DecimalInputException;

/**
 * Represents a BigDecimal commonly used on forms and provides access to common operations that might be applied to that decimal.
 */
public final class DecimalInput {

  private static final Logger LOGGER = LoggerFactory.getLogger(DecimalInput.class);

  private String value;

  public DecimalInput() {
  }

  public DecimalInput(String value) {
    this.value = value;
  }

  public DecimalInput(BigDecimal value) {
    this.value = value != null ? String.valueOf(value) : "";
  }

  public static DecimalInput from(double value) {
    return new DecimalInput(BigDecimal.valueOf(value));
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public BigDecimal createBigDecimalOrNull() {
    return this.asBigDecimal()
        .orElse(null);
  }

  public Optional<BigDecimal> asBigDecimal() {

    try {

      if (value != null && value.toLowerCase().contains("e")) {
        throw new DecimalInputException(String.format("Exponents aren't allowed in DecimalInputs: [%s]", value));
      }

      return Optional.ofNullable(value)
          .map(BigDecimal::new);

    } catch (NumberFormatException | DecimalInputException e) {
      LOGGER.debug("Could not convert the input value to a valid DecimalInput for value: " + value, e);
      return Optional.empty();
    }

  }

  public boolean hasContent() {
    return value != null && !value.isBlank();
  }

  public static DecimalInput createEmptyIfNull(DecimalInput decimalInput) {
    if (decimalInput == null) {
      return new DecimalInput();
    }
    return decimalInput;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DecimalInput that = (DecimalInput) o;
    return Objects.equals(getValue(), that.getValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getValue());
  }
}
