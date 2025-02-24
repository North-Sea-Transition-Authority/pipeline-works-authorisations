package uk.co.ogauthority.pwa.integrations.energyportal.organisations.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;

@WebMvcTest(controllers = PortalOrganisationUnitRestController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class PortalOrganisationUnitRestControllerTest extends PwaApplicationContextAbstractControllerTest {

  @MockBean
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @MockBean
  private SearchSelectorService searchSelectorService;

  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() {
    user = new AuthenticatedUserAccount(new WebUserAccount(), Set.of(PwaUserPrivilege.PWA_ACCESS));
  }

  @Test
  void searchPortalOrgUnits_unauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class).searchPortalOrgUnits("Test"))))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  void searchPortalOrgUnits_authenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class).searchPortalOrgUnits("Test")))
            .with(user(user))
            .with(csrf())
    ).andExpect(status().isOk());
  }

  @Test
  void searchPortalOrgUnitsNoManualEntry_authenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class).searchPortalOrgUnitsNoManualEntry("Test")))
            .with(user(user))
            .with(csrf())
    ).andExpect(status().isOk());
  }

  @Test
  void searchPortalOrgUnitsNoManualEntry_unauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(PortalOrganisationUnitRestController.class).searchPortalOrgUnitsNoManualEntry("Test")))
            .with(csrf())
    ).andExpect(status().is3xxRedirection());
  }
}