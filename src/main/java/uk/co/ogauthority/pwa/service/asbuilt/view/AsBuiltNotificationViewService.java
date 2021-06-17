package uk.co.ogauthority.pwa.service.asbuilt.view;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.asbuilt.AsBuiltNotificationSubmissionController;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmission;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.view.asbuilt.AsBuiltNotificationView;
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
    var accessLink = ReverseRouter.route(on(AsBuiltNotificationSubmissionController.class)
        .renderSubmitAsBuiltNotificationForm(
            asBuiltNotificationSubmission.getAsBuiltNotificationGroupPipeline().getAsBuiltNotificationGroup().getId(),
            pipelineDetail.getPipelineDetailId().asInt(), null, null));
    var person = personService.getPersonById(asBuiltNotificationSubmission.getSubmittedByPersonId());
    return new AsBuiltNotificationView(
        pipelineDetail.getPipelineNumber(),
        pipelineDetail.getPipelineType().getDisplayName(),
        person.getFullName(),
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
        pipelineDetail.getPipelineNumber(),
        pipelineDetail.getPipelineType().getDisplayName(),
        null,
        null,
        null,
        null,
        null,
        null,
        accessLink);
  }

}
