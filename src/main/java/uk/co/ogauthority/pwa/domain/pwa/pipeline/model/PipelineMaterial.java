package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

import java.util.Arrays;
import java.util.List;
import uk.co.ogauthority.pwa.model.diff.DiffableAsString;

public enum PipelineMaterial implements DiffableAsString {

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

  @Override
  public String getDiffableString() {
    return getDisplayText();
  }
}
