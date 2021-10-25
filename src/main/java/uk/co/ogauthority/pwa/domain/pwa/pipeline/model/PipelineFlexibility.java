package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

import java.util.Arrays;
import java.util.List;
import uk.co.ogauthority.pwa.model.diff.DiffableAsString;

public enum PipelineFlexibility implements DiffableAsString {

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

  @Override
  public String getDiffableString() {
    return getDisplayText();
  }
}
