package uk.co.ogauthority.pwa.service.pwaapplications;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.EntityLatestVersionNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PadInitialReview;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.PadInitialReviewRepository;


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



  void addApprovedInitialReview(PwaApplicationDetail pwaApplicationDetail, WebUserAccount acceptingUser) {
    padInitialReviewRepository.save(
        new PadInitialReview(pwaApplicationDetail, acceptingUser.getWuaId(), clock.instant()));
  }

  private PadInitialReview getLatestInitialReviewForDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padInitialReviewRepository.findFirstByPwaApplicationDetailOrderByInitialReviewApprovedTimestampDesc(
        pwaApplicationDetail).orElseThrow(() -> new EntityLatestVersionNotFoundException(
            "Could not find latest initial review for application detail with id: " + pwaApplicationDetail.getId()));
  }

  void revokeLatestInitialReview(PwaApplicationDetail pwaApplicationDetail, WebUserAccount revokingUser) {

    var latestInitialReview = getLatestInitialReviewForDetail(pwaApplicationDetail);
    latestInitialReview.setApprovalRevokedByWuaId(revokingUser.getWuaId());
    latestInitialReview.setApprovalRevokedTimestamp(clock.instant());

    padInitialReviewRepository.save(latestInitialReview);
  }

  boolean isInitialReviewComplete(List<PwaApplicationDetail> pwaApplicationDetails) {

    //get a map of app details and a list of initialReviews associated with them for the details provided
    var appDetailToInitialReviewsMap = padInitialReviewRepository.findAllByPwaApplicationDetailIn(pwaApplicationDetails)
        .stream()
        .collect(Collectors.groupingBy(PadInitialReview::getPwaApplicationDetail, Collectors.toList()));

    //get the latest initial review from each list associated with the details, resulting in a list of initialReviews (latest for each app detail)
    var initialReviews = appDetailToInitialReviewsMap.values().stream()
        .map(initialReviewsForDetail ->
            initialReviewsForDetail.stream().max(Comparator.comparing(PadInitialReview::getInitialReviewApprovedTimestamp)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());

    //get the latest initial review of all those extracted in the previous step
    var latestInitialReview = initialReviews.stream().max(
        Comparator.comparing(PadInitialReview::getInitialReviewApprovedTimestamp));

    //initial review is only complete if the latest initial review has not been revoked
    return latestInitialReview.isPresent() && !latestInitialReview.get().isInitialReviewRevoked();

  }



}
