package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositOverview;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.Tag;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadDepositPipelineRepository;
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;


/**
 * Mapping of form data to entity and entity to form data for Permanent Deposits application form.
 */
@Service
public class PermanentDepositEntityMappingService {
  private static final DecimalFormat DECIMAL_FORMAT_2DP = new DecimalFormat("#.##");

  private final PadDepositPipelineRepository padDepositPipelineRepository;

  @Autowired
  public PermanentDepositEntityMappingService(PadDepositPipelineRepository padDepositPipelineRepository) {
    this.padDepositPipelineRepository = padDepositPipelineRepository;
  }

  /**
   * Map Permanent Deposits stored data to form.
   */
  void mapDepositInformationDataToForm(PadPermanentDeposit entity, PermanentDepositsForm form) {

    form.setEntityID(entity.getId());
    form.setDepositIsForConsentedPipeline(entity.getDepositForConsentedPipeline());
    form.setDepositReference(entity.getReference());
    form.setDepositIsForPipelinesOnOtherApp(entity.getDepositIsForPipelinesOnOtherApp());
    form.setAppRefAndPipelineNum(entity.getAppRefAndPipelineNum());
    form.setFromDate(new TwoFieldDateInput(entity.getFromYear(), entity.getFromMonth()));
    form.setToDate(new TwoFieldDateInput(entity.getToYear(), entity.getToMonth()));

    if (entity.getMaterialType() != null) {
      form.setMaterialType(entity.getMaterialType());

      if (form.getMaterialType().equals(MaterialType.CONCRETE_MATTRESSES)) {
        form.setConcreteMattressLength(entity.getConcreteMattressLength());
        form.setConcreteMattressWidth(entity.getConcreteMattressWidth());
        form.setConcreteMattressDepth(entity.getConcreteMattressDepth());
        form.setQuantityConcrete(String.valueOf(entity.getQuantity()));
        form.setContingencyConcreteAmount(entity.getContingencyAmount());

      } else if (form.getMaterialType().equals(MaterialType.ROCK)) {
        form.setRocksSize(entity.getMaterialSize());
        form.setQuantityRocks(String.valueOf(entity.getQuantity()));
        form.setContingencyRocksAmount(entity.getContingencyAmount());

      } else if (form.getMaterialType().equals(MaterialType.GROUT_BAGS)) {
        form.setGroutBagsSize(Integer.parseInt(entity.getMaterialSize()));
        form.setQuantityGroutBags(String.valueOf(entity.getQuantity()));
        form.setContingencyGroutBagsAmount(entity.getContingencyAmount());
        form.setGroutBagsBioDegradable(entity.getGroutBagsBioDegradable());
        form.setBioGroutBagsNotUsedDescription(entity.getBagsNotUsedDescription());

      } else if (form.getMaterialType().equals(MaterialType.OTHER)) {
        form.setOtherMaterialType(entity.getOtherMaterialType());
        form.setOtherMaterialSize(entity.getMaterialSize());
        form.setQuantityOther(String.valueOf(entity.getQuantity()));
        form.setContingencyOtherAmount(entity.getContingencyAmount());
      }

      form.setFromCoordinateForm(new CoordinateForm());
      form.setToCoordinateForm(new CoordinateForm());
      CoordinateUtils.mapCoordinatePairToForm(entity.getFromCoordinates(), form.getFromCoordinateForm());
      CoordinateUtils.mapCoordinatePairToForm(entity.getToCoordinates(), form.getToCoordinateForm());
    }


  }


