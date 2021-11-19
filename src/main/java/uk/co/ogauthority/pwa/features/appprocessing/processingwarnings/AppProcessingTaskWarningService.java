package uk.co.ogauthority.pwa.features.appprocessing.processingwarnings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentDocumentUrlProvider;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.ConsultationService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;

/**
 * A service to check app processing tasks for issues and produce warning information when the tasks should be started.
 */
@Service
public class AppProcessingTaskWarningService {

  private final ConsultationService consultationService;
  private final PublicNoticeService publicNoticeService;


  @Autowired
  public AppProcessingTaskWarningService(
      ConsultationService consultationService,
      PublicNoticeService publicNoticeService) {
    this.consultationService = consultationService;
    this.publicNoticeService = publicNoticeService;
  }


  public NonBlockingTasksWarning getNonBlockingTasksWarning(PwaApplication pwaApplication,
                                                            NonBlockingWarningPage nonBlockingWarningPage) {

    var consultationsMissing = consultationService.consultationsTaskRequired(pwaApplication)
        && consultationService.getTaskStatus(pwaApplication).equals(TaskStatus.NOT_STARTED);
    var publicNoticesMissing = publicNoticeService.publicNoticeTaskRequired(pwaApplication)
        && !publicNoticeService.publicNoticeTaskStarted(pwaApplication);

    if (consultationsMissing || publicNoticesMissing) {
      String incompleteTasksWarningText;
      if (consultationsMissing && publicNoticesMissing) {
        incompleteTasksWarningText = "The consultations and public notice tasks have not been started";
      } else if (consultationsMissing) {
        incompleteTasksWarningText = "The consultations task has not been started";
      } else {
        incompleteTasksWarningText = "The public notice task has not been started";
      }

      return NonBlockingTasksWarning.withWarning(
          incompleteTasksWarningText,
          constructNonBlockingWarningReturnMessage(pwaApplication, nonBlockingWarningPage));
    }

    return NonBlockingTasksWarning.withoutWarning();
  }


  private NonBlockingWarningReturnMessage constructNonBlockingWarningReturnMessage(PwaApplication pwaApplication,
                                                                                   NonBlockingWarningPage nonBlockingWarningPage) {

    if (NonBlockingWarningPage.SEND_FOR_APPROVAL.equals(nonBlockingWarningPage)) {
      return new NonBlockingWarningReturnMessage(
          "You can continue to send for approval or go back to ",
          "case management","to start the tasks.",
          CaseManagementUtils.routeCaseManagement(pwaApplication));

    } else {
      var urlProvider = new ConsentDocumentUrlProvider(pwaApplication);
      return NonBlockingWarningReturnMessage.withoutSuffixMessage(
          "You can continue to issue the consent or ",
          "return to case officer",
          urlProvider.getReturnToCaseOfficerUrl());
    }

  }


}
