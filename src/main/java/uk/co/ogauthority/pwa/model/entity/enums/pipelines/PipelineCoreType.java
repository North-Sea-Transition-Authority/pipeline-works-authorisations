package uk.co.ogauthority.pwa.model.entity.enums.pipelines;


public enum PipelineCoreType {

  SINGLE_CORE("Single core", "PL"),
  MULTI_CORE("Multi core", "PLU");

  private final String displayName;
  private final String referencePrefix;

  PipelineCoreType(String displayName, String referencePrefix) {
    this.displayName = displayName;
    this.referencePrefix = referencePrefix;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getReferencePrefix() {
    return referencePrefix;
  }

}
