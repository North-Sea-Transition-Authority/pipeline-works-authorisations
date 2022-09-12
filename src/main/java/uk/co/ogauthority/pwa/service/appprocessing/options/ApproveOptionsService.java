package uk.co.ogauthority.pwa.service.appprocessing.options;

import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApplicationApproval;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApprovalDeadlineHistory;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.options.OptionsApprovalDeadlineView;
import uk.co.ogauthority.pwa.model.view.banner.BannerLink;
import uk.co.ogauthority.pwa.model.view.banner.PageBannerView;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApplicationApprovalRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApprovalDeadlineHistoryRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDetailVersioningService;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
public class ApproveOptionsService {

  private final OptionsApprovalPersister optionsApprovalPersister;

  private final OptionsApplicationApprovalRepository optionsApplicationApprovalRepository;

  private final OptionsApprovalDeadlineHistoryRepository optionsApprovalDeadlineHistoryRepository;

  private final OptionsCaseManagementEmailService optionsCaseManagementEmailService;

  private final PadOptionConfirmedService padOptionConfirmedService;

  private final OptionsCaseManagementWorkflowService optionsCaseManagementWorkflowService;

  private final PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService;

  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public ApproveOptionsService(OptionsApprovalPersister optionsApprovalPersister,
                               OptionsApplicationApprovalRepository optionsApplicationApprovalRepository,
                               OptionsApprovalDeadlineHistoryRepository optionsApprovalDeadlineHistoryRepository,
                               OptionsCaseManagementEmailService optionsCaseManagementEmailService,
                               PadOptionConfirmedService padOptionConfirmedService,
                               OptionsCaseManagementWorkflowService optionsCaseManagementWorkflowService,
                               PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService,
                               PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.optionsApprovalPersister = optionsApprovalPersister;
    this.optionsApplicationApprovalRepository = optionsApplicationApprovalRepository;
    this.optionsApprovalDeadlineHistoryRepository = optionsApprovalDeadlineHistoryRepository;
    this.optionsCaseManagementEmailService = optionsCaseManagementEmailService;
    this.padOptionConfirmedService = padOptionConfirmedService;
    this.optionsCaseManagementWorkflowService = optionsCaseManagementWorkflowService;
    this.pwaApplicationDetailVersioningService = pwaApplicationDetailVersioningService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  /**
   * Return true if an options approval has occurred.
   */
  public boolean optionsApproved(PwaApplication pwaApplication) {
    return getOptionsApproval(pwaApplication).isPresent();
  }

  public Optional<OptionsApplicationApproval> getOptionsApproval(PwaApplication pwaApplication) {
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

    var confirmedOption = padOptionConfirmedService.getConfirmedOptionType(pwaApplicationDetail);

    if (confirmedOption.isEmpty()) {
      return OptionsApprovalStatus.APPROVED_UNRESPONDED;
    }

    return confirmedOption.filter(ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS::equals)
        .map(o -> OptionsApprovalStatus.APPROVED_CONSENTED_OPTION_CONFIRMED)
        .orElse(OptionsApprovalStatus.APPROVED_OTHER_CONFIRMED);
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
  public void closeOutOptions(PwaApplicationDetail pwaApplicationDetail, AuthenticatedUserAccount closingUser) {

    optionsCaseManagementWorkflowService.doCloseOutWork(pwaApplicationDetail, closingUser);

    optionsCaseManagementEmailService.sendOptionsCloseOutEmailsIfRequired(pwaApplicationDetail, closingUser.getLinkedPerson());

  }

  @Transactional
  public void approveOptions(PwaApplicationDetail pwaApplicationDetail, WebUserAccount approverWua,
                             Instant deadlineDate) {

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

    optionsCaseManagementWorkflowService.doOptionsApprovalWork(newTipDetail);

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

    optionsCaseManagementEmailService.sendOptionsDeadlineChangedEmail(pwaApplicationDetail,
        newTipHistoryitem.getDeadlineDate());
  }


  public Optional<PageBannerView> getOptionsApprovalPageBannerView(
      PwaApplicationDetail pwaApplicationDetail) {

    var optionsApprovalStatus = getOptionsApprovalStatus(pwaApplicationDetail);

    if (!OptionsApprovalStatus.APPROVED_UNRESPONDED.equals(optionsApprovalStatus)) {
      return Optional.empty();
    }

    var deadlineHist = optionsApprovalDeadlineHistoryRepository.findByOptionsApplicationApproval_PwaApplicationAndTipFlagIsTrue(
        pwaApplicationDetail.getPwaApplication()
    );

    return deadlineHist.map(optionsApprovalDeadlineHistory ->
        new PageBannerView.PageBannerViewBuilder()
            .setHeader("Options have been approved")
            .setHeaderCaption("Approved " + DateUtils.formatDateTime(
                optionsApprovalDeadlineHistory.getOptionsApplicationApproval().getCreatedTimestamp())
            )
            .setBodyHeader("Confirmation of works completed must be submitted by " + DateUtils.formatDate(
                optionsApprovalDeadlineHistory.getDeadlineDate())
            )
            .setBannerLink(new BannerLink(
                pwaApplicationRedirectService.getTaskListRoute(pwaApplicationDetail.getPwaApplication()),
                "Confirm work completed"
            ))
            .build()

    );

  }

}
