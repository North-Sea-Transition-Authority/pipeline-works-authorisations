package uk.co.ogauthority.pwa.service.appprocessing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;

@RunWith(MockitoJUnitRunner.class)
public class CaseSetupServiceTest {

  private CaseSetupService caseSetupService;

  @Before
  public void setUp() {
    caseSetupService = new CaseSetupService();
  }

  @Test
  public void canShowInTaskList() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_OFFICER_REVIEW), null);

    boolean canShow = caseSetupService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_industry() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null);

    boolean canShow = caseSetupService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

}
