package uk.co.ogauthority.pwa.model.form.enums;

public enum CrossingOverview {

  LICENCE_AND_BLOCKS("Licence and block overview"),
  PIPELINE_CROSSINGS("Pipeline crossing overview"),
  CABLE_CROSSINGS("Cable crossing overview"),
  MEDIAN_LINE_CROSSING("Median line crossing overview");

  private String sectionTitle;

  CrossingOverview(String sectionTitle) {
    this.sectionTitle = sectionTitle;
  }

  public String getSectionTitle() {
    return sectionTitle;
  }
}
