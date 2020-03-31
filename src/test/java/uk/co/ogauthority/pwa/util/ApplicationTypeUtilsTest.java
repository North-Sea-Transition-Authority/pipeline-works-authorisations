package uk.co.ogauthority.pwa.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import org.junit.Test;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.MedianLineImplication;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class ApplicationTypeUtilsTest {

  @Test
  public void getFormattedDuration() {
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
          assertThat(ApplicationTypeUtils.getFormattedDuration(type)).isEqualTo("6+ months");
          break;
      }
    });
  }

  @Test(expected = ActionNotAllowedException.class)
  public void getFormattedMedianLineDuration_NotAllowed() {
    EnumSet.allOf(PwaApplicationType.class)
        .stream()
        .filter(type -> type.getMedianLineImplication().equals(MedianLineImplication.FALSE))
        .forEach(ApplicationTypeUtils::getFormattedMedianLineDuration);
  }

  @Test
  public void getFormattedMedianLineDuration_Allowed() {
    EnumSet.allOf(PwaApplicationType.class)
        .stream()
        .filter(type -> type.getMedianLineImplication().equals(MedianLineImplication.TRUE))
        .forEach(type -> {
          switch (type) {
            case CAT_1_VARIATION:
            case DECOMMISSIONING:
              assertThat(ApplicationTypeUtils.getFormattedMedianLineDuration(type)).isEqualTo("6+ months");
              break;
            case CAT_2_VARIATION:
              assertThat(ApplicationTypeUtils.getFormattedMedianLineDuration(type)).isEqualTo("8+ weeks");
              break;
            default:
              throw new RuntimeException(String.format("PwaApplicationType.%s was not expected in test", type.name()));
          }
        });
  }
}