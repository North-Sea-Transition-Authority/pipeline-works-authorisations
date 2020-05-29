package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositsOverview;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.util.CoordinateUtils;

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

  public PermanentDepositsOverview buildBaseView(PadPermanentDeposit baseEntity) {
    PermanentDepositsOverview baseView = new PermanentDepositsOverview();
    baseView.setEntityID(baseEntity.getId());
    baseView.setDepositReference(baseEntity.getReference());
    baseView.setFromMonth(baseEntity.getFromMonth());
    baseView.setFromYear(baseEntity.getFromYear());
    baseView.setToMonth(baseEntity.getToMonth());
    baseView.setToYear(baseEntity.getToYear());

    baseView.setFromCoordinateForm(new CoordinateForm());
    baseView.getFromCoordinateForm().setLatitudeDegrees(baseEntity.getFromLatitudeDegrees());
    baseView.getFromCoordinateForm().setLatitudeMinutes(baseEntity.getFromLatitudeMinutes());
    baseView.getFromCoordinateForm().setLatitudeSeconds(baseEntity.getFromLatitudeSeconds());
    baseView.getFromCoordinateForm().setLatitudeDirection(baseEntity.getFromLatitudeDirection());
    baseView.getFromCoordinateForm().setLongitudeDegrees(baseEntity.getFromLongitudeDegrees());
    baseView.getFromCoordinateForm().setLongitudeMinutes(baseEntity.getFromLongitudeMinutes());
    baseView.getFromCoordinateForm().setLongitudeSeconds(baseEntity.getFromLongitudeSeconds());
    baseView.getFromCoordinateForm().setLongitudeDirection(baseEntity.getFromLongitudeDirection());

    baseView.setToCoordinateForm(new CoordinateForm());
    baseView.getToCoordinateForm().setLatitudeDegrees(baseEntity.getToLatitudeDegrees());
    baseView.getToCoordinateForm().setLatitudeMinutes(baseEntity.getToLatitudeMinutes());
    baseView.getToCoordinateForm().setLatitudeSeconds(baseEntity.getToLatitudeSeconds());
    baseView.getToCoordinateForm().setLatitudeDirection(baseEntity.getToLatitudeDirection());
    baseView.getToCoordinateForm().setLongitudeDegrees(baseEntity.getToLongitudeDegrees());
    baseView.getToCoordinateForm().setLongitudeMinutes(baseEntity.getToLongitudeMinutes());
    baseView.getToCoordinateForm().setLongitudeSeconds(baseEntity.getToLongitudeSeconds());
    baseView.getToCoordinateForm().setLongitudeDirection(baseEntity.getToLongitudeDirection());
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
    view.setRocksSize(Integer.parseInt(entity.getMaterialSize()));
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