package uk.co.ogauthority.pwa.features.application.submission.controller;

import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.ReviewAndSubmitApplicationEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class SendAppToSubmitterService {

  private final CaseLinkService caseLinkService;
  private final ApplicationUpdateRequestService applicationUpdateRequestService;
  private final EmailService emailService;

  @Autowired
  public SendAppToSubmitterService(CaseLinkService caseLinkService,
                                   ApplicationUpdateRequestService applicationUpdateRequestService,
                                   EmailService emailService) {
    this.caseLinkService = caseLinkService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
    this.emailService = emailService;
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
        caseLinkService.generateReviewAndSubmitLink(detail.getPwaApplication()));

    emailService.sendEmail(emailProps, submitterPersonSendingTo, detail.getPwaApplicationRef());
  }

}
