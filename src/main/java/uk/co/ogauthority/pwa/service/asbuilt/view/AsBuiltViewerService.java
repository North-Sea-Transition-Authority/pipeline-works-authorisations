package uk.co.ogauthority.pwa.service.asbuilt.view;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.exception.AsBuiltNotificationGroupNotFoundException;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmission;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.view.asbuilt.AsBuiltNotificationGroupSummaryView;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupPipelineRepository;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupStatusHistoryRepository;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationSubmissionRepository;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationGroupDetailService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationGroupService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

/**
 * Allows access to view services related to the as-built domain, following Facade pattern.
 */
@Service
public class AsBuiltViewerService {

  private final AsBuiltNotificationViewService asBuiltNotificationViewService;
  private final AsBuiltNotificationSummaryService asBuiltNotificationSummaryService;
  private final AsBuiltNotificationGroupService asBuiltNotificationGroupService;
  private final AsBuiltNotificationGroupDetailService asBuiltNotificationGroupDetailService;
  private final AsBuiltNotificationSubmissionRepository asBuiltNotificationSubmissionRepository;
  private final AsBuiltNotificationGroupPipelineRepository asBuiltNotificationGroupPipelineRepository;
  private final AsBuiltNotificationGroupStatusHistoryRepository asBuiltNotificationGroupStatusHistoryRepository;
  private final PipelineDetailService pipelineDetailService;

  @Autowired
  public AsBuiltViewerService(AsBuiltNotificationViewService asBuiltNotificationViewService,
                              AsBuiltNotificationSummaryService asBuiltNotificationSummaryService,
                              AsBuiltNotificationGroupService asBuiltNotificationGroupService,
                              AsBuiltNotificationGroupDetailService asBuiltNotificationGroupDetailService,
                              AsBuiltNotificationSubmissionRepository asBuiltNotificationSubmissionRepository,
                              AsBuiltNotificationGroupPipelineRepository asBuiltNotificationGroupPipelineRepository,
                              AsBuiltNotificationGroupStatusHistoryRepository asBuiltNotificationGroupStatusHistoryRepository,
                              PipelineDetailService pipelineDetailService) {
    this.asBuiltNotificationViewService = asBuiltNotificationViewService;
    this.asBuiltNotificationSummaryService = asBuiltNotificationSummaryService;
    this.asBuiltNotificationGroupService = asBuiltNotificationGroupService;
    this.asBuiltNotificationGroupDetailService = asBuiltNotificationGroupDetailService;
    this.asBuiltNotificationSubmissionRepository = asBuiltNotificationSubmissionRepository;
    this.asBuiltNotificationGroupPipelineRepository = asBuiltNotificationGroupPipelineRepository;
    this.asBuiltNotificationGroupStatusHistoryRepository = asBuiltNotificationGroupStatusHistoryRepository;
    this.pipelineDetailService = pipelineDetailService;
  }

  public AsBuiltNotificationGroupSummaryView getAsBuiltNotificationGroupSummaryView(Integer notificationGroupId) {
    var ngGroup = getNotificationGroup(notificationGroupId);
    var ngGroupDetail = getNotificationGroupDetail(ngGroup);
    return asBuiltNotificationSummaryService.getAsBuiltNotificationGroupSummaryView(ngGroupDetail);
  }

  public List<AsBuiltNotificationView> getAsBuiltPipelineNotificationSubmissionViews(Integer notificationGroupId) {
    var asBuiltGroupPipelines = asBuiltNotificationGroupPipelineRepository
        .findAllByAsBuiltNotificationGroup_Id(notificationGroupId);
    var latestSubmissionForEachPipeline = asBuiltNotificationSubmissionRepository
        .findAllByAsBuiltNotificationGroupPipelineInAndTipFlagIsTrue(asBuiltGroupPipelines);
    var pipelineDetailIdAsBuiltSubmissionMap = mapSubmissionsToPipelineDetailIds(latestSubmissionForEachPipeline);
    return getAsBuiltNotificationViewsFromSubmissions(notificationGroupId, asBuiltGroupPipelines, pipelineDetailIdAsBuiltSubmissionMap);
  }

