package uk.co.ogauthority.pwa.service.licence;



import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.co.ogauthority.pwa.model.entity.enums.BlockLocation;
import uk.co.ogauthority.pwa.model.entity.enums.LicenceStatus;
import uk.co.ogauthority.pwa.model.entity.licence.PearsBlock;
import uk.co.ogauthority.pwa.model.entity.licence.PearsLicence;

public class PickablePearsBlockTest {

  private PearsLicence licence = new PearsLicence(1, "P", 1, "P1", LicenceStatus.EXTANT);

  private PearsBlock licensedBlock = new PearsBlock(
      "licenceKey1",
      licence,
      "1/2/3",
      "3",
      "2",
      "1",
      BlockLocation.OFFSHORE);

  private PearsBlock unlicensedBlock = new PearsBlock(
      "unlicencedKey",
      null,
      "4/5/6",
      "4",
      "5",
      "6",
      BlockLocation.OFFSHORE);


  @Test
  public void pickablePearsBlock_constructedWithLicensedBlock() {

    var pb = new PickablePearsBlock(licensedBlock);
    assertThat(pb.getData()).isEqualTo(licensedBlock.getCompositeKey());
    assertThat(pb.getKey()).isEqualTo(licensedBlock.getBlockReference() + " (" + licence.getLicenceName() + ")");
  }

  @Test
  public void pickablePearsBlock_constructedWithUnlicensedBlock() {

    var pb = new PickablePearsBlock(unlicensedBlock);
    assertThat(pb.getData()).isEqualTo(unlicensedBlock.getCompositeKey());
    assertThat(pb.getKey()).isEqualTo(unlicensedBlock.getBlockReference() + " (Unlicensed)");
  }

}