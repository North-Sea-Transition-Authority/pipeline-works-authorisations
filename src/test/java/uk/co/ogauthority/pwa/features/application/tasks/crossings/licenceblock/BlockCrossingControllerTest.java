package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.controller.BlockCrossingController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsSection;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsValidationResult;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsBlockService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(
    controllers = BlockCrossingController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class)
)
class BlockCrossingControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int BLOCK_CROSSING_ID = 1;
  private static final int APP_ID = 100;

  // Dont understand why this needs to be spybean and not a mock bean
  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @MockBean
  private AddBlockCrossingFormValidator addBlockCrossingFormValidator;

  @MockBean
  private EditBlockCrossingFormValidator editBlockCrossingFormValidator;

  @MockBean
  private PearsBlockService pearsBlockService;

  @MockBean
  private BlockCrossingService blockCrossingService;

  @Mock
  private PadCrossedBlock padCrossedBlock;

  @MockBean
  private BlockCrossingFileService blockCrossingFileService;

  @MockBean
  private CrossingAgreementsService crossingAgreementsService;

  @MockBean
  private PadFileManagementService padFileManagementService;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(
      new WebUserAccount(1),
      EnumSet.allOf(PwaUserPrivilege.class));

  private PwaApplicationEndpointTestBuilder endpointTester;

  @BeforeEach
  void setup() {
    doCallRealMethod().when(applicationBreadcrumbService).fromCrossings(any(), any(), any());
    // set default checks for entire controller
    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DEPOSIT_CONSENT,
            PwaApplicationType.DECOMMISSIONING)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

    when(padCrossedBlock.getBlockReference()).thenReturn("some block");

    when(blockCrossingService.getCrossedBlockByIdAndApplicationDetail(eq(BLOCK_CROSSING_ID), any())).thenReturn(
        padCrossedBlock);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetailByAppId(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(
        pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any()))
        .thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    when(blockCrossingService.getCrossedBlockView(any(), any())).thenReturn(
        new BlockCrossingView(BLOCK_CROSSING_ID, "ref", "ref", List.of(), true));
  }

  @Test
  void addBlockCrossingFormSave_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .addBlockCrossingFormSave(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null
                    , null)
            )
        );

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void addBlockCrossingFormSave_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .addBlockCrossingFormSave(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null
                    , null)
            )
        );

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void addBlockCrossingFormSave_contactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .addBlockCrossingFormSave(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null
                    , null)
            )
        );

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  private MultiValueMap<String, String> getValidAddBlockFormAsMap() {

    return new LinkedMultiValueMap<>() {{
      add("pickedBlock", "10BLOCK");
      add("crossingOwner", "HOLDER");
    }};
  }

  private MultiValueMap<String, String> getValidEditBlockFormAsMap() {

    return new LinkedMultiValueMap<>() {{
      add("crossingOwner", "HOLDER");
    }};
  }

  @Test
  void addBlockCrossingFormSave_whenFormValid() throws Exception {

    mockMvc.perform(
        post(ReverseRouter.route(on(BlockCrossingController.class)
            .addBlockCrossingFormSave(
                pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(),
                null,
                null
                , null)
        ))
            .with(user(user))
            .with(csrf())
            .params(getValidAddBlockFormAsMap()))
        .andExpect(status().is3xxRedirection());

    verify(addBlockCrossingFormValidator, times(1)).validate(any(), any(), any(Object[].class));

  }

  @Test
  void addBlockCrossingFormSave_whenFormInvalid() throws Exception {

    mockMvc.perform(
        post(ReverseRouter.route(on(BlockCrossingController.class)
            .addBlockCrossingFormSave(
                pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(),
                null,
                null
                , null)
        ))
            .with(user(user))
            .with(csrf())
            .params(new LinkedMultiValueMap<>()))
        .andExpect(status().isOk());
    verify(addBlockCrossingFormValidator, times(1)).validate(any(), any(), any(Object[].class));

  }

  @Test
  void renderAddBlockCrossing_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .renderAddBlockCrossing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null)
            )
        );


    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());
  }

  @Test
  void renderAddBlockCrossing_appStatusSmokeTest() {

    endpointTester.setEndpointUrlProducer((applicationDetail, type) ->
        ReverseRouter.route(on(BlockCrossingController.class)
            .renderAddBlockCrossing(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                null,
                null))
    )
        .setRequestMethod(HttpMethod.GET);

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  void renderAddBlockCrossing_contactRoleSmokeTest() {

    endpointTester.setEndpointUrlProducer((applicationDetail, type) ->
        ReverseRouter.route(on(BlockCrossingController.class)
            .renderAddBlockCrossing(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                null,
                null))
    )
        .setRequestMethod(HttpMethod.GET);

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void removeBlockCrossing_whenBlockCrossingNotFound() throws Exception {

    when(blockCrossingService.getCrossedBlockByIdAndApplicationDetail(BLOCK_CROSSING_ID, pwaApplicationDetail))
        .thenThrow(new PwaEntityNotFoundException("BANG"));

    mockMvc.perform(
        post(ReverseRouter.route(on(BlockCrossingController.class)
            .removeBlockCrossing(
                pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(),
                null,
                null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isNotFound());

  }

  @Test
  void removeBlockCrossing_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .removeBlockCrossing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    BLOCK_CROSSING_ID,
                    null)
            )
        );

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void removeBlockCrossing_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .removeBlockCrossing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    BLOCK_CROSSING_ID,
                    null)
            )
        );

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void removeBlockCrossing_contactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .removeBlockCrossing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    BLOCK_CROSSING_ID,
                    null)
            )
        );

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }


  @Test
  void editBlockCrossingFormSave_whenFormValid() throws Exception {

    mockMvc.perform(
        post(ReverseRouter.route(on(BlockCrossingController.class)
            .editBlockCrossingFormSave(
                pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(),
                BLOCK_CROSSING_ID,
                null,
                null,
                null)
        ))
            .with(user(user))
            .with(csrf())
            .params(getValidEditBlockFormAsMap()))
        .andExpect(status().is3xxRedirection());

    verify(editBlockCrossingFormValidator, times(1)).validate(any(), any(), any(Object[].class));

  }

  @Test
  void editBlockCrossingFormSave_whenFormInvalid() throws Exception {

    mockMvc.perform(
        post(ReverseRouter.route(on(BlockCrossingController.class)
            .editBlockCrossingFormSave(
                pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(),
                BLOCK_CROSSING_ID,
                null,
                null,
                null)
        ))
            .with(user(user))
            .with(csrf())
            .params(new LinkedMultiValueMap<>()))
        .andExpect(status().isOk());

    verify(editBlockCrossingFormValidator, times(1)).validate(any(), any(), any(Object[].class));
  }

  @Test
  void editBlockCrossingFormSave_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .editBlockCrossingFormSave(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    BLOCK_CROSSING_ID,
                    null,
                    null,
                    null)
            )
        )
        .addRequestParam("crossingOwner", "HOLDER");

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void editBlockCrossingFormSave_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .editBlockCrossingFormSave(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    BLOCK_CROSSING_ID,
                    null,
                    null,
                    null)
            )
        )
        .addRequestParam("crossingOwner", "HOLDER");


    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void editBlockCrossingFormSave_contactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .editBlockCrossingFormSave(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    BLOCK_CROSSING_ID,
                    null,
                    null,
                    null)
            )
        )
        .addRequestParam("crossingOwner", "HOLDER");

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void renderEditBlockCrossing_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .renderEditBlockCrossing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    BLOCK_CROSSING_ID,
                    null,
                    null)
            )
        );

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());
  }

  @Test
  void renderEditBlockCrossing_appStatusSmokeTest() {

    endpointTester.setEndpointUrlProducer((applicationDetail, type) ->
        ReverseRouter.route(on(BlockCrossingController.class)
            .renderEditBlockCrossing(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                BLOCK_CROSSING_ID,
                null,
                null))
    )
        .setRequestMethod(HttpMethod.GET);

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  void renderEditBlockCrossing_contactRoleSmokeTest() {

    endpointTester.setEndpointUrlProducer((applicationDetail, type) ->
        ReverseRouter.route(on(BlockCrossingController.class)
            .renderEditBlockCrossing(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                BLOCK_CROSSING_ID,
                null,
                null))
    )
        .setRequestMethod(HttpMethod.GET);

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void renderRemoveBlockCrossing_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .renderRemoveBlockCrossing(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    BLOCK_CROSSING_ID,
                    null)
            )
        );

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());
  }

  @Test
  void renderRemoveBlockCrossing_appStatusSmokeTest() {

    endpointTester.setEndpointUrlProducer((applicationDetail, type) ->
        ReverseRouter.route(on(BlockCrossingController.class)
            .renderRemoveBlockCrossing(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                BLOCK_CROSSING_ID,
                null))
    )
        .setRequestMethod(HttpMethod.GET);

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  void renderRemoveBlockCrossing_contactRoleSmokeTest() {

    endpointTester.setEndpointUrlProducer((applicationDetail, type) ->
        ReverseRouter.route(on(BlockCrossingController.class)
            .renderRemoveBlockCrossing(
                type,
                applicationDetail.getMasterPwaApplicationId(),
                BLOCK_CROSSING_ID,
                null))
    )
        .setRequestMethod(HttpMethod.GET);

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void renderBlockCrossingOverview_appTypeSmokeTest() {
    var crossingAgreementsValidationResult = new CrossingAgreementsValidationResult(
        Set.of(CrossingAgreementsSection.BLOCK_CROSSINGS));
    when(crossingAgreementsService.getValidationResult(any()))
        .thenReturn(crossingAgreementsValidationResult);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .renderBlockCrossingOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

    verify(padFileManagementService, times(endpointTester.getAllowedTypes().size())).getUploadedFileViews(any(),
        eq(FileDocumentType.BLOCK_CROSSINGS));
  }

  @Test
  void renderBlockCrossingOverview_appStatusSmokeTest() {
    var crossingAgreementsValidationResult = new CrossingAgreementsValidationResult(
        Set.of(CrossingAgreementsSection.BLOCK_CROSSINGS));
    when(crossingAgreementsService.getValidationResult(any()))
        .thenReturn(crossingAgreementsValidationResult);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .renderBlockCrossingOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

    verify(padFileManagementService, times(endpointTester.getAllowedStatuses().size())).getUploadedFileViews(any(),
        eq(FileDocumentType.BLOCK_CROSSINGS));
  }

  @Test
  void renderBlockCrossingOverview_appContactRoleSmokeTest() {
    var crossingAgreementsValidationResult = new CrossingAgreementsValidationResult(
        Set.of(CrossingAgreementsSection.BLOCK_CROSSINGS));
    when(crossingAgreementsService.getValidationResult(any()))
        .thenReturn(crossingAgreementsValidationResult);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .renderBlockCrossingOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

    verify(padFileManagementService, times(endpointTester.getAllowedAppPermissions().size())).getUploadedFileViews(any(),
        eq(FileDocumentType.BLOCK_CROSSINGS));
  }

  @Test
  void postOverview_appTypeSmokeTest() {

    when(blockCrossingFileService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());
  }

  @Test
  void postOverview_appStatusSmokeTest() {

    when(blockCrossingFileService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  void postOverview_appContactRoleSmokeTest() {

    when(blockCrossingFileService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(BlockCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

}
