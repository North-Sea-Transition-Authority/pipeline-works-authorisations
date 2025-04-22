package uk.co.ogauthority.pwa.controller.documents;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.ResolverAbstractControllerTest;
import uk.co.ogauthority.pwa.controller.WithDefaultPageControllerAdvice;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

@WebMvcTest(DocumentTemplateSelectController.class)
@ContextConfiguration(classes = DocumentTemplateSelectController.class)
@WithDefaultPageControllerAdvice
class DocumentTemplateSelectControllerTest extends ResolverAbstractControllerTest {

  private AuthenticatedUserAccount templateClauseManager, caseOfficer, pwaManagerAndTcManager;

  @BeforeEach
  void setUp() {

    templateClauseManager = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_ACCESS));
    caseOfficer = new AuthenticatedUserAccount(new WebUserAccount(2), List.of(PwaUserPrivilege.PWA_ACCESS));
    pwaManagerAndTcManager = new AuthenticatedUserAccount(new WebUserAccount(3), List.of(PwaUserPrivilege.PWA_ACCESS));

    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(templateClauseManager, Map.of(TeamType.REGULATOR, Set.of(Role.TEMPLATE_CLAUSE_MANAGER))))
        .thenReturn(true);
    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(pwaManagerAndTcManager, Map.of(TeamType.REGULATOR, Set.of(Role.TEMPLATE_CLAUSE_MANAGER))))
        .thenReturn(true);
    doCallRealMethod().when(hasTeamRoleService).userHasAnyRoleInTeamType(any(AuthenticatedUserAccount.class),
        eq(TeamType.REGULATOR), anySet());
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

    when(hasTeamRoleService.userHasAnyRoleInTeamType(pwaManagerAndTcManager, TeamType.REGULATOR, Set.of(Role.PWA_MANAGER)))
        .thenReturn(true);

    mockMvc.perform(get("/document-templates/select")
            .with(user(pwaManagerAndTcManager)))
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
