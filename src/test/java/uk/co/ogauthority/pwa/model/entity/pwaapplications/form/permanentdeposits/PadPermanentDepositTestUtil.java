package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits;

import java.time.LocalDate;
import org.junit.platform.commons.util.StringUtils;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

public final class PadPermanentDepositTestUtil {

  private PadPermanentDepositTestUtil(){}

  public static PadPermanentDeposit createRockPadDeposit(
      int entityId,
      String reference,
      PwaApplicationDetail pwaApplicationDetail,
      String materialSize,
      double quantity,
      String contingency,
      LocalDate fromDate,
      LocalDate toDate,
      CoordinatePair fromCoordPair,
      CoordinatePair toCoordPair
  ) {
    var pd = new PadPermanentDeposit();
    pd.setReference(reference);
    pd.setId(entityId);
    pd.setPwaApplicationDetail(pwaApplicationDetail);
    pd.setMaterialType(MaterialType.ROCK);
    pd.setQuantity(quantity);
    pd.setContingencyAmount(contingency);
    pd.setMaterialSize(materialSize);
    pd.setFromCoordinates(fromCoordPair);
    pd.setToCoordinates(toCoordPair);
    pd.setToMonth(toDate.getMonthValue());
    pd.setToYear(toDate.getYear());
    pd.setFromMonth(fromDate.getMonthValue());
    pd.setFromYear(fromDate.getYear());
    return pd;
  }

  public static PadPermanentDeposit createConcreteMattressPadDeposit(
      int entityId,
      String reference,
      PwaApplicationDetail pwaApplicationDetail,
      int length, int width, int depth,
      double quantity,
      String contingency,
      LocalDate fromDate,
      LocalDate toDate,
      CoordinatePair fromCoordPair,
      CoordinatePair toCoordPair
  ) {
    var pd = new PadPermanentDeposit();
    pd.setReference(reference);
    pd.setId(entityId);
    pd.setPwaApplicationDetail(pwaApplicationDetail);
    pd.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    pd.setQuantity(quantity);
    pd.setContingencyAmount(contingency);
    pd.setConcreteMattressLength(length);
    pd.setConcreteMattressWidth(width);
    pd.setConcreteMattressDepth(depth);
    pd.setFromCoordinates(fromCoordPair);
    pd.setToCoordinates(toCoordPair);
    pd.setToMonth(toDate.getMonthValue());
    pd.setToYear(toDate.getYear());
    pd.setFromMonth(fromDate.getMonthValue());
    pd.setFromYear(fromDate.getYear());
    return pd;
  }

  public static PadPermanentDeposit createGroutBagPadDeposit(
      int entityId,
      String reference,
      PwaApplicationDetail pwaApplicationDetail,
      String materialSize,
      double quantity,
      String contingency,
      LocalDate fromDate,
      LocalDate toDate,
      CoordinatePair fromCoordPair,
      CoordinatePair toCoordPair,
      String nonBioBagUsed
  ) {
    var pd = new PadPermanentDeposit();
    pd.setReference(reference);
    pd.setId(entityId);
    pd.setPwaApplicationDetail(pwaApplicationDetail);
    pd.setMaterialType(MaterialType.GROUT_BAGS);
    pd.setQuantity(quantity);
    pd.setContingencyAmount(contingency);
    pd.setMaterialSize(materialSize);
    pd.setFromCoordinates(fromCoordPair);
    pd.setToCoordinates(toCoordPair);
    pd.setToMonth(toDate.getMonthValue());
    pd.setToYear(toDate.getYear());
    pd.setFromMonth(fromDate.getMonthValue());
    pd.setFromYear(fromDate.getYear());
    pd.setBagsNotUsedDescription(nonBioBagUsed);
    pd.setGroutBagsBioDegradable(StringUtils.isBlank(nonBioBagUsed));
    return pd;
  }

  public static PadPermanentDeposit createOtherPadDeposit(
      int entityId,
      String reference,
      PwaApplicationDetail pwaApplicationDetail,
      String otherType,
      String materialSize,
      double quantity,
      String contingency,
      LocalDate fromDate,
      LocalDate toDate,
      CoordinatePair fromCoordPair,
      CoordinatePair toCoordPair
  ) {
    var pd = new PadPermanentDeposit();
    pd.setReference(reference);
    pd.setId(entityId);
    pd.setPwaApplicationDetail(pwaApplicationDetail);
    pd.setMaterialType(MaterialType.OTHER);
    pd.setOtherMaterialType(otherType);
    pd.setQuantity(quantity);
    pd.setContingencyAmount(contingency);
    pd.setMaterialSize(materialSize);
    pd.setFromCoordinates(fromCoordPair);
    pd.setToCoordinates(toCoordPair);
    pd.setToMonth(toDate.getMonthValue());
    pd.setToYear(toDate.getYear());
    pd.setFromMonth(fromDate.getMonthValue());
    pd.setFromYear(fromDate.getYear());
    return pd;
  }


}