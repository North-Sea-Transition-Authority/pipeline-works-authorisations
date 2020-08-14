package uk.co.ogauthority.pwa.model.entity.enums.pipelines;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PipelineStatus {

  IN_SERVICE("In service", 10, false),
  RETURNED_TO_SHORE("Returned to shore", 20, false),
  OUT_OF_USE_ON_SEABED("Out of use but left on the seabed", 30, false),
  NEVER_LAID("Never laid and will not be laid", 40, false),
  AUTHORISED("Authorised (legacy)", 50, true),
  CURRENT("Current (legacy)", 60, true),
  DELETED("Deleted (legacy)", 70, true),
  LEGACY("Legacy (legacy)", 80, true),
  PENDING("Pending (legacy)", 90, true);

  private final String displayText;
  private final Integer displayOrder;
  private final Boolean historical;

  PipelineStatus(String displayText, Integer displayOrder, Boolean historical) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
    this.historical = historical;
  }

  public String getDisplayText() {
    return displayText;
  }

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public Boolean getHistorical() {
    return historical;
  }

  public static Stream<PipelineStatus> streamInOrder() {
    return Arrays.stream(PipelineStatus.values())
        .sorted(Comparator.comparing(PipelineStatus::getDisplayOrder));
  }

  public static List<PipelineStatus> toOrderedListWithoutHistorical() {
    return streamInOrder()
        .filter(pipelineStatus -> !pipelineStatus.getHistorical())
        .collect(Collectors.toList());
  }

}
