package uk.co.ogauthority.pwa.service.pwaapplications;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyPhase;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingTypesForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.partnerletters.PartnerLettersForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.service.appprocessing.initialreview.InitialReviewPaymentDecision;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;

@Service
public class PwaApplicationDetailService {

  private final PwaApplicationDetailRepository pwaApplicationDetailRepository;
  private final Clock clock;
  private final PadFastTrackService padFastTrackService;
  private final UserTypeService userTypeService;

  @Autowired
  public PwaApplicationDetailService(PwaApplicationDetailRepository pwaApplicationDetailRepository,
                                     @Qualifier("utcClock") Clock clock,
                                     PadFastTrackService padFastTrackService,
                                     UserTypeService userTypeService) {
    this.pwaApplicationDetailRepository = pwaApplicationDetailRepository;
    this.clock = clock;
    this.padFastTrackService = padFastTrackService;
    this.userTypeService = userTypeService;
  }

  public PwaApplicationDetail getTipDetail(PwaApplication pwaApplication) {
    return getTipDetail(pwaApplication.getId());
  }

  public PwaApplicationDetail getTipDetail(Integer pwaApplicationId) {
    return pwaApplicationDetailRepository.findByPwaApplicationIdAndTipFlagIsTrue(pwaApplicationId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find tip PwaApplicationDetail for PwaApplication ID: %s",
                pwaApplicationId
            ))
        );
  }

  public PwaApplicationDetail getDetailByVersionNo(PwaApplication pwaApplication, int versionNo) {
    return pwaApplicationDetailRepository.findByPwaApplicationIdAndVersionNo(pwaApplication.getId(), versionNo)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Could not find version %s for appId: %s", versionNo, pwaApplication.getId())
        ));
  }

  /**
   * Set attributes related to application being linked to fields.
   *
   * @param pwaApplicationDetail The current application detail.
   * @param linked               True/False. If linked, requires fields to be added.
   * @return Saved app detail.
   */
  @Transactional
  public PwaApplicationDetail setLinkedToFields(PwaApplicationDetail pwaApplicationDetail, Boolean linked) {
    pwaApplicationDetail.setLinkedToField(linked);
    if (linked) {
      pwaApplicationDetail.setNotLinkedDescription(null);
    }
    return pwaApplicationDetailRepository.save(pwaApplicationDetail);
  }

  /**
   * Set the description for what the PWA is in relation to, in the application details.
   *
   * @param pwaApplicationDetail     The current application detail.
   * @param noLinkedFieldDescription Description for what PWA is in relation to.
   */
  public void setNotLinkedFieldDescription(PwaApplicationDetail pwaApplicationDetail, String noLinkedFieldDescription) {
    pwaApplicationDetail.setNotLinkedDescription(noLinkedFieldDescription);
    pwaApplicationDetailRepository.save(pwaApplicationDetail);
  }

  /**
   * Update the status of the application.
   *
   * @return Saved app detail.
   */
  @Transactional
  public PwaApplicationDetail updateStatus(PwaApplicationDetail pwaApplicationDetail,
                                           PwaApplicationStatus status,
                                           WebUserAccount webUserAccount) {
    pwaApplicationDetail.setStatus(status);
    pwaApplicationDetail.setStatusLastModifiedTimestamp(Instant.now(clock));
    pwaApplicationDetail.setStatusLastModifiedByWuaId(webUserAccount.getWuaId());
    return pwaApplicationDetailRepository.save(pwaApplicationDetail);
  }


  /**
   * Update all app detail fields required when application detail is submitted.
   *
   * @return Saved app detail.
   */
  @Transactional
  public PwaApplicationDetail setSubmitted(PwaApplicationDetail pwaApplicationDetail,
                                           WebUserAccount webUserAccount,
                                           PwaApplicationStatus pwaApplicationStatus) {

    pwaApplicationDetail.setSubmittedByPersonId(webUserAccount.getLinkedPerson().getId());
    pwaApplicationDetail.setSubmittedTimestamp(clock.instant());

    boolean fastTrackFlag = padFastTrackService.isFastTrackRequired(pwaApplicationDetail);
    pwaApplicationDetail.setSubmittedAsFastTrackFlag(fastTrackFlag);

    return updateStatus(
        pwaApplicationDetail,
        pwaApplicationStatus,
        webUserAccount
    );

  }

  /**
   * Create and return new tip detail.
   * New tip detail duplicates all suitable application data into the new detail from the old one.
   *
   * @return Saved app detail.
   */
  @Transactional
  public PwaApplicationDetail createNewTipDetail(PwaApplicationDetail currentTipDetail,
                                                 PwaApplicationStatus newStatus,
                                                 WebUserAccount webUserAccount) {
    if (!currentTipDetail.isTipFlag()) {
      throw new ActionNotAllowedException("Cannot create new tip detail from non tip detail. pad_id:" + currentTipDetail.getId());
    }
    currentTipDetail.setTipFlag(false);
    currentTipDetail = pwaApplicationDetailRepository.save(currentTipDetail);
    var newTipDetail = new PwaApplicationDetail(
        currentTipDetail.getPwaApplication(),
        currentTipDetail.getVersionNo() + 1,
        webUserAccount.getWuaId(),
        clock.instant());
    newTipDetail.setStatus(newStatus);
    copyApplicationDetailData(currentTipDetail, newTipDetail);
    return pwaApplicationDetailRepository.save(newTipDetail);
  }

  private void copyApplicationDetailData(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {

    toDetail.setLinkedToField(fromDetail.getLinkedToField());
    toDetail.setNotLinkedDescription(fromDetail.getNotLinkedDescription());
    toDetail.setPipelinesCrossed(fromDetail.getPipelinesCrossed());
    toDetail.setCablesCrossed(fromDetail.getCablesCrossed());
    toDetail.setMedianLineCrossed(fromDetail.getMedianLineCrossed());
    toDetail.setNumOfHolders(fromDetail.getNumOfHolders());
    toDetail.setPipelinePhaseProperties(fromDetail.getPipelinePhaseProperties());
    toDetail.setOtherPhaseDescription(fromDetail.getOtherPhaseDescription());
    toDetail.setPartnerLettersRequired(fromDetail.getPartnerLettersRequired());
    toDetail.setPartnerLettersConfirmed(fromDetail.getPartnerLettersConfirmed());
    toDetail.setSupplementaryDocumentsFlag(fromDetail.getSupplementaryDocumentsFlag());

  }

  /**
   * Create and persist the first detail associated with an application.
   *
   * @return Saved app detail.
   */
  @Transactional
  public PwaApplicationDetail createFirstDetail(PwaApplication application, WebUserAccount webUserAccount,
                                                Long activeHoldersCount) {
    var pwaApplicationDetail = new PwaApplicationDetail(application, 1, webUserAccount.getWuaId(), clock.instant());
    pwaApplicationDetail.setNumOfHolders(Math.toIntExact(activeHoldersCount));
    return pwaApplicationDetailRepository.save(pwaApplicationDetail);
  }

  /**
   * Updates the crossing related values on the detail.
   *
   * @param detail The current application detail.
   * @param form   A CrossingTypesForm.
   */
  @Transactional
  public void updateCrossingTypes(PwaApplicationDetail detail, CrossingTypesForm form) {
    detail.setCablesCrossed(form.getCablesCrossed());
    detail.setPipelinesCrossed(form.getPipelinesCrossed());
    detail.setMedianLineCrossed(form.getMedianLineCrossed());
    pwaApplicationDetailRepository.save(detail);
  }

  @Transactional
  public void setInitialReviewApproved(PwaApplicationDetail detail,
                                       WebUserAccount acceptingUser,
                                       InitialReviewPaymentDecision initialReviewPaymentDecision) {
    updateStatus(detail, initialReviewPaymentDecision.getPostReviewPwaApplicationStatus(), acceptingUser);
    pwaApplicationDetailRepository.save(detail);
  }

  @Transactional
  public void setPhasesPresent(PwaApplicationDetail pwaApplicationDetail, Set<PropertyPhase> phasesPresent,
                               String otherPhaseDescription) {
    pwaApplicationDetail.setPipelinePhaseProperties(phasesPresent);
    pwaApplicationDetail.setOtherPhaseDescription(otherPhaseDescription);
    pwaApplicationDetailRepository.save(pwaApplicationDetail);
  }

  @Transactional
  public void updatePartnerLetters(PwaApplicationDetail applicationDetail, PartnerLettersForm form) {
    applicationDetail.setPartnerLettersRequired(form.getPartnerLettersRequired());
    applicationDetail.setPartnerLettersConfirmed(BooleanUtils.isTrue(form.getPartnerLettersRequired())
        ? form.getPartnerLettersConfirmed() : null);
    pwaApplicationDetailRepository.save(applicationDetail);
  }

  @Transactional
  public void setWithdrawn(PwaApplicationDetail pwaApplicationDetail, Person withdrawingUser, String withdrawalReason) {
    pwaApplicationDetail.setStatus(PwaApplicationStatus.WITHDRAWN);
    pwaApplicationDetail.setWithdrawalReason(withdrawalReason);
    pwaApplicationDetail.setWithdrawalTimestamp(Instant.now(clock));
    pwaApplicationDetail.setWithdrawingPersonId(withdrawingUser.getId());
    pwaApplicationDetailRepository.save(pwaApplicationDetail);
  }

  @Transactional
  public void setDeleted(PwaApplicationDetail pwaApplicationDetail, Person deletingUser) {
    pwaApplicationDetail.setStatus(PwaApplicationStatus.DELETED);
    pwaApplicationDetail.setDeletedTimestamp(Instant.now(clock));
    pwaApplicationDetail.setDeletingPersonId(deletingUser.getId());
    pwaApplicationDetailRepository.save(pwaApplicationDetail);
  }

  public boolean applicationDetailCanBeDeleted(PwaApplicationDetail appDetail) {
    return appDetail.isTipFlag()
        && appDetail.isFirstVersion()
        && PwaApplicationStatus.DRAFT.equals(appDetail.getStatus());
  }

  public void setSupplementaryDocumentsFlag(PwaApplicationDetail detail, Boolean filesToUpload) {
    detail.setSupplementaryDocumentsFlag(filesToUpload);
    pwaApplicationDetailRepository.save(detail);
  }

  public List<PwaApplicationDetail> getAllSubmittedApplicationDetailsForApplication(PwaApplication pwaApplication) {
    return pwaApplicationDetailRepository.findByPwaApplicationAndSubmittedTimestampIsNotNull(pwaApplication);
  }

  public Optional<PwaApplicationDetail> getLatestSubmittedDetail(PwaApplication pwaApplication) {

    var submittedDetails = getAllSubmittedApplicationDetailsForApplication(pwaApplication);

    if (submittedDetails.isEmpty()) {
      return Optional.empty();
    }

    return submittedDetails.stream()
        .max(Comparator.comparing(PwaApplicationDetail::getSubmittedTimestamp));

  }

  public List<PwaApplicationDetail> getAllWithdrawnApplicationDetailsForApplication(PwaApplication pwaApplication) {
    return pwaApplicationDetailRepository.findByPwaApplicationAndStatus(pwaApplication, PwaApplicationStatus.WITHDRAWN);
  }

  @Transactional
  public void setConfirmedSatisfactoryData(PwaApplicationDetail applicationDetail,
                                           String reason,
                                           Person confirmingPerson) {

    applicationDetail.setConfirmedSatisfactoryByPersonId(confirmingPerson.getId());
    applicationDetail.setConfirmedSatisfactoryTimestamp(Instant.now(clock));
    applicationDetail.setConfirmedSatisfactoryReason(reason);

    pwaApplicationDetailRepository.save(applicationDetail);

  }

  /**
   * This is not designed to be used for Authorisation checks on its own, simply as a starting point for authorisation checks.
   * See ApplicationInvolvementService.getApplicationInvolvement.
   */
  public Optional<PwaApplicationDetail> getLatestDetailForUser(int applicationId,
                                                               AuthenticatedUserAccount user) {

    var details = pwaApplicationDetailRepository.findByPwaApplicationId(applicationId);
    var userTypes = userTypeService.getUserTypes(user);
    var tipDetail = details.stream()
        .filter(PwaApplicationDetail::isTipFlag)
        .findFirst()
        .orElseThrow(() -> new RuntimeException(String.format("Requested AppId:%s has no tip detail", applicationId)));

    var lastSubmittedDetail = details.stream()
        .filter(d -> d.getSubmittedTimestamp() != null)
        .max(Comparator.comparing(PwaApplicationDetail::getSubmittedTimestamp));

    var latestSatisfactoryDetail = details.stream()
        .filter(d -> d.getConfirmedSatisfactoryTimestamp() != null)
        .max(Comparator.comparing(PwaApplicationDetail::getConfirmedSatisfactoryTimestamp));

    if (userTypes.contains(UserType.INDUSTRY) && tipDetail.isFirstVersion()) {
      return Optional.of(tipDetail);
    }

    if (userTypes.contains(UserType.OGA) || userTypes.contains(UserType.INDUSTRY)) {
      return lastSubmittedDetail;
    }

    if (userTypes.contains(UserType.CONSULTEE)) {
      return latestSatisfactoryDetail;
    }

    throw new IllegalStateException(String.format(
        "Unrecognised user types [%s] encountered when retrieving app detail for user with WUA id [%s]",
        userTypes,
        user.getWuaId()));

  }

  public List<Integer> getInProgressApplicationIds() {
    var inProgressStatuses = ApplicationState.IN_PROGRESS.getStatuses();
    return pwaApplicationDetailRepository.findLastSubmittedAppDetailsWithStatusIn(inProgressStatuses)
        .stream()
        .map(detail -> detail.getPwaApplication().getId())
        .collect(Collectors.toList());
  }

  public List<PwaApplicationDetail> getAllDetailsForApplication(PwaApplication pwaApplication) {
    return pwaApplicationDetailRepository.findByPwaApplication(pwaApplication);
  }

  public PwaApplicationDetail getDetailById(Integer appDetailId) {
    return pwaApplicationDetailRepository.findById(appDetailId).orElseThrow(() ->
        new PwaEntityNotFoundException(String.format("Couldn't find PwaApplicationDetail for PwaApplicationDetail ID: %s", appDetailId))
    );
  }


}
