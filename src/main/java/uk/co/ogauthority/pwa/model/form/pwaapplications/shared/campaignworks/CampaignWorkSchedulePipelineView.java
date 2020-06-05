package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks;

import java.math.BigDecimal;
import java.math.RoundingMode;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;

/**
 * Summarised view of Pipeline due to be worked on in scheduled campaign of work.
 */
public class CampaignWorkSchedulePipelineView {

  private final String pipelineNumber;
  private final String fromLocation;
  private final String toLocation;
  private final String metreLength;
  private final String pipelineTypeDisplayName;

  private CampaignWorkSchedulePipelineView(String pipelineNumber,
                                           String fromLocation,
                                           String toLocation,
                                           BigDecimal pipelineLength,
                                           PipelineType pipelineType) {
    this.pipelineNumber = pipelineNumber;
    this.toLocation = toLocation;
    this.fromLocation = fromLocation;
    this.metreLength = pipelineLength
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString() + "m";
    this.pipelineTypeDisplayName = pipelineType.getDisplayName();
  }

  // package private static constructor and private standard constructor to ensure only created by package campaign works code for now
  static CampaignWorkSchedulePipelineView fromPipelineOverview(PipelineOverview pipelineOverview) {
    return new CampaignWorkSchedulePipelineView(
        pipelineOverview.getPipelineNumber(),
        pipelineOverview.getFromLocation(),
        pipelineOverview.getToLocation(),
        pipelineOverview.getLength(),
        pipelineOverview.getPipelineType()
    );
  }

  public String getPipelineNumber() {
    return pipelineNumber;
  }

  public String getToLocation() {
    return toLocation;
  }

  public String getFromLocation() {
    return fromLocation;
  }

  public String getMetreLength() {
    return metreLength;
  }

  public String getPipelineTypeDisplayName() {
    return pipelineTypeDisplayName;
  }
}

