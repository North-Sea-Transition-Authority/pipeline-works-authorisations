package uk.co.ogauthority.pwa.service.asbuilt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipelineUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmission;
import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.form.asbuilt.AsBuiltNotificationSubmissionForm;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationSubmissionRepository;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltNotificationSubmissionServiceTest {

  @Mock
  private AsBuiltNotificationSubmissionRepository asBuiltNotificationSubmissionRepository;

  @Mock
  private AsBuiltPipelineNotificationService asBuiltPipelineNotificationService;

  @Mock
  private AsBuiltNotificationGroupStatusService asBuiltNotificationGroupStatusService;

  @Mock
  private AsBuiltNotificationEmailService asBuiltNotificationEmailService;

  private AsBuiltNotificationSubmissionService asBuiltNotificationSubmissionService;

  @Captor
  private ArgumentCaptor<AsBuiltNotificationSubmission> asBuiltSubmissionArgumentCaptor;

  private final AuthenticatedUserAccount user = AuthenticatedUserAccountTestUtil.createAllPrivUserAccount(1);

  private static final int NOTIFICATION_GROUP_ID  = 10;
  private static final int PIPElINE_DETAIL_ID  = 20;

  private final AsBuiltNotificationGroup asBuiltNotificationGroup = AsBuiltNotificationGroupTestUtil
      .createGroupWithConsent_fromNgId(NOTIFICATION_GROUP_ID);
  private final PipelineDetail pipelineDetail = PipelineDetailTestUtil
      .createPipelineDetail_withDefaultPipelineNumber(PIPElINE_DETAIL_ID, new PipelineId(50), Instant.now());
  private final AsBuiltNotificationGroupPipeline asBuiltNotificationGroupPipeline = AsBuiltNotificationGroupPipelineUtil
      .createAsBuiltNotificationGroupPipeline(asBuiltNotificationGroup, pipelineDetail.getPipelineDetailId(), PipelineChangeCategory.NEW_PIPELINE);
  private final AsBuiltNotificationSubmissionForm form = getAsBuiltNotificationSubmissionForm();
  private final AsBuiltNotificationSubmission asBuiltNotificationSubmission = getAsBuiltNotificationSubmission();

  @Before
  public void setup() {
    asBuiltNotificationSubmissionService = new AsBuiltNotificationSubmissionService(asBuiltNotificationSubmissionRepository,
        asBuiltNotificationGroupStatusService, asBuiltPipelineNotificationService, asBuiltNotificationEmailService,
        "consents@oga.co.uk");
    when(asBuiltPipelineNotificationService.getAllAsBuiltNotificationGroupPipelines(asBuiltNotificationGroup.getId()))
        .thenReturn(List.of(asBuiltNotificationGroupPipeline));
    when(asBuiltNotificationSubmissionRepository.findAllByAsBuiltNotificationGroupPipelineInAndTipFlagIsTrue(List.of(asBuiltNotificationGroupPipeline)))
        .thenReturn(List.of(asBuiltNotificationSubmission));
    when(asBuiltNotificationSubmissionRepository.findByAsBuiltNotificationGroupPipelineAndTipFlagIsTrue(asBuiltNotificationGroupPipeline))
        .thenReturn(Optional.of(asBuiltNotificationSubmission));
  }

  @Test
  public void submitAsBuiltNotification_submitsSuccessfully_noPriorSubmissions_savesCurrent() {
    when(asBuiltNotificationSubmissionRepository.findByAsBuiltNotificationGroupPipelineAndTipFlagIsTrue(asBuiltNotificationGroupPipeline))
        .thenReturn(Optional.empty());
    asBuiltNotificationSubmissionService.submitAsBuiltNotification(asBuiltNotificationGroupPipeline, form, user);
    verify(asBuiltNotificationSubmissionRepository).save(asBuiltSubmissionArgumentCaptor.capture());
  }

  @Test
  public void submitAsBuiltNotification_submitsSuccessfully_withPriorSubmissions_savesBothLastAndCurrent_setsInProgressStatusToCurrent() {
    asBuiltNotificationSubmissionService.submitAsBuiltNotification(asBuiltNotificationGroupPipeline, form, user);
    verify(asBuiltNotificationSubmissionRepository, times(2)).save(asBuiltSubmissionArgumentCaptor.capture());

    var asBuiltNotification = asBuiltSubmissionArgumentCaptor.getValue();
    assertThat(asBuiltNotification.getSubmittedByPersonId()).isEqualTo(user.getLinkedPerson().getId());
    assertThat(asBuiltNotification.getAsBuiltNotificationStatus())
        .isEqualTo(form.getAsBuiltNotificationStatus());
    assertThat(asBuiltNotification.getDateWorkCompleted())
        .isEqualTo(DateUtils.datePickerStringToDate(form.getPerConsentDateWorkCompletedTimestampStr()));
    assertThat(asBuiltNotification.getDatePipelineBroughtIntoUse())
        .isEqualTo(DateUtils.datePickerStringToDate(form.getPerConsentDateBroughtIntoUseTimestampStr()));
    assertThat(asBuiltNotification.getRegulatorSubmissionReason())
        .isEqualTo(form.getOgaSubmissionReason());
    assertThat(asBuiltNotification.getSubmittedTimestamp()).isNotNull();

    verify(asBuiltNotificationGroupStatusService).setGroupStatusIfNewOrChanged(asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.IN_PROGRESS,
        user.getLinkedPerson());
    verify(asBuiltNotificationEmailService, never()).sendAsBuiltNotificationNotPerConsentEmail(any(), any(), any(), any(), any());
  }

  @Test
  public void submitAsBuiltNotification_submitsSuccessfully_withPriorSubmissions_savesBothLastAndCurrent_setsCompleteStatusToCurrent() {
    asBuiltNotificationSubmission.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.PER_CONSENT);
    asBuiltNotificationSubmissionService.submitAsBuiltNotification(asBuiltNotificationGroupPipeline, form, user);
    verify(asBuiltNotificationSubmissionRepository, times(2)).save(asBuiltSubmissionArgumentCaptor.capture());

    var asBuiltNotification = asBuiltSubmissionArgumentCaptor.getValue();
    assertThat(asBuiltNotification.getSubmittedByPersonId()).isEqualTo(user.getLinkedPerson().getId());
    assertThat(asBuiltNotification.getAsBuiltNotificationStatus())
        .isEqualTo(form.getAsBuiltNotificationStatus());
    assertThat(asBuiltNotification.getDateWorkCompleted())
        .isEqualTo(DateUtils.datePickerStringToDate(form.getPerConsentDateWorkCompletedTimestampStr()));
    assertThat(asBuiltNotification.getDatePipelineBroughtIntoUse())
        .isEqualTo(DateUtils.datePickerStringToDate(form.getPerConsentDateBroughtIntoUseTimestampStr()));
    assertThat(asBuiltNotification.getRegulatorSubmissionReason())
        .isEqualTo(form.getOgaSubmissionReason());
    assertThat(asBuiltNotification.getSubmittedTimestamp()).isNotNull();

    verify(asBuiltNotificationGroupStatusService).setGroupStatusIfNewOrChanged(asBuiltNotificationGroup,
        AsBuiltNotificationGroupStatus.COMPLETE, user.getLinkedPerson());
    verify(asBuiltNotificationEmailService, never()).sendAsBuiltNotificationNotPerConsentEmail(any(), any(), any(), any(), any());
  }


  @Test
  public void submitAsBuiltNotification_submitsSuccessfully_withPriorSubmissions_savesBothLastAndCurrent_sendsOgaEmail() {
    when(asBuiltPipelineNotificationService.getPipelineDetail(pipelineDetail.getPipelineDetailId().asInt()))
        .thenReturn(pipelineDetail);

    asBuiltNotificationSubmission.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.NOT_PER_CONSENT);
    var notPerConsentForm = getNotPerConsentAsBuiltNotificationSubmissionForm();
    asBuiltNotificationSubmissionService.submitAsBuiltNotification(asBuiltNotificationGroupPipeline, notPerConsentForm, user);
    verify(asBuiltNotificationSubmissionRepository, times(2)).save(asBuiltSubmissionArgumentCaptor.capture());

    var asBuiltNotification = asBuiltSubmissionArgumentCaptor.getValue();
    assertThat(asBuiltNotification.getSubmittedByPersonId()).isEqualTo(user.getLinkedPerson().getId());
    assertThat(asBuiltNotification.getAsBuiltNotificationStatus())
        .isEqualTo(notPerConsentForm.getAsBuiltNotificationStatus());
    assertThat(asBuiltNotification.getDateWorkCompleted())
        .isEqualTo(DateUtils.datePickerStringToDate(notPerConsentForm.getNotPerConsentDateWorkCompletedTimestampStr()));
    assertThat(asBuiltNotification.getDatePipelineBroughtIntoUse())
        .isEqualTo(DateUtils.datePickerStringToDate(notPerConsentForm.getNotPerConsentDateBroughtIntoUseTimestampStr()));
    assertThat(asBuiltNotification.getRegulatorSubmissionReason())
        .isEqualTo(notPerConsentForm.getOgaSubmissionReason());
    assertThat(asBuiltNotification.getSubmittedTimestamp()).isNotNull();

    verify(asBuiltNotificationGroupStatusService).setGroupStatusIfNewOrChanged(asBuiltNotificationGroup,
        AsBuiltNotificationGroupStatus.COMPLETE, user.getLinkedPerson());
    verify(asBuiltNotificationEmailService).sendAsBuiltNotificationNotPerConsentEmail(any(), any(), eq(asBuiltNotificationGroup),
        eq(pipelineDetail), eq(notPerConsentForm.getAsBuiltNotificationStatus()));
  }

  private AsBuiltNotificationSubmissionForm getAsBuiltNotificationSubmissionForm() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setPerConsentDateWorkCompletedTimestampStr("01/01/2010");
    form.setPerConsentDateBroughtIntoUseTimestampStr("02/02/2020");
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.PER_CONSENT);
    form.setOgaSubmissionReason("Reason");
    return form;
  }

  private AsBuiltNotificationSubmissionForm getNotPerConsentAsBuiltNotificationSubmissionForm() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setNotPerConsentDateWorkCompletedTimestampStr("01/01/2010");
    form.setNotPerConsentDateBroughtIntoUseTimestampStr("02/02/2020");
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.NOT_PER_CONSENT);
    form.setOgaSubmissionReason("Reason");
    return form;
  }

  private AsBuiltNotificationSubmission getAsBuiltNotificationSubmission() {
    return new AsBuiltNotificationSubmission(1,
        asBuiltNotificationGroupPipeline, user.getLinkedPerson().getId(), Instant.now(), AsBuiltNotificationStatus.NOT_PROVIDED,
        LocalDate.now(), LocalDate.now(), "", true);
  }

}
