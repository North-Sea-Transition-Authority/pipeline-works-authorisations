package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositsOverview;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositViewMappingServiceTest {

  private PermanentDepositEntityMappingService permanentDepositEntityMappingService;

  @Before
  public void setUp() {
    permanentDepositEntityMappingService = new PermanentDepositEntityMappingService();
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
    baseEntity.setFromCoordinates(new CoordinatePair(
        new LatitudeCoordinate(55, 55, BigDecimal.valueOf(55.55), LatitudeDirection.NORTH),
        new LongitudeCoordinate(12, 12, BigDecimal.valueOf(12), LongitudeDirection.EAST)
    ));
    baseEntity.setToCoordinates(new CoordinatePair(
        new LatitudeCoordinate(55, 55, BigDecimal.valueOf(55.55), LatitudeDirection.NORTH),
        new LongitudeCoordinate(12, 12, BigDecimal.valueOf(12), LongitudeDirection.EAST)
    ));
    return baseEntity;
  }

  public PermanentDepositsOverview buildBaseView(PadPermanentDeposit baseEntity) {
    PermanentDepositsOverview baseView = new PermanentDepositsOverview();
    baseView.setEntityID(baseEntity.getId());
    baseView.setDepositReference(baseEntity.getReference());
    baseView.setFromMonth(baseEntity.getFromMonth());
    baseView.setFromYear(baseEntity.getFromYear());
    baseView.setToMonth(baseEntity.getToMonth());
    baseView.setToYear(baseEntity.getToYear());
    baseView.setFromCoordinates(baseEntity.getFromCoordinates());
    baseView.setToCoordinates(baseEntity.getToCoordinates());
    return baseView;
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

  public void setFormConcreteProperties(PermanentDepositsOverview view) {
    var entity = buildBaseEntity();
    setEntityConcreteProperties(entity);
    view.setMaterialType(entity.getMaterialType());
    view.setConcreteMattressLength(entity.getConcreteMattressLength());
    view.setConcreteMattressWidth(entity.getConcreteMattressWidth());
    view.setConcreteMattressDepth(entity.getConcreteMattressDepth());
    view.setQuantityConcrete(String.valueOf(entity.getQuantity()));
    view.setContingencyConcreteAmount(entity.getContingencyAmount());
  }

  public void setFormRocksProperties(PermanentDepositsOverview view) {
    var entity = buildBaseEntity();
    setEntityRockProperties(entity);
    view.setMaterialType(MaterialType.ROCK);
    view.setRocksSize(entity.getMaterialSize());
    view.setQuantityRocks(String.valueOf(entity.getQuantity()));
    view.setContingencyRocksAmount(entity.getContingencyAmount());
  }

  public void setFormGroutBagsProperties(PermanentDepositsOverview view) {
    var entity = buildBaseEntity();
    setEntityGroutBagProperties(entity);
    view.setMaterialType(entity.getMaterialType());
    view.setGroutBagsSize(Integer.parseInt(entity.getMaterialSize()));
    view.setQuantityGroutBags(String.valueOf(entity.getQuantity()));
    view.setContingencyGroutBagsAmount(entity.getContingencyAmount());
    view.setGroutBagsBioDegradable(entity.getGroutBagsBioDegradable());
    view.setBioGroutBagsNotUsedDescription(entity.getBagsNotUsedDescription());
  }

  public void setFormOtherMaterialProperties(PermanentDepositsOverview view) {
    var entity = buildBaseEntity();
    setEntityOtherProperties(entity);
    view.setMaterialType(MaterialType.OTHER);
    view.setOtherMaterialType(entity.getOtherMaterialType());
    view.setOtherMaterialSize(entity.getMaterialSize());
    view.setQuantityOther(String.valueOf(entity.getQuantity()));
    view.setContingencyOtherAmount(entity.getContingencyAmount());
  }


  //TESTS

  @Test
  public void mapDepositInformationDataToView_materialTypeConcrete() {
    PadPermanentDeposit entity = buildBaseEntity();
    setEntityConcreteProperties(entity);
    var actualView = new PermanentDepositsOverview();
    permanentDepositEntityMappingService.mapDepositInformationDataToView(entity, actualView);

    PermanentDepositsOverview expectedView = buildBaseView(entity);
    setFormConcreteProperties(expectedView);
    assertThat(actualView).isEqualTo(expectedView);
  }

  @Test
  public void mapDepositInformationDataToView_materialTypeRocks() {
    PadPermanentDeposit entity = buildBaseEntity();
    setEntityRockProperties(entity);
    var actualView = new PermanentDepositsOverview();
    permanentDepositEntityMappingService.mapDepositInformationDataToView(entity, actualView);

    PermanentDepositsOverview expectedView = buildBaseView(entity);
    setFormRocksProperties(expectedView);
    assertThat(actualView).isEqualTo(expectedView);
  }

  @Test
  public void mapDepositInformationDataToView_materialTypeGroutBags() {
    PadPermanentDeposit entity = buildBaseEntity();
    setEntityGroutBagProperties(entity);
    var actualView = new PermanentDepositsOverview();
    permanentDepositEntityMappingService.mapDepositInformationDataToView(entity, actualView);

    PermanentDepositsOverview expectedView = buildBaseView(entity);
    setFormGroutBagsProperties(expectedView);
    assertThat(actualView).isEqualTo(expectedView);
  }

  @Test
  public void mapDepositInformationDataToView_materialTypeOther() {
    PadPermanentDeposit entity = buildBaseEntity();
    setEntityOtherProperties(entity);
    var actualView = new PermanentDepositsOverview();
    permanentDepositEntityMappingService.mapDepositInformationDataToView(entity, actualView);

    PermanentDepositsOverview expectedView = buildBaseView(entity);
    setFormOtherMaterialProperties(expectedView);
    assertThat(actualView).isEqualTo(expectedView);
  }





}