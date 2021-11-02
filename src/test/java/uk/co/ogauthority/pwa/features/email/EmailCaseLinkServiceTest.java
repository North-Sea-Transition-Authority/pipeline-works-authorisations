package uk.co.ogauthority.pwa.features.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.submission.ReviewAndSubmitController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@ActiveProfiles("integration-test")
public class EmailCaseLinkServiceTest {

  @Autowired
  private EmailCaseLinkService emailCaseLinkService;

  @Test
  public void generateCaseManagementLink_application() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    String caseManagementLink = emailCaseLinkService.generateCaseManagementLink(detail.getPwaApplication());

    assertThat(caseManagementLink).isEqualTo("http://test/test" + CaseManagementUtils.routeCaseManagement(detail.getPwaApplication()));

  }

  @Test
  public void generateReviewAndSubmitLink_application() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    String caseManagementLink = emailCaseLinkService.generateReviewAndSubmitLink(detail.getPwaApplication());

    assertThat(caseManagementLink).isEqualTo("http://test/test" + ReverseRouter.route(on(ReviewAndSubmitController.class)
        .review(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)));

  }

}
