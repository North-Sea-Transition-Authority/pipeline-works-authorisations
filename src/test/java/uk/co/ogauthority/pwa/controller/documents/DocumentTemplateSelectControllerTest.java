package uk.co.ogauthority.pwa.controller.documents;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;

@RunWith(SpringRunner.class)
@WebMvcTest(DocumentTemplateSelectController.class)
public class DocumentTemplateSelectControllerTest extends AbstractControllerTest {

  @MockBean
  private PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  private PwaAppProcessingContextService appProcessingContextService;

  private AuthenticatedUserAccount templateClauseManager, caseOfficer;

  @Before
  public void setUp() throws Exception {

    templateClauseManager = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_TEMPLATE_CLAUSE_MANAGE));
    caseOfficer = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_CASE_OFFICER));

  }

  @Test
  public void getTemplatesForSelect_hasPriv() throws Exception {

    mockMvc.perform(get("/document-templates/select")
        .with(authenticatedUserAndSession(templateClauseManager)))
        .andExpect(status().isOk());

  }

  @Test
  public void getTemplatesForSelect_doesntHavePriv() throws Exception {

    mockMvc.perform(get("/document-templates/select")
        .with(authenticatedUserAndSession(caseOfficer)))
        .andExpect(status().isForbidden());

  }

}