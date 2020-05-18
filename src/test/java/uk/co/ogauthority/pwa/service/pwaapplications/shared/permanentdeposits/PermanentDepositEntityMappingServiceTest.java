package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositEntityMappingServiceTest {

  private PermanentDepositEntityMappingService permanentDepositEntityMappingService;

  @Before
  public void setUp() {
    permanentDepositEntityMappingService = new PermanentDepositEntityMappingService();
  }

  public PadPermanentDeposit buildBaseEntity() {
    PadPermanentDeposit baseEntity = new PadPermanentDeposit();
    baseEntity.setFromMonth(2);
    baseEntity.setFromYear(2020);
    baseEntity.setToMonth(3);
    baseEntity.setToYear(2020);

    baseEntity.setQuantity(Double.parseDouble("5.7"));
    baseEntity.setContingencyAmount("88");

    baseEntity.setFromLatitudeDegrees(1);
    baseEntity.setFromLatitudeMinutes(33);
    baseEntity.setFromLatitudeSeconds(new BigDecimal("15"));
    baseEntity.setFromLongitudeDegrees(166);
    baseEntity.setFromLongitudeMinutes(35);
    baseEntity.setFromLongitudeSeconds(new BigDecimal("61"));
    baseEntity.setFromLongitudeDirection(LongitudeDirection.EAST);

    baseEntity.setToLatitudeDegrees(55);
    baseEntity.setToLatitudeMinutes(32);
    baseEntity.setToLatitudeSeconds(new BigDecimal("16"));
    baseEntity.setToLongitudeDegrees(53);
    baseEntity.setToLongitudeMinutes(65);
    baseEntity.setToLongitudeSeconds(new BigDecimal("23"));
    baseEntity.setToLongitudeDirection(LongitudeDirection.WEST);
    return baseEntity;
  }

  public PermanentDepositsForm buildBaseForm(PadPermanentDeposit baseEntity) {
    PermanentDepositsForm baseForm = new PermanentDepositsForm();
    baseForm.setFromMonth(baseEntity.getFromMonth());
    baseForm.setFromYear(baseEntity.getFromYear());
    baseForm.setToMonth(baseEntity.getToMonth());
    baseForm.setToYear(baseEntity.getToYear());

    baseForm.setFromLatitudeDegrees(String.valueOf(baseEntity.getFromLatitudeDegrees()));
    baseForm.setFromLatitudeMinutes(String.valueOf(baseEntity.getFromLatitudeMinutes()));
    baseForm.setFromLatitudeSeconds(String.valueOf(baseEntity.getFromLatitudeSeconds()));
    baseForm.setFromLongitudeDegrees(String.valueOf(baseEntity.getFromLongitudeDegrees()));
    baseForm.setFromLongitudeMinutes(String.valueOf(baseEntity.getFromLongitudeMinutes()));
    baseForm.setFromLongitudeSeconds(String.valueOf(baseEntity.getFromLongitudeSeconds()));
    baseForm.setFromLongitudeDirection(baseEntity.getFromLongitudeDirection().name());

    baseForm.setToLatitudeDegrees(String.valueOf(baseEntity.getToLatitudeDegrees()));
    baseForm.setToLatitudeMinutes(String.valueOf(baseEntity.getToLatitudeMinutes()));
    baseForm.setToLatitudeSeconds(String.valueOf(baseEntity.getToLatitudeSeconds()));
    baseForm.setToLongitudeDegrees(String.valueOf(baseEntity.getToLongitudeDegrees()));
    baseForm.setToLongitudeMinutes(String.valueOf(baseEntity.getToLongitudeMinutes()));
    baseForm.setToLongitudeSeconds(String.valueOf(baseEntity.getToLongitudeSeconds()));
    baseForm.setToLongitudeDirection(baseEntity.getToLongitudeDirection().name());
    return baseForm;
  }


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
    form.setRocksSize(Integer.parseInt(entity.getMaterialSize()));
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