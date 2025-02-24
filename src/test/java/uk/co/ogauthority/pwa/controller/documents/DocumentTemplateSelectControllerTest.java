package uk.co.ogauthority.pwa.controller.documents;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;

@WebMvcTest(DocumentTemplateSelectController.class)
@Import(PwaMvcTestConfiguration.class)
class DocumentTemplateSelectControllerTest extends AbstractControllerTest {

  private AuthenticatedUserAccount templateClauseManager, caseOfficer, pwaManager;

  @BeforeEach
  void setUp() throws Exception {

    templateClauseManager = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_TEMPLATE_CLAUSE_MANAGE));
    caseOfficer = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_CASE_OFFICER));
    pwaManager = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_TEMPLATE_CLAUSE_MANAGE, PwaUserPrivilege.PWA_MANAGER));
  }

  @Test
  void getTemplatesForSelect_hasPriv() throws Exception {

    mockMvc.perform(get("/document-templates/select")
        .with(user(templateClauseManager)))
        .andExpect(status().isOk());

  }

  @Test
  void getTemplatesForSelect_doesntHavePriv() throws Exception {

    mockMvc.perform(get("/document-templates/select")
        .with(user(caseOfficer)))
        .andExpect(status().isForbidden());

  }

  @Test
  void getTemplatesForSelect_doesntHaveTcPriv() throws Exception {
    mockMvc.perform(get("/document-templates/select")
            .with(user(templateClauseManager)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("tcManagementAllowed", false));
  }

  @Test
  void getTemplatesForSelect_HasTcPriv() throws Exception {
    mockMvc.perform(get("/document-templates/select")
            .with(user(pwaManager)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("tcManagementAllowed", true));
  }

  @Test
  void getTemplatesForSelect_SmokeTest() throws Exception {
    mockMvc.perform(get("/document-templates/select")
            .with(user(templateClauseManager)))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists(
            "documentTemplates",
            "urlProvider",
            "tcManagementAllowed",
            "tcUrl"
            ));
  }

}
