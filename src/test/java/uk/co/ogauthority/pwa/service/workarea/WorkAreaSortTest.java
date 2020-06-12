package uk.co.ogauthority.pwa.service.workarea;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;

@RunWith(MockitoJUnitRunner.class)
public class WorkAreaSortTest {

  @Test
  public void allValuesHaveValidSortProperty() {

    var validFieldsSet = Arrays.stream(FieldUtils.getAllFields(ApplicationDetailSearchItem.class))
        .map(Field::getName)
        .collect(Collectors.toSet());

    for (WorkAreaSort workAreaSort : WorkAreaSort.values()) {
      try {
        assertThat(validFieldsSet.contains(workAreaSort.getSortAttribute())).isTrue();
      } catch (AssertionError e) {
        throw new AssertionError("Fail at sort:" + workAreaSort.toString() + "\n" + e.getMessage(), e);
      }
    }

  }
}