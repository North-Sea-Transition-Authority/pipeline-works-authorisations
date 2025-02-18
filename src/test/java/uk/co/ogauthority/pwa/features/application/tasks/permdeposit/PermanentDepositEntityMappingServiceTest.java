package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePair;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinateUtils;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeCoordinate;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeDirection;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeCoordinate;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeDirection;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

@ExtendWith(MockitoExtension.class)
public class PermanentDepositEntityMappingServiceTest {

  private PermanentDepositEntityMappingService permanentDepositEntityMappingService;


  @BeforeEach
  void setUp() {
    permanentDepositEntityMappingService = new PermanentDepositEntityMappingService();
  }

  //Common Properties
  public PadPermanentDeposit buildBaseEntity() {
    PadPermanentDeposit baseEntity = new PadPermanentDeposit();
    baseEntity.setId(1);
    baseEntity.setDepositForConsentedPipeline(true);
    baseEntity.setReference("my ref");
    baseEntity.setDepositIsForPipelinesOnOtherApp(true);
    baseEntity.setAppRefAndPipelineNum("ref pl");
    baseEntity.setFromMonth(2);
    baseEntity.setFromYear(2020);
    baseEntity.setToMonth(3);
    baseEntity.setToYear(2020);

    baseEntity.setQuantity(Double.parseDouble("5.7"));
    baseEntity.setContingencyAmount("88");
    baseEntity.setFootnote("footnote information");


    var fromCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(55, 55, BigDecimal.valueOf(55.55), LatitudeDirection.NORTH),
            new LongitudeCoordinate(12, 12, BigDecimal.valueOf(12), LongitudeDirection.EAST)
        ), fromCoordinateForm
    );
    baseEntity.setFromCoordinates(CoordinateUtils.coordinatePairFromForm(fromCoordinateForm));

    var toCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(46, 46, BigDecimal.valueOf(46), LatitudeDirection.SOUTH),
            new LongitudeCoordinate(6, 6, BigDecimal.valueOf(6.66), LongitudeDirection.WEST)
        ), toCoordinateForm
    );
    baseEntity.setToCoordinates(CoordinateUtils.coordinatePairFromForm(toCoordinateForm));
    return baseEntity;
  }

  public PermanentDepositsForm buildBaseForm(PadPermanentDeposit baseEntity) {
    PermanentDepositsForm baseForm = new PermanentDepositsForm();
    baseForm.setEntityID(baseEntity.getId());
    baseForm.setDepositIsForConsentedPipeline(baseEntity.getDepositForConsentedPipeline());
    baseForm.setDepositReference(baseEntity.getReference());
    baseForm.setDepositIsForPipelinesOnOtherApp(baseEntity.getDepositIsForPipelinesOnOtherApp());
    baseForm.setAppRefAndPipelineNum(baseEntity.getAppRefAndPipelineNum());
    baseForm.setFromDate(new TwoFieldDateInput(baseEntity.getFromYear(), baseEntity.getFromMonth()));
    baseForm.setToDate(new TwoFieldDateInput(baseEntity.getToYear(), baseEntity.getToMonth()));

    baseForm.setFromCoordinateForm(new CoordinateForm());
    baseForm.getFromCoordinateForm().setLatitudeDegrees(baseEntity.getFromLatitudeDegrees());
    baseForm.getFromCoordinateForm().setLatitudeMinutes(baseEntity.getFromLatitudeMinutes());
    baseForm.getFromCoordinateForm().setLatitudeSeconds(baseEntity.getFromLatitudeSeconds());
    baseForm.getFromCoordinateForm().setLatitudeDirection(baseEntity.getFromLatitudeDirection());
    baseForm.getFromCoordinateForm().setLongitudeDegrees(baseEntity.getFromLongitudeDegrees());
    baseForm.getFromCoordinateForm().setLongitudeMinutes(baseEntity.getFromLongitudeMinutes());
    baseForm.getFromCoordinateForm().setLongitudeSeconds(baseEntity.getFromLongitudeSeconds());
    baseForm.getFromCoordinateForm().setLongitudeDirection(baseEntity.getFromLongitudeDirection());

    baseForm.setToCoordinateForm(new CoordinateForm());
    baseForm.getToCoordinateForm().setLatitudeDegrees(baseEntity.getToLatitudeDegrees());
    baseForm.getToCoordinateForm().setLatitudeMinutes(baseEntity.getToLatitudeMinutes());
    baseForm.getToCoordinateForm().setLatitudeSeconds(baseEntity.getToLatitudeSeconds());
    baseForm.getToCoordinateForm().setLatitudeDirection(baseEntity.getToLatitudeDirection());
    baseForm.getToCoordinateForm().setLongitudeDegrees(baseEntity.getToLongitudeDegrees());
    baseForm.getToCoordinateForm().setLongitudeMinutes(baseEntity.getToLongitudeMinutes());
    baseForm.getToCoordinateForm().setLongitudeSeconds(baseEntity.getToLongitudeSeconds());
    baseForm.getToCoordinateForm().setLongitudeDirection(baseEntity.getToLongitudeDirection());

    baseForm.setFootnote(baseEntity.getFootnote());
    return baseForm;
  }


  //Unique Properties
  public void setEntityConcreteProperties(PadPermanentDeposit entity) {
    entity.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    entity.setConcreteMattressLength(BigDecimal.valueOf(13));
    entity.setConcreteMattressWidth(BigDecimal.valueOf(22));
    entity.setConcreteMattressDepth(BigDecimal.valueOf(32));
  }

  public void setEntityGroutBagProperties(PadPermanentDeposit entity) {
    entity.setMaterialType(MaterialType.GROUT_BAGS);
    entity.setMaterialSize("43");
    entity.setGroutBagsBioDegradable(true);
    entity.setBagsNotUsedDescription("...");
  }

  public void setEntityRockProperties(PadPermanentDeposit entity) {
    entity.setMaterialType(MaterialType.ROCK);
    entity.setMaterialSize("43");
  }

  public void setEntityOtherProperties(PadPermanentDeposit entity) {
    entity.setMaterialType(MaterialType.OTHER);
    entity.setOtherMaterialType("metal");
    entity.setMaterialSize("43");
  }

  public void setFormConcreteProperties(PermanentDepositsForm form) {
    var entity = buildBaseEntity();
    setEntityConcreteProperties(entity);
    form.setMaterialType(entity.getMaterialType());
    form.setConcreteMattressLength(new DecimalInput(entity.getConcreteMattressLength()));
    form.setConcreteMattressWidth(new DecimalInput(entity.getConcreteMattressWidth()));
    form.setConcreteMattressDepth(new DecimalInput(entity.getConcreteMattressDepth()));
    form.setQuantityConcrete(DecimalInput.from(entity.getQuantity()));
    form.setContingencyConcreteAmount(entity.getContingencyAmount());
  }

  public void setFormRocksProperties(PermanentDepositsForm form) {
    var entity = buildBaseEntity();
    setEntityRockProperties(entity);
    form.setMaterialType(MaterialType.ROCK);
    form.setRocksSize(entity.getMaterialSize());
    form.setQuantityRocks(DecimalInput.from(entity.getQuantity()));
    form.setContingencyRocksAmount(entity.getContingencyAmount());
  }

  public void setFormGroutBagsProperties(PermanentDepositsForm form) {
    var entity = buildBaseEntity();
    setEntityGroutBagProperties(entity);
    form.setMaterialType(entity.getMaterialType());
    form.setGroutBagsSize(new DecimalInput(entity.getMaterialSize()));
    form.setQuantityGroutBags(DecimalInput.from(entity.getQuantity()));
    form.setContingencyGroutBagsAmount(entity.getContingencyAmount());
    form.setGroutBagsBioDegradable(entity.getGroutBagsBioDegradable());
    form.setBioGroutBagsNotUsedDescription(entity.getBagsNotUsedDescription());
  }

  public void setFormOtherMaterialProperties(PermanentDepositsForm form) {
    var entity = buildBaseEntity();
    setEntityOtherProperties(entity);
    form.setMaterialType(MaterialType.OTHER);
    form.setOtherMaterialType(entity.getOtherMaterialType());
    form.setOtherMaterialSize(entity.getMaterialSize());
    form.setQuantityOther(DecimalInput.from(entity.getQuantity()));
    form.setContingencyOtherAmount(entity.getContingencyAmount());
  }


  //TESTS

  @Test
  void mapDepositInformationDataToForm_materialTypeConcrete() {
    PadPermanentDeposit entity = buildBaseEntity();
    setEntityConcreteProperties(entity);
    var actualForm = new PermanentDepositsForm();
    permanentDepositEntityMappingService.mapDepositInformationDataToForm(entity, actualForm);

    PermanentDepositsForm expectedForm = buildBaseForm(entity);
    setFormConcreteProperties(expectedForm);
    assertThat(actualForm).isEqualTo(expectedForm);
  }

  @Test
  void mapDepositInformationDataToForm_materialTypeRocks() {
    PadPermanentDeposit entity = buildBaseEntity();
    setEntityRockProperties(entity);
    var actualForm = new PermanentDepositsForm();
    permanentDepositEntityMappingService.mapDepositInformationDataToForm(entity, actualForm);

    PermanentDepositsForm expectedForm = buildBaseForm(entity);
    setFormRocksProperties(expectedForm);
    assertThat(actualForm).isEqualTo(expectedForm);
  }

  @Test
  void mapDepositInformationDataToForm_materialTypeGroutBags() {
    PadPermanentDeposit entity = buildBaseEntity();
    setEntityGroutBagProperties(entity);
    var actualForm = new PermanentDepositsForm();
    permanentDepositEntityMappingService.mapDepositInformationDataToForm(entity, actualForm);

    PermanentDepositsForm expectedForm = buildBaseForm(entity);
    setFormGroutBagsProperties(expectedForm);
    assertThat(actualForm).isEqualTo(expectedForm);
  }

  @Test
  void mapDepositInformationDataToForm_materialTypeOther() {
    PadPermanentDeposit entity = buildBaseEntity();
    setEntityOtherProperties(entity);
    var actualForm = new PermanentDepositsForm();
    permanentDepositEntityMappingService.mapDepositInformationDataToForm(entity, actualForm);

    PermanentDepositsForm expectedForm = buildBaseForm(entity);
    setFormOtherMaterialProperties(expectedForm);
    assertThat(actualForm).isEqualTo(expectedForm);
  }


  @Test
  void setEntityValuesUsingForm_materialTypeConcrete() {
    PadPermanentDeposit expectedEntity = buildBaseEntity();
    PermanentDepositsForm form = buildBaseForm(expectedEntity);
    setFormConcreteProperties(form);

    var actualEntity = new PadPermanentDeposit();
    permanentDepositEntityMappingService.setEntityValuesUsingForm(actualEntity, form);

    setEntityConcreteProperties(expectedEntity);
    assertThat(actualEntity).isEqualTo(expectedEntity);
  }

  @Test
  void setEntityValuesUsingForm_materialTypeRocks() {
    PadPermanentDeposit expectedEntity = buildBaseEntity();
    PermanentDepositsForm form = buildBaseForm(expectedEntity);
    setFormRocksProperties(form);

    var actualEntity = new PadPermanentDeposit();
    permanentDepositEntityMappingService.setEntityValuesUsingForm(actualEntity, form);

    setEntityRockProperties(expectedEntity);
    assertThat(actualEntity).isEqualTo(expectedEntity);
  }

  @Test
  void setEntityValuesUsingForm_materialTypeGroutBags() {
    PadPermanentDeposit expectedEntity = buildBaseEntity();
    PermanentDepositsForm form = buildBaseForm(expectedEntity);
    setFormGroutBagsProperties(form);

    var actualEntity = new PadPermanentDeposit();
    permanentDepositEntityMappingService.setEntityValuesUsingForm(actualEntity, form);

    setEntityGroutBagProperties(expectedEntity);
    assertThat(actualEntity).isEqualTo(expectedEntity);
  }

  @Test
  void setEntityValuesUsingForm_materialTypeOther() {
    PadPermanentDeposit expectedEntity = buildBaseEntity();
    PermanentDepositsForm form = buildBaseForm(expectedEntity);
    setFormOtherMaterialProperties(form);

    var actualEntity = new PadPermanentDeposit();
    permanentDepositEntityMappingService.setEntityValuesUsingForm(actualEntity, form);

    setEntityOtherProperties(expectedEntity);
    assertThat(actualEntity).isEqualTo(expectedEntity);
  }


}