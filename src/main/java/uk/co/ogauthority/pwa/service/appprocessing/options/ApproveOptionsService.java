package uk.co.ogauthority.pwa.service.appprocessing.options;

import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApplicationApproval;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApprovalDeadlineHistory;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.options.OptionsApprovalDeadlineView;
import uk.co.ogauthority.pwa.model.workflow.GenericMessageEvent;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApplicationApprovalRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApprovalDeadlineHistoryRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowMessageEvents;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDetailVersioningService;
import uk.co.ogauthority.pwa.service.pwaapplications.options.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;

@Service
public class ApproveOptionsService {

  private final OptionsApprovalPersister optionsApprovalPersister;

  private final OptionsApplicationApprovalRepository optionsApplicationApprovalRepository;

  private final OptionsApprovalDeadlineHistoryRepository optionsApprovalDeadlineHistoryRepository;

  private final OptionsCaseManagementEmailService optionsCaseManagementEmailService;

  private final PadOptionConfirmedService padOptionConfirmedService;

  private final WorkflowAssignmentService workflowAssignmentService;

  private final PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService;

  @Autowired
  public ApproveOptionsService(OptionsApprovalPersister optionsApprovalPersister,
                               OptionsApplicationApprovalRepository optionsApplicationApprovalRepository,
                               OptionsApprovalDeadlineHistoryRepository optionsApprovalDeadlineHistoryRepository,
                               OptionsCaseManagementEmailService optionsCaseManagementEmailService,
                               PadOptionConfirmedService padOptionConfirmedService,
                               WorkflowAssignmentService workflowAssignmentService,
                               PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService) {
    this.optionsApprovalPersister = optionsApprovalPersister;
    this.optionsApplicationApprovalRepository = optionsApplicationApprovalRepository;
    this.optionsApprovalDeadlineHistoryRepository = optionsApprovalDeadlineHistoryRepository;
    this.optionsCaseManagementEmailService = optionsCaseManagementEmailService;
    this.padOptionConfirmedService = padOptionConfirmedService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.pwaApplicationDetailVersioningService = pwaApplicationDetailVersioningService;
  }

  /**
   * Return true if an options approval has occurred.
   */
  public boolean optionsApproved(PwaApplication pwaApplication) {
    return getOptionsApproval(pwaApplication).isPresent();
  }

  private Optional<OptionsApplicationApproval> getOptionsApproval(PwaApplication pwaApplication) {
    return optionsApplicationApprovalRepository.findByPwaApplication(
        pwaApplication
    );
  }

  public OptionsApprovalStatus getOptionsApprovalStatus(PwaApplicationDetail pwaApplicationDetail) {
    if (!PwaApplicationType.OPTIONS_VARIATION.equals(pwaApplicationDetail.getPwaApplicationType())) {
      return OptionsApprovalStatus.NOT_APPLICABLE;
    }

    if (!optionsApproved(pwaApplicationDetail.getPwaApplication())) {
      return OptionsApprovalStatus.NOT_APPROVED;
    }

    if (padOptionConfirmedService.optionConfirmationExists(pwaApplicationDetail)) {
      return OptionsApprovalStatus.APPROVED_RESPONDED;
    }

    return OptionsApprovalStatus.APPROVED_UNRESPONDED;

  }

  private OptionsApplicationApproval getOptionsApprovalOrError(PwaApplication pwaApplication) {
    return getOptionsApproval(pwaApplication)
        .orElseThrow(
            () -> new PwaEntityNotFoundException(
                "Could not find active options approval deadline for app.id:" + pwaApplication.getId()
            )
        );
  }

  private Optional<OptionsApprovalDeadlineHistory> getCurrentOptionsApprovalDeadline(PwaApplication pwaApplication) {
    return optionsApprovalDeadlineHistoryRepository.findByOptionsApplicationApproval_PwaApplicationAndTipFlagIsTrue(
        pwaApplication
    );
  }


  @Transactional
  public void approveOptions(PwaApplicationDetail pwaApplicationDetail, WebUserAccount approverWua, Instant deadlineDate) {

    // create a new detail that will be resubmitted with confirmed options.
    // Important this is done first as doing it after creating the approval means versioning will try copy a confirmation
    // that doesnt exist and error.
    var newTipDetail = pwaApplicationDetailVersioningService.createNewApplicationVersion(
        pwaApplicationDetail,
        approverWua
    );

    var initialApprovalDeadlineHistory = optionsApprovalPersister.createInitialOptionsApproval(
        pwaApplicationDetail.getPwaApplication(),
        approverWua.getLinkedPerson(),
        deadlineDate
    );

    optionsCaseManagementEmailService.sendInitialOptionsApprovedEmail(
        pwaApplicationDetail,
        initialApprovalDeadlineHistory.getDeadlineDate()
    );

    // update workflow
    workflowAssignmentService.triggerWorkflowMessageAndAssertTaskExists(
        GenericMessageEvent.from(
            newTipDetail.getPwaApplication(),
            PwaApplicationWorkflowMessageEvents.OPTIONS_APPROVED.getMessageEventName()
        ),
        PwaApplicationWorkflowTask.UPDATE_APPLICATION
    );

  }

  private Optional<OptionsApprovalDeadlineView> getOptionsApprovalDeadlineView(PwaApplication pwaApplication) {
    return getCurrentOptionsApprovalDeadline(pwaApplication)
        .map(optionsApprovalDeadlineHistory -> new OptionsApprovalDeadlineView(
            optionsApprovalDeadlineHistory.getOptionsApplicationApproval().getCreatedByPersonId(),
            optionsApprovalDeadlineHistory.getOptionsApplicationApproval().getCreatedTimestamp(),
            optionsApprovalDeadlineHistory.getCreatedByPersonId(),
            optionsApprovalDeadlineHistory.getCreatedTimestamp(),
            optionsApprovalDeadlineHistory.getDeadlineDate(),
            optionsApprovalDeadlineHistory.getNote()
        ));

  }

  public OptionsApprovalDeadlineView getOptionsApprovalDeadlineViewOrError(PwaApplication pwaApplication) {
    return getOptionsApprovalDeadlineView(pwaApplication)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Cannot create Options approval deadline view for app.id:" + pwaApplication.getId())
        );
  }


  @Transactional
  public void changeOptionsApprovalDeadline(PwaApplicationDetail pwaApplicationDetail,
                                            Person person,
                                            Instant deadlineDate,
                                            String note) {

    var approval = getOptionsApprovalOrError(pwaApplicationDetail.getPwaApplication());


    optionsApprovalPersister.endTipDeadlineHistoryItem(approval);
    var newTipHistoryitem = optionsApprovalPersister.createTipDeadlineHistoryItem(approval, person, deadlineDate, note);

    optionsCaseManagementEmailService.sendOptionsDeadlineChangedEmail(pwaApplicationDetail, newTipHistoryitem.getDeadlineDate());
  }

}
