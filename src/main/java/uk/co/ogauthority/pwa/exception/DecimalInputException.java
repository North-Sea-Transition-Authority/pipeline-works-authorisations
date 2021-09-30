package uk.co.ogauthority.pwa.exception;

import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;

/**
 * Exceptions associated with the {@link DecimalInput} class.
 */
public class DecimalInputException extends RuntimeException {

  public DecimalInputException(String message) {
    super(message);
  }

}
