package uk.co.ogauthority.pwa.features.feemanagement.service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeeItemRepository;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriod;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetail;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetailFeeItem;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetailItemRepository;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetailRepository;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.form.feeperiod.FeePeriodForm;
import uk.co.ogauthority.pwa.util.CurrencyUtils;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
public class FeePeriodService {

  private final FeePeriodRepository feePeriodRepository;

  private final FeePeriodDetailRepository feePeriodDetailRepository;

  private final FeePeriodDetailItemRepository feePeriodDetailItemRepository;

  private final FeeItemRepository feeItemRepository;

  private final Clock clock;

  private static final Logger LOGGER = LoggerFactory.getLogger(FeePeriodService.class);


  @Autowired
  public FeePeriodService(FeePeriodRepository feePeriodRepository,
                          FeePeriodDetailRepository feePeriodDetailRepository,
                          FeePeriodDetailItemRepository feePeriodDetailItemRepository,
                          FeeItemRepository feeItemRepository, @Qualifier("utcClock") Clock clock) {
    this.feePeriodRepository = feePeriodRepository;
    this.feePeriodDetailRepository = feePeriodDetailRepository;
    this.feePeriodDetailItemRepository = feePeriodDetailItemRepository;
    this.feeItemRepository = feeItemRepository;
    this.clock = clock;
  }

  public void saveFeePeriod(FeePeriodForm form, Person person) {

    var feePeriod = feePeriodRepository.save(getFeePeriodObject(form));
    LOGGER.debug("Persisted Fee Period Object");

    var newPeriodDetail = getFeePeriodDetailObject(form);
    newPeriodDetail.setFeePeriod(feePeriod);
    updateFeePeriodDetail(newPeriodDetail, person.getId().asInt());

    var feePeriodItemDetails = getFeePeriodDetailFeeItemsFromForm(form);
    for (var feePeriodItemDetail : feePeriodItemDetails) {
      feePeriodItemDetail.setFeePeriodDetail(newPeriodDetail);
    }
    feePeriodDetailItemRepository.saveAll(feePeriodItemDetails);
  }

  public boolean pendingPeriodExists() {
    var nextFeePeriod = feePeriodDetailRepository.findFirstByTipFlagIsTrueOrderByPeriodStartTimestampDesc();
    return nextFeePeriod.isPresent() && nextFeePeriod.get().getPeriodStartTimestamp().isAfter(Instant.now());
  }

  private List<FeePeriodDetailFeeItem> getFeePeriodDetailFeeItemsFromForm(FeePeriodForm form) {
    var feeItemList = new ArrayList<FeePeriodDetailFeeItem>();

    for (var cost : form.getApplicationCostMap().entrySet()) {
      var costDetails = cost.getKey().split(":");
      var applicationType = PwaApplicationType.valueOf(costDetails[0]);
      var applicationFeeType = PwaApplicationFeeType.valueOf(costDetails[1]);
      var feeItemOptional = feeItemRepository.findByPwaApplicationTypeAndPwaApplicationFeeType(applicationType,
          applicationFeeType);

      if (feeItemOptional.isPresent()) {
        var feePeriodDetailFeeItem = new FeePeriodDetailFeeItem();
        feePeriodDetailFeeItem.setPennyAmount(CurrencyUtils.currencyToPennyAmount(cost.getValue()));
        feePeriodDetailFeeItem.setFeeItem(feeItemOptional.get());

        feeItemList.add(feePeriodDetailFeeItem);
      }
    }
    return feeItemList;
  }

  private FeePeriod getFeePeriodObject(FeePeriodForm form) {
    var feePeriod = new FeePeriod();
    if (form.getId() != null) {
      feePeriod.setId(Integer.valueOf(form.getId()));
    }
    feePeriod.setDescription(form.getPeriodDescription());
    return feePeriod;
  }

  private FeePeriodDetail getFeePeriodDetailObject(FeePeriodForm form) {
    var feePeriodDetail = new FeePeriodDetail();
    feePeriodDetail.setPeriodStartTimestamp(DateUtils.datePickerStringToInstant(form.getPeriodStartDate()));
    feePeriodDetail.setTipFlag(true);
    return feePeriodDetail;
  }

