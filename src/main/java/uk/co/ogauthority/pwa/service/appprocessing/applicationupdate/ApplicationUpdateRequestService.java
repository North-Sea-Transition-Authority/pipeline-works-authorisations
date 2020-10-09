package uk.co.ogauthority.pwa.service.appprocessing.applicationupdate;

import com.google.common.annotations.VisibleForTesting;
import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.applicationupdates.ApplicationUpdateRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.notify.emailproperties.ApplicationUpdateRequestEmailProps;
import uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateRequestView;
import uk.co.ogauthority.pwa.model.workflow.GenericMessageEvent;
import uk.co.ogauthority.pwa.repository.appprocessing.applicationupdates.ApplicationUpdateRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowMessageEvents;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDetailVersioningService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;

@Service
public class ApplicationUpdateRequestService implements AppProcessingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationUpdateRequestService.class);

  private final ApplicationUpdateRequestRepository applicationUpdateRequestRepository;
  private final Clock clock;
  private final NotifyService notifyService;
  private final PwaContactService pwaContactService;
  private final PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService;
  private final WorkflowAssignmentService workflowAssignmentService;

  @Autowired
  public ApplicationUpdateRequestService(ApplicationUpdateRequestRepository applicationUpdateRequestRepository,
                                         @Qualifier("utcClock") Clock clock,
                                         NotifyService notifyService,
                                         PwaContactService pwaContactService,
                                         PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService,
                                         WorkflowAssignmentService workflowAssignmentService) {
    this.applicationUpdateRequestRepository = applicationUpdateRequestRepository;
    this.clock = clock;
    this.notifyService = notifyService;
    this.pwaContactService = pwaContactService;
    this.pwaApplicationDetailVersioningService = pwaApplicationDetailVersioningService;
    this.workflowAssignmentService = workflowAssignmentService;
  }


  @Transactional
  public void submitApplicationUpdateRequest(PwaApplicationDetail pwaApplicationDetail,
                                             WebUserAccount requestingUser,
                                             String requestReason) {
    // The update request was made for a specific version, so the original is linked.
    createApplicationUpdateRequest(pwaApplicationDetail, requestingUser.getLinkedPerson(), requestReason);
    // then a new detail is created which is what the will be resubmitted with any changes.
    var newTipDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(pwaApplicationDetail, requestingUser);
    // then we attempt to send an email to alert the application preparers that changes are required.
    sendApplicationUpdateRequestedEmail(newTipDetail, requestingUser.getLinkedPerson());
    // update workflow
    workflowAssignmentService.triggerWorkflowMessageAndAssertTaskExists(
        GenericMessageEvent.from(
            newTipDetail.getPwaApplication(),
            PwaApplicationWorkflowMessageEvents.UPDATE_APPLICATION_REQUEST.getMessageEventName()
        ),
        PwaApplicationWorkflowTask.UPDATE_APPLICATION
    );
  }


  @VisibleForTesting
  void createApplicationUpdateRequest(PwaApplicationDetail pwaApplicationDetail,
                                      Person requestingPerson,
                                      String requestReason) {
    var updateRequest = ApplicationUpdateRequest.createRequest(
        pwaApplicationDetail,
        requestingPerson,
        clock,
        requestReason);

    applicationUpdateRequestRepository.save(updateRequest);

  }

  @VisibleForTesting
  void sendApplicationUpdateRequestedEmail(PwaApplicationDetail pwaApplicationDetail, Person requestingPerson) {
    var recipients = pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplicationDetail.getPwaApplication(),
        PwaContactRole.PREPARER
    );

    if (!recipients.isEmpty()) {
      recipients.forEach(person ->
          notifyService.sendEmail(
              new ApplicationUpdateRequestEmailProps(
                  person.getFullName(),
                  pwaApplicationDetail.getPwaApplicationRef(),
                  requestingPerson.getFullName()
              ),
              person.getEmailAddress()
          )
      );

    } else {
      LOGGER.error(
          "Tried to send application update request email, but no recipients found. pwaApplication.id:" +
              pwaApplicationDetail.getMasterPwaApplicationId()
      );
    }

  }


  // TODO PWA-161 implements submission of app updates and therefore should determine how update requests go from opened to closed.
  public boolean applicationDetailHasOpenUpdateRequest(PwaApplicationDetail pwaApplicationDetail) {
    return applicationUpdateRequestRepository.existsByPwaApplicationDetail(pwaApplicationDetail);
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE);
  }

  @Override
  public Optional<TaskStatus> getTaskStatus(PwaAppProcessingContext processingContext) {
    return Optional.empty();
  }

  public Optional<ApplicationUpdateRequestView> getOpenRequestView(PwaApplicationDetail pwaApplicationDetail) {
    return applicationUpdateRequestRepository.findByPwaApplicationDetail(pwaApplicationDetail);
  }
}
