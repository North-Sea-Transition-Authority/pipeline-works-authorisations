package uk.co.ogauthority.pwa.service.enums.documents;

public enum DocumentImageMethod {

  BASE_64(
      "Base64 conversion",
      "data:image/jpg;base64,"),

  TEMP_FILE(
      "Temp file creation",
      "file:///");

  private final String descriptor;
  private final String uriPrefix;

  DocumentImageMethod(String descriptor, String uriPrefix) {
    this.descriptor = descriptor;
    this.uriPrefix = uriPrefix;
  }

  public String getDescriptor() {
    return descriptor;
  }

  public String getUriPrefix() {
    return uriPrefix;
  }

}