  private void updateFeePeriodDetail(FeePeriodDetail newPendingDetail, Integer personId) {
    var currentInstant = clock.instant();

    // get the current detail for this new detail's fee period and end it
    var oldPendingDetailOptional = feePeriodDetailRepository
        .findFirstByTipFlagIsTrueAndPeriodStartTimestampAfterOrderByPeriodStartTimestampDesc(currentInstant);
    oldPendingDetailOptional.ifPresent(
        feePeriodDetail -> updateOldPendingDetail(feePeriodDetail, newPendingDetail, personId));

    // update the detail for the latest un-ended detail
    var oldActiveDetailOptional = feePeriodDetailRepository
        .findFirstByTipFlagIsTrueAndPeriodStartTimestampLessThanEqualOrderByPeriodStartTimestampDesc(currentInstant);
    oldActiveDetailOptional.ifPresent(
        feePeriodDetail -> updateActivePeriodDetails(feePeriodDetail, newPendingDetail, personId));

    newPendingDetail.setLastModifiedBy(personId);
    feePeriodDetailRepository.save(newPendingDetail);
    LOGGER.debug("Added new pending fee period detail object");
  }

  private void updateOldPendingDetail(FeePeriodDetail oldPendingDetail, FeePeriodDetail newPendingDetail,
                                      Integer username) {

    // end the previous 'current' detail
    oldPendingDetail.setTipFlag(false);
    oldPendingDetail.setLastModifiedBy(username);
    feePeriodDetailRepository.save(oldPendingDetail);
    LOGGER.debug("Removed tip flag from Old Pending Fee Period Detail Object");

    // get the day before the previous 'current' detail's period start
    var oldActiveEndDate = oldPendingDetail.getPeriodStartTimestamp().minus(1, ChronoUnit.SECONDS);

    // try and find a fee period which currently ends on that date (should be the currently active fee period)
    var oldActiveDetailOptional = feePeriodDetailRepository.findByTipFlagIsTrueAndPeriodEndTimestamp(oldActiveEndDate);

    if (oldActiveDetailOptional.isPresent()) {

      var oldActiveDetail = oldActiveDetailOptional.get();

      // end the detail
      oldActiveDetail.setTipFlag(false);
      oldActiveDetail.setLastModifiedBy(username);
      feePeriodDetailRepository.save(oldActiveDetail);
      LOGGER.debug("Removed tip flag from Old Active Fee Period Detail Object");

      var oldActiveItems = feePeriodDetailItemRepository.findAllByFeePeriodDetail(oldActiveDetail);

      // get the day before the new period detail is due to start
      var newActiveEndDate = newPendingDetail.getPeriodStartTimestamp().minus(1, ChronoUnit.SECONDS);

      // update the end date of the old active period, set this to be the new tip detail for that period and save
      oldActiveDetail.setPeriodEndTimestamp(newActiveEndDate);
      oldActiveDetail.setTipFlag(true);
      oldActiveDetail.setId(null);
      oldActiveDetail.setLastModifiedBy(username);
      oldActiveDetail = feePeriodDetailRepository.save(oldActiveDetail);
      LOGGER.debug("Added new active fee period detail object");

      // copy forward each fee item to the new tip detail and save
      for (var oldActiveItem : oldActiveItems) {
        oldActiveItem.setFeePeriodDetail(oldActiveDetail);
        oldActiveItem.setId(null);
      }
      feePeriodDetailItemRepository.saveAll(oldActiveItems);
      LOGGER.debug("Updated fee period items to be associated with updated detail object");
    }
  }

  private void updateActivePeriodDetails(FeePeriodDetail oldActiveDetail, FeePeriodDetail newPendingDetail,
                                         Integer username) {

    var newActiveEndDate = newPendingDetail.getPeriodStartTimestamp().minus(1, ChronoUnit.SECONDS);

    oldActiveDetail.setTipFlag(false);
    oldActiveDetail.setLastModifiedBy(username);
    feePeriodDetailRepository.save(oldActiveDetail);
    LOGGER.debug("Removed tip flag from old active fee period detail object");
    var oldActiveItems = feePeriodDetailItemRepository.findAllByFeePeriodDetail(oldActiveDetail);

    oldActiveDetail.setPeriodEndTimestamp(newActiveEndDate);
    oldActiveDetail.setTipFlag(true);
    oldActiveDetail.setId(null);
    oldActiveDetail.setLastModifiedBy(username);
    oldActiveDetail = feePeriodDetailRepository.save(oldActiveDetail);
    LOGGER.debug("Updated active fee period end date");

    for (var oldActiveItem : oldActiveItems) {
      oldActiveItem.setFeePeriodDetail(oldActiveDetail);
      oldActiveItem.setId(null);
    }
    feePeriodDetailItemRepository.saveAll(oldActiveItems);
    LOGGER.debug("Updated fee period items to be associated with updated detail object");
  }
}
