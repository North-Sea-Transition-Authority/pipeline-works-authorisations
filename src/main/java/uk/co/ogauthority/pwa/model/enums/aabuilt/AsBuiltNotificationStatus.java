package uk.co.ogauthority.pwa.model.enums.aabuilt;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;

public enum AsBuiltNotificationStatus {

  PER_CONSENT("Exactly as per consent", List.of()),
  NOT_PER_CONSENT("Not exactly as per consent", List.of()),
  NEVER_LAID("Work not completed in the consent timeframe", List.of(PipelineStatus.IN_SERVICE, PipelineStatus.RETURNED_TO_SHORE)),
  NOT_LAID_CONSENT_TIMEFRAME("Not laid in the consent timeframe", List.of()),
  NOT_PROVIDED("Not provided", List.of());

  private final String displayName;
  private final List<PipelineStatus> nonSupportedPipelineStatuses;

  AsBuiltNotificationStatus(String displayName,
                            List<PipelineStatus> nonSupportedPipelineStatuses) {
    this.displayName = displayName;
    this.nonSupportedPipelineStatuses = nonSupportedPipelineStatuses;
  }

  public String getDisplayName() {
    return displayName;
  }

  public List<PipelineStatus> getNonSupportedPipelineStatuses() {
    return nonSupportedPipelineStatuses;
  }

  public static List<AsBuiltNotificationStatus> asList(PipelineStatus pipelineStatus) {
    return Arrays.stream(AsBuiltNotificationStatus.values())
        .filter(asBuiltNotificationStatus -> !asBuiltNotificationStatus.nonSupportedPipelineStatuses.contains(pipelineStatus))
        .collect(toList());
  }

}