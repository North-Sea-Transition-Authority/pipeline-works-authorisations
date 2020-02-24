package uk.co.ogauthority.pwa.temp.model.contacts;

public enum UooAgreement {

  NCS_EXPORT_GAS(
      "Any body corporate which is party to an arrangement to export gas to the United Kingdom from the Norwegian Continental Shelf (NCS)"
  );

  private String agreementText;

  UooAgreement(String agreementText) {
    this.agreementText = agreementText;
  }

  @Override
  public String toString() {
    return agreementText;
  }
}
