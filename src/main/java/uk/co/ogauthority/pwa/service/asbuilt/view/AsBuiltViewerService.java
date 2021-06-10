package uk.co.ogauthority.pwa.service.asbuilt.view;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.AsBuiltNotificationGroupNotFoundException;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.model.view.asbuilt.AsBuiltNotificationGroupSummaryView;
import uk.co.ogauthority.pwa.model.view.asbuilt.AsBuiltPipelineNotificationSubmissionView;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationGroupDetailService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationGroupService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltPipelineNotificationService;

/**
 * Allows access to view services related to the as-built domain, following Facade pattern.
 */
@Service
public class AsBuiltViewerService {

  private final AsBuiltNotificationViewService asBuiltNotificationViewService;
  private final AsBuiltNotificationSummaryService asBuiltNotificationSummaryService;
  private final AsBuiltNotificationGroupService asBuiltNotificationGroupService;
  private final AsBuiltNotificationGroupDetailService asBuiltNotificationGroupDetailService;
  private final AsBuiltPipelineNotificationService asBuiltPipelineNotificationService;

  @Autowired
  public AsBuiltViewerService(AsBuiltNotificationViewService asBuiltNotificationViewService,
                              AsBuiltNotificationSummaryService asBuiltNotificationSummaryService,
                              AsBuiltNotificationGroupService asBuiltNotificationGroupService,
                              AsBuiltNotificationGroupDetailService asBuiltNotificationGroupDetailService,
                              AsBuiltPipelineNotificationService asBuiltPipelineNotificationService) {
    this.asBuiltNotificationViewService = asBuiltNotificationViewService;
    this.asBuiltNotificationSummaryService = asBuiltNotificationSummaryService;
    this.asBuiltNotificationGroupService = asBuiltNotificationGroupService;
    this.asBuiltNotificationGroupDetailService = asBuiltNotificationGroupDetailService;
    this.asBuiltPipelineNotificationService = asBuiltPipelineNotificationService;
  }

  public AsBuiltNotificationGroupSummaryView getAsBuiltNotificationGroupSummaryView(Integer notificationGroupId) {
    var ngGroup = getNotificationGroup(notificationGroupId);
    var ngGroupDetail = getNotificationGroupDetail(ngGroup);
    return asBuiltNotificationSummaryService.getAsBuiltNotificationGroupSummaryView(ngGroupDetail);
  }

  public List<AsBuiltPipelineNotificationSubmissionView> getAsBuiltPipelineNotificationSubmissionViews(Integer notificationGroupId) {
    var pipelineDetails = asBuiltPipelineNotificationService
        .getPipelineDetailsForAsBuiltNotificationGroup(notificationGroupId);
    return asBuiltNotificationViewService.getAsBuiltPipelineNotificationSubmissionViews(notificationGroupId, pipelineDetails);
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

}
