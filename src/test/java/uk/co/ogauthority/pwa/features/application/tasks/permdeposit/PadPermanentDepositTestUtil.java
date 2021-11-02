package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.RandomUtils;
import org.junit.platform.commons.util.StringUtils;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.CoordinatePairTestUtil;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

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
        true,
        "REFERENCE",
        true,
        "ref",
        pwaApplicationDetail,
        1,
        2,
        3,
        4.4,
        "CONTINGENCY",
        LocalDate.now().plusDays(1),
        LocalDate.now().plusDays(2),
        CoordinatePairTestUtil.getDefaultCoordinate(45, 0),
        CoordinatePairTestUtil.getDefaultCoordinate(45, 0),
        "footnote information"
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
      Boolean depositForConsentedPipeline,
      String reference,
      Boolean depositIsForPipelinesOnOtherApp,
      String appRefAndPipelineNum,
      PwaApplicationDetail pwaApplicationDetail,
      double length, double width, double depth,
      double quantity,
      String contingency,
      LocalDate fromDate,
      LocalDate toDate,
      CoordinatePair fromCoordPair,
      CoordinatePair toCoordPair,
      String footnote
  ) {
    var pd = new PadPermanentDeposit();
    pd.setDepositForConsentedPipeline(depositForConsentedPipeline);
    pd.setReference(reference);
    pd.setDepositIsForPipelinesOnOtherApp(depositIsForPipelinesOnOtherApp);
    pd.setAppRefAndPipelineNum(appRefAndPipelineNum);
    pd.setId(entityId);
    pd.setPwaApplicationDetail(pwaApplicationDetail);
    pd.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    pd.setQuantity(quantity);
    pd.setContingencyAmount(contingency);
    pd.setConcreteMattressLength(BigDecimal.valueOf(length));
    pd.setConcreteMattressWidth(BigDecimal.valueOf(width));
    pd.setConcreteMattressDepth(BigDecimal.valueOf(depth));
    pd.setFromCoordinates(fromCoordPair);
    pd.setToCoordinates(toCoordPair);
    pd.setToMonth(toDate.getMonthValue());
    pd.setToYear(toDate.getYear());
    pd.setFromMonth(fromDate.getMonthValue());
    pd.setFromYear(fromDate.getYear());
    pd.setFootnote(footnote);
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
                                                         Pipeline pipeline) {
    var pdp = new PadDepositPipeline(padPermanentDeposit, pipeline);
    return pdp;
  }


  public static PermanentDepositsForm createDefaultDepositForm() {
    var form = new PermanentDepositsForm();
    var today = LocalDate.now();
    form.setFromDate(new TwoFieldDateInput(today.getYear(), today.getMonthValue()));
    form.setToDate(new TwoFieldDateInput(today.getYear(), today.getMonthValue()));
    form.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    form.setFromCoordinateForm(new CoordinateForm());
    form.setToCoordinateForm(new CoordinateForm());
    return form;
  }

  public static PermanentDepositsForm createFormWithStartDate(int month, int year) {
    var form = createDefaultDepositForm();
    form.getFromDate().setMonth(month);
    form.getFromDate().setYear(year);
    return form;
  }

  public static PadPermanentDeposit createDepositsWithReference(int id, String reference) {
    var deposit = new PadPermanentDeposit();
    deposit.setId(id);
    deposit.setReference(reference);
    return deposit;
  }

  public static PermanentDepositsForm createDepositFormWithReference(int id, String reference) {
    var form = createDefaultDepositForm();
    form.setEntityID(id);
    form.setDepositReference(reference);
    return form;
  }

  public static PermanentDepositsValidationHints createValidationHints(PwaApplicationDetail pwaApplicationDetail) {
    return new PermanentDepositsValidationHints(pwaApplicationDetail, null, List.of());
  }

  public static PermanentDepositsValidationHints createValidationHintsWithDeposits(PwaApplicationDetail pwaApplicationDetail, List<PadPermanentDeposit> deposits) {
    return new PermanentDepositsValidationHints(pwaApplicationDetail, null, deposits);
  }

  public static PermanentDepositsValidationHints createValidationHintsWithTimestamp(PwaApplicationDetail pwaApplicationDetail, Instant projectInfoProposedStartTimestamp) {
    return new PermanentDepositsValidationHints(pwaApplicationDetail, projectInfoProposedStartTimestamp, List.of());
  }


}