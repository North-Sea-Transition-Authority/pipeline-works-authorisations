package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import jakarta.validation.constraints.NotNull;

public class AddBlockCrossingForm extends EditBlockCrossingForm {

  @NotNull(message = "Select a block")
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