  /**
   * Map Permanent Deposits form data to entity.
   */
  void setEntityValuesUsingForm(PadPermanentDeposit entity, PermanentDepositsForm form) {

    entity.setId(form.getEntityID());
    entity.setDepositForConsentedPipeline(form.getDepositIsForConsentedPipeline());
    entity.setReference(form.getDepositReference());
    entity.setDepositIsForPipelinesOnOtherApp(form.getDepositIsForPipelinesOnOtherApp());
    entity.setAppRefAndPipelineNum(form.getAppRefAndPipelineNum());
    entity.setFromMonth(Integer.parseInt(form.getFromDate().getMonth()));
    entity.setFromYear(Integer.parseInt(form.getFromDate().getYear()));
    entity.setToMonth(Integer.parseInt(form.getToDate().getMonth()));
    entity.setToYear(Integer.parseInt(form.getToDate().getYear()));

    entity.setMaterialType(form.getMaterialType());

    if (form.getMaterialType().equals(MaterialType.CONCRETE_MATTRESSES)) {
      entity.setConcreteMattressLength(form.getConcreteMattressLength());
      entity.setConcreteMattressWidth(form.getConcreteMattressWidth());
      entity.setConcreteMattressDepth(form.getConcreteMattressDepth());
      entity.setQuantity(Double.parseDouble(form.getQuantityConcrete()));
      entity.setContingencyAmount(form.getContingencyConcreteAmount());

    } else if (form.getMaterialType().equals(MaterialType.ROCK)) {
      entity.setMaterialSize(String.valueOf(form.getRocksSize()));
      entity.setQuantity(Double.parseDouble(form.getQuantityRocks()));
      entity.setContingencyAmount(form.getContingencyRocksAmount());

    } else if (form.getMaterialType().equals(MaterialType.GROUT_BAGS)) {
      entity.setMaterialSize(String.valueOf(form.getGroutBagsSize()));
      entity.setQuantity(Double.parseDouble(form.getQuantityGroutBags()));
      entity.setContingencyAmount(form.getContingencyGroutBagsAmount());
      entity.setGroutBagsBioDegradable(form.getGroutBagsBioDegradable());
      entity.setBagsNotUsedDescription(form.getBioGroutBagsNotUsedDescription());

    } else if (form.getMaterialType().equals(MaterialType.OTHER)) {
      entity.setOtherMaterialType(form.getOtherMaterialType());
      entity.setMaterialSize(String.valueOf(form.getOtherMaterialSize()));
      entity.setQuantity(Double.parseDouble(form.getQuantityOther()));
      entity.setContingencyAmount(form.getContingencyOtherAmount());
    }

    entity.setFromCoordinates(CoordinateUtils.coordinatePairFromForm(form.getFromCoordinateForm()));
    entity.setToCoordinates(CoordinateUtils.coordinatePairFromForm(form.getToCoordinateForm()));
  }


  /**
   * Map Permanent Deposits stored data to view object.
   */
  PermanentDepositOverview createPermanentDepositOverview(
      PadPermanentDeposit entity, Map<PipelineId, PipelineOverview> pipelineIdAndOverviewMap) {

    var sortedLinkedPipelineNames = pipelineIdAndOverviewMap.entrySet().stream()
        .map(entry -> entry.getValue().getPipelineName())
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());


    return new PermanentDepositOverview(
        entity.getId(),
        entity.getDepositForConsentedPipeline(),
        entity.getMaterialType(),
        entity.getReference(),
        sortedLinkedPipelineNames,
        entity.getDepositIsForPipelinesOnOtherApp(),
        entity.getAppRefAndPipelineNum(),
        DateUtils.createDateEstimateString(entity.getFromMonth(), entity.getFromYear()),
        DateUtils.createDateEstimateString(entity.getToMonth(), entity.getToYear()),
        entity.getMaterialType().equals(MaterialType.OTHER)
            ? new StringWithTag(entity.getOtherMaterialType(), Tag.NOT_FROM_PORTAL)
            : new StringWithTag(entity.getMaterialType().getDisplayText(), Tag.NONE),
        getSizeDisplayString(entity),
        entity.getGroutBagsBioDegradable(),
        entity.getBagsNotUsedDescription(),
        DECIMAL_FORMAT_2DP.format(entity.getQuantity()),
        entity.getContingencyAmount(),
        entity.getFromCoordinates(),
        entity.getToCoordinates()
    );


  }

  private String getSizeDisplayString(PadPermanentDeposit entity) {
    var unicodeMultiplication = " × ";
    var concreteMattressFormat = "%s " + UnitMeasurement.METRE.getSuffixScreenReaderDisplay() +
        unicodeMultiplication + "%s " + UnitMeasurement.METRE.getSuffixScreenReaderDisplay() +
        unicodeMultiplication + "%s " + UnitMeasurement.METRE.getSuffixScreenReaderDisplay();

    if (entity.getMaterialType().equals(MaterialType.CONCRETE_MATTRESSES)) {
      return String.format(
          concreteMattressFormat,
          entity.getConcreteMattressLength(),
          entity.getConcreteMattressWidth(),
          entity.getConcreteMattressDepth());
    } else if (entity.getMaterialType().equals(MaterialType.ROCK)) {
      return entity.getMaterialSize() + " " + UnitMeasurement.ROCK_GRADE.getSuffixScreenReaderDisplay();
    } else if (entity.getMaterialType().equals(MaterialType.GROUT_BAGS)) {
      return entity.getMaterialSize() + " " + UnitMeasurement.KILOGRAM.getSuffixScreenReaderDisplay();
    } else {
      return entity.getMaterialSize();
    }

  }

}
