package uk.co.ogauthority.pwa.service.diff;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import uk.co.ogauthority.pwa.model.view.StringWithTag;

/**
 * Package private enum defining which classes are supported by diffService.
 */
enum DiffComparisonTypes {
  STRING(Set.of(String.class, Integer.class), new StringComparisonStrategy()),
  LIST(Set.of(List.class), null),
  STRING_WITH_TAG(Set.of(StringWithTag.class), new StringWithTagComparisonStrategy()),
  NOT_SUPPORTED(Collections.emptySet(), null);

  private Set<Class<?>> supportedClasses;
  private final DiffComparisonStrategy<?> diffComparisonStrategy;

  DiffComparisonTypes(Set<Class<?>> supportedClasses, DiffComparisonStrategy<?> diffComparisonStrategy) {
    this.supportedClasses = supportedClasses;
    this.diffComparisonStrategy = diffComparisonStrategy;
  }


  public Set<Class> getSupportedClasses() {
    return Collections.unmodifiableSet(this.supportedClasses);
  }

  public static DiffComparisonTypes findDiffComparisonType(Class searchClass) {
    for (DiffComparisonTypes type : DiffComparisonTypes.values()) {
      for (Class<?> clazz : type.getSupportedClasses()) {
        if (clazz.equals(searchClass)) {
          return type;
        }
      }
    }

    return NOT_SUPPORTED;
  }

  public DiffComparisonStrategy<?> getDiffComparisonStrategy() {
    return diffComparisonStrategy;
  }
}