  private List<AsBuiltNotificationSubmission> getAsBuiltNotificationSubmissionsForPipelineId(Integer pipelineId) {
    var pipelineDetailIds = pipelineDetailService.getAllPipelineDetailsForPipeline(new PipelineId(pipelineId)).stream()
        .map(PipelineDetail::getPipelineDetailId)
        .collect(toList());
    var asBuiltNotificationGroupPipelines = asBuiltNotificationGroupPipelineRepository
        .findAllByPipelineDetailIdIn(pipelineDetailIds);
    var allSubmissions = asBuiltNotificationSubmissionRepository
        .findAllByAsBuiltNotificationGroupPipelineIn(asBuiltNotificationGroupPipelines);
    return allSubmissions.stream()
        .sorted(Comparator.comparing(AsBuiltNotificationSubmission::getSubmittedTimestamp).reversed())
        .collect(Collectors.toList());
  }

  public Optional<AsBuiltNotificationGroup> getNotificationGroupOptionalFromConsent(PwaConsent consent) {
    return asBuiltNotificationGroupService.getAsBuiltNotificationGroupPerConsent(consent);
  }

  public AsBuiltNotificationGroup getNotificationGroup(Integer notificationGroupId) {
    return asBuiltNotificationGroupService
        .getAsBuiltNotificationGroup(notificationGroupId)
        .orElseThrow(() ->
            new AsBuiltNotificationGroupNotFoundException(String.format("Could not find as-built notification group with id %s",
            notificationGroupId)));
  }

  private AsBuiltNotificationGroupDetail getNotificationGroupDetail(AsBuiltNotificationGroup asBuiltNotificationGroup) {
    return asBuiltNotificationGroupDetailService
        .getAsBuiltNotificationGroupDetail(asBuiltNotificationGroup)
        .orElseThrow(() -> new AsBuiltNotificationGroupNotFoundException(
            String.format("Could not find as-built notification group detail for group with with id %s",
                asBuiltNotificationGroup.getId())));
  }

  public Map<PipelineDetailId, AsBuiltNotificationSubmission> getLatestSubmissionsForEachPipelineDetailId(List<PipelineDetailId>
                                                                                                              pipelineDetailIds) {
    var asBuiltNotificationGroupPipelines = asBuiltNotificationGroupPipelineRepository
        .findAllByPipelineDetailIdIn(pipelineDetailIds);
    var latestSubmissions = asBuiltNotificationSubmissionRepository
        .findAllByAsBuiltNotificationGroupPipelineInAndTipFlagIsTrue(asBuiltNotificationGroupPipelines);
    return mapSubmissionsToPipelineDetailIds(latestSubmissions);
  }

  private Map<PipelineDetailId, AsBuiltNotificationSubmission> mapSubmissionsToPipelineDetailIds(List<AsBuiltNotificationSubmission>
                                                                                                       latestSubmissions) {
    Map<PipelineDetailId, AsBuiltNotificationSubmission> pipelineDetailIdToSubmissionMap = new HashMap<>();
    for (AsBuiltNotificationSubmission submission : latestSubmissions) {
      pipelineDetailIdToSubmissionMap.put(submission.getAsBuiltNotificationGroupPipeline().getPipelineDetailId(), submission);
    }
    return pipelineDetailIdToSubmissionMap;
  }

  private List<AsBuiltNotificationView> getAsBuiltNotificationViewsFromSubmissions(Integer notificationGroupId,
                                                                    List<AsBuiltNotificationGroupPipeline> asBuiltGroupPipelines,
                                                                    Map<PipelineDetailId, AsBuiltNotificationSubmission>
                                                                                      latestSubmissionsForEachPipeline) {
    var pipelineDetails = getAllPipelineDetailsForAsBuiltGroup(notificationGroupId);
    return asBuiltGroupPipelines.stream().map(asBuiltNotificationGroupPipeline -> {
      var pipelineDetail = findPipelineDetail(asBuiltNotificationGroupPipeline.getPipelineDetailId(),
          asBuiltNotificationGroupPipeline.getId(), pipelineDetails);
      var latestSubmissionForPipeline = latestSubmissionsForEachPipeline.get(pipelineDetail.getPipelineDetailId());
      if (latestSubmissionForPipeline != null) {
        return asBuiltNotificationViewService.mapToAsBuiltNotificationView(pipelineDetail, latestSubmissionForPipeline);
      }
      return asBuiltNotificationViewService.mapToAsBuiltNotificationViewWithNoSubmission(asBuiltNotificationGroupPipeline
          .getAsBuiltNotificationGroup().getId(), pipelineDetail);
    }).collect(Collectors.toList());
  }

