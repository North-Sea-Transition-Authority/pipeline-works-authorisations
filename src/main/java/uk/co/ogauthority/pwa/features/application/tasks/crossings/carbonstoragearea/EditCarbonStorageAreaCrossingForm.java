package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.CrossingOwner;

public class EditCarbonStorageAreaCrossingForm {

  @NotNull(message = "Select an owner")
  private CrossingOwner crossingOwner;

  private List<Integer> ownersOuIdList = Collections.emptyList();

  public EditCarbonStorageAreaCrossingForm() {
  }

  public EditCarbonStorageAreaCrossingForm(
      CrossingOwner crossingOwner,
      List<Integer> ownerOuId) {
    this.crossingOwner = crossingOwner;
    this.ownersOuIdList = ownerOuId;
  }

  public CrossingOwner getCrossingOwner() {
    return crossingOwner;
  }

  public EditCarbonStorageAreaCrossingForm setCrossingOwner(
      CrossingOwner crossingOwner) {
    this.crossingOwner = crossingOwner;
    return this;
  }

  public List<Integer> getOwnersOuIdList() {
    return ownersOuIdList;
  }

  public EditCarbonStorageAreaCrossingForm setOwnersOuIdList(List<Integer> ownersOuIdList) {
    this.ownersOuIdList = ownersOuIdList;
    return this;
  }
}
