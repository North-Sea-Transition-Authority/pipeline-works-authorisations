package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@RunWith(MockitoJUnitRunner.class)
public class CarbonStorageAreaCrossingServiceTest {

  private CarbonStorageAreaCrossingService carbonStorageAreaCrossingService;

  @Before
  public void setup() {
    carbonStorageAreaCrossingService = new CarbonStorageAreaCrossingService();
  }

  @Test
  public void canShowInTaskList_nonCCUS_noCrossings() {
    var application = new PwaApplication();
    application.setResourceType(PwaResourceType.PETROLEUM);

    var applicationDetail = new PwaApplicationDetail();
    applicationDetail.setPwaApplication(application);
    applicationDetail.setCsaCrossed(false);

    assertFalse(carbonStorageAreaCrossingService.canShowInTaskList(applicationDetail));
  }

  @Test
  public void canShowInTaskList_nonCCUS_crossingsNull() {
    var application = new PwaApplication();
    application.setResourceType(PwaResourceType.PETROLEUM);

    var applicationDetail = new PwaApplicationDetail();
    applicationDetail.setPwaApplication(application);
    applicationDetail.setCsaCrossed(null);

    assertFalse(carbonStorageAreaCrossingService.canShowInTaskList(applicationDetail));
  }

  @Test
  public void canShowInTaskList_CCUS_noCrossings() {
    var application = new PwaApplication();
    application.setResourceType(PwaResourceType.CCUS);

    var applicationDetail = new PwaApplicationDetail();
    applicationDetail.setPwaApplication(application);
    applicationDetail.setCsaCrossed(false);

    assertTrue(carbonStorageAreaCrossingService.canShowInTaskList(applicationDetail));
  }

  @Test
  public void canShowInTaskList_nonCCUS_Crossings() {
    var application = new PwaApplication();
    application.setResourceType(PwaResourceType.PETROLEUM);

    var applicationDetail = new PwaApplicationDetail();
    applicationDetail.setPwaApplication(application);
    applicationDetail.setCsaCrossed(true);

    assertTrue(carbonStorageAreaCrossingService.canShowInTaskList(applicationDetail));
  }

  @Test
  public void canShowInTaskList_CCUS_Crossings() {
    var application = new PwaApplication();
    application.setResourceType(PwaResourceType.CCUS);

    var applicationDetail = new PwaApplicationDetail();
    applicationDetail.setPwaApplication(application);
    applicationDetail.setCsaCrossed(true);

    assertTrue(carbonStorageAreaCrossingService.canShowInTaskList(applicationDetail));
  }

}
