package uk.co.ogauthority.pwa.model.entity.enums.pipelines;

import java.util.Arrays;
import java.util.List;

public enum PipelineFlexibility {

  FLEXIBLE("Flexible"),
  RIGID("Rigid");

  private final String displayText;

  PipelineFlexibility(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }

  public static List<PipelineFlexibility> asList() {
    return Arrays.asList(PipelineFlexibility.values());
  }

}
