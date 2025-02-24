package uk.co.ogauthority.pwa.validators.appprocessing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PwaApplicationValidationServiceTest {

  @Mock
  private TaskListService taskListService;

  private PwaApplicationValidationService pwaApplicationValidationService;

  private final PwaApplicationDetail detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1);

  @BeforeEach
  void setup() {
    pwaApplicationValidationService = new PwaApplicationValidationService(taskListService);
  }

  @Test
  void isApplicationValid_valid() {
    when(taskListService.areAllApplicationTasksComplete(detail)).thenReturn(true);
    assertTrue(pwaApplicationValidationService.isApplicationValid(detail));
  }

  @Test
  void isApplicationValid_invalid() {
    when(taskListService.areAllApplicationTasksComplete(detail)).thenReturn(false);
    assertFalse(pwaApplicationValidationService.isApplicationValid(detail));
  }

}
