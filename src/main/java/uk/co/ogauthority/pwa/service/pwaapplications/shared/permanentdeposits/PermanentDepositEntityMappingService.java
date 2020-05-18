package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;


/**
 * Mapping of form data to entity and entity to form data for Permanent Deposits application form.
 */
@Service
public class PermanentDepositEntityMappingService {

  /**
   * Map Permanent Deposits stored data to form.
   */
  void mapDepositInformationDataToForm(PadPermanentDeposit entity, PermanentDepositsForm form) {

    form.setFromMonth(entity.getFromMonth());
    form.setFromYear(entity.getFromYear());
    form.setToMonth(entity.getToMonth());
    form.setToYear(entity.getToYear());

    if (entity.getMaterialType() != null) {
      form.setMaterialType(entity.getMaterialType());

      if (form.getMaterialType().equals(MaterialType.CONCRETE_MATTRESSES)) {
        form.setConcreteMattressLength(entity.getConcreteMattressLength());
        form.setConcreteMattressWidth(entity.getConcreteMattressWidth());
        form.setConcreteMattressDepth(entity.getConcreteMattressDepth());
        form.setQuantityConcrete(String.valueOf(entity.getQuantity()));
        form.setContingencyConcreteAmount(entity.getContingencyAmount());

      } else if (form.getMaterialType().equals(MaterialType.ROCK)) {
        form.setRocksSize(Integer.parseInt(entity.getMaterialSize()));
        form.setQuantityRocks(String.valueOf(entity.getQuantity()));
        form.setContingencyRocksAmount(entity.getContingencyAmount());

      } else if (form.getMaterialType().equals(MaterialType.GROUT_BAGS)) {
        form.setGroutBagsSize(Integer.parseInt(entity.getMaterialSize()));
        form.setQuantityGroutBags(String.valueOf(entity.getQuantity()));
        form.setContingencyGroutBagsAmount(entity.getContingencyAmount());
        form.setGroutBagsBioDegradable(entity.getGroutBagsBioDegradable());
        form.setBioGroutBagsNotUsedDescription(entity.getBagsNotUsedDescription());

      } else if (form.getMaterialType().equals(MaterialType.OTHER)) {
        form.setOtherMaterialSize(entity.getMaterialSize());
        form.setQuantityOther(String.valueOf(entity.getQuantity()));
        form.setContingencyOtherAmount(entity.getContingencyAmount());
      }

      form.setFromLatitudeDegrees(String.valueOf(entity.getFromLatitudeDegrees()));
      form.setFromLatitudeMinutes(String.valueOf(entity.getFromLatitudeMinutes()));
      form.setFromLatitudeSeconds(String.valueOf(entity.getFromLatitudeSeconds()));
      form.setFromLongitudeDegrees(String.valueOf(entity.getFromLongitudeDegrees()));
      form.setFromLongitudeMinutes(String.valueOf(entity.getFromLongitudeMinutes()));
      form.setFromLongitudeSeconds(String.valueOf(entity.getFromLongitudeSeconds()));
      form.setFromLongitudeDirection(entity.getFromLongitudeDirection().name());

      form.setToLatitudeDegrees(String.valueOf(entity.getToLatitudeDegrees()));
      form.setToLatitudeMinutes(String.valueOf(entity.getToLatitudeMinutes()));
      form.setToLatitudeSeconds(String.valueOf(entity.getToLatitudeSeconds()));
      form.setToLongitudeDegrees(String.valueOf(entity.getToLongitudeDegrees()));
      form.setToLongitudeMinutes(String.valueOf(entity.getToLongitudeMinutes()));
      form.setToLongitudeSeconds(String.valueOf(entity.getToLongitudeSeconds()));
      form.setToLongitudeDirection(entity.getToLongitudeDirection().name());
    }


  }


  /**
   * Map Permanent Deposits form data to entity.
   */
  void setEntityValuesUsingForm(PadPermanentDeposit entity, PermanentDepositsForm form) {

    entity.setFromMonth(form.getFromMonth());
    entity.setFromYear(form.getFromYear());
    entity.setToMonth(form.getToMonth());
    entity.setToYear(form.getToYear());

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
      entity.setMaterialSize(String.valueOf(form.getOtherMaterialSize()));
      entity.setQuantity(Double.parseDouble(form.getQuantityOther()));
      entity.setContingencyAmount(form.getContingencyOtherAmount());
    }

    entity.setFromLatitudeDegrees(Integer.parseInt(form.getFromLatitudeDegrees()));
    entity.setFromLatitudeMinutes(Integer.parseInt(form.getFromLatitudeMinutes()));
    entity.setFromLatitudeSeconds(new BigDecimal(form.getFromLatitudeSeconds()));
    entity.setFromLongitudeDegrees(Integer.parseInt(form.getFromLongitudeDegrees()));
    entity.setFromLongitudeMinutes(Integer.parseInt(form.getFromLongitudeMinutes()));
    entity.setFromLongitudeSeconds(new BigDecimal(form.getFromLongitudeSeconds()));
    entity.setFromLongitudeDirection(LongitudeDirection.valueOf(form.getFromLongitudeDirection()));

    entity.setToLatitudeDegrees(Integer.parseInt(form.getToLatitudeDegrees()));
    entity.setToLatitudeMinutes(Integer.parseInt(form.getToLatitudeMinutes()));
    entity.setToLatitudeSeconds(new BigDecimal(form.getToLatitudeSeconds()));
    entity.setToLongitudeDegrees(Integer.parseInt(form.getToLongitudeDegrees()));
    entity.setToLongitudeMinutes(Integer.parseInt(form.getToLongitudeMinutes()));
    entity.setToLongitudeSeconds(new BigDecimal(form.getToLongitudeSeconds()));
    entity.setToLongitudeDirection(LongitudeDirection.valueOf(form.getToLongitudeDirection()));

  }

}
