package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.appprocessing.PublicNoticeService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;

@RunWith(MockitoJUnitRunner.class)
public class PublicNoticeServiceTest {

  private PublicNoticeService publicNoticeService;

  @Before
  public void setUp() {
    publicNoticeService = new PublicNoticeService();
  }

  @Test
  public void canShowInTaskList() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null);

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

}
