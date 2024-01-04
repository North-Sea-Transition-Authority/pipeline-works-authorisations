package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

import javax.validation.constraints.NotNull;

public class AddCarbonStorageAreaCrossingForm extends EditCarbonStorageAreaCrossingForm {

  @NotNull(message = "Enter a storage area reference")
  private String storageAreaRef;

  public AddCarbonStorageAreaCrossingForm() {
    super();
  }

  public String getStorageAreaRef() {
    return storageAreaRef;
  }

  public AddCarbonStorageAreaCrossingForm setStorageAreaRef(String storageAreaRef) {
    this.storageAreaRef = storageAreaRef;
    return this;
  }
}
