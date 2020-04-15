package uk.co.ogauthority.pwa.controller.pwaapplications.initial.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.h2.mvstore.DataUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.ApplicationHolderOrganisation;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.devuk.DevukFieldService;
import uk.co.ogauthority.pwa.service.devuk.PadFieldService;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.ApplicationHolderService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.PwaFieldFormValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = InitialFieldsController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class InitialFieldsControllerTest extends PwaApplicationContextAbstractControllerTest {

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private DevukFieldService devukFieldService;

  @MockBean
  private PwaApplicationService pwaApplicationService;

  @MockBean
  private ApplicationHolderService applicationHolderService;

  @MockBean
  private PadFieldService padFieldService;

  @SpyBean
  private PwaFieldFormValidator pwaFieldFormValidator;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private PadField padField;
  private DevukField devukField;
  private ApplicationHolderOrganisation applicationHolderOrganisation;
  private PortalOrganisationUnit portalOrganisationUnit;

  @Before
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    devukField = new DevukField(1, "abc", 500);
    padField = new PadField();
    padField.setId(1);
    padField.setDevukField(devukField);
    portalOrganisationUnit = new PortalOrganisationUnit();
    applicationHolderOrganisation = new ApplicationHolderOrganisation();
    applicationHolderOrganisation.setOrganisationUnit(portalOrganisationUnit);

    when(pwaApplicationDetailService.getTipDetail(anyInt())).thenReturn(pwaApplicationDetail);
    when(pwaContactService.getContactRoles(any(), any())).thenReturn(EnumSet.allOf(PwaContactRole.class));


    when(padFieldService.getActiveFieldsForApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(
        padField));

    when(applicationHolderService.getHoldersFromApplicationDetail(pwaApplicationDetail)).thenReturn(
        List.of(applicationHolderOrganisation));

    when(devukFieldService.getByOrganisationUnitWithStatusCodes(applicationHolderOrganisation.getOrganisationUnit(),
        List.of(500, 600, 700))).thenReturn(List.of(devukField));
  }

  @Test
  public void testAuthentication() throws Exception {
    var wua = new WebUserAccount();
    var user = new AuthenticatedUserAccount(wua, List.of());
    mockMvc.perform(get(ReverseRouter.route(on(InitialFieldsController.class)
        .renderFields(PwaApplicationType.INITIAL, 1, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isOk());

    mockMvc.perform(post(ReverseRouter.route(on(InitialFieldsController.class)
        .renderFields(PwaApplicationType.INITIAL, 1, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  public void testAuthentication_Invalid() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(InitialFieldsController.class)
        .renderFields(PwaApplicationType.INITIAL, 1, null, null, null))))
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(post(ReverseRouter.route(on(InitialFieldsController.class)
        .renderFields(PwaApplicationType.INITIAL, 1, null, null, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderFields() throws Exception {
    var wua = new WebUserAccount();
    var user = new AuthenticatedUserAccount(wua, List.of());
    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(InitialFieldsController.class)
        .renderFields(PwaApplicationType.INITIAL, 1, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("breadcrumbMap", "currentPage", "backUrl"))
        .andExpect(view().name("pwaApplication/initial/fieldInformation"))
        .andReturn()
        .getModelAndView();
    var fields = (List<PadField>) modelAndView.getModel().get("fields");
    var fieldMap = (Map<String, String>) modelAndView.getModel().get("fieldMap");
    assertThat(fields).containsExactly(padField);
    assertThat(fieldMap).containsExactly(new DataUtils.MapEntry<>(padField.getDevukField().getFieldId().toString(), padField.getDevukField().getFieldName()));
  }

  @Test
  public void postFields_FailedValidationNoRadio() throws Exception {
    var wua = new WebUserAccount();
    var user = new AuthenticatedUserAccount(wua, List.of());
    mockMvc.perform(post(ReverseRouter.route(on(InitialFieldsController.class)
        .renderFields(PwaApplicationType.INITIAL, 1, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  public void postFields_FailedValidationNoSelection() throws Exception {
    var wua = new WebUserAccount();
    var user = new AuthenticatedUserAccount(wua, List.of());
    mockMvc.perform(post(ReverseRouter.route(on(InitialFieldsController.class)
        .postFields(PwaApplicationType.INITIAL, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("result", "true"))
        .andExpect(status().isOk());
  }

  @Test
  public void postFields_Valid_SetSingleCalled() throws Exception {
    var wua = new WebUserAccount();
    var user = new AuthenticatedUserAccount(wua, List.of());

    when(devukFieldService.findById(1)).thenReturn(devukField);

    mockMvc.perform(post(ReverseRouter.route(on(InitialFieldsController.class)
        .postFields(PwaApplicationType.INITIAL, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("linkedToField", "true")
        .param("fieldId", "1"))
        .andExpect(status().is3xxRedirection());

    var argumentCaptor = ArgumentCaptor.forClass(List.of(new DevukField()).getClass());
    verify(padFieldService, times(1)).setFields(eq(pwaApplicationDetail), argumentCaptor.capture());

    assertThat((List<DevukField>) argumentCaptor.getValue()).containsExactly(devukField);
  }

  @Test
  public void postFields_Valid_EndAllCalled() throws Exception {

    var wua = new WebUserAccount();
    var user = new AuthenticatedUserAccount(wua, List.of());
    mockMvc.perform(post(ReverseRouter.route(on(InitialFieldsController.class)
        .postFields(PwaApplicationType.INITIAL, 1, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param("linkedToField", "false"))
        .andExpect(status().is3xxRedirection());

    verify(padFieldService, times(1)).setFields(eq(pwaApplicationDetail), any());
  }
}