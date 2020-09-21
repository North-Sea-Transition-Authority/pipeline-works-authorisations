package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import org.apache.commons.lang3.RandomUtils;
import org.junit.platform.commons.util.StringUtils;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawingLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawingLink_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawing_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.CoordinatePairTestUtil;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public final class PadPermanentDepositTestUtil {

  private PadPermanentDepositTestUtil() {
  }

  public static PadDepositDrawing createPadDepositDrawing(PwaApplicationDetail pwaApplicationDetail, PadFile padFile) {
    var drawing = new PadDepositDrawing();
    drawing.setPwaApplicationDetail(pwaApplicationDetail);
    drawing.setFile(padFile);
    // dont care about reference specifics
    drawing.setReference(Arrays.toString(RandomUtils.nextBytes(10)));
    ObjectTestUtils.assertAllFieldsNotNull(
        drawing,
        PadDepositDrawing.class,
        Set.of(PadDepositDrawing_.ID)
    );
    return drawing;
  }

  public static PadDepositDrawingLink createPadDepositDrawingLink(PadPermanentDeposit padPermanentDeposit, PadDepositDrawing padDepositDrawing) {
    var link = new PadDepositDrawingLink();
    link.setPadDepositDrawing(padDepositDrawing);
    link.setPadPermanentDeposit(padPermanentDeposit);
    ObjectTestUtils.assertAllFieldsNotNull(
        link,
        PadDepositDrawingLink.class,
        Set.of(PadDepositDrawingLink_.ID)
    );
    return link;
  }

  public static PadPermanentDeposit createPadDepositWithAllFieldsPopulated(PwaApplicationDetail pwaApplicationDetail) {
    var pd = createConcreteMattressPadDeposit(
        null,
        "REFERENCE",
        pwaApplicationDetail,
        1,
        2,
        3,
        4.4,
        "CONTINGENCY",
        LocalDate.now().plusDays(1),
        LocalDate.now().plusDays(2),
        CoordinatePairTestUtil.getDefaultCoordinate(45, 0),
        CoordinatePairTestUtil.getDefaultCoordinate(45, 0)
        );

    pd.setOtherMaterialType("OTHER MATERIAL");
    pd.setBagsNotUsedDescription("BAGS NOT USED");
    pd.setGroutBagsBioDegradable(true);
    pd.setMaterialSize("MATERIAL SIZE");

    ObjectTestUtils.assertAllFieldsNotNull(
        pd,
        PadPermanentDeposit.class,
        Set.of(PadPermanentDeposit_.ID));
    return pd;
  }

  public static PadPermanentDeposit createRockPadDeposit(
      Integer entityId,
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
      Integer entityId,
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
      Integer entityId,
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
      Integer entityId,
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

  public static PadDepositPipeline createDepositPipeline(PadPermanentDeposit padPermanentDeposit,
                                                         PadPipeline padPipeline) {
    var pdp = new PadDepositPipeline(padPermanentDeposit, padPipeline);
    return pdp;
  }


}