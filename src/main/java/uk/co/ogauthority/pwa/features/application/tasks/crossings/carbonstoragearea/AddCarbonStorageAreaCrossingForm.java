package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

public class AddCarbonStorageAreaCrossingForm extends EditCarbonStorageAreaCrossingForm {
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
