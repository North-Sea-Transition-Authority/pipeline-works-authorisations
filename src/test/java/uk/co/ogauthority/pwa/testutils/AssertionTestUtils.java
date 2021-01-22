package uk.co.ogauthority.pwa.testutils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

public class AssertionTestUtils {

  private AssertionTestUtils() {
    throw new AssertionError();
  }

  public static <T> void  assertNotEmptyAndContains(Collection<T> collection,
                                                    T object) {
    assertThat(collection).isNotEmpty();
    assertThat(collection).contains(object);
  }

  public static <T> void  assertNotEmptyAndDoesNotContain(Collection<T> collection,
                                                          T object) {
    assertThat(collection).isNotEmpty();
    assertThat(collection).doesNotContain(object);
  }

}
