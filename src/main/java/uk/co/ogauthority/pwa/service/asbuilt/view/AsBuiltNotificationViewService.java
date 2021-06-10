package uk.co.ogauthority.pwa.service.asbuilt.view;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.asbuilt.AsBuiltNotificationSubmissionController;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.view.asbuilt.AsBuiltPipelineNotificationSubmissionView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

//TODO: PWA-1253 this class will be changed when an actual AsBuiltNotification model will exist and can be mapped to an actual view.

@Service
class AsBuiltNotificationViewService {

  @Autowired
  AsBuiltNotificationViewService() {
  }

  List<AsBuiltPipelineNotificationSubmissionView> getAsBuiltPipelineNotificationSubmissionViews(Integer asBuiltNotificationGroupId,
                                                                                                List<PipelineDetail> pipelineDetails) {
    return pipelineDetails.stream()
        .map(pipelineDetail -> mapToAsBuiltNotificationView(pipelineDetail,
            ReverseRouter.route(on(AsBuiltNotificationSubmissionController.class)
                .renderSubmitAsBuiltNotificationForm(asBuiltNotificationGroupId, pipelineDetail.getPipelineDetailId().asInt(),
                    null, null))))
        .collect(Collectors.toList());
  }

  private AsBuiltPipelineNotificationSubmissionView mapToAsBuiltNotificationView(PipelineDetail pipelineDetail, String accessLink) {
    return new AsBuiltPipelineNotificationSubmissionView(
        pipelineDetail.getPipelineNumber(),
        pipelineDetail.getPipelineType().getDisplayName(),
        null,
        null,
        null,
        null,
        null,
        LocalDate.now(),
        accessLink);
  }

}
