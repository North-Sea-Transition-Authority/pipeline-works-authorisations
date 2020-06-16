package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings;

import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.CrossedBlockOwner;

public class EditBlockCrossingForm {

  @NotNull(message = "You must provide block owner details")
  private CrossedBlockOwner crossedBlockOwner;

  private List<Integer> blockOwnersOuIdList = Collections.emptyList();

  public EditBlockCrossingForm() {
  }

  public EditBlockCrossingForm(
      CrossedBlockOwner crossedBlockOwner,
      List<Integer> blockOwnerOuId) {
    this.crossedBlockOwner = crossedBlockOwner;
    this.blockOwnersOuIdList = blockOwnerOuId;
  }

  public CrossedBlockOwner getCrossedBlockOwner() {
    return crossedBlockOwner;
  }

  public void setCrossedBlockOwner(CrossedBlockOwner crossedBlockOwner) {
    this.crossedBlockOwner = crossedBlockOwner;
  }

  public List<Integer> getBlockOwnersOuIdList() {
    return blockOwnersOuIdList;
  }

  public void setBlockOwnersOuIdList(List<Integer> blockOwnersOuIdList) {
    this.blockOwnersOuIdList = blockOwnersOuIdList;
  }
}
