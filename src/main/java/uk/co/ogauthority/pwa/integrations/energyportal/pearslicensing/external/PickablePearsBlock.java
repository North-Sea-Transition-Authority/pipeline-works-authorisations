package uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external;

import uk.co.ogauthority.pwa.model.searchselector.SearchSelectable;

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
    return getData();
  }

  @Override
  public String getSelectionText() {
    return getKey();
  }
}
