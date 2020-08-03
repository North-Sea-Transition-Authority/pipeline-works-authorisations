package uk.co.ogauthority.pwa.service.pwaapplications;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.function.Function;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyPhase;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingTypesForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.partnerletters.PartnerLettersForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;

@Service
public class PwaApplicationDetailService {

  private final PwaApplicationDetailRepository pwaApplicationDetailRepository;
  private final Clock clock;
  private final PadFastTrackService padFastTrackService;

  @Autowired
  public PwaApplicationDetailService(PwaApplicationDetailRepository pwaApplicationDetailRepository,
                                     @Qualifier("utcClock") Clock clock,
                                     PadFastTrackService padFastTrackService) {
    this.pwaApplicationDetailRepository = pwaApplicationDetailRepository;
    this.clock = clock;
    this.padFastTrackService = padFastTrackService;
  }

  /**
   * Execute a caller-defined function if the tip detail for a specific PWA application is in Draft status and accessible to the user.
   *
   * @param pwaApplicationId for application being queried
   * @param user             attempting to access the application
   * @param function         to execute if application is in the right state and user has access to it
   * @return result of function
   */
  public ModelAndView withDraftTipDetail(Integer pwaApplicationId,
                                         AuthenticatedUserAccount user,
                                         Function<PwaApplicationDetail, ModelAndView> function) {
    PwaApplicationDetail detail = getTipDetailWithStatus(pwaApplicationId, PwaApplicationStatus.DRAFT);
    return function.apply(detail);
  }

  public PwaApplicationDetail getTipDetail(Integer pwaApplicationId) {
    return pwaApplicationDetailRepository.findByPwaApplicationIdAndTipFlagIsTrue(pwaApplicationId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find tip PwaApplicationDetail for PwaApplication ID: %s",
                pwaApplicationId
            ))
        );
  }

  /**
   * Get the tip detail for a PwaApplication if that detail's status matches the one passed-in.
   */
  public PwaApplicationDetail getTipDetailWithStatus(Integer pwaApplicationId, PwaApplicationStatus status) {
    return pwaApplicationDetailRepository.findByPwaApplicationIdAndStatusAndTipFlagIsTrue(pwaApplicationId, status)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find tip PwaApplicationDetail for PwaApplication ID: %s and status: %s",
                pwaApplicationId,
                status.name())));
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
  public PwaApplicationDetail updateStatus(PwaApplicationDetail pwaApplicationDetail, PwaApplicationStatus status,
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
  public PwaApplicationDetail setSubmitted(PwaApplicationDetail pwaApplicationDetail, WebUserAccount webUserAccount) {

    pwaApplicationDetail.setSubmittedByWuaId(webUserAccount.getWuaId());
    pwaApplicationDetail.setSubmittedTimestamp(clock.instant());

    boolean fastTrackFlag = padFastTrackService.isFastTrackRequired(pwaApplicationDetail);
    pwaApplicationDetail.setSubmittedAsFastTrackFlag(fastTrackFlag);

    return updateStatus(
        pwaApplicationDetail,
        PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW,
        webUserAccount
    );

  }

  /**
   * Create and persist the first detail associated with an application.
   *
   * @return Saved app detail.
   */
  @Transactional
  public PwaApplicationDetail createFirstDetail(PwaApplication application, WebUserAccount webUserAccount, Long activeHoldersCount) {
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
  public void setInitialReviewApproved(PwaApplicationDetail detail, WebUserAccount acceptingUser) {
    detail.setInitialReviewApprovedByWuaId(acceptingUser.getWuaId());
    detail.setInitialReviewApprovedTimestamp(Instant.now(clock));
    updateStatus(detail, PwaApplicationStatus.CASE_OFFICER_REVIEW, acceptingUser);
    pwaApplicationDetailRepository.save(detail);
  }

  @Transactional
  public void setPhasesPresent(PwaApplicationDetail pwaApplicationDetail, Set<PropertyPhase> phasesPresent, String otherPhaseDescription) {
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



}
