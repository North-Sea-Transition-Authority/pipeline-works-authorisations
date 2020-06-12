package uk.co.ogauthority.pwa.service.pickpwa;

import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;

public class PickablePwaDto {

  private final String reference;

  private final String pickablePwaString;

  PickablePwaDto(String reference, String pickablePwaId) {
    this.reference = reference;
    this.pickablePwaString = pickablePwaId;
  }

  public String getReference() {
    return reference;
  }

  public String getPickablePwaString() {
    return pickablePwaString;
  }


  public static PickablePwaDto from(MasterPwaDetail masterPwaDetail) {
    var pickablePwa = new PickablePwa(masterPwaDetail);
    return new PickablePwaDto(masterPwaDetail.getReference(), pickablePwa.getPickablePwaString());
  }

}
