package uk.co.ogauthority.pwa.validators.appprocessing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationValidationServiceTest {

  @Mock
  private TaskListService taskListService;

  private PwaApplicationValidationService pwaApplicationValidationService;

  private final PwaApplicationDetail detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1);

  @Before
  public void setup() {
    pwaApplicationValidationService = new PwaApplicationValidationService(taskListService);
  }

  @Test
  public void isApplicationValid_valid() {
    when(taskListService.areAllApplicationTasksComplete(detail)).thenReturn(true);
    assertTrue(pwaApplicationValidationService.isApplicationValid(detail));
  }

  @Test
  public void isApplicationValid_invalid() {
    when(taskListService.areAllApplicationTasksComplete(detail)).thenReturn(false);
    assertFalse(pwaApplicationValidationService.isApplicationValid(detail));
  }

}
