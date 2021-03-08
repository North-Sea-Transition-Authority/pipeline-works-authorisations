package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.FeePeriodDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.FeePeriodDetailFeeItem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.FeePeriodDetailItemRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.FeePeriodDetailRepository;

/**
 * Generates application Fee views based on PwaApplication data.
 * Whats the difference between charges and fees? "They have been charged the application fee" e.g
 * -> Fee is what an item will cost
 * -> Charge is when that fee has been actually demanded from a person/org.
 * This service generates the Fee that can, but has not necessarily been, charged to an org for submitting an application.
 */
@Service
public class ApplicationFeeService {

  private static final String FAST_TRACK_SUMMARY_FORMAT = "Fast-track %s application %s submission";
  private static final String STANDARD_SUMMARY_FORMAT = "%s application %s submission";
  private static final String FAST_TRACK_FEE_DESCRIPTION = "Fast-track application surcharge (plus 100% standard application cost)";

  private final FeePeriodDetailRepository feePeriodDetailRepository;
  private final FeePeriodDetailItemRepository feePeriodDetailItemRepository;

  private final Clock clock;

  private final Instant maxFeePeriodEndOverrideIfNull;

  @Autowired
  public ApplicationFeeService(FeePeriodDetailRepository feePeriodDetailRepository,
                               FeePeriodDetailItemRepository feePeriodDetailItemRepository,
                               @Qualifier("utcClock") Clock clock) {
    this.feePeriodDetailRepository = feePeriodDetailRepository;
    this.feePeriodDetailItemRepository = feePeriodDetailItemRepository;
    this.clock = clock;

    maxFeePeriodEndOverrideIfNull = LocalDateTime.of(4000, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC);
  }


  public ApplicationFeeReport getApplicationFeeReport(PwaApplicationDetail pwaApplicationDetail) {

    var currentFeeDetail = getCurrentFeePeriodDetailOrError();

    var feeDetailChargeItems = feePeriodDetailItemRepository.findAllByFeePeriodDetailAndFeeItem_PwaApplicationType(
        currentFeeDetail,
        pwaApplicationDetail.getPwaApplicationType()
    );

    var appFeeItems = generateFullFeeItemList(pwaApplicationDetail, feeDetailChargeItems);
    var feeSummary = BooleanUtils.isTrue(pwaApplicationDetail.getSubmittedAsFastTrackFlag())
        ? String.format(
        FAST_TRACK_SUMMARY_FORMAT,
        pwaApplicationDetail.getPwaApplicationType().getDisplayName(),
        pwaApplicationDetail.getPwaApplicationRef())
        : String.format(
        STANDARD_SUMMARY_FORMAT,
        pwaApplicationDetail.getPwaApplicationType().getDisplayName(),
        pwaApplicationDetail.getPwaApplicationRef());

    var totalFeePennies = appFeeItems.stream()
        .mapToInt(ApplicationFeeItem::getPennyAmount)
        .sum();

    return new ApplicationFeeReport(
        pwaApplicationDetail.getPwaApplication(),
        totalFeePennies,
        feeSummary,
        appFeeItems
    );

  }

  private List<ApplicationFeeItem> generateFullFeeItemList(PwaApplicationDetail pwaApplicationDetail,
                                                           List<FeePeriodDetailFeeItem> feePeriodDetailFeeItems) {

    var appFeeList = new ArrayList<ApplicationFeeItem>();
    feePeriodDetailFeeItems.forEach(feePeriodDetailFeeItem -> appFeeList.add(
        new ApplicationFeeItem(
            feePeriodDetailFeeItem.getFeeItem().getDisplayDescription(),
            feePeriodDetailFeeItem.getPennyAmount()
        )
    ));

    if (BooleanUtils.isTrue(pwaApplicationDetail.getSubmittedAsFastTrackFlag())) {

      var feeItemTotal = (Integer) appFeeList
          .stream()
          .mapToInt(ApplicationFeeItem::getPennyAmount)
          .sum();

      var fastTrackAppFee = new ApplicationFeeItem(
          FAST_TRACK_FEE_DESCRIPTION, feeItemTotal
      );

      appFeeList.add(fastTrackAppFee);

    }

    return appFeeList;

  }


  private FeePeriodDetail getCurrentFeePeriodDetailOrError() {

    return feePeriodDetailRepository.findByTipFlagIsTrueAndPeriodStartTimestampIsBeforeAndPeriodEndTimestampIsAfter(
        clock.instant(), maxFeePeriodEndOverrideIfNull)
        .orElseThrow(() -> new FeeException("Could not find tip fee detail covering requested instant"));

  }
}
