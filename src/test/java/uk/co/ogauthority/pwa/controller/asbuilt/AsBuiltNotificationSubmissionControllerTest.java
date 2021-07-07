package uk.co.ogauthority.pwa.controller.asbuilt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipelineUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.form.asbuilt.AsBuiltNotificationSubmissionForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltBreadCrumbService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltInteractorService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationAuthService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationGroupService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltPipelineNotificationService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;
import uk.co.ogauthority.pwa.validators.asbuilt.AsBuiltNotificationSubmissionValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(AsBuiltNotificationSubmissionController.class)
public class AsBuiltNotificationSubmissionControllerTest extends AbstractControllerTest {

  @MockBean
  private PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  private PwaAppProcessingContextService pwaAppProcessingContextService;

  @MockBean
  private AsBuiltNotificationAuthService asBuiltNotificationAuthService;

  @MockBean
  private AsBuiltNotificationGroupService asBuiltNotificationGroupService;

  @MockBean
  private AsBuiltPipelineNotificationService asBuiltPipelineNotificationService;

  @MockBean
  private AsBuiltNotificationSubmissionValidator asBuiltNotificationSubmissionValidator;

  @MockBean
  private AsBuiltInteractorService asBuiltInteractorService;

  @SpyBean
  private AsBuiltBreadCrumbService asBuiltBreadCrumbService;

  private final AuthenticatedUserAccount user = AuthenticatedUserAccountTestUtil.createAllPrivUserAccount(1);

  private static final int NOTIFICATION_GROUP_ID  = 10;
  private static final int PIPElINE_DETAIL_ID  = 20;

  private final AsBuiltNotificationGroup asBuiltNotificationGroup = AsBuiltNotificationGroupTestUtil
      .createGroupWithConsent_withNgId(NOTIFICATION_GROUP_ID);
  private final PipelineDetail pipelineDetail = PipelineDetailTestUtil
      .createPipelineDetail_withDefaultPipelineNumber(PIPElINE_DETAIL_ID, new PipelineId(50), Instant.now());
  private final AsBuiltNotificationGroupPipeline asBuiltNotificationGroupPipeline = AsBuiltNotificationGroupPipelineUtil
      .createAsBuiltNotificationGroupPipeline(asBuiltNotificationGroup, pipelineDetail.getPipelineDetailId(), PipelineChangeCategory.NEW_PIPELINE);

  @Before
  public void setup() {
    when(asBuiltNotificationAuthService.canPersonAccessAsbuiltNotificationGroup(user.getLinkedPerson(), NOTIFICATION_GROUP_ID))
        .thenReturn(true);
    when(asBuiltNotificationAuthService.isPersonAsBuiltNotificationAdmin(user.getLinkedPerson())).thenReturn(false);
    when(asBuiltNotificationGroupService.getAsBuiltNotificationGroup(NOTIFICATION_GROUP_ID))
        .thenReturn(Optional.of(asBuiltNotificationGroup));
    when(asBuiltPipelineNotificationService.getPipelineDetail(PIPElINE_DETAIL_ID)).thenReturn(pipelineDetail);
    when(asBuiltPipelineNotificationService.getAsBuiltNotificationGroupPipeline(NOTIFICATION_GROUP_ID,
        pipelineDetail.getPipelineDetailId())).thenReturn(asBuiltNotificationGroupPipeline);
  }

