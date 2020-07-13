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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.ModifyPipelineHuooJourneyController.UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
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
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickablePipelineOptionTestUtil;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickablePipelineService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ModifyPipelineHuooJourneyController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class ModifyPipelineHuooJourneyControllerTest extends PwaApplicationContextAbstractControllerTest {
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
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).renderPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderPipelinesForHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).renderPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderPipelinesForHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).renderPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void selectPipelinesForHuooAssignment_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).selectPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null
            ))));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void selectPipelinesForHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).selectPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null
            ))));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void selectPipelinesForHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).selectPipelinesForHuooAssignment(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null, null
            ))));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void selectPipelinesForHuooAssignment_validForm_setsJourneyDataAndRedirects_thenLoadsJourneyDataIntoForm() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_PIPELINE_ATTR, PICKED_PIPELINE_STRING)
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(result -> result.getModelAndView().getViewName().equals(
            "pwaApplication/shared/pipelinehuoo/addPipelineHuooAssociateOrganisations"));

    MvcResult result = mockMvc.perform(get(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
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

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
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
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).renderOrganisationsForPipelineHuooAssignment(
                    pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
                ))));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderOrganisationsForPipelineHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).renderOrganisationsForPipelineHuooAssignment(
                    pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
                ))));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderOrganisationsForPipelineHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).renderOrganisationsForPipelineHuooAssignment(
                    pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
                ))));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void selectOrganisationsForPipelineHuooAssignment_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).selectOrganisationsForPipelineHuooAssignment(
                    pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null,
                    null,
                    null
                ))));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void selectOrganisationsForPipelineHuooAssignment_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).selectOrganisationsForPipelineHuooAssignment(
                    pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null,
                    null,
                    null
                ))));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void selectOrganisationsForPipelineHuooAssignment_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(
                on(ModifyPipelineHuooJourneyController.class).selectOrganisationsForPipelineHuooAssignment(
                    pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null,
                    null,
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

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_PIPELINE_ATTR, PICKED_PIPELINE_STRING)
    )
        .andExpect(status().is3xxRedirection());

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
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
  public void completeValidJourney_journeyDataGetsClearedOnCompletion() throws Exception {

    var pickedPipelines = Set.of(new Pipeline(), new Pipeline());
    var foundPadOrgRoles = List.of(new PadOrganisationRole(), new PadOrganisationRole());
    when(pickablePipelineService.getPickedPipelinesFromStrings(any())).thenReturn(pickedPipelines);
    when(padPipelinesHuooService.getPadOrganisationRolesFrom(any(), any(), any(), any())).thenReturn(foundPadOrgRoles);

    // Step 1: Mock loading and selecting of Pipelines
    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_PIPELINE_ATTR, PICKED_PIPELINE_STRING)
    )
        .andExpect(status().is3xxRedirection());

    // Step 2: select orgs and complete journey
    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .selectOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_ORG_ATTR, PICKED_ORG_STRING)
        .param(FORM_PICKED_TREATY_ATTR, String.format("%s,%s", TreatyAgreement.BELGIUM, TreatyAgreement.IRELAND))
    )
        .andExpect(status().is3xxRedirection());

    // Step 3: loading up each journey page has nothing pre-selected
    assertPreLoadedJourneyDataMatches(Collections.emptySet(), Collections.emptySet(), Collections.emptySet());


  }


  @Test
  public void selectOrganisationsForPipelineHuooAssignment_invalidForm() throws Exception {

    doAnswer(invocation -> {
      // add error to binding result
      ((BindingResult) invocation.getArgument(2)).rejectValue(FORM_PICKED_PIPELINE_ATTR,
          FORM_PICKED_PIPELINE_ATTR + ".invalid", "blah");
      return invocation;
    }).when(padPipelinesHuooService).validateAddPipelineHuooForm(any(), any(), any(), any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
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
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).returnToPipelineSelection(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))))
        .addRequestParam(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "");

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void returnToPipelineSelection_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).returnToPipelineSelection(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))))
        .addRequestParam(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void returnToPipelineSelection_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).returnToPipelineSelection(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), DEFAULT_ROLE, null, null
            ))))
        .addRequestParam(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "");

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void returnToPipelineSelection_selectedOrganisationsPersisted_andSelectedTreatiesPersisted() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .returnToPipelineSelection(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
        .param(FORM_PICKED_ORG_ATTR, PICKED_ORG_STRING)
        .param(FORM_PICKED_TREATY_ATTR, TreatyAgreement.BELGIUM.name())
        .param(UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT, "")
    )
        .andExpect(status().is3xxRedirection());

    MvcResult result = mockMvc.perform(get(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
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


  @Test
  public void editGroupRouter_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroupRouter(
                pwaApplicationType,
                pwaApplicationDetail.getMasterPwaApplicationId(),
                DEFAULT_ROLE,
                null,
                ModifyPipelineHuooJourneyController.JourneyPage.PIPELINE_SELECTION,
                null,
                null,
                null
            ))));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void editGroupRouter_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroupRouter(
                pwaApplicationType,
                pwaApplicationDetail.getMasterPwaApplicationId(),
                DEFAULT_ROLE,
                null,
                ModifyPipelineHuooJourneyController.JourneyPage.PIPELINE_SELECTION,
                null,
                null,
                null
            ))));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void editGroupRouter_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class).editGroupRouter(
                pwaApplicationType,
                pwaApplicationDetail.getMasterPwaApplicationId(),
                DEFAULT_ROLE,
                null,
                ModifyPipelineHuooJourneyController.JourneyPage.PIPELINE_SELECTION,
                null,
                null,
                null
            ))));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }


  private MultiValueMap<String, String> generateIdParams(String paramName, int... ids) {

    var map = new LinkedMultiValueMap<String, String>();
    Arrays.stream(ids).forEach(o -> map.add(paramName, String.valueOf(o)));
    return map;
  }


  @Test
  public void editGroupRouter_pipelineSelectionPage_preventsInvalidPipelinesPopulatingJourney() throws Exception {
    var pipelineIds = Set.of(1, 2);
    var reconciledPipeline = PickablePipelineOptionTestUtil.createConsentedReconciledPickablePipeline(
        new PipelineId(1)
    );

    when(padPipelinesHuooService.reconcilePickablePipelinesFromPipelineIds(
        pwaApplicationDetail,
        pipelineIds
    )).thenReturn(Set.of(reconciledPipeline));

    // check redirect target as expected
    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .editGroupRouter(
            PwaApplicationType.INITIAL,
            pwaApplicationDetail.getMasterPwaApplicationId(),
            DEFAULT_ROLE,
            null,
            ModifyPipelineHuooJourneyController.JourneyPage.PIPELINE_SELECTION,
            pipelineIds,
            null,
            null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/pwa-application/initial/10/pipeline-huoo/HOLDER/pipelines"))
        .andReturn();

    assertPreLoadedJourneyDataMatches(Set.of(reconciledPipeline.getPickablePipelineId().getId()),
        Collections.emptySet(), Collections.emptySet());

  }

  @Test
  public void editGroupRouter_preventsInvalidDataPopulatingJourney() throws Exception {
    var pipelineIds = Set.of(1, 2);
    var reconciledPipeline = PickablePipelineOptionTestUtil.createConsentedReconciledPickablePipeline(
        new PipelineId(1)
    );

    var validOrgUnitRoleOwner = OrganisationRoleOwnerDto.fromOrganisationUnitId(new OrganisationUnitId(10));

    var validTreaty = TreatyAgreement.NORWAY;
    var invalidTreaty = TreatyAgreement.BELGIUM;

    var paramOrgUnitSet = Set.of(validOrgUnitRoleOwner.getOrganisationUnitId().asInt(), 9999);
    var paramTreatySet = Set.of(validTreaty, invalidTreaty);


    var validRoleOwnerSet = Set.of(
        validOrgUnitRoleOwner,
        OrganisationRoleOwnerDto.fromTreaty(validTreaty)
    );
    when(padPipelinesHuooService.reconcileOrganisationRoleOwnersFrom(
        pwaApplicationDetail,
        DEFAULT_ROLE,
        paramOrgUnitSet,
        paramTreatySet
    )).thenReturn(validRoleOwnerSet);

    when(padPipelinesHuooService.reconcilePickablePipelinesFromPipelineIds(
        pwaApplicationDetail,
        pipelineIds
    )).thenReturn(Set.of(reconciledPipeline));

    // check redirect target as expected
    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .editGroupRouter(
            PwaApplicationType.INITIAL,
            pwaApplicationDetail.getMasterPwaApplicationId(),
            DEFAULT_ROLE,
            null,
            ModifyPipelineHuooJourneyController.JourneyPage.PIPELINE_SELECTION,
            pipelineIds,
            paramOrgUnitSet,
            paramTreatySet
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().is3xxRedirection());

    assertPreLoadedJourneyDataMatches(
        Set.of(reconciledPipeline.getPickablePipelineId().getId()),
        Set.of(validOrgUnitRoleOwner.getOrganisationUnitId().asInt()),
        Set.of(validTreaty));

  }

  @Test
  public void editGroupRouter_pipelineJourneyPageParam_redirect() throws Exception {

    // check redirect target as expected
    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .editGroupRouter(
            PwaApplicationType.INITIAL,
            pwaApplicationDetail.getMasterPwaApplicationId(),
            DEFAULT_ROLE,
            null,
            ModifyPipelineHuooJourneyController.JourneyPage.PIPELINE_SELECTION,
            null,
            null,
            null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/pwa-application/initial/10/pipeline-huoo/HOLDER/pipelines"));
  }

  @Test
  public void editGroupRouter_organisationJourneyPageParam_redirect() throws Exception {

    // check redirect target as expected
    mockMvc.perform(post(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .editGroupRouter(
            PwaApplicationType.INITIAL,
            pwaApplicationDetail.getMasterPwaApplicationId(),
            DEFAULT_ROLE,
            null,
            ModifyPipelineHuooJourneyController.JourneyPage.ORGANISATION_SELECTION,
            null,
            null,
            null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/pwa-application/initial/10/pipeline-huoo/HOLDER/pipelines/organisations"));
  }


  private void assertPreLoadedJourneyDataMatches(Set<String> pickedPipelines,
                                                 Set<Integer> orgUnitsIds,
                                                 Set<TreatyAgreement> treatyAgreements) throws Exception {
    // load selected pipelines screen for same huoo journey and app and assert matching pipelines match
    MvcResult pipelineResult = mockMvc.perform(get(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .renderPipelinesForHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().isOk())
        .andReturn();
    var pipelinePageForm = (PickHuooPipelinesForm) pipelineResult.getModelAndView().getModelMap().getAttribute("form");
    if (pickedPipelines.isEmpty()) {
      assertThat(pipelinePageForm.getPickedPipelineStrings()).isEmpty();
    } else {
      assertThat(pipelinePageForm.getPickedPipelineStrings()).containsExactlyInAnyOrder(
          pickedPipelines.toArray(String[]::new));
    }

    // load select organisations screen for same huoo journey and app and assert expected orgs match
    MvcResult orgResult = mockMvc.perform(get(ReverseRouter.route(on(ModifyPipelineHuooJourneyController.class)
        .renderOrganisationsForPipelineHuooAssignment(APP_TYPE, APP_ID, DEFAULT_ROLE, null, null
        )))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().isOk())
        .andReturn();
    var orgPageForm = (PickHuooPipelinesForm) orgResult.getModelAndView().getModelMap().getAttribute("form");
    if (orgUnitsIds.isEmpty()) {
      assertThat(orgPageForm.getOrganisationUnitIds()).isEmpty();
    } else {
      assertThat(orgPageForm.getOrganisationUnitIds()).containsExactlyInAnyOrder(orgUnitsIds.toArray(Integer[]::new));
    }

    if (treatyAgreements.isEmpty()) {
      assertThat(orgPageForm.getTreatyAgreements()).isEmpty();
    } else {
      assertThat(orgPageForm.getTreatyAgreements()).containsExactlyInAnyOrder(
          treatyAgreements.toArray(TreatyAgreement[]::new));
    }

  }


}
