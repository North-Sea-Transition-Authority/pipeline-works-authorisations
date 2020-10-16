package uk.co.ogauthority.pwa.service.notify;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
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

}
