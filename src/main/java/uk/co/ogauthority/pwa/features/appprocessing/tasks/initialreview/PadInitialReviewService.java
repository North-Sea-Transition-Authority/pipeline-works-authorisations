package uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.EntityLatestVersionNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;


@Service
public class PadInitialReviewService {

  private final PadInitialReviewRepository padInitialReviewRepository;
  private final Clock clock;

  @Autowired
  public PadInitialReviewService(
      PadInitialReviewRepository padInitialReviewRepository,
      @Qualifier("utcClock") Clock clock) {
    this.padInitialReviewRepository = padInitialReviewRepository;
    this.clock = clock;
  }



  public void addApprovedInitialReview(PwaApplicationDetail pwaApplicationDetail, WebUserAccount acceptingUser) {
    padInitialReviewRepository.save(
        new PadInitialReview(pwaApplicationDetail, acceptingUser.getWuaId(), clock.instant()));
  }

  private PadInitialReview getLatestInitialReviewForDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padInitialReviewRepository.findFirstByPwaApplicationDetailOrderByInitialReviewApprovedTimestampDesc(
        pwaApplicationDetail).orElseThrow(() -> new EntityLatestVersionNotFoundException(
            "Could not find latest initial review for application detail with id: " + pwaApplicationDetail.getId()));
  }

  public void revokeLatestInitialReview(PwaApplicationDetail pwaApplicationDetail, WebUserAccount revokingUser) {

    var latestInitialReview = getLatestInitialReviewForDetail(pwaApplicationDetail);
    latestInitialReview.setApprovalRevokedByWuaId(revokingUser.getWuaId());
    latestInitialReview.setApprovalRevokedTimestamp(clock.instant());

    padInitialReviewRepository.save(latestInitialReview);
  }

  public boolean isInitialReviewComplete(PwaApplication pwaApplication) {
    var latestUnRevokedInitialReviewsForApplication  =
        padInitialReviewRepository.findByPwaApplicationDetail_pwaApplicationAndApprovalRevokedTimestampIsNull(pwaApplication);
    return !latestUnRevokedInitialReviewsForApplication.isEmpty();
  }



}
