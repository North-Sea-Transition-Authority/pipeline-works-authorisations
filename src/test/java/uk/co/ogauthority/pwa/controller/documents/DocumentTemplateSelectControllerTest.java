package uk.co.ogauthority.pwa.controller.documents;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;

@RunWith(SpringRunner.class)
@WebMvcTest(DocumentTemplateSelectController.class)
@Import(PwaMvcTestConfiguration.class)
public class DocumentTemplateSelectControllerTest extends AbstractControllerTest {

  private AuthenticatedUserAccount templateClauseManager, caseOfficer, pwaManager;

  @Before
  public void setUp() throws Exception {

    templateClauseManager = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_TEMPLATE_CLAUSE_MANAGE));
    caseOfficer = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_CASE_OFFICER));
    pwaManager = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_TEMPLATE_CLAUSE_MANAGE, PwaUserPrivilege.PWA_MANAGER));
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

  @Test
  public void getTemplatesForSelect_doesntHaveTcPriv() throws Exception {
    mockMvc.perform(get("/document-templates/select")
            .with(authenticatedUserAndSession(templateClauseManager)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("tcManagementAllowed", false));
  }

  @Test
  public void getTemplatesForSelect_HasTcPriv() throws Exception {
    mockMvc.perform(get("/document-templates/select")
            .with(authenticatedUserAndSession(pwaManager)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("tcManagementAllowed", true));
  }

  @Test
  public void getTemplatesForSelect_SmokeTest() throws Exception {
    mockMvc.perform(get("/document-templates/select")
            .with(authenticatedUserAndSession(templateClauseManager)))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists(
            "documentTemplates",
            "urlProvider",
            "tcManagementAllowed",
            "tcUrl"
            ));
  }

}
