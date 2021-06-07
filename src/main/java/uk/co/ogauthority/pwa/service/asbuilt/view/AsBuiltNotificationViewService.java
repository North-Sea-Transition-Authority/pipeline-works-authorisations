package uk.co.ogauthority.pwa.service.asbuilt.view;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.view.asbuilt.AsBuiltPipelineNotificationSubmissionView;

//TODO: PWA-988 this class will be changed when an actual AsBuiltNotification model will exist.

@Service
class AsBuiltNotificationViewService {

  @Autowired
  AsBuiltNotificationViewService() {
  }

  List<AsBuiltPipelineNotificationSubmissionView> getAsBuiltPipelineNotificationSubmissionViews(List<PipelineDetail> pipelineDetails) {
    return pipelineDetails.stream()
        .map(this::mapToAsBuiltNotificationView)
        .collect(Collectors.toList());
  }

  private AsBuiltPipelineNotificationSubmissionView mapToAsBuiltNotificationView(PipelineDetail pipelineDetail) {
    return new AsBuiltPipelineNotificationSubmissionView(
        pipelineDetail.getPipelineNumber(),
        pipelineDetail.getPipelineType().getDisplayName(),
        null,
        null,
        null,
        null,
        null,
        LocalDate.now(),
        null);
  }

}
