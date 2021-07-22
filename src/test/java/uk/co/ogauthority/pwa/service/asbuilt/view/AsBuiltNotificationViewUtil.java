package uk.co.ogauthority.pwa.service.asbuilt.view;

import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmission;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationSubmissionUtil;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;

public class AsBuiltNotificationViewUtil {

  public static AsBuiltNotificationView createDefaultAsBuiltNotificationView() {
    var person = PersonTestUtil.createDefaultPerson();
    var submission = AsBuiltNotificationSubmissionUtil.createDefaultAsBuiltNotificationSubmission_fromStatus(
        AsBuiltNotificationStatus.PER_CONSENT);
    return new AsBuiltNotificationView(submission.getAsBuiltNotificationGroupPipeline().getAsBuiltNotificationGroup().getReference(),
        null,
        null,
        person.getFullName(),
        person.getEmailAddress(),
        submission.getSubmittedTimestamp(),
        submission.getAsBuiltNotificationStatus().getDisplayName(),
        submission.getDateWorkCompleted(),
        null,
        submission.getDatePipelineBroughtIntoUse(),
        "submission reason",
        "submission link"
    );
  }

  public static AsBuiltNotificationView createHistoricAsBuiltNotificationView(
      AsBuiltNotificationSubmission asBuiltNotificationSubmission,
      Person person) {
    return new AsBuiltNotificationView(
        asBuiltNotificationSubmission.getAsBuiltNotificationGroupPipeline().getAsBuiltNotificationGroup().getReference(),
        null,
        null,
        person.getFullName(),
        person.getEmailAddress(),
        asBuiltNotificationSubmission.getSubmittedTimestamp(),
        asBuiltNotificationSubmission.getAsBuiltNotificationStatus().getDisplayName(),
        asBuiltNotificationSubmission.getDateWorkCompleted(),
        null,
        asBuiltNotificationSubmission.getDatePipelineBroughtIntoUse(),
        "submission reason",
        null
        );
  }

}