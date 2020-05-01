package uk.co.ogauthority.pwa.controller.pwaapplications.initial;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.util.ControllerTestUtils;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.TeamTestingUtils;
import uk.co.ogauthority.pwa.validators.PwaHolderFormValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PwaHolderController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PwaHolderControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 1;

  @MockBean
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @SpyBean
  private ApplicationBreadcrumbService breadcrumbService;

  @MockBean
  private PwaHolderFormValidator pwaHolderFormValidator;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(123),
      Collections.emptyList());

  private PortalOrganisationUnit orgUnit;

  private PwaApplicationDetail detail;

  @MockBean
  private PadOrganisationRoleService padOrganisationRoleService;

  @Before
  public void before() {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.getPwaApplication().setId(APP_ID);

    when(pwaApplicationDetailService.getTipDetail(APP_ID)).thenReturn(detail);
    when(pwaContactService.getContactRoles(any(), any())).thenReturn(EnumSet.allOf(PwaContactRole.class));

    orgUnit = TeamTestingUtils.createOrgUnit();
    when(portalOrganisationsAccessor.getOrganisationUnitById(111)).thenReturn(Optional.of(orgUnit));
    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(any())).thenReturn(List.of(orgUnit));

  }

  @Test
  public void renderHolderScreen_withAuthenticatedUser() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(PwaHolderController.class)
        .renderHolderScreen(PwaApplicationType.INITIAL, APP_ID, null, null, null)))
        .with(authenticatedUserAndSession(user))
    ).andExpect(status().isOk());

  }

  @Test
  public void postHolderScreen_withHolderOrgId() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class)
        .postHolderScreen(PwaApplicationType.INITIAL, APP_ID, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("holderOuId", "111"))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void postHolderScreen_whenPathAppIdDoesntExist_andHolderOrgSet() throws Exception {

    when(pwaApplicationDetailService.getTipDetail(123)).thenThrow(new PwaEntityNotFoundException(""));

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class)
        .postHolderScreen(PwaApplicationType.INITIAL, 123, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("holderOuId", "111"))
        .andExpect(status().is4xxClientError());

  }

  @Test
  public void postHolderScreen_whenAppExists_andNoHolderOrgSelected() throws Exception {

    ControllerTestUtils.mockValidatorErrors(pwaHolderFormValidator, List.of("holderOuId"));

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class)
        .postHolderScreen(PwaApplicationType.INITIAL, APP_ID, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("holderOuId", ""))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/form/holder"))
        .andExpect(model().attributeHasErrors("form"));

  }

  @Test
  public void postHolderScreen_whenAppExists_andHolderOrgDoesntExist() throws Exception {

    ControllerTestUtils.mockValidatorErrors(pwaHolderFormValidator, List.of("holderOuId"));

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class)
        .postHolderScreen(PwaApplicationType.INITIAL, APP_ID, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("holderOuId", "999"))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/form/holder"))
        .andExpect(model().attributeHasErrors("form"));

  }

  @Test
  public void postHolderScreen_whenAppExists_andHolderOrgExists_andUserDoesntHaveAccessToOrg() throws Exception {

    when(portalOrganisationsAccessor.getOrganisationUnitById(44)).thenReturn(Optional.of(orgUnit));
    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(any())).thenReturn(List.of());

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class)
        .postHolderScreen(PwaApplicationType.INITIAL, APP_ID, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("holderOuId", "44"))
        .andExpect(status().is4xxClientError());

  }

}
