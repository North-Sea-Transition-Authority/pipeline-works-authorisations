package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePairTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


/**
 * This is testing the same class as {@link PermanentDepositEntityMappingServiceTest}?.
 */
@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositViewMappingServiceTest {

  private static final int ID = 100;
  private static final String REFERENCE = "TEST";

  private static final int C_LENGTH = 1;
  private static final int C_WIDTH = 2;
  private static final int C_DEPTH = 3;
  private static final String CONTINGENCY = "4 and a half things";
  private static final String SIZE = "Bigger than small";
  private static final double QUANTITY = 4.0;

  private static final LocalDate FROM_DATE = LocalDate.now();
  private static final LocalDate TO_DATE = LocalDate.now().plusMonths(3);

  private PermanentDepositEntityMappingService permanentDepositEntityMappingService;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    permanentDepositEntityMappingService = new PermanentDepositEntityMappingService();
  }

  private PadPermanentDeposit getConcreteDeposit(int id, String reference) {
    return PadPermanentDepositTestUtil.createConcreteMattressPadDeposit(
        id,
        true,
        reference,
        true,
        "ref",
        pwaApplicationDetail,
        C_LENGTH,
        C_WIDTH,
        C_DEPTH,
        QUANTITY,
        CONTINGENCY,
        FROM_DATE,
        TO_DATE,
        CoordinatePairTestUtil.getDefaultCoordinate(),
        CoordinatePairTestUtil.getDefaultCoordinate(),
        "footnote information"
    );

  }

  private PadPermanentDeposit getRockDeposit(int id, String reference) {
    return PadPermanentDepositTestUtil.createRockPadDeposit(
        id,
        reference,
        pwaApplicationDetail,
        SIZE,
        QUANTITY,
        CONTINGENCY,
        FROM_DATE,
        TO_DATE,
        CoordinatePairTestUtil.getDefaultCoordinate(),
        CoordinatePairTestUtil.getDefaultCoordinate()
    );

  }

  private PadPermanentDeposit getGroutBagDeposit(int id, String reference, String nonBioReason) {
    return PadPermanentDepositTestUtil.createGroutBagPadDeposit(
        id,
        reference,
        pwaApplicationDetail,
        SIZE,
        QUANTITY,
        CONTINGENCY,
        FROM_DATE,
        TO_DATE,
        CoordinatePairTestUtil.getDefaultCoordinate(),
        CoordinatePairTestUtil.getDefaultCoordinate(),
        nonBioReason
    );

  }

  private PadPermanentDeposit getOtherTypeDeposit(int id, String reference, String type) {
    return PadPermanentDepositTestUtil.createOtherPadDeposit(
        id,
        reference,
        pwaApplicationDetail,
        type,
        SIZE,
        QUANTITY,
        CONTINGENCY,
        FROM_DATE,
        TO_DATE,
        CoordinatePairTestUtil.getDefaultCoordinate(),
        CoordinatePairTestUtil.getDefaultCoordinate()
    );

  }

  @Test
  public void createPermanentDepositOverview_materialTypeConcrete() {
    PadPermanentDeposit entity = getConcreteDeposit(20, "TEST");

    var actualView = permanentDepositEntityMappingService.createPermanentDepositOverview(entity, new HashMap<>());

    assertThat(actualView.getEntityID()).isEqualTo(20);
    assertThat(actualView.getMaterialTypeLookup()).isEqualTo(MaterialType.CONCRETE_MATTRESSES);
    assertThat(actualView.getDepositReference()).isEqualTo("TEST");
    assertThat(actualView.getBioGroutBagsNotUsedDescription()).isNull();
    assertThat(actualView.getMaterialSize()).isEqualTo("1.0 metre × 2.0 metre × 3.0 metre");
    assertThat(actualView.getContingencyAmount()).isEqualTo(CONTINGENCY);
    assertThat(actualView.getQuantity()).isEqualTo(new DecimalFormat("##.####").format(QUANTITY));
  }

  @Test
  public void createPermanentDepositOverview_materialTypeRocks() {
    PadPermanentDeposit entity = getRockDeposit(30, "TEST1");

    var actualView = permanentDepositEntityMappingService.createPermanentDepositOverview(entity, new HashMap<>());
    assertThat(actualView.getEntityID()).isEqualTo(30);
    assertThat(actualView.getMaterialTypeLookup()).isEqualTo(MaterialType.ROCK);
    assertThat(actualView.getDepositReference()).isEqualTo("TEST1");
    assertThat(actualView.getBioGroutBagsNotUsedDescription()).isNull();
    assertThat(actualView.getMaterialSize()).isEqualTo(SIZE + " grade");
    assertThat(actualView.getContingencyAmount()).isEqualTo(CONTINGENCY);
    assertThat(actualView.getQuantity()).isEqualTo(new DecimalFormat("##.####").format(QUANTITY));
  }

  @Test
  public void createPermanentDepositOverview_materialTypeGroutBags_withNonBioBags() {
    PadPermanentDeposit entity = getGroutBagDeposit(40, "TEST2", "some reason");
    var actualView = permanentDepositEntityMappingService.createPermanentDepositOverview(entity, new HashMap<>());

    assertThat(actualView.getEntityID()).isEqualTo(40);
    assertThat(actualView.getDepositReference()).isEqualTo("TEST2");
    assertThat(actualView.getMaterialTypeLookup()).isEqualTo(MaterialType.GROUT_BAGS);
    assertThat(actualView.getBioGroutBagsNotUsedDescription()).isEqualTo("some reason");
    assertThat(actualView.getGroutBagsBioDegradable()).isFalse();
    assertThat(actualView.getMaterialSize()).isEqualTo(SIZE + " kilograms");
    assertThat(actualView.getContingencyAmount()).isEqualTo(CONTINGENCY);
    assertThat(actualView.getQuantity()).isEqualTo(new DecimalFormat("##.####").format(QUANTITY));
  }

  @Test
  public void createPermanentDepositOverview_materialTypeGroutBags_withBioBags() {
    PadPermanentDeposit entity = getGroutBagDeposit(40, "TEST2", null);
    var actualView = permanentDepositEntityMappingService.createPermanentDepositOverview(entity, new HashMap<>());

    assertThat(actualView.getEntityID()).isEqualTo(40);
    assertThat(actualView.getDepositReference()).isEqualTo("TEST2");
    assertThat(actualView.getMaterialTypeLookup()).isEqualTo(MaterialType.GROUT_BAGS);
    assertThat(actualView.getBioGroutBagsNotUsedDescription()).isBlank();
    assertThat(actualView.getGroutBagsBioDegradable()).isTrue();
    assertThat(actualView.getMaterialSize()).isEqualTo(SIZE + " kilograms");
    assertThat(actualView.getContingencyAmount()).isEqualTo(CONTINGENCY);
    assertThat(actualView.getQuantity()).isEqualTo(new DecimalFormat("##.####").format(QUANTITY));
  }

  @Test
  public void createPermanentDepositOverview_materialTypeOther() {
    PadPermanentDeposit entity = getOtherTypeDeposit(50, "TEST3", "SOME_TYPE");
    var actualView = permanentDepositEntityMappingService.createPermanentDepositOverview(entity, new HashMap<>());
    assertThat(actualView.getEntityID()).isEqualTo(50);
    assertThat(actualView.getMaterialTypeLookup()).isEqualTo(MaterialType.OTHER);
    assertThat(actualView.getDepositReference()).isEqualTo("TEST3");
    assertThat(actualView.getBioGroutBagsNotUsedDescription()).isNull();
    assertThat(actualView.getMaterialSize()).isEqualTo(SIZE);
    assertThat(actualView.getContingencyAmount()).isEqualTo(CONTINGENCY);
    assertThat(actualView.getQuantity()).isEqualTo(new DecimalFormat("##.####").format(QUANTITY));
  }


}