package uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class CrossingAgreementsTaskListServiceTest {

  @Mock
  private ApplicationContext applicationContext;

  private CrossingAgreementsTaskListService taskListService;

  @BeforeEach
  void setUp() {
    taskListService = new CrossingAgreementsTaskListService(applicationContext);
  }

  @Test
  void getServiceBean() {
    EnumSet.allOf(CrossingAgreementTask.class).forEach(crossingAgreementTask -> {
      taskListService.getServiceBean(crossingAgreementTask);
      verify(applicationContext, times(1)).getBean(crossingAgreementTask.getSectionClass());
    });
  }

  @Test
  void getRoute() {
    // Tests to make sure all enums have a route implemented. An error will be thrown if not.
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1);
    EnumSet.allOf(CrossingAgreementTask.class).forEach(crossingAgreementTask ->
      taskListService.getRoute(detail, crossingAgreementTask)
    );
  }

  @Test
  void getOverviewRedirect() {
    // Tests to make sure all enums have a redirect implemented. An error will be thrown if not.
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1);
    EnumSet.allOf(CrossingAgreementTask.class).forEach(crossingAgreementTask ->
      taskListService.getOverviewRedirect(detail, crossingAgreementTask)
    );
  }
}