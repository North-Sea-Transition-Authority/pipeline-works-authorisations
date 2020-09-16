package uk.co.ogauthority.pwa.testutils;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.reflect.FieldUtils;

public class ObjectTestUtils {

  private ObjectTestUtils() {
    throw new AssertionError();
  }


  public static void assertAllFieldsNotNull(Object object, Class clazz, Set<String> ignoreFields) {
    Arrays.asList(FieldUtils.getAllFields(clazz)).forEach(field -> {
      if (!field.isSynthetic() && !ignoreFields.contains(field.getName())) {

        try {
          if (FieldUtils.readField(field, object, true) == null) {
            throw new RuntimeException("Found null field. Expected all fields to have value." + field.getName());
          }
        } catch (IllegalAccessException e) {
          throw new RuntimeException("Expected to be able to access field!", e.getCause());
        }
      }

    });

  }


  /**
   * Asserts that all fields on the object have a non-null value except from those included in nullFieldNames which have a null value.
   * @param object being tested
   * @param nullFieldNames names of fields that should have a null value associated with them
   */
  public static void assertAllExpectedFieldsHaveValue(Object object, List<String> nullFieldNames) {

    Arrays.stream(FieldUtils.getAllFields(object.getClass()))
        .forEach(field -> {

          var fieldValue = getFieldValue(field, object);

          boolean fieldShouldBeNull = nullFieldNames.contains(field.getName());

          try {
            if (fieldShouldBeNull) {
              assertThat(fieldValue).isNull();
            } else {
              assertThat(fieldValue).isNotNull();
            }
          } catch (AssertionError e) {
            throw new AssertionError(String.format("Expected [%s] field [%s] to %s but value is [%s]",
                object.getClass().getName(),
                field.getName(),
                fieldShouldBeNull ? "be null" : "have value",
                fieldValue));
          }

        });

  }

  /**
   * Assert that all fields not included in the ignore list have equal values between object1 and object2
   */
  public static <T> void assertValuesEqual(T object1,
                                           T object2,
                                           Collection<String> ignoredForEqualsComparison) {

    // check that all fields apart from those we know will be different (or expect to be null) are equal between the two objects
    Arrays.stream(FieldUtils.getAllFields(object1.getClass()))
        .filter(field -> !ignoredForEqualsComparison.contains(field.getName()))
        .forEach(field -> {

          var oldValue = getFieldValue(field, object1);
          var newValue = getFieldValue(field, object2);

          try {
            assertThat(oldValue).isEqualTo(newValue);
          } catch (AssertionError e) {
            throw new AssertionError(String.format("Expected [%s] field [%s] to be equal, object1 value [%s] != object2 value [%s]",
                object1.getClass(),
                field.getName(),
                oldValue,
                newValue));
          }

        });

  }

  private static Object getFieldValue(Field field, Object object) {
    try {
      return FieldUtils.readField(field, object, true);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(
          String.format("Failed to access field '%s' on class '%s'", field.getName(), object.getClass()));
    }
  }

}
