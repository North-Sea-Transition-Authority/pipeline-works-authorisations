package uk.co.ogauthority.pwa.testutils;

import java.util.Arrays;
import java.util.Set;
import org.apache.commons.lang3.reflect.FieldUtils;

public class ObjectTestUtils {

  //no instantiation
  private ObjectTestUtils() {
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
}
