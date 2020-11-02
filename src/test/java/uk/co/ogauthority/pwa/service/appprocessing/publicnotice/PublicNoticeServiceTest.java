package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.appprocessing.PublicNoticeService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;

@RunWith(MockitoJUnitRunner.class)
public class PublicNoticeServiceTest {

  private PublicNoticeService publicNoticeService;

  @Before
  public void setUp() {
    publicNoticeService = new PublicNoticeService();
  }

  @Test
  public void canShowInTaskList_editConsentDocumentPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.PUBLIC_NOTICE), null, null);

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_caseManagementIndustryPermission_true() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, null);

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_noPermissions_false() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null, null);

    boolean canShow = publicNoticeService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

}