  @Test
  public void getAsBuiltNotificationSubmissionForm_unauthorizedUser_forbidden() throws Exception {
    when(asBuiltNotificationAuthService.canPersonAccessAsbuiltNotificationGroup(user.getLinkedPerson(), NOTIFICATION_GROUP_ID))
        .thenReturn(false);
    mockMvc.perform(get(
        ReverseRouter.route(on(AsBuiltNotificationSubmissionController.class)
            .renderSubmitAsBuiltNotificationForm(NOTIFICATION_GROUP_ID, PIPElINE_DETAIL_ID, user, new AsBuiltNotificationSubmissionForm())))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void getAsBuiltNotificationSubmissionForm_authorizedIndustryUser_permitted() throws Exception {
    mockMvc.perform(get(
        ReverseRouter.route(on(AsBuiltNotificationSubmissionController.class)
            .renderSubmitAsBuiltNotificationForm(NOTIFICATION_GROUP_ID, PIPElINE_DETAIL_ID, user, new AsBuiltNotificationSubmissionForm())))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("isOgaUser", false));
  }

  @Test
  public void getAsBuiltNotificationSubmissionForm_authorizedOgaUser_permitted_hasOgaOnlyQuestion() throws Exception {
    when(asBuiltNotificationAuthService.canPersonAccessAsbuiltNotificationGroup(user.getLinkedPerson(), NOTIFICATION_GROUP_ID))
        .thenReturn(true);
    when(asBuiltNotificationAuthService.isPersonAsBuiltNotificationAdmin(user.getLinkedPerson())).thenReturn(true);
    mockMvc.perform(get(
        ReverseRouter.route(on(AsBuiltNotificationSubmissionController.class)
            .renderSubmitAsBuiltNotificationForm(NOTIFICATION_GROUP_ID, PIPElINE_DETAIL_ID, user, new AsBuiltNotificationSubmissionForm())))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("isOgaUser", true));
  }

  @Test
  public void getAsBuiltNotificationSubmissionForm_InServicePipeline_noNeverLaidRadioOption() throws Exception {
    asBuiltNotificationGroupPipeline.setPipelineChangeCategory(PipelineChangeCategory.CONSENT_UPDATE);
    when(asBuiltNotificationAuthService.canPersonAccessAsbuiltNotificationGroup(user.getLinkedPerson(), NOTIFICATION_GROUP_ID))
        .thenReturn(true);
    when(asBuiltNotificationAuthService.isPersonAsBuiltNotificationAdmin(user.getLinkedPerson())).thenReturn(true);
    mockMvc.perform(get(
        ReverseRouter.route(on(AsBuiltNotificationSubmissionController.class)
            .renderSubmitAsBuiltNotificationForm(NOTIFICATION_GROUP_ID, PIPElINE_DETAIL_ID, user, new AsBuiltNotificationSubmissionForm())))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("isOgaUser", true))
        .andExpect(model().attribute("asBuiltStatusOptions",
            List.of(AsBuiltNotificationStatus.PER_CONSENT, AsBuiltNotificationStatus.NOT_PER_CONSENT,
                AsBuiltNotificationStatus.NOT_LAID_CONSENT_TIMEFRAME, AsBuiltNotificationStatus.NOT_PROVIDED)));
  }

  @Test
  public void postSubmitAsBuiltNotification_unauthorizedUser_forbidden() throws Exception {
    when(asBuiltNotificationAuthService.canPersonAccessAsbuiltNotificationGroup(user.getLinkedPerson(), NOTIFICATION_GROUP_ID))
        .thenReturn(false);

    mockMvc.perform(post(
        ReverseRouter.route(on(AsBuiltNotificationSubmissionController.class)
            .postSubmitAsBuiltNotification(NOTIFICATION_GROUP_ID, PIPElINE_DETAIL_ID, user, new AsBuiltNotificationSubmissionForm(), null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postSubmitAsBuiltNotification_failsValidation() throws Exception {
    doAnswer(invocation -> {
      var errors = (Errors) invocation.getArgument(1);
      errors.rejectValue("ogaSubmissionReason", MAX_LENGTH_EXCEEDED.errorCode("ogaSubmissionReason"), "error message");
      return errors;
    }).when(asBuiltNotificationSubmissionValidator).validate(any(), any(), any());

    mockMvc.perform(post(
        ReverseRouter.route(on(AsBuiltNotificationSubmissionController.class)
            .postSubmitAsBuiltNotification(NOTIFICATION_GROUP_ID, PIPElINE_DETAIL_ID, user, new AsBuiltNotificationSubmissionForm(), null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  public void postSubmitAsBuiltNotification_validationSuccess_returnToDashboard() throws Exception {
    mockMvc.perform(post(
        ReverseRouter.route(on(AsBuiltNotificationSubmissionController.class)
            .postSubmitAsBuiltNotification(NOTIFICATION_GROUP_ID, PIPElINE_DETAIL_ID, user, new AsBuiltNotificationSubmissionForm(), null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(AsBuiltNotificationController.class).getAsBuiltNotificationDashboard(NOTIFICATION_GROUP_ID, user))));

    verify(asBuiltInteractorService)
        .submitAsBuiltNotification(eq(asBuiltNotificationGroupPipeline), any(AsBuiltNotificationSubmissionForm.class), eq(user));
  }

}
