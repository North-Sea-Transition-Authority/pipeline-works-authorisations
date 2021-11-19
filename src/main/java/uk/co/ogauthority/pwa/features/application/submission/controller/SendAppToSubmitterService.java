package uk.co.ogauthority.pwa.features.application.submission.controller;

import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.features.email.EmailCaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.ReviewAndSubmitApplicationEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class SendAppToSubmitterService {

  private final NotifyService notifyService;
  private final EmailCaseLinkService emailCaseLinkService;
  private final ApplicationUpdateRequestService applicationUpdateRequestService;

  @Autowired
  public SendAppToSubmitterService(NotifyService notifyService,
                                   EmailCaseLinkService emailCaseLinkService,
                                   ApplicationUpdateRequestService applicationUpdateRequestService) {
    this.notifyService = notifyService;
    this.emailCaseLinkService = emailCaseLinkService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
  }

  @Transactional
  public void sendToSubmitter(PwaApplicationDetail detail,
                              Person sentByPerson,
                              @Nullable String updateReasonText,
                              Person submitterPersonSendingTo) {

    // store the update request details (if any have been provided) without submitting the response (this will be done by submitter)
    Optional.ofNullable(updateReasonText).ifPresent(text ->
        applicationUpdateRequestService.storeResponseWithoutSubmitting(detail, sentByPerson, text));

    var emailProps = new ReviewAndSubmitApplicationEmailProps(
        submitterPersonSendingTo.getFullName(),
        detail.getPwaApplicationRef(),
        sentByPerson.getFullName(),
        emailCaseLinkService.generateReviewAndSubmitLink(detail.getPwaApplication()));

    notifyService.sendEmail(emailProps, submitterPersonSendingTo.getEmailAddress());

  }

}
