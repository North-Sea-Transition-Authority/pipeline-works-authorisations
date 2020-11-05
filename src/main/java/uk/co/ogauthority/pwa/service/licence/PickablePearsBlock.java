package uk.co.ogauthority.pwa.service.licence;

import uk.co.ogauthority.pwa.model.entity.licence.PearsBlock;
import uk.co.ogauthority.pwa.model.search.SearchSelectable;

public class PickablePearsBlock implements SearchSelectable {

  private final String key;

  private final String data;

  public PickablePearsBlock(PearsBlock pearsBlock) {
    this.data = pearsBlock.getCompositeKey();
    var keySuffix = pearsBlock.getPearsLicence() == null ? "(Unlicensed)" : "(" + pearsBlock.getPearsLicence().getLicenceName() + ")";
    this.key = pearsBlock.getBlockReference() + " " + keySuffix;
  }

  public String getKey() {
    return key;
  }

  public String getData() {
    return data;
  }

  @Override
  public String getSelectionId() {
    return data;
  }

  @Override
  public String getSelectionText() {
    return key;
  }
}
