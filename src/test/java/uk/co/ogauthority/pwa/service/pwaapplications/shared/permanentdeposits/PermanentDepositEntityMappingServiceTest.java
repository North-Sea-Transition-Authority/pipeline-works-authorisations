package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadDepositPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositEntityMappingServiceTest {

  private PermanentDepositEntityMappingService permanentDepositEntityMappingService;

  @Mock
  private PadDepositPipelineRepository padDepositPipelineRepository;

  @Before
  public void setUp() {
    permanentDepositEntityMappingService = new PermanentDepositEntityMappingService(padDepositPipelineRepository);
  }

  //Common Properties
  public PadPermanentDeposit buildBaseEntity() {
    PadPermanentDeposit baseEntity = new PadPermanentDeposit();
    baseEntity.setId(1);
    baseEntity.setReference("my ref");
    baseEntity.setFromMonth(2);
    baseEntity.setFromYear(2020);
    baseEntity.setToMonth(3);
    baseEntity.setToYear(2020);

    baseEntity.setQuantity(Double.parseDouble("5.7"));
    baseEntity.setContingencyAmount("88");


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
    baseForm.setDepositReference(baseEntity.getReference());
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
    return baseForm;
  }


  //Unique Properties
  public void setEntityConcreteProperties(PadPermanentDeposit entity){
    entity.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    entity.setConcreteMattressLength(13);
    entity.setConcreteMattressWidth(22);
    entity.setConcreteMattressDepth(32);
  }

  public void setEntityGroutBagProperties(PadPermanentDeposit entity){
    entity.setMaterialType(MaterialType.GROUT_BAGS);
    entity.setMaterialSize("43");
    entity.setGroutBagsBioDegradable(true);
    entity.setBagsNotUsedDescription("...");
  }

  public void setEntityRockProperties(PadPermanentDeposit entity){
    entity.setMaterialType(MaterialType.ROCK);
    entity.setMaterialSize("43");
  }

  public void setEntityOtherProperties(PadPermanentDeposit entity){
    entity.setMaterialType(MaterialType.OTHER);
    entity.setOtherMaterialType("metal");
    entity.setMaterialSize("43");
  }

  public void setFormConcreteProperties(PermanentDepositsForm form) {
    var entity = buildBaseEntity();
    setEntityConcreteProperties(entity);
    form.setMaterialType(entity.getMaterialType());
    form.setConcreteMattressLength(entity.getConcreteMattressLength());
    form.setConcreteMattressWidth(entity.getConcreteMattressWidth());
    form.setConcreteMattressDepth(entity.getConcreteMattressDepth());
    form.setQuantityConcrete(String.valueOf(entity.getQuantity()));
    form.setContingencyConcreteAmount(entity.getContingencyAmount());
  }

  public void setFormRocksProperties(PermanentDepositsForm form) {
    var entity = buildBaseEntity();
    setEntityRockProperties(entity);
    form.setMaterialType(MaterialType.ROCK);
    form.setRocksSize(entity.getMaterialSize());
    form.setQuantityRocks(String.valueOf(entity.getQuantity()));
    form.setContingencyRocksAmount(entity.getContingencyAmount());
  }

  public void setFormGroutBagsProperties(PermanentDepositsForm form) {
    var entity = buildBaseEntity();
    setEntityGroutBagProperties(entity);
    form.setMaterialType(entity.getMaterialType());
    form.setGroutBagsSize(Integer.parseInt(entity.getMaterialSize()));
    form.setQuantityGroutBags(String.valueOf(entity.getQuantity()));
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
    form.setQuantityOther(String.valueOf(entity.getQuantity()));
    form.setContingencyOtherAmount(entity.getContingencyAmount());
  }


  //TESTS

  @Test
  public void mapDepositInformationDataToForm_materialTypeConcrete() {
    PadPermanentDeposit entity = buildBaseEntity();
    setEntityConcreteProperties(entity);
    var actualForm = new PermanentDepositsForm();
    permanentDepositEntityMappingService.mapDepositInformationDataToForm(entity, actualForm);

    PermanentDepositsForm expectedForm = buildBaseForm(entity);
    setFormConcreteProperties(expectedForm);
    assertThat(actualForm).isEqualTo(expectedForm);
  }

  @Test
  public void mapDepositInformationDataToForm_materialTypeRocks() {
    PadPermanentDeposit entity = buildBaseEntity();
    setEntityRockProperties(entity);
    var actualForm = new PermanentDepositsForm();
    permanentDepositEntityMappingService.mapDepositInformationDataToForm(entity, actualForm);

    PermanentDepositsForm expectedForm = buildBaseForm(entity);
    setFormRocksProperties(expectedForm);
    assertThat(actualForm).isEqualTo(expectedForm);
  }

  @Test
  public void mapDepositInformationDataToForm_materialTypeGroutBags() {
    PadPermanentDeposit entity = buildBaseEntity();
    setEntityGroutBagProperties(entity);
    var actualForm = new PermanentDepositsForm();
    permanentDepositEntityMappingService.mapDepositInformationDataToForm(entity, actualForm);

    PermanentDepositsForm expectedForm = buildBaseForm(entity);
    setFormGroutBagsProperties(expectedForm);
    assertThat(actualForm).isEqualTo(expectedForm);
  }

  @Test
  public void mapDepositInformationDataToForm_materialTypeOther() {
    PadPermanentDeposit entity = buildBaseEntity();
    setEntityOtherProperties(entity);
    var actualForm = new PermanentDepositsForm();
    permanentDepositEntityMappingService.mapDepositInformationDataToForm(entity, actualForm);

    PermanentDepositsForm expectedForm = buildBaseForm(entity);
    setFormOtherMaterialProperties(expectedForm);
    assertThat(actualForm).isEqualTo(expectedForm);
  }



  @Test
  public void setEntityValuesUsingForm_materialTypeConcrete() {
    PadPermanentDeposit expectedEntity = buildBaseEntity();
    PermanentDepositsForm form = buildBaseForm(expectedEntity);
    setFormConcreteProperties(form);

    var actualEntity = new PadPermanentDeposit();
    permanentDepositEntityMappingService.setEntityValuesUsingForm(actualEntity, form);

    setEntityConcreteProperties(expectedEntity);
    assertThat(actualEntity).isEqualTo(expectedEntity);
  }

  @Test
  public void setEntityValuesUsingForm_materialTypeRocks() {
    PadPermanentDeposit expectedEntity = buildBaseEntity();
    PermanentDepositsForm form = buildBaseForm(expectedEntity);
    setFormRocksProperties(form);

    var actualEntity = new PadPermanentDeposit();
    permanentDepositEntityMappingService.setEntityValuesUsingForm(actualEntity, form);

    setEntityRockProperties(expectedEntity);
    assertThat(actualEntity).isEqualTo(expectedEntity);
  }

  @Test
  public void setEntityValuesUsingForm_materialTypeGroutBags() {
    PadPermanentDeposit expectedEntity = buildBaseEntity();
    PermanentDepositsForm form = buildBaseForm(expectedEntity);
    setFormGroutBagsProperties(form);

    var actualEntity = new PadPermanentDeposit();
    permanentDepositEntityMappingService.setEntityValuesUsingForm(actualEntity, form);

    setEntityGroutBagProperties(expectedEntity);
    assertThat(actualEntity).isEqualTo(expectedEntity);
  }

  @Test
  public void setEntityValuesUsingForm_materialTypeOther() {
    PadPermanentDeposit expectedEntity = buildBaseEntity();
    PermanentDepositsForm form = buildBaseForm(expectedEntity);
    setFormOtherMaterialProperties(form);

    var actualEntity = new PadPermanentDeposit();
    permanentDepositEntityMappingService.setEntityValuesUsingForm(actualEntity, form);

    setEntityOtherProperties(expectedEntity);
    assertThat(actualEntity).isEqualTo(expectedEntity);
  }


}