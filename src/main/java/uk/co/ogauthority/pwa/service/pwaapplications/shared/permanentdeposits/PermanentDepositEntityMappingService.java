package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.util.CoordinateUtils;


/**
 * Mapping of form data to entity and entity to form data for Permanent Deposits application form.
 */
@Service
public class PermanentDepositEntityMappingService {

  /**
   * Map Permanent Deposits stored data to form.
   */
  void mapDepositInformationDataToForm(PadPermanentDeposit entity, PermanentDepositsForm form) {

    form.setEntityID(entity.getId());
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

    entity.setFromCoordinates(CoordinateUtils.coordinatePairFromForm(form.getFromCoordinateForm()));
    entity.setToCoordinates(CoordinateUtils.coordinatePairFromForm(form.getToCoordinateForm()));
  }

}
