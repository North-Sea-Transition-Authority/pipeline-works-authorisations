package uk.co.ogauthority.pwa.service.workarea.consultations;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.service.consultations.search.ConsultationRequestSearchItem;

@ExtendWith(MockitoExtension.class)
class ConsultationWorkAreaSortTest {

  @Test
  void allValuesHaveValidSortProperty() {

    var validFieldsSet = Arrays.stream(FieldUtils.getAllFields(ConsultationRequestSearchItem.class))
        .map(Field::getName)
        .collect(Collectors.toSet());

    Stream.of(ConsultationWorkAreaSort.values()).forEach(sort -> {

      try {
        assertThat(validFieldsSet.contains(sort.getPrimarySortAttribute())).isTrue();
      } catch (AssertionError e) {
        throw new AssertionError("Fail at sort:" + sort.toString() + "\n" + e.getMessage(), e);
      }

    });

  }
}