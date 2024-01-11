package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo;

import java.util.List;

public class PwaAreaForm {

  private Boolean linkedToArea;

  private List<String> linkedAreas;

  private String noLinkedAreaDescription;

  public Boolean getLinkedToArea() {
    return linkedToArea;
  }

  public void setLinkedToArea(Boolean linkedToArea) {
    this.linkedToArea = linkedToArea;
  }

  public List<String> getLinkedAreas() {
    return linkedAreas;
  }

  public void setLinkedAreas(List<String> linkedAreas) {
    this.linkedAreas = linkedAreas;
  }

  public String getNoLinkedAreaDescription() {
    return noLinkedAreaDescription;
  }

  public void setNoLinkedAreaDescription(String noLinkedAreaDescription) {
    this.noLinkedAreaDescription = noLinkedAreaDescription;
  }
}
