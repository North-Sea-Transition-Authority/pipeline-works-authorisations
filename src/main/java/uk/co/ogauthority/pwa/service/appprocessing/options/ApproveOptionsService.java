package uk.co.ogauthority.pwa.service.appprocessing.options;

import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApplicationApproval;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApprovalDeadlineHistory;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.options.OptionsApprovalDeadlineView;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApplicationApprovalRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApprovalDeadlineHistoryRepository;

@Service
public class ApproveOptionsService {

  private final OptionsApprovalPersister optionsApprovalPersister;

  private final OptionsApplicationApprovalRepository optionsApplicationApprovalRepository;

  private final OptionsApprovalDeadlineHistoryRepository optionsApprovalDeadlineHistoryRepository;

  private final OptionsCaseManagementEmailService optionsCaseManagementEmailService;

  @Autowired
  public ApproveOptionsService(OptionsApprovalPersister optionsApprovalPersister,
                               OptionsApplicationApprovalRepository optionsApplicationApprovalRepository,
                               OptionsApprovalDeadlineHistoryRepository optionsApprovalDeadlineHistoryRepository,
                               OptionsCaseManagementEmailService optionsCaseManagementEmailService) {
    this.optionsApprovalPersister = optionsApprovalPersister;
    this.optionsApplicationApprovalRepository = optionsApplicationApprovalRepository;
    this.optionsApprovalDeadlineHistoryRepository = optionsApprovalDeadlineHistoryRepository;
    this.optionsCaseManagementEmailService = optionsCaseManagementEmailService;
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
  public void approveOptions(PwaApplicationDetail pwaApplicationDetail, Person approverPerson, Instant deadlineDate) {

    var initialApprovalDeadlineHistory = optionsApprovalPersister.createInitialOptionsApproval(
        pwaApplicationDetail.getPwaApplication(),
        approverPerson,
        deadlineDate
    );

    optionsCaseManagementEmailService.sendInitialOptionsApprovedEmail(pwaApplicationDetail);

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

    // TODO PWA-132 email CO and Preparers.
  }

}
