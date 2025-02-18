package uk.co.ogauthority.pwa.features.application.authorisation.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@RunWith(SpringJUnit4ClassRunner.class)
class ApplicationContextUtilsTest {

  private PwaApplicationDetail detail;

  @BeforeEach
  void setUp() {
    detail = new PwaApplicationDetail();
    detail.setStatus(PwaApplicationStatus.DRAFT);
  }

  // use default class to tell sonarcloud we aren't expecting an exception
  @Test
  void performAppStatusCheck_valid() {
    assertDoesNotThrow(() -> {
      ApplicationContextUtils.performAppStatusCheck(Set.of(PwaApplicationStatus.DRAFT), detail);
    });
  }

  @Test
  void performAppStatusCheck_invalid() {
    assertThrows(PwaEntityNotFoundException.class, () ->
      ApplicationContextUtils.performAppStatusCheck(Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW), detail));
  }

}
