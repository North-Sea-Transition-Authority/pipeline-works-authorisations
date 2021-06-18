package uk.co.ogauthority.pwa.service.asbuilt.view;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.AsBuiltNotificationGroupNotFoundException;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmission;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.view.asbuilt.AsBuiltNotificationGroupSummaryView;
import uk.co.ogauthority.pwa.model.view.asbuilt.AsBuiltNotificationView;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupPipelineRepository;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationSubmissionRepository;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailRepository;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationGroupDetailService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationGroupService;

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
  private final PipelineDetailRepository pipelineDetailRepository;

  @Autowired
  public AsBuiltViewerService(AsBuiltNotificationViewService asBuiltNotificationViewService,
                              AsBuiltNotificationSummaryService asBuiltNotificationSummaryService,
                              AsBuiltNotificationGroupService asBuiltNotificationGroupService,
                              AsBuiltNotificationGroupDetailService asBuiltNotificationGroupDetailService,
                              AsBuiltNotificationSubmissionRepository asBuiltNotificationSubmissionRepository,
                              AsBuiltNotificationGroupPipelineRepository asBuiltNotificationGroupPipelineRepository,
                              PipelineDetailRepository pipelineDetailRepository) {
    this.asBuiltNotificationViewService = asBuiltNotificationViewService;
    this.asBuiltNotificationSummaryService = asBuiltNotificationSummaryService;
    this.asBuiltNotificationGroupService = asBuiltNotificationGroupService;
    this.asBuiltNotificationGroupDetailService = asBuiltNotificationGroupDetailService;
    this.asBuiltNotificationSubmissionRepository = asBuiltNotificationSubmissionRepository;
    this.asBuiltNotificationGroupPipelineRepository = asBuiltNotificationGroupPipelineRepository;
    this.pipelineDetailRepository = pipelineDetailRepository;
  }

  public AsBuiltNotificationGroupSummaryView getAsBuiltNotificationGroupSummaryView(Integer notificationGroupId) {
    var ngGroup = getNotificationGroup(notificationGroupId);
    var ngGroupDetail = getNotificationGroupDetail(ngGroup);
    return asBuiltNotificationSummaryService.getAsBuiltNotificationGroupSummaryView(ngGroupDetail);
  }

  public List<AsBuiltNotificationView> getAsBuiltPipelineNotificationSubmissionViews(Integer notificationGroupId) {
    var asBuiltGroupPipelines = asBuiltNotificationGroupPipelineRepository
        .findAllByAsBuiltNotificationGroup_Id(notificationGroupId);
    var submissionsForAsGroupPipelines = getAsBuiltNotificationSubmissions(asBuiltGroupPipelines);
    var latestSubmissionForEachPipeline = getLatestSubmissionsForEachPipeline(asBuiltGroupPipelines,
        submissionsForAsGroupPipelines);
    return getAsBuiltNotificationViewsFromSubmissions(notificationGroupId, asBuiltGroupPipelines, latestSubmissionForEachPipeline);
  }

  private List<AsBuiltNotificationSubmission> getAsBuiltNotificationSubmissions(List<AsBuiltNotificationGroupPipeline>
                                                                                   asBuiltNotificationGroupPipelines) {
    return asBuiltNotificationSubmissionRepository.findAllByAsBuiltNotificationGroupPipelineIn(asBuiltNotificationGroupPipelines);
  }

  public Optional<AsBuiltNotificationGroup> getNotificationGroupOptional(Integer notificationGroupId) {
    return asBuiltNotificationGroupService.getAsBuiltNotificationGroup(notificationGroupId);
  }

  private AsBuiltNotificationGroup getNotificationGroup(Integer notificationGroupId) {
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
    var submissionsForAsGroupPipelines = getAsBuiltNotificationSubmissions(asBuiltNotificationGroupPipelines);
    Map<PipelineDetailId, AsBuiltNotificationSubmission> latestSubmissionsForEachPipelineDetail = new HashMap<>();
    for (AsBuiltNotificationGroupPipeline asBuiltNotificationGroupPipeline : asBuiltNotificationGroupPipelines) {
      latestSubmissionsForEachPipelineDetail.put(asBuiltNotificationGroupPipeline.getPipelineDetailId(),
          submissionsForAsGroupPipelines.stream()
              .filter(submission -> submission.getAsBuiltNotificationGroupPipeline().getPipelineDetailId()
                  .equals(asBuiltNotificationGroupPipeline.getPipelineDetailId()))
              .max(Comparator.comparing(AsBuiltNotificationSubmission::getSubmittedTimestamp)).orElse(null));
    }
    return latestSubmissionsForEachPipelineDetail;
  }

  private Map<PipelineDetailId, AsBuiltNotificationSubmission> getLatestSubmissionsForEachPipeline(List<AsBuiltNotificationGroupPipeline>
                                                                                                asBuiltNotificationGroupPipelines,
                                                                                                   List<AsBuiltNotificationSubmission>
                                                                                                asBuiltNotificationSubmissions) {
    Map<PipelineDetailId, AsBuiltNotificationSubmission> latestSubmissionsForEachPipelineDetail = new HashMap<>();
    for (AsBuiltNotificationGroupPipeline asBuiltNotificationGroupPipeline : asBuiltNotificationGroupPipelines) {
      latestSubmissionsForEachPipelineDetail.put(asBuiltNotificationGroupPipeline.getPipelineDetailId(),
          asBuiltNotificationSubmissions.stream()
          .filter(submission -> submission.getAsBuiltNotificationGroupPipeline().getPipelineDetailId()
          .equals(asBuiltNotificationGroupPipeline.getPipelineDetailId()))
          .max(Comparator.comparing(AsBuiltNotificationSubmission::getSubmittedTimestamp)).orElse(null));
    }
    return latestSubmissionsForEachPipelineDetail;
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
        .map(ngGroupPipeline -> pipelineDetailRepository.findById(ngGroupPipeline.getPipelineDetailId().asInt()).orElseThrow())
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

}
