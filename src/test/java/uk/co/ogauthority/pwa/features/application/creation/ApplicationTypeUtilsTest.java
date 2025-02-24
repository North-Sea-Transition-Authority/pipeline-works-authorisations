package uk.co.ogauthority.pwa.features.application.creation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;

class ApplicationTypeUtilsTest {

  @Test
  void getFormattedDuration() {
    EnumSet.allOf(PwaApplicationType.class).forEach(type -> {
      switch (type) {
        case INITIAL:
        case CAT_1_VARIATION:
          assertThat(ApplicationTypeUtils.getFormattedDuration(type)).isEqualTo("4-6 months");
          break;
        case CAT_2_VARIATION:
        case HUOO_VARIATION:
        case DEPOSIT_CONSENT:
        case OPTIONS_VARIATION:
          assertThat(ApplicationTypeUtils.getFormattedDuration(type)).isEqualTo("6-8 weeks");
          break;
        case DECOMMISSIONING:
          assertThat(ApplicationTypeUtils.getFormattedDuration(type)).isEqualTo("6 months");
          break;
      }
    });
  }

  @Test
  void getFormattedMedianLineDuration_NotAllowed() {
    assertThrows(ActionNotAllowedException.class, () ->
      EnumSet.allOf(PwaApplicationType.class)
          .stream()
          .filter(type -> type.getMedianLineImplication().equals(MedianLineImplication.FALSE))
          .forEach(ApplicationTypeUtils::getFormattedMedianLineDuration));
  }

  @Test
  void getFormattedMedianLineDuration_Allowed() {
    EnumSet.allOf(PwaApplicationType.class)
        .stream()
        .filter(type -> type.getMedianLineImplication().equals(MedianLineImplication.TRUE))
        .forEach(type -> {
          switch (type) {
            case INITIAL:
            case CAT_1_VARIATION:
            case DECOMMISSIONING:
              assertThat(ApplicationTypeUtils.getFormattedMedianLineDuration(type)).isEqualTo("more than 6 months");
              break;
            case CAT_2_VARIATION:
              assertThat(ApplicationTypeUtils.getFormattedMedianLineDuration(type)).isEqualTo("more than 8 weeks");
              break;
            default:
              throw new RuntimeException(String.format("PwaApplicationType.%s was not expected in test", type.name()));
          }
        });
  }
}