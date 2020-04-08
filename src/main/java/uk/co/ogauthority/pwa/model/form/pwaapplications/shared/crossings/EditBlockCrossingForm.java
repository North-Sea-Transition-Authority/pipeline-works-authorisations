package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings;

import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.CrossedBlockOwner;

public class EditBlockCrossingForm {

  @NotNull(message = "You must provide block owner details")
  private CrossedBlockOwner crossedBlockOwner;

  private List<Integer> blockOwnersOuIdList = Collections.emptyList();

  @Length(max = 4000, message = "Block owner details must be 4000 characters or fewer")
  private String operatorNotFoundFreeTextBox;

  public EditBlockCrossingForm() {
  }

  public EditBlockCrossingForm(
      CrossedBlockOwner crossedBlockOwner,
      List<Integer> blockOwnerOuId,
      String operatorNotFoundFreeTextBox) {
    this.crossedBlockOwner = crossedBlockOwner;
    this.blockOwnersOuIdList = blockOwnerOuId;
    this.operatorNotFoundFreeTextBox = operatorNotFoundFreeTextBox;
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

  public String getOperatorNotFoundFreeTextBox() {
    return operatorNotFoundFreeTextBox;
  }

  public void setOperatorNotFoundFreeTextBox(String operatorNotFoundFreeTextBox) {
    this.operatorNotFoundFreeTextBox = operatorNotFoundFreeTextBox;
  }
}
