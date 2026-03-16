package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;


import uk.co.ogauthority.pwa.util.enumutils.Displayable;

public enum PipelineCoreType implements Displayable {

  SINGLE_CORE("Single core", "PL"),
  MULTI_CORE("Multi core", "PLU");

  private final String displayName;
  private final String referencePrefix;

  PipelineCoreType(String displayName, String referencePrefix) {
    this.displayName = displayName;
    this.referencePrefix = referencePrefix;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  public String getReferencePrefix() {
    return referencePrefix;
  }

}
