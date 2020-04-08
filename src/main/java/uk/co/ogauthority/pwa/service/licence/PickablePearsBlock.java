package uk.co.ogauthority.pwa.service.licence;

import uk.co.ogauthority.pwa.model.entity.licence.PearsBlock;

public class PickablePearsBlock {

  private final String key;

  private final String data;

  PickablePearsBlock(PearsBlock pearsBlock) {

    this.data = pearsBlock.getCompositeKey();
    var keySuffix = pearsBlock.getPearsLicence() == null ? "(Unlicensed)" : "(" + pearsBlock.getPearsLicence().getLicenceName() + ")";
    this.key = pearsBlock.getBlockReference() + " " + keySuffix;
  }

  PickablePearsBlock(String key, String data) {

    this.data = data;
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public String getData() {
    return data;
  }
}
