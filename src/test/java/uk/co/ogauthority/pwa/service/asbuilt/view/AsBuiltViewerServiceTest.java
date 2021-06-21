package uk.co.ogauthority.pwa.service.asbuilt.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipelineUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmission;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmissionUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentTestUtil;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupPipelineRepository;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationSubmissionRepository;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationGroupDetailService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationGroupService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;

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

  private static final int NOTIFICATION_GROUP_ID = 1;
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

  @Before
  public void setup() {
    asBuiltViewerService = new AsBuiltViewerService(asBuiltNotificationViewService, asBuiltNotificationSummaryService,
        asBuiltNotificationGroupService, asBuiltNotificationGroupDetailService, asBuiltNotificationSubmissionRepository,
        asBuiltNotificationGroupPipelineRepository, pipelineDetailService);

    asBuiltNotificationGroup.setId(NOTIFICATION_GROUP_ID);
    when(asBuiltNotificationGroupService.getAsBuiltNotificationGroup(asBuiltNotificationGroup.getId()))
        .thenReturn(Optional.of(asBuiltNotificationGroup));
    when(asBuiltNotificationGroupDetailService.getAsBuiltNotificationGroupDetail(asBuiltNotificationGroup))
        .thenReturn(Optional.of(asBuiltNotificationGroupDetail));
    when(pipelineDetailService.getLatestByPipelineId(pipelineDetail.getPipelineDetailId().asInt())).thenReturn(pipelineDetail);
    when(pipelineDetailService.getLatestByPipelineId(pipelineDetail2.getPipelineDetailId().asInt())).thenReturn(pipelineDetail2);
    when(asBuiltNotificationGroupPipelineRepository.findAllByAsBuiltNotificationGroup_Id(asBuiltNotificationGroup.getId()))
        .thenReturn(List.of(asBuiltNotificationGroupPipeline, asBuiltNotificationGroupPipeline2));
    when(asBuiltNotificationSubmissionRepository.findAllByAsBuiltNotificationGroupPipelineIn(List.of(asBuiltNotificationGroupPipeline,
        asBuiltNotificationGroupPipeline2)))
        .thenReturn(List.of(asBuiltNotificationSubmission));
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
    when(asBuiltNotificationSubmissionRepository.findAllByAsBuiltNotificationGroupPipelineIn(List.of(asBuiltNotificationGroupPipeline)))
        .thenReturn(List.of(submission));

    assertThat(asBuiltViewerService.getOverviewsWithAsBuiltStatus(List.of(overview)))
        .extracting(PipelineOverview::getPipelineId, PipelineOverview::getAsBuiltNotificationStatus)
        .containsExactly(tuple(overview.getPipelineId(), submission.getAsBuiltNotificationStatus()));
  }

}
