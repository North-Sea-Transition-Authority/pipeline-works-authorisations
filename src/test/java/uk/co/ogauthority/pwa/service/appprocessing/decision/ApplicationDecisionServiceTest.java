package uk.co.ogauthority.pwa.service.appprocessing.decision;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationDecisionServiceTest {

  private ApplicationDecisionService applicationDecisionService;

  @Before
  public void setUp() {
    applicationDecisionService = new ApplicationDecisionService();
  }

  @Test
  public void canShowInTaskList() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null);

    boolean canShow = applicationDecisionService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

}
