package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.CrossingOwner;

public class EditBlockCrossingForm {

  @NotNull(message = "Select a block owner")
  private CrossingOwner crossingOwner;

  private List<Integer> blockOwnersOuIdList = Collections.emptyList();

  public EditBlockCrossingForm() {
  }

  public EditBlockCrossingForm(
      CrossingOwner crossingOwner,
      List<Integer> blockOwnerOuId) {
    this.crossingOwner = crossingOwner;
    this.blockOwnersOuIdList = blockOwnerOuId;
  }

  public CrossingOwner getCrossingOwner() {
    return crossingOwner;
  }

  public void setCrossingOwner(CrossingOwner crossingOwner) {
    this.crossingOwner = crossingOwner;
  }

  public List<Integer> getBlockOwnersOuIdList() {
    return blockOwnersOuIdList;
  }

  public void setBlockOwnersOuIdList(List<Integer> blockOwnersOuIdList) {
    this.blockOwnersOuIdList = blockOwnersOuIdList;
  }
}
