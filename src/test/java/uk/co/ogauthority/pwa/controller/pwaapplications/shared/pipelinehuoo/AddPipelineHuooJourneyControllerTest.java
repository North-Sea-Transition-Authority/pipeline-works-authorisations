package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.AddPipelineHuooJourneyController.UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickablePipelineService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AddPipelineHuooJourneyController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class AddPipelineHuooJourneyControllerTest extends PwaApplicationContextAbstractControllerTest {
  private final HuooRole DEFAULT_ROLE = HuooRole.HOLDER;
  private final int APP_ID = 10;
  private final PwaApplicationType APP_TYPE = PwaApplicationType.INITIAL;
  private final String PICKED_PIPELINE_STRING = "123++CONSENTED,123++APPLICATION";
  private final String PICKED_ORG_STRING = "1,100";
  private final Set<String> PICKED_PIPELINE_IDS = Set.of(PICKED_PIPELINE_STRING.split(","));
  private final Set<Integer> PICKED_ORG_IDS = Set.of(PICKED_ORG_STRING.split(",")).stream()
      .map(Integer::valueOf).collect(
          Collectors.toSet());


  private final String FORM_PICKED_PIPELINE_ATTR = "pickedPipelineStrings";
  private final String FORM_PICKED_ORG_ATTR = "organisationUnitIds";
  private final String FORM_PICKED_TREATY_ATTR = "treatyAgreements";

  private PwaApplicationEndpointTestBuilder endpointTester;

  @MockBean
  private PadPipelinesHuooService padPipelinesHuooService;

  @MockBean
  private PickablePipelineService pickablePipelineService;

  private PwaApplicationDetail pwaApplicationDetail;
  private WebUserAccount wua = new WebUserAccount(1);
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(wua, EnumSet.allOf(PwaUserPrivilege.class));

  @Before
  public void setup() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_TYPE, APP_ID);
    when(pwaApplicationDetailService.getTipDetail(APP_ID)).thenReturn(pwaApplicationDetail);
    when(pwaContactService.getContactRoles(eq(pwaApplicationDetail.getPwaApplication()), any()))
        .thenReturn(EnumSet.allOf(PwaContactRole.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaContactService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.HUOO_VARIATION)
        .setAllowedContactRoles(PwaContactRole.PREPARER)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);

  }

  @Test
  public void renderPipelinesForHuooAssignment_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(AddPipelineHuooJourneyController.class).renderPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderPipelinesForHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(AddPipelineHuooJourneyController.class).renderPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderPipelinesForHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(AddPipelineHuooJourneyController.class).renderPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void selectPipelinesForHuooAssignment_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(AddPipelineHuooJourneyController.class).selectPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null
            ))));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void selectPipelinesForHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(AddPipelineHuooJourneyController.class).selectPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null
            ))));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void selectPipelinesForHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(AddPipelineHuooJourneyController.class).selectPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null
            ))));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void selectPipelinesForHuooAssignment_validForm_setsJourneyDataAndRedirects_thenLoadsJourneyDataIntoForm() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(AddPipelineHuooJourneyController.class)
        .selectPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_PIPELINE_ATTR, PICKED_PIPELINE_STRING)
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(result -> result.getModelAndView().getViewName().equals(
            "pwaApplication/shared/pipelinehuoo/addPipelineHuooAssociateOrganisations"));

    MvcResult result = mockMvc.perform(get(ReverseRouter.route(on(AddPipelineHuooJourneyController.class)
        .renderOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null)))
        .with(authenticatedUserAndSession(user))
    )
        //  .andExpect(status().isOk())
        .andExpect(model().attributeExists("form"))
        .andReturn();
    PickHuooPipelinesForm form = (PickHuooPipelinesForm) result.getModelAndView().getModel().get("form");
    assertThat(form.getPickedPipelineStrings()).isEqualTo(PICKED_PIPELINE_IDS);

  }

  @Test
  public void selectPipelinesForHuooAssignment_invalidForm() throws Exception {

    doAnswer(invocation -> {
      // add error to binding result
      ((BindingResult) invocation.getArgument(2)).rejectValue(FORM_PICKED_PIPELINE_ATTR,
          FORM_PICKED_PIPELINE_ATTR + ".invalid", "blah");
      return invocation;
    }).when(padPipelinesHuooService).validateAddPipelineHuooForm(any(), any(), any(), any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(AddPipelineHuooJourneyController.class)
        .selectPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().isOk());


  }

  @Test
  public void renderOrganisationsForPipelineHuooAssignment_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(AddPipelineHuooJourneyController.class).renderOrganisationsForPipelineHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderOrganisationsForPipelineHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(AddPipelineHuooJourneyController.class).renderOrganisationsForPipelineHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderOrganisationsForPipelineHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(AddPipelineHuooJourneyController.class).renderOrganisationsForPipelineHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void selectOrganisationsForPipelineHuooAssignment_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(AddPipelineHuooJourneyController.class).selectOrganisationsForPipelineHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null,
                null
            ))));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void selectOrganisationsForPipelineHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(AddPipelineHuooJourneyController.class).selectOrganisationsForPipelineHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null,
                null
            ))));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void selectOrganisationsForPipelineHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(AddPipelineHuooJourneyController.class).selectOrganisationsForPipelineHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null,
                null
            ))));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void completeValidJourney_journeyCompleteSubmitServiceInteraction() throws Exception {

    var pickedPipelines = Set.of(new Pipeline(), new Pipeline());
    var foundPadOrgRoles = List.of(new PadOrganisationRole(), new PadOrganisationRole());
    when(pickablePipelineService.getPickedPipelinesFromStrings(any())).thenReturn(pickedPipelines);
    when(padPipelinesHuooService.getPadOrganisationRolesFrom(any(), any(), any(), any())).thenReturn(foundPadOrgRoles);

    mockMvc.perform(post(ReverseRouter.route(on(AddPipelineHuooJourneyController.class)
        .selectPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_PIPELINE_ATTR, PICKED_PIPELINE_STRING)
    )
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(post(ReverseRouter.route(on(AddPipelineHuooJourneyController.class)
        .selectOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_ORG_ATTR, PICKED_ORG_STRING)
        .param(FORM_PICKED_TREATY_ATTR, String.format("%s,%s", TreatyAgreement.BELGIUM, TreatyAgreement.IRELAND))
    )
        .andExpect(status().is3xxRedirection());

    verify(pickablePipelineService, times(1)).getPickedPipelinesFromStrings(PICKED_PIPELINE_IDS);

    verify(padPipelinesHuooService, times(1))
        .updatePipelineHuooLinks(
            pwaApplicationDetail,
            pickedPipelines,
            DEFAULT_ROLE,
            PICKED_ORG_IDS.stream().map(OrganisationUnitId::new)
            .collect(Collectors.toSet()),
            Set.of(TreatyAgreement.BELGIUM, TreatyAgreement.IRELAND)
            );

  }

  @Test
  public void selectOrganisationsForPipelineHuooAssignment_invalidForm() throws Exception {

    doAnswer(invocation -> {
      // add error to binding result
      ((BindingResult) invocation.getArgument(2)).rejectValue(FORM_PICKED_PIPELINE_ATTR,
          FORM_PICKED_PIPELINE_ATTR + ".invalid", "blah");
      return invocation;
    }).when(padPipelinesHuooService).validateAddPipelineHuooForm(any(), any(), any(), any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(AddPipelineHuooJourneyController.class)
        .selectOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_ORG_ATTR, PICKED_ORG_STRING)
    )
        .andExpect(status().isOk());

    verify(padPipelinesHuooService, times(0)).updatePipelineHuooLinks(any(), any(), any(), any(), any());

  }

  @Test
  public void returnToPipelineSelection_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(AddPipelineHuooJourneyController.class).returnToPipelineSelection(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))))
        .addRequestParam(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "");

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void returnToPipelineSelection_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(AddPipelineHuooJourneyController.class).returnToPipelineSelection(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))))
        .addRequestParam(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void returnToPipelineSelection_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(AddPipelineHuooJourneyController.class).returnToPipelineSelection(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))))
        .addRequestParam(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "");

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void returnToPipelineSelection_selectedOrganisationsPersisted_andSelectedTreatiesPersisted() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(AddPipelineHuooJourneyController.class)
        .returnToPipelineSelection(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_ORG_ATTR, PICKED_ORG_STRING)
        .param(FORM_PICKED_TREATY_ATTR, TreatyAgreement.BELGIUM.name())
        .param(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "")
    )
        .andExpect(status().is3xxRedirection());

    MvcResult result = mockMvc.perform(get(ReverseRouter.route(on(AddPipelineHuooJourneyController.class)
        .renderOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().isOk())
        .andReturn();

    PickHuooPipelinesForm form = (PickHuooPipelinesForm) result.getModelAndView().getModel().get("form");
    assertThat(form.getOrganisationUnitIds()).isEqualTo(PICKED_ORG_IDS);
    assertThat(form.getTreatyAgreements()).containsExactly(TreatyAgreement.BELGIUM);

  }

}
