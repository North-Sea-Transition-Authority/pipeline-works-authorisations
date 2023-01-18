package uk.co.ogauthority.pwa.features.feemanagement.display;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;
import uk.co.ogauthority.pwa.features.feemanagement.display.internal.DisplayableFeeItemDetail;
import uk.co.ogauthority.pwa.features.feemanagement.display.internal.DisplayableFeePeriodDetail;
import uk.co.ogauthority.pwa.features.feemanagement.display.internal.FeePeriodDetailViewRepository;
import uk.co.ogauthority.pwa.features.feemanagement.display.internal.FeePeriodItemViewRepository;
import uk.co.ogauthority.pwa.model.form.feeperiod.FeePeriodForm;
import uk.co.ogauthority.pwa.util.CurrencyUtils;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class FeePeriodDisplayService {

  private final FeePeriodDetailViewRepository feePeriodDetailViewRepository;

  private final FeePeriodItemViewRepository feePeriodItemViewRepository;

  @Autowired
  public FeePeriodDisplayService(FeePeriodDetailViewRepository feePeriodDetailViewRepository,
                                 FeePeriodItemViewRepository feePeriodItemViewRepository) {
    this.feePeriodDetailViewRepository = feePeriodDetailViewRepository;
    this.feePeriodItemViewRepository = feePeriodItemViewRepository;
  }


  public List<DisplayableFeePeriodDetail> listAllPeriods() {
    return IterableUtils.toList(feePeriodDetailViewRepository.findAll());
  }

  public Optional<DisplayableFeePeriodDetail> findPeriodById(Integer id) {
    return feePeriodDetailViewRepository.findById(id);
  }

  public Map<PwaApplicationFeeType, List<DisplayableFeeItemDetail>> getFeesByPeriodId(Integer id) {

    // group items by fee type
    var feesMap = feePeriodItemViewRepository.findAllByFeePeriodId(id).stream()
        .collect(Collectors.groupingBy(DisplayableFeeItemDetail::getApplicationFeeType));

    // return sorted map
    return feesMap.entrySet().stream()
        .sorted(Comparator.comparing(entry -> entry.getKey().getDisplayOrder()))
        .collect(StreamUtils.toLinkedHashMap(Map.Entry::getKey, Map.Entry::getValue));

  }

  public boolean futurePeriodExists() {
    return !(feePeriodDetailViewRepository.findAllByPeriodStartTimestampAfter(Instant.now()).isEmpty());
  }

  public FeePeriodForm populatePeriodFormForEdit(FeePeriodForm form, Integer periodId) {
    var periodOptional = feePeriodDetailViewRepository.findById(periodId);
    if (periodOptional.isPresent()) {
      var period = periodOptional.get();
      if (form.getId() == null) {
        form.setId(String.valueOf(periodId));
      }
      if (form.getPeriodDescription() == null) {
        form.setPeriodDescription(period.getDescription());
      }
      if (form.getPeriodStartDate() == null) {
        var localDate = DateUtils.instantToLocalDate(period.getPeriodStartTimestamp());
        form.setPeriodStartDate(DateUtils.formatToDatePickerString(localDate));
      }
      if (form.getApplicationCostMap() == null || form.getApplicationCostMap().isEmpty()) {
        var costs = feePeriodItemViewRepository.findAllByFeePeriodId(periodId);
        var applicationMap = new HashMap<String, String>();
        for (var cost : costs) {
          var key = cost.getApplicationType().name() + ":" + cost.getApplicationFeeType().name();
          applicationMap.put(key, CurrencyUtils.pennyAmountToCurrency(cost.getPennyAmount()));
        }
        form.setApplicationCostMap(applicationMap);
      }
    }
    return form;
  }
}
