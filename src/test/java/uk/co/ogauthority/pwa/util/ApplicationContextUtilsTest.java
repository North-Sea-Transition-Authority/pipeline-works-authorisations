package uk.co.ogauthority.pwa.util;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@RunWith(SpringJUnit4ClassRunner.class)
public class ApplicationContextUtilsTest {

  private PwaApplicationDetail detail;

  @Before
  public void setUp() {
    detail = new PwaApplicationDetail();
    detail.setStatus(PwaApplicationStatus.DRAFT);
  }

  @Test
  public void performAppStatusCheck_valid() {
    ApplicationContextUtils.performAppStatusCheck(Set.of(PwaApplicationStatus.DRAFT), detail);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void performAppStatusCheck_invalid() {
    ApplicationContextUtils.performAppStatusCheck(Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW), detail);
  }

}
