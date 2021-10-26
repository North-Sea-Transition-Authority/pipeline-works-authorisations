package uk.co.ogauthority.pwa.service.asbuilt.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.exception.AsBuiltNotificationGroupNotFoundException;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipelineUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatusHistory;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmission;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmissionUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentTestUtil;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupPipelineRepository;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupStatusHistoryRepository;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationSubmissionRepository;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationGroupDetailService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationGroupService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;
import uk.co.ogauthority.pwa.testutils.AsBuiltNotificationGroupStatusHistoryTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltViewerServiceTest {

  private AsBuiltViewerService asBuiltViewerService;

  @Mock
  private AsBuiltNotificationViewService asBuiltNotificationViewService;

  @Mock
  private AsBuiltNotificationSummaryService asBuiltNotificationSummaryService;

  @Mock
  private AsBuiltNotificationGroupService asBuiltNotificationGroupService;

  @Mock
  private AsBuiltNotificationGroupDetailService asBuiltNotificationGroupDetailService;

  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private AsBuiltNotificationSubmissionRepository asBuiltNotificationSubmissionRepository;

  @Mock
  private AsBuiltNotificationGroupPipelineRepository asBuiltNotificationGroupPipelineRepository;

  @Mock
  private AsBuiltNotificationGroupStatusHistoryRepository asBuiltNotificationGroupStatusHistoryRepository;

  private static final int NOTIFICATION_GROUP_ID = 1;
  private static final int PIPELINE_ID = 99;
  private final PipelineId pipelineId = new PipelineId(PIPELINE_ID);
  private final Person person = PersonTestUtil.createDefaultPerson();
  private final PwaConsent pwaConsent = PwaConsentTestUtil.createPwaConsent(40, "CONSENT_REF", Instant.now());
  private final AsBuiltNotificationGroup asBuiltNotificationGroup = new AsBuiltNotificationGroup(pwaConsent, "APP_REF",
      Instant.now());
  private final AsBuiltNotificationGroupDetail
      asBuiltNotificationGroupDetail = new AsBuiltNotificationGroupDetail(asBuiltNotificationGroup, LocalDate.now(), person.getId(),
      Instant.now());
  private final PipelineDetail pipelineDetail = PipelineDetailTestUtil.createPipelineDetail(20, new PipelineId(30), Instant.now());
  private final PipelineDetail pipelineDetail2 = PipelineDetailTestUtil.createPipelineDetail(21, new PipelineId(31), Instant.now());
  private final AsBuiltNotificationGroupPipeline asBuiltNotificationGroupPipeline =
      AsBuiltNotificationGroupPipelineUtil.createAsBuiltNotificationGroupPipeline(asBuiltNotificationGroup,
          pipelineDetail.getPipelineDetailId(), PipelineChangeCategory.NEW_PIPELINE);
  private final AsBuiltNotificationGroupPipeline asBuiltNotificationGroupPipeline2 =
      AsBuiltNotificationGroupPipelineUtil.createAsBuiltNotificationGroupPipeline(asBuiltNotificationGroup,
          pipelineDetail2.getPipelineDetailId(), PipelineChangeCategory.NEW_PIPELINE);
  private final AsBuiltNotificationSubmission asBuiltNotificationSubmission = AsBuiltNotificationSubmissionUtil
      .createAsBuiltNotificationSubmission_withPerson(asBuiltNotificationGroupPipeline, person);
  private final AsBuiltNotificationSubmission olderAsBuiltNotificationSubmission = AsBuiltNotificationSubmissionUtil
      .createAsBuiltNotificationSubmission_withPersonAndSubmittedDateTime(asBuiltNotificationGroupPipeline, person,
          Instant.now().minusSeconds(1000L));
  private final AsBuiltNotificationGroupStatusHistory asBuiltNotificationGroupStatusHistory =
      AsBuiltNotificationGroupStatusHistoryTestUtil.createAsBuiltStatusHistory_withNotificationGroup(asBuiltNotificationGroup,
          AsBuiltNotificationGroupStatus.COMPLETE);

  @Before
  public void setup() {
    asBuiltViewerService = new AsBuiltViewerService(asBuiltNotificationViewService, asBuiltNotificationSummaryService,
        asBuiltNotificationGroupService, asBuiltNotificationGroupDetailService, asBuiltNotificationSubmissionRepository,
        asBuiltNotificationGroupPipelineRepository, asBuiltNotificationGroupStatusHistoryRepository, pipelineDetailService);

    asBuiltNotificationGroup.setId(NOTIFICATION_GROUP_ID);
    when(asBuiltNotificationGroupService.getAsBuiltNotificationGroup(asBuiltNotificationGroup.getId()))
        .thenReturn(Optional.of(asBuiltNotificationGroup));
    when(asBuiltNotificationGroupDetailService.getAsBuiltNotificationGroupDetail(asBuiltNotificationGroup))
        .thenReturn(Optional.of(asBuiltNotificationGroupDetail));

    when(pipelineDetailService.getByPipelineDetailId(pipelineDetail.getPipelineDetailId().asInt())).thenReturn(pipelineDetail);
    when(pipelineDetailService.getByPipelineDetailId(pipelineDetail2.getPipelineDetailId().asInt())).thenReturn(pipelineDetail2);
    when(pipelineDetailService.getAllPipelineDetailsForPipeline(pipelineId)).thenReturn(List.of(pipelineDetail, pipelineDetail2));

    when(asBuiltNotificationGroupPipelineRepository.findAllByAsBuiltNotificationGroup_Id(asBuiltNotificationGroup.getId()))
        .thenReturn(List.of(asBuiltNotificationGroupPipeline, asBuiltNotificationGroupPipeline2));
    when(asBuiltNotificationGroupPipelineRepository.findAllByPipelineDetailIdIn(List.of(pipelineDetail.getPipelineDetailId(),
        pipelineDetail2.getPipelineDetailId())))
        .thenReturn(List.of(asBuiltNotificationGroupPipeline, asBuiltNotificationGroupPipeline2));

    when(asBuiltNotificationSubmissionRepository.findAllByAsBuiltNotificationGroupPipelineIn(List.of(asBuiltNotificationGroupPipeline,
        asBuiltNotificationGroupPipeline2)))
        .thenReturn(List.of(asBuiltNotificationSubmission, olderAsBuiltNotificationSubmission));
    when(asBuiltNotificationSubmissionRepository.findAllByAsBuiltNotificationGroupPipelineInAndTipFlagIsTrue(
        List.of(asBuiltNotificationGroupPipeline, asBuiltNotificationGroupPipeline2))).thenReturn(List.of(asBuiltNotificationSubmission));
  }

  @Test
  public void getAsBuiltNotificationGroupSummaryView_serviceCalledSuccessfully() {
    asBuiltViewerService.getAsBuiltNotificationGroupSummaryView(asBuiltNotificationGroup.getId());
    verify(asBuiltNotificationSummaryService).getAsBuiltNotificationGroupSummaryView(asBuiltNotificationGroupDetail);
  }

  @Test
  public void getAsBuiltPipelineNotificationSubmissionViews_viewWithSubmission_callsCorrectMappingMethod() {
    asBuiltViewerService.getAsBuiltPipelineNotificationSubmissionViews(NOTIFICATION_GROUP_ID);
    verify(asBuiltNotificationViewService).mapToAsBuiltNotificationView(pipelineDetail, asBuiltNotificationSubmission);
  }

  @Test
  public void getAsBuiltPipelineNotificationSubmissionViews_viewWithNoSubmission_callsCorrectMappingMethod() {
    asBuiltViewerService.getAsBuiltPipelineNotificationSubmissionViews(NOTIFICATION_GROUP_ID);
    verify(asBuiltNotificationViewService).mapToAsBuiltNotificationViewWithNoSubmission(NOTIFICATION_GROUP_ID, pipelineDetail2);
  }

  @Test
  public void getOverviewsWithAsBuiltStatus() {
    var overview = PipelineDetailTestUtil
        .createPipelineOverviewWithAsBuiltStatus("REF", PipelineStatus.IN_SERVICE, AsBuiltNotificationStatus.PER_CONSENT);
    var pipelineDetailFromOverview = PipelineDetailTestUtil.createPipelineDetail(10, new PipelineId(overview.getPipelineId()), Instant.now());
    var submission = AsBuiltNotificationSubmissionUtil
        .createDefaultAsBuiltNotificationSubmission_fromPipelineDetail(pipelineDetailFromOverview, overview.getAsBuiltNotificationStatus());

    when(pipelineDetailService.getLatestPipelineDetailsForIds(List.of(pipelineDetailFromOverview.getPipeline().getId())))
        .thenReturn(List.of(pipelineDetailFromOverview));
    when(asBuiltNotificationGroupPipelineRepository.findAllByPipelineDetailIdIn(List.of(pipelineDetailFromOverview.getPipelineDetailId())))
        .thenReturn(List.of(asBuiltNotificationGroupPipeline));
    when(asBuiltNotificationSubmissionRepository.findAllByAsBuiltNotificationGroupPipelineInAndTipFlagIsTrue(
        List.of(asBuiltNotificationGroupPipeline))).thenReturn(List.of(submission));

    assertThat(asBuiltViewerService.getOverviewsWithAsBuiltStatus(List.of(overview)))
        .extracting(PipelineOverview::getPipelineId, PipelineOverview::getAsBuiltNotificationStatus)
        .containsExactly(tuple(overview.getPipelineId(), submission.getAsBuiltNotificationStatus()));
  }

  @Test
  public void getHistoricAsBuiltSubmissionView() {
    asBuiltViewerService.getHistoricAsBuiltSubmissionView(PIPELINE_ID);
    verify(asBuiltNotificationViewService).getSubmissionHistoryView(List.of(asBuiltNotificationSubmission,
        olderAsBuiltNotificationSubmission));
  }

  @Test
  public void getNotificationGroup() {
    when(asBuiltNotificationGroupService.getAsBuiltNotificationGroup(NOTIFICATION_GROUP_ID)).thenReturn(Optional.of(asBuiltNotificationGroup));
    assertThat(asBuiltViewerService.getNotificationGroup(NOTIFICATION_GROUP_ID)).isEqualTo(asBuiltNotificationGroup);
  }

  @Test
  public void getNotificationGroup_notFound_thowsAsBuiltNotificationGroupNotFoundException() {
    when(asBuiltNotificationGroupService.getAsBuiltNotificationGroup(NOTIFICATION_GROUP_ID)).thenReturn(Optional.empty());

    Exception exception = assertThrows(AsBuiltNotificationGroupNotFoundException.class,
        () -> asBuiltViewerService.getNotificationGroup(NOTIFICATION_GROUP_ID));

    String expectedMessage = String.format("Could not find as-built notification group with id %s", NOTIFICATION_GROUP_ID);
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void getNotificationGroupOptionalFromConsent() {
    when(asBuiltNotificationGroupService.getAsBuiltNotificationGroupPerConsent(pwaConsent)).thenReturn(Optional.of(asBuiltNotificationGroup));
    assertThat(asBuiltViewerService.getNotificationGroupOptionalFromConsent(pwaConsent)).isEqualTo(Optional.of(asBuiltNotificationGroup));
  }

  @Test
  public void canGroupBeReopened_canReopen() {
    when(asBuiltNotificationGroupStatusHistoryRepository
        .findByAsBuiltNotificationGroupAndStatusAndEndedTimestampIsNull(asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.COMPLETE))
        .thenReturn(Optional.of(asBuiltNotificationGroupStatusHistory));
    when(asBuiltNotificationGroupService.getAsBuiltNotificationGroupPerConsent(pwaConsent)).thenReturn(Optional.of(asBuiltNotificationGroup));
    assertTrue(asBuiltViewerService.canGroupBeReopened(pwaConsent));
  }

  @Test
  public void canGroupBeReopened_noCompleteHistoryFound_cannotReopen() {
    when(asBuiltNotificationGroupStatusHistoryRepository
        .findByAsBuiltNotificationGroupAndStatusAndEndedTimestampIsNull(asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.COMPLETE))
        .thenReturn(Optional.empty());
    when(asBuiltNotificationGroupService.getAsBuiltNotificationGroupPerConsent(pwaConsent)).thenReturn(Optional.of(asBuiltNotificationGroup));
    assertFalse(asBuiltViewerService.canGroupBeReopened(pwaConsent));
  }

  @Test
  public void isGroupStatusComplete_complete() {
    when(asBuiltNotificationGroupStatusHistoryRepository.findByAsBuiltNotificationGroupAndStatusAndEndedTimestampIsNull(
        asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.COMPLETE))
        .thenReturn(Optional.of(asBuiltNotificationGroupStatusHistory));
    assertTrue(asBuiltViewerService.isGroupStatusComplete(asBuiltNotificationGroup));
  }

  @Test
  public void isGroupStatusComplete_notComplete() {
    when(asBuiltNotificationGroupStatusHistoryRepository.findByAsBuiltNotificationGroupAndStatusAndEndedTimestampIsNull(
        asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.COMPLETE))
        .thenReturn(Optional.empty());
    assertFalse(asBuiltViewerService.isGroupStatusComplete(asBuiltNotificationGroup));
  }

}
