package uk.co.ogauthority.pwa.service.appprocessing.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;

@RunWith(MockitoJUnitRunner.class)
public class AcceptApplicationServiceTest {

  private AcceptApplicationService acceptApplicationService;

  @Before
  public void setUp() {
    acceptApplicationService = new AcceptApplicationService();
  }

  @Test
  public void canShowInTaskList() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null);

    boolean canShow = acceptApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

}
