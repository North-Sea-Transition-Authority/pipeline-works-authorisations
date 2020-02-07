package uk.co.ogauthority.pwa.util;

import org.springframework.web.bind.annotation.ResponseStatus;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;

/**
 * Helper class to provide access to commonly used Enum-related methods.
 */
public class EnumUtils {

  private EnumUtils() {
    throw new AssertionError();
  }

  /**
   * Check to see whether the passed-in enum contains a value matching the passed-in String value.
   * This method uses Enum.valueOf but throws an exception with a {@link ResponseStatus} mapping.
   * @return the corresponding enum value if matched
   * @throws ValueNotFoundException if no values matched
   */
  public static <E extends Enum<E>> E getEnumValue(Class<E> enumClass, String value) throws ValueNotFoundException {

    boolean isValidEnumValue = org.apache.commons.lang3.EnumUtils.isValidEnum(enumClass, value);

    if (isValidEnumValue) {
      return Enum.valueOf(enumClass, value);
    } else {
      throw new ValueNotFoundException(String.format("Enum value '%s' is not a valid value of enum '%s'", value, enumClass.getName()));
    }
  }

}
