package uk.co.ogauthority.pwa.features.application.tasks.projectextension;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;

@ExtendWith(MockitoExtension.class)
class MaxCompletionPeriodTest {

  @Test
  void completionPeriodForEachApplicationType() {
    for (PwaApplicationType applicationType: PwaApplicationType.values()) {
      assertThat(MaxCompletionPeriod.valueOf(applicationType.name())).isNotNull();
    }
  }
}
