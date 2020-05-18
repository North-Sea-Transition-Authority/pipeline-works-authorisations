package uk.co.ogauthority.pwa.service.tasklist;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.EnumSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class CrossingAgreementsTaskListServiceTest {

  @Mock
  private ApplicationContext applicationContext;

  private CrossingAgreementsTaskListService taskListService;

  @Before
  public void setUp() {
    taskListService = new CrossingAgreementsTaskListService(applicationContext);
  }

  @Test
  public void getServiceBean() {
    EnumSet.allOf(CrossingAgreementTask.class).forEach(crossingAgreementTask -> {
      taskListService.getServiceBean(crossingAgreementTask);
      verify(applicationContext, times(1)).getBean(crossingAgreementTask.getSectionClass());
    });
  }

  @Test
  public void getRoute() {
    // Tests to make sure all enums have a route implemented. An error will be thrown if not.
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1);
    EnumSet.allOf(CrossingAgreementTask.class).forEach(crossingAgreementTask ->
      taskListService.getRoute(detail, crossingAgreementTask)
    );
  }

  @Test
  public void getOverviewRedirect() {
    // Tests to make sure all enums have a redirect implemented. An error will be thrown if not.
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1);
    EnumSet.allOf(CrossingAgreementTask.class).forEach(crossingAgreementTask ->
      taskListService.getOverviewRedirect(detail, crossingAgreementTask)
    );
  }
}