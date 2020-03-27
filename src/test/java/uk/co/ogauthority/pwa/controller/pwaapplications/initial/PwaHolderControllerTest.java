package uk.co.ogauthority.pwa.controller.pwaapplications.initial;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaHolderForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.ApplicationHolderService;
import uk.co.ogauthority.pwa.util.ControllerTestUtils;
import uk.co.ogauthority.pwa.util.TeamTestingUtils;
import uk.co.ogauthority.pwa.validators.PwaHolderFormValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PwaHolderController.class)
public class PwaHolderControllerTest extends AbstractControllerTest {

  private static final int APP_ID = 1;

  @MockBean
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @MockBean
  private ApplicationHolderService applicationHolderService;

  @SpyBean
  private ApplicationBreadcrumbService breadcrumbService;

  @MockBean
  private PwaHolderFormValidator pwaHolderFormValidator;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(123), Collections.emptyList());

  private PortalOrganisationUnit orgUnit;

  @Before
  public void before() {

    MasterPwa pwa = new MasterPwa();
    PwaApplication application = new PwaApplication(pwa, PwaApplicationType.INITIAL, 0);
    application.setId(1);
    PwaApplicationDetail detail = new PwaApplicationDetail(application, 1, 123, Instant.now());
    when(pwaApplicationDetailService.getTipDetailWithStatus(1, PwaApplicationStatus.DRAFT)).thenReturn(detail);
    given(pwaApplicationDetailService.getTipDetailWithStatus(123, PwaApplicationStatus.DRAFT))
        .willThrow(PwaEntityNotFoundException.class);
    when(pwaApplicationDetailService.withDraftTipDetail(any(), any(), any())).thenCallRealMethod();

    orgUnit = TeamTestingUtils.createOrgUnit();
    when(portalOrganisationsAccessor.getOrganisationUnitById(111)).thenReturn(Optional.of(orgUnit));
    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(any())).thenReturn(List.of(orgUnit));

    when(applicationHolderService.mapHolderDetailsToForm(any())).thenReturn(new PwaHolderForm());

  }

  @Test
  public void renderHolderScreen_withAuthenticatedUser() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(PwaHolderController.class).renderHolderScreen(PwaApplicationType.INITIAL, APP_ID, null, null)))
      .with(authenticatedUserAndSession(user))
    ).andExpect(status().isOk());

  }

  @Test
  public void postHolderScreen_withHolderOrgId() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class).postHolderScreen(PwaApplicationType.INITIAL, APP_ID, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("holderOuId", "111"))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void postHolderScreen_whenPathAppIdDoesntExist_andHolderOrgSet() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class).postHolderScreen(PwaApplicationType.INITIAL, 123, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("holderOuId", "111"))
        .andExpect(status().is4xxClientError());

  }

  @Test
  public void postHolderScreen_whenAppExists_andNoHolderOrgSelected() throws Exception {

    ControllerTestUtils.mockValidatorErrors(pwaHolderFormValidator, List.of("holderOuId"));

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class).postHolderScreen(PwaApplicationType.INITIAL, APP_ID, null, null, null)))
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

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class).postHolderScreen(PwaApplicationType.INITIAL, APP_ID, null, null, null)))
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

    mockMvc.perform(post(ReverseRouter.route(on(PwaHolderController.class).postHolderScreen(PwaApplicationType.INITIAL, APP_ID, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("holderOuId", "44"))
        .andExpect(status().is4xxClientError());

  }

}
