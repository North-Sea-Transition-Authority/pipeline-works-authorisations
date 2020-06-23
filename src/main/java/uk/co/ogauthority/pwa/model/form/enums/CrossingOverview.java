package uk.co.ogauthority.pwa.model.form.enums;

public enum CrossingOverview {

  LICENCE_AND_BLOCKS("Licence and blocks"),
  PIPELINE_CROSSINGS("Pipeline crossings"),
  CABLE_CROSSINGS("Cable crossings"),
  MEDIAN_LINE_CROSSING("Median line crossings");

  private String sectionTitle;

  CrossingOverview(String sectionTitle) {
    this.sectionTitle = sectionTitle;
  }

  public String getSectionTitle() {
    return sectionTitle;
  }
}
