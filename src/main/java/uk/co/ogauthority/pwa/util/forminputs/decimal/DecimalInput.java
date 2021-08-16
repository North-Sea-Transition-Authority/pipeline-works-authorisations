package uk.co.ogauthority.pwa.util.forminputs.decimal;

import java.math.BigDecimal;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a BigDecimal commonly used on forms and provides access to common operations that might be applied to that decimal.
 */
public class DecimalInput {

  private static final Logger LOGGER = LoggerFactory.getLogger(DecimalInput.class);


  private String value;

  public DecimalInput() {
  }

  public DecimalInput(String value) {
    this.value = value;
  }

  public DecimalInput(BigDecimal value) {
    this.value = String.valueOf(value);
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

  Optional<BigDecimal> asBigDecimal() {

    try {
      return value != null ? Optional.of(new BigDecimal(value)) : Optional.empty();

    } catch (NumberFormatException e) {
      LOGGER.debug("Could not convert the input value to a valid number for value: " + value, e);
      return Optional.empty();
    }
  }

  public boolean hasContent() {
    return value != null;
  }


}
