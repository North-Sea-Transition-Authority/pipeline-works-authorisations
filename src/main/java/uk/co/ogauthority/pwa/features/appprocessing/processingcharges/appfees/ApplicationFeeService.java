package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees;

import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.feeproviders.ApplicationFeeItem;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetail;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetailRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * Generates application Fee views based on PwaApplication data.
 * Whats the difference between charges and fees? "They have been charged the application fee" e.g
 * -> Fee is what an item will cost
 * -> Charge is when that fee has been actually demanded from a person/org.
 * This service generates the Fee that can, but has not necessarily been, charged to an org for submitting an application.
 */
@Service
public class ApplicationFeeService {

  private static final String STANDARD_SUMMARY_FORMAT = "%s application %s submission";
  private static final String FAST_TRACK_SUMMARY_FORMAT = "Fast-track %s application %s submission";

  private final FeePeriodDetailRepository feePeriodDetailRepository;
  private final List<ApplicationFeeItemProvider> applicationFeeItemProviders;

  private final Clock clock;

  private final Instant maxFeePeriodEndOverrideIfNull;

  @Autowired
  public ApplicationFeeService(FeePeriodDetailRepository feePeriodDetailRepository,
                               List<ApplicationFeeItemProvider> applicationFeeItemProviders,
                               @Qualifier("utcClock") Clock clock) {
    this.feePeriodDetailRepository = feePeriodDetailRepository;
    this.applicationFeeItemProviders = applicationFeeItemProviders;
    this.clock = clock;

    maxFeePeriodEndOverrideIfNull = LocalDateTime.of(4000, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC);
  }


  public ApplicationFeeReport getApplicationFeeReport(PwaApplicationDetail pwaApplicationDetail) {

    var currentFeeDetail = getCurrentFeePeriodDetailOrError();

    var appFeeItems = applicationFeeItemProviders.stream()
        .sorted(Comparator.comparing(ApplicationFeeItemProvider::getProvisionOrdering))
        .filter(applicationFeeItemProvider -> applicationFeeItemProvider.canProvideFeeItems(pwaApplicationDetail))
        .flatMap(applicationFeeItemProvider -> applicationFeeItemProvider.provideFees(currentFeeDetail, pwaApplicationDetail).stream())
        .collect(toList());

    var useFastTrackSummary =  applicationFeeItemProviders
        .stream()
        .filter(applicationFeeItemProvider -> applicationFeeItemProvider.getApplicationFeeType().equals(PwaApplicationFeeType.FAST_TRACK))
        .anyMatch(applicationFeeItemProvider -> applicationFeeItemProvider.canProvideFeeItems(pwaApplicationDetail));

    var feeSummary = useFastTrackSummary
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

  private FeePeriodDetail getCurrentFeePeriodDetailOrError() {

    return feePeriodDetailRepository.findByTipFlagIsTrueAndPeriodStartTimestampIsBeforeAndPeriodEndTimestampIsAfter(
        clock.instant(), maxFeePeriodEndOverrideIfNull)
        .orElseThrow(() -> new FeeException("Could not find tip fee detail covering requested instant"));

  }
}
