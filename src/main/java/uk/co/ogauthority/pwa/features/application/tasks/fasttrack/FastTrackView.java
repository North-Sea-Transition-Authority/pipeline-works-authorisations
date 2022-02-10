package uk.co.ogauthority.pwa.features.application.tasks.fasttrack;

import org.apache.commons.lang3.BooleanUtils;

public class FastTrackView {

  private final Boolean avoidEnvironmentalDisaster;
  private final Boolean savingBarrels;
  private final Boolean projectPlanning;
  private final Boolean hasOtherReason;

  private final String environmentalDisasterReason;
  private final String savingBarrelsReason;
  private final String projectPlanningReason;
  private final String otherReason;

  private final boolean fastTrackDataExists;


  public FastTrackView(Boolean avoidEnvironmentalDisaster, Boolean savingBarrels, Boolean projectPlanning,
                       Boolean hasOtherReason, String environmentalDisasterReason, String savingBarrelsReason,
                       String projectPlanningReason, String otherReason) {
    this.avoidEnvironmentalDisaster = avoidEnvironmentalDisaster;
    this.savingBarrels = savingBarrels;
    this.projectPlanning = projectPlanning;
    this.hasOtherReason = hasOtherReason;
    this.environmentalDisasterReason = environmentalDisasterReason;
    this.savingBarrelsReason = savingBarrelsReason;
    this.projectPlanningReason = projectPlanningReason;
    this.otherReason = otherReason;
    this.fastTrackDataExists = BooleanUtils.isTrue(avoidEnvironmentalDisaster) || BooleanUtils.isTrue(savingBarrels)
        || BooleanUtils.isTrue(projectPlanning) || BooleanUtils.isTrue(hasOtherReason);
  }


  public Boolean getAvoidEnvironmentalDisaster() {
    return avoidEnvironmentalDisaster;
  }

  public Boolean getSavingBarrels() {
    return savingBarrels;
  }

  public Boolean getProjectPlanning() {
    return projectPlanning;
  }

  public Boolean getHasOtherReason() {
    return hasOtherReason;
  }

  public String getEnvironmentalDisasterReason() {
    return environmentalDisasterReason;
  }

  public String getSavingBarrelsReason() {
    return savingBarrelsReason;
  }

  public String getProjectPlanningReason() {
    return projectPlanningReason;
  }

  public String getOtherReason() {
    return otherReason;
  }

  public boolean isFastTrackDataExists() {
    return fastTrackDataExists;
  }
}
