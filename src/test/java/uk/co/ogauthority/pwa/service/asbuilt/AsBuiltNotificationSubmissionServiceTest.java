package uk.co.ogauthority.pwa.service.asbuilt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipelineUtil;
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

  private AsBuiltNotificationSubmissionService asBuiltNotificationSubmissionService;

  @Captor
  private ArgumentCaptor<AsBuiltNotificationSubmission> asBuiltSubmissionArgumentCaptor;

  private final AuthenticatedUserAccount user = AuthenticatedUserAccountTestUtil.createAllPrivUserAccount(1);

  private static final int NOTIFICATION_GROUP_ID  = 10;
  private static final int PIPElINE_DETAIL_ID  = 20;

  private final AsBuiltNotificationGroup asBuiltNotificationGroup = AsBuiltNotificationGroupTestUtil
      .createGroupWithConsent_withNgId(NOTIFICATION_GROUP_ID);
  private final PipelineDetail pipelineDetail = PipelineDetailTestUtil
      .createPipelineDetail_withDefaultPipelineNumber(PIPElINE_DETAIL_ID, new PipelineId(50), Instant.now());
  private final AsBuiltNotificationGroupPipeline asBuiltNotificationGroupPipeline = AsBuiltNotificationGroupPipelineUtil
      .createAsBuiltNotificationGroupPipeline(asBuiltNotificationGroup, pipelineDetail.getPipelineDetailId(),
          PipelineChangeCategory.NEW_PIPELINE);
  private final AsBuiltNotificationSubmissionForm asBuiltNotificationSubmissionForm = getAsBuiltNotificationSubmissionForm();


  @Before
  public void setup() {
    asBuiltNotificationSubmissionService = new AsBuiltNotificationSubmissionService(asBuiltNotificationSubmissionRepository);
  }

  @Test
  public void submitAsBuiltNotification() {
    asBuiltNotificationSubmissionService.submitAsBuiltNotification(asBuiltNotificationGroupPipeline, asBuiltNotificationSubmissionForm,
        user);
    verify(asBuiltNotificationSubmissionRepository).save(asBuiltSubmissionArgumentCaptor.capture());

    var asBuiltNotification = asBuiltSubmissionArgumentCaptor.getValue();
    assertThat(asBuiltNotification.getSubmittedByPersonId()).isEqualTo(user.getLinkedPerson().getId());
    assertThat(asBuiltNotification.getAsBuiltNotificationStatus())
        .isEqualTo(asBuiltNotificationSubmissionForm.getAsBuiltNotificationStatus());
    assertThat(asBuiltNotification.getDateLaid())
        .isEqualTo(DateUtils.datePickerStringToDate(asBuiltNotificationSubmissionForm.getPerConsentDateLaidTimestampStr()));
    assertThat(asBuiltNotification.getDatePipelineBroughtIntoUse())
        .isEqualTo(DateUtils.datePickerStringToDate(asBuiltNotificationSubmissionForm.getPerConsentDateBroughtIntoUseTimestampStr()));
    assertThat(asBuiltNotification.getRegulatorSubmissionReason())
        .isEqualTo(asBuiltNotificationSubmissionForm.getOgaSubmissionReason());
    assertThat(asBuiltNotification.getSubmittedTimestamp()).isNotNull();
  }

  private AsBuiltNotificationSubmissionForm getAsBuiltNotificationSubmissionForm() {
    var form = new AsBuiltNotificationSubmissionForm();
    form.setPerConsentDateLaidTimestampStr("01/01/2010");
    form.setPerConsentDateBroughtIntoUseTimestampStr("02/02/2020");
    form.setAsBuiltNotificationStatus(AsBuiltNotificationStatus.PER_CONSENT);
    form.setOgaSubmissionReason("Reason");
    return form;
  }

}
