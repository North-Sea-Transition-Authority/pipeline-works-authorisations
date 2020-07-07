package uk.co.ogauthority.pwa.model.entity.enums.pipelines;


public enum PipelineCoreType {

  SINGLE_CORE("Single core"),
  MULTI_CORE("Multi core");

  private String displayName;

  PipelineCoreType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

}
