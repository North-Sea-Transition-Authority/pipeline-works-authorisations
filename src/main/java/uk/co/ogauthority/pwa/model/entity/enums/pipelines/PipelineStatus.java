package uk.co.ogauthority.pwa.model.entity.enums.pipelines;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.model.diff.DiffableAsString;

public enum PipelineStatus implements DiffableAsString {

  IN_SERVICE("In service", 10, false, true),
  RETURNED_TO_SHORE("Returned to shore", 20, false, false),
  OUT_OF_USE_ON_SEABED("Out of use but left on the seabed", 30, false, true),
  NEVER_LAID("Never laid and will not be laid", 40, false, false),

  DELETED("Deleted (legacy)", 70, true, false),
  PENDING("Pending (legacy)", 90, true, false);

  private final String displayText;
  private final Integer displayOrder;
  private final Boolean historical;
  private final Boolean pipelineExistsOnSeabed;

  PipelineStatus(String displayText,
                 Integer displayOrder,
                 Boolean historical,
                 Boolean pipelineExistsOnSeabed) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
    this.historical = historical;
    this.pipelineExistsOnSeabed = pipelineExistsOnSeabed;
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

  /**
   * Does this status indicate that the which holds it exists as described on the seabed.
   */
  public Boolean pipelineExistsOnSeabed() {
    return pipelineExistsOnSeabed;
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
}
