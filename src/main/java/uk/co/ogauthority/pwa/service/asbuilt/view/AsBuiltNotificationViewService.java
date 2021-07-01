package uk.co.ogauthority.pwa.service.asbuilt.view;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.asbuilt.AsBuiltNotificationSubmissionController;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmission;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.person.PersonService;

@Service
class AsBuiltNotificationViewService {

  private final PersonService personService;

  @Autowired
  AsBuiltNotificationViewService(PersonService personService) {
    this.personService = personService;
  }

  AsBuiltNotificationView mapToAsBuiltNotificationView(PipelineDetail pipelineDetail,
                                                       AsBuiltNotificationSubmission asBuiltNotificationSubmission) {
    String accessLink = null;
    if (pipelineDetail != null) {
      accessLink = ReverseRouter.route(on(AsBuiltNotificationSubmissionController.class)
          .renderSubmitAsBuiltNotificationForm(
              asBuiltNotificationSubmission.getAsBuiltNotificationGroupPipeline().getAsBuiltNotificationGroup().getId(),
              pipelineDetail.getPipelineDetailId().asInt(), null, null));
    }
    var person = personService.getPersonById(asBuiltNotificationSubmission.getSubmittedByPersonId());
    return new AsBuiltNotificationView(
        asBuiltNotificationSubmission.getAsBuiltNotificationGroupPipeline().getAsBuiltNotificationGroup().getReference(),
        pipelineDetail != null ? pipelineDetail.getPipelineNumber() : null,
        pipelineDetail != null ? pipelineDetail.getPipelineType().getDisplayName() : null,
        person.getFullName(),
        person.getEmailAddress(),
        asBuiltNotificationSubmission.getSubmittedTimestamp(),
        asBuiltNotificationSubmission.getAsBuiltNotificationStatus().getDisplayName(),
        asBuiltNotificationSubmission.getAsBuiltNotificationStatus() != AsBuiltNotificationStatus.NOT_LAID_CONSENT_TIMEFRAME
            ? asBuiltNotificationSubmission.getDateLaid() : null,
        asBuiltNotificationSubmission.getAsBuiltNotificationStatus() == AsBuiltNotificationStatus.NOT_LAID_CONSENT_TIMEFRAME
            ? asBuiltNotificationSubmission.getDateLaid() : null,
        asBuiltNotificationSubmission.getDatePipelineBroughtIntoUse(),
        accessLink);
  }

  AsBuiltNotificationView mapToAsBuiltNotificationViewWithNoSubmission(Integer asBuiltNotificationGroupId, PipelineDetail pipelineDetail) {
    var accessLink = ReverseRouter.route(on(AsBuiltNotificationSubmissionController.class)
        .renderSubmitAsBuiltNotificationForm(asBuiltNotificationGroupId, pipelineDetail.getPipelineDetailId().asInt(),
            null, null));
    return new AsBuiltNotificationView(
        null,
        pipelineDetail.getPipelineNumber(),
        pipelineDetail.getPipelineType().getDisplayName(),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        accessLink);
  }

  private AsBuiltNotificationView mapToAsBuiltNotificationHistoricView(AsBuiltNotificationSubmission asBuiltNotificationSubmission) {
    return mapToAsBuiltNotificationView(null, asBuiltNotificationSubmission);
  }

  AsBuiltSubmissionHistoryView getSubmissionHistoryView(List<AsBuiltNotificationSubmission> asBuiltNotificationSubmissions) {
    if (asBuiltNotificationSubmissions.size() < 1) {
      return new AsBuiltSubmissionHistoryView(null, List.of());
    }
    var latestSubmission = asBuiltNotificationSubmissions.get(0);
    var latestSubmissionHistoryView = mapToAsBuiltNotificationHistoricView(latestSubmission);
    if (asBuiltNotificationSubmissions.size() == 1) {
      return new AsBuiltSubmissionHistoryView(latestSubmissionHistoryView, List.of());
    }
    var historicalSubmissionsViews = asBuiltNotificationSubmissions.subList(1, asBuiltNotificationSubmissions.size()).stream()
        .map(this::mapToAsBuiltNotificationHistoricView)
        .collect(Collectors.toList());
    return new AsBuiltSubmissionHistoryView(latestSubmissionHistoryView, historicalSubmissionsViews);
  }

}
