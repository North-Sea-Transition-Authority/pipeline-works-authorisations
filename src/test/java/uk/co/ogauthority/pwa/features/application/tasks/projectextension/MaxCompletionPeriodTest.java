package uk.co.ogauthority.pwa.features.application.tasks.projectextension;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;

@RunWith(MockitoJUnitRunner.class)
public class MaxCompletionPeriodTest {

  @Test
  public void completionPeriodForEachApplicationType() {
    for (PwaApplicationType applicationType: PwaApplicationType.values()) {
      assertThat(MaxCompletionPeriod.valueOf(applicationType.name())).isNotNull();
    }
  }
}
