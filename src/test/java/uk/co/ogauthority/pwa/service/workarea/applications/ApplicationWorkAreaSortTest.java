package uk.co.ogauthority.pwa.service.workarea.applications;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;

@ExtendWith(MockitoExtension.class)
class ApplicationWorkAreaSortTest {

  @Test
  void allValuesHaveValidSortProperty() {

    var validFieldsSet = Arrays.stream(FieldUtils.getAllFields(WorkAreaApplicationDetailSearchItem.class))
        .map(Field::getName)
        .collect(Collectors.toSet());

    for (ApplicationWorkAreaSort applicationWorkAreaSort : ApplicationWorkAreaSort.values()) {
      try {
        assertThat(validFieldsSet.contains(applicationWorkAreaSort.getPrimarySortAttribute())).isTrue();
      } catch (AssertionError e) {
        throw new AssertionError("Fail at sort:" + applicationWorkAreaSort.toString() + "\n" + e.getMessage(), e);
      }
    }

  }
}