  private List<PipelineDetail> getAllPipelineDetailsForAsBuiltGroup(Integer notificationGroupId) {
    var asBuiltGroupPipelines =  asBuiltNotificationGroupPipelineRepository
        .findAllByAsBuiltNotificationGroup_Id(notificationGroupId);
    return asBuiltGroupPipelines.stream()
        .map(ngGroupPipeline -> pipelineDetailService.getByPipelineDetailId(ngGroupPipeline.getPipelineDetailId().asInt()))
        .collect(Collectors.toList());
  }

  private PipelineDetail findPipelineDetail(PipelineDetailId pipelineDetailId, Integer asBuiltPipelineGroupId,
                                            List<PipelineDetail> pipelineDetails) {
    return pipelineDetails.stream()
        .filter(detail ->
            detail.getPipelineDetailId().equals(pipelineDetailId))
        .findAny().orElseThrow(
            () -> new EntityNotFoundException(
                String.format("Pipeline detail with id %s could not be found within as-built pipeline group with id %s",
                    pipelineDetailId.asInt(), asBuiltPipelineGroupId)));
  }

  public List<PipelineOverview> getOverviewsWithAsBuiltStatus(List<PipelineOverview> pipelineOverviews) {
    var pipelineIds = pipelineOverviews.stream()
        .map(PipelineOverview::getPipelineId)
        .collect(toList());
    var pipelineIdToDetailMap = pipelineDetailService.getLatestPipelineDetailsForIds(pipelineIds).stream()
        .collect(Collectors.toMap(PipelineDetail::getPipelineId, PipelineDetail::getPipelineDetailId));
    var latestSubmissionsForEachPipelineDetailId = getLatestSubmissionsForEachPipelineDetailId(
        new ArrayList<>(pipelineIdToDetailMap.values()));
    Map<Integer, AsBuiltNotificationSubmission> pipelineIdToSubmissionMap = new HashMap<>();
    for (PipelineId pipelineId : pipelineIdToDetailMap.keySet()) {
      pipelineIdToSubmissionMap.put(pipelineId.asInt(), latestSubmissionsForEachPipelineDetailId
          .get(pipelineIdToDetailMap.get(pipelineId)));
    }
    return pipelineOverviews.stream().map(pipelineOverview -> {
      var submission = pipelineIdToSubmissionMap.get(pipelineOverview.getPipelineId());
      if (Objects.nonNull(submission)) {
        return PadPipelineOverview.from(pipelineOverview, submission.getAsBuiltNotificationStatus());
      }
      return pipelineOverview;
    }).collect(toList());
  }

  public AsBuiltSubmissionHistoryView getHistoricAsBuiltSubmissionView(Integer pipelineId) {
    var asBuiltNotificationSubmissions = getAsBuiltNotificationSubmissionsForPipelineId(pipelineId);
    return asBuiltNotificationViewService.getSubmissionHistoryView(asBuiltNotificationSubmissions);
  }

  public boolean canGroupBeReopened(PwaConsent pwaConsent) {
    var asBuiltGroupOptional = getNotificationGroupOptionalFromConsent(pwaConsent);
    return asBuiltGroupOptional.map(this::isGroupStatusComplete)
        .orElse(false);
  }

  public boolean isGroupStatusComplete(AsBuiltNotificationGroup asBuiltNotificationGroup) {
    return asBuiltNotificationGroupStatusHistoryRepository.findByAsBuiltNotificationGroupAndStatusAndEndedTimestampIsNull(
            asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.COMPLETE).isPresent();
  }

}
