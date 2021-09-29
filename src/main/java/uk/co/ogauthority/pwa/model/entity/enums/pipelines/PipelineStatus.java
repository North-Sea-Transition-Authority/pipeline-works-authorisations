package uk.co.ogauthority.pwa.model.entity.enums.pipelines;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.model.diff.DiffableAsString;

public enum PipelineStatus implements DiffableAsString {

  IN_SERVICE("In service", 10, false, PhysicalPipelineState.ON_SEABED),
  RETURNED_TO_SHORE("Returned to shore", 20, false, PhysicalPipelineState.ONSHORE),
  OUT_OF_USE_ON_SEABED("Out of use but left on the seabed", 30, false, PhysicalPipelineState.ON_SEABED),
  NEVER_LAID("Never laid and will not be laid", 40, false, PhysicalPipelineState.NEVER_EXISTED),
  TRANSFERRED("Transferred", 50, false, PhysicalPipelineState.NEVER_EXISTED),

  DELETED("Deleted (legacy)", 70, true, PhysicalPipelineState.NEVER_EXISTED),
  PENDING("Pending (legacy)", 90, true, PhysicalPipelineState.NEVER_EXISTED),
  LEGACY_RENUMBERED("Renumbered (legacy)", 100, true, PhysicalPipelineState.NEVER_EXISTED);

  private final String displayText;
  private final Integer displayOrder;
  private final Boolean historical;
  private final PhysicalPipelineState physicalPipelineState;

  PipelineStatus(String displayText,
                 Integer displayOrder,
                 Boolean historical,
                 PhysicalPipelineState physicalPipelineState) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
    this.historical = historical;
    this.physicalPipelineState = physicalPipelineState;
  }

  public String getDisplayText() {
    return displayText;
  }

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  /**
   * Does this status belongs to the previous system and therefore can be ignored while
   * processing applications and consents within this application.
   */
  public Boolean isHistorical() {
    return historical;
  }

  public PhysicalPipelineState getPhysicalPipelineState() {
    return physicalPipelineState;
  }

  public boolean hasPhysicalPipelineState(PhysicalPipelineState queryPhysicalPipelineState) {
    return this.physicalPipelineState == queryPhysicalPipelineState;
  }

  public static Stream<PipelineStatus> stream() {
    return Arrays.stream(PipelineStatus.values());
  }

  public static Stream<PipelineStatus> streamCurrentStatusesInOrder() {
    return Arrays.stream(PipelineStatus.values())
        .filter(pipelineStatus -> !pipelineStatus.historical)
        .sorted(Comparator.comparing(PipelineStatus::getDisplayOrder));
  }

  public static Set<PipelineStatus> currentStatusSet() {
    return streamCurrentStatusesInOrder()
        .collect(Collectors.toUnmodifiableSet());
  }

  public static Set<PipelineStatus> historicalStatusSet() {
    return Arrays.stream(PipelineStatus.values())
        .filter(PipelineStatus::isHistorical)
        .collect(Collectors.toUnmodifiableSet());
  }

  public static List<PipelineStatus> toOrderedListWithoutHistorical() {
    return streamCurrentStatusesInOrder()
        .collect(Collectors.toList());
  }

  @Override
  public String getDiffableString() {
    return getDisplayText();
  }

  public static Set<PipelineStatus> getStatusesWithState(PhysicalPipelineState state) {
    return stream()
        .filter(s -> s.getPhysicalPipelineState() == state)
        .collect(Collectors.toSet());
  }

}
