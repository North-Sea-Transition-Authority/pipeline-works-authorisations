package uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview;

import java.time.Clock;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.EntityLatestVersionNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;


@Service
public class PadInitialReviewService {

  private final PadInitialReviewRepository padInitialReviewRepository;
  private final Clock clock;
  private final UserAccountService userAccountService;

  @Autowired
  public PadInitialReviewService(PadInitialReviewRepository padInitialReviewRepository,
                                 @Qualifier("utcClock") Clock clock,
                                 UserAccountService userAccountService) {
    this.padInitialReviewRepository = padInitialReviewRepository;
    this.clock = clock;
    this.userAccountService = userAccountService;
  }

  public void addApprovedInitialReview(PwaApplicationDetail pwaApplicationDetail, WebUserAccount acceptingUser) {
    padInitialReviewRepository.save(
        new PadInitialReview(pwaApplicationDetail, acceptingUser.getWuaId(), clock.instant()));
  }

  private Optional<PadInitialReview> getLatestInitialReviewForDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padInitialReviewRepository
        .findFirstByPwaApplicationDetailOrderByInitialReviewApprovedTimestampDesc(pwaApplicationDetail);
  }

  private PadInitialReview getLatestInitialReviewForDetailOrThrow(PwaApplicationDetail pwaApplicationDetail) {
    return getLatestInitialReviewForDetail(pwaApplicationDetail).orElseThrow(
        () -> new EntityLatestVersionNotFoundException(
            "Could not find latest initial review for application detail with id: " + pwaApplicationDetail.getId()));
  }

  public void revokeLatestInitialReview(PwaApplicationDetail pwaApplicationDetail, WebUserAccount revokingUser) {

    var latestInitialReview = getLatestInitialReviewForDetailOrThrow(pwaApplicationDetail);
    latestInitialReview.setApprovalRevokedByWuaId(revokingUser.getWuaId());
    latestInitialReview.setApprovalRevokedTimestamp(clock.instant());

    padInitialReviewRepository.save(latestInitialReview);
  }

  public boolean isInitialReviewComplete(PwaApplication pwaApplication) {
    var latestUnRevokedInitialReviewsForApplication  =
        padInitialReviewRepository.findByPwaApplicationDetail_pwaApplicationAndApprovalRevokedTimestampIsNull(
            pwaApplication
        );
    return !latestUnRevokedInitialReviewsForApplication.isEmpty();
  }

  public Optional<Person> getLatestInitialReviewer(PwaApplicationDetail tipAppDetail) {

    return getLatestInitialReviewForDetail(tipAppDetail)
        .map(initialReview -> userAccountService.getWebUserAccount(initialReview.getInitialReviewApprovedByWuaId()))
        .map(WebUserAccount::getLinkedPerson);

  }

  public void carryForwardInitialReview(PwaApplicationDetail oldDetail, PwaApplicationDetail newDetail) {
    var padInitialReviewOptional = getLatestInitialReviewForDetail(oldDetail);
    padInitialReviewOptional.ifPresent(
        oldInitialReview -> copyInitialReviewEntity(oldInitialReview, newDetail)
    );
  }

  private void copyInitialReviewEntity(PadInitialReview oldInitialReview, PwaApplicationDetail newDetail) {
    var newPadInitialReview = new PadInitialReview(
        newDetail,
        oldInitialReview.getInitialReviewApprovedByWuaId(),
        oldInitialReview.getInitialReviewApprovedTimestamp()
    );

    padInitialReviewRepository.save(newPadInitialReview);
  }
}
