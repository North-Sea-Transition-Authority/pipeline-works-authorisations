package uk.co.ogauthority.pwa.model.entity.enums.pipelines;

import java.util.Arrays;
import java.util.List;

public enum PipelineMaterial {

  CARBON_STEEL("Carbon steel"),
  DUPLEX("Duplex"),
  OTHER("Other");

  private final String displayText;

  PipelineMaterial(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static List<PipelineMaterial> asList() {
    return Arrays.asList(PipelineMaterial.values());
  }

}
