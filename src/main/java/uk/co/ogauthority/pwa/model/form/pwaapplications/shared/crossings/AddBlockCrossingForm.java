package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings;

import javax.validation.constraints.NotNull;

public class AddBlockCrossingForm extends EditBlockCrossingForm {

  @NotNull(message = "You must provide a crossed block")
  private String pickedBlock;

  public AddBlockCrossingForm() {
    super();
  }

  public String getPickedBlock() {
    return pickedBlock;
  }

  public void setPickedBlock(String pickedBlock) {
    this.pickedBlock = pickedBlock;
  }

}
