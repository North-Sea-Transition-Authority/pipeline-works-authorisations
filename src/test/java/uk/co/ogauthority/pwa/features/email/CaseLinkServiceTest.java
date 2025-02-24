package uk.co.ogauthority.pwa.features.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.submission.controller.ReviewAndSubmitController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@ActiveProfiles("integration-test")
class CaseLinkServiceTest {

  @Autowired
  private CaseLinkService caseLinkService;

  @Test
  void generateCaseManagementLink_application() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    String caseManagementLink = caseLinkService.generateCaseManagementLink(detail.getPwaApplication());

    assertThat(caseManagementLink).isEqualTo("http://test/test" + CaseManagementUtils.routeCaseManagement(detail.getPwaApplication()));

  }

  @Test
  void generateReviewAndSubmitLink_application() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    String caseManagementLink = caseLinkService.generateReviewAndSubmitLink(detail.getPwaApplication());

    assertThat(caseManagementLink).isEqualTo("http://test/test" + ReverseRouter.route(on(ReviewAndSubmitController.class)
        .review(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)));

  }

}
