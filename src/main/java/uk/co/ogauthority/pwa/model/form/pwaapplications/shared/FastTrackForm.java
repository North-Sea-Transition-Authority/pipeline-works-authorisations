package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

import org.hibernate.validator.constraints.Length;

@SuppressWarnings("checkstyle:CommentsIndentation")
public class FastTrackForm {

  private Boolean avoidEnvironmentalDisaster;

  private Boolean savingBarrels;

  private Boolean projectPlanning;

  private Boolean hasOtherReason;

  @Length(max = 4000, message = "The environmental disaster section must be 4000 characters or less")
  private String environmentalDisasterReason;

  @Length(max = 4000, message = "The saving barrels section must be 4000 characters or less")
  private String savingBarrelsReason;

  @Length(max = 4000, message = "The project planning section must be 4000 characters or less")
  private String projectPlanningReason;

  @Length(max = 4000, message = "The other reasons section must be 4000 characters or less")
  private String otherReason;

  public Boolean getAvoidEnvironmentalDisaster() {
    return avoidEnvironmentalDisaster;
  }

  public void setAvoidEnvironmentalDisaster(Boolean avoidEnvironmentalDisaster) {
    this.avoidEnvironmentalDisaster = avoidEnvironmentalDisaster;
  }

  public Boolean getSavingBarrels() {
    return savingBarrels;
  }

  public void setSavingBarrels(Boolean savingBarrels) {
    this.savingBarrels = savingBarrels;
  }

  public Boolean getProjectPlanning() {
    return projectPlanning;
  }

  public void setProjectPlanning(Boolean projectPlanning) {
    this.projectPlanning = projectPlanning;
  }

  public Boolean getHasOtherReason() {
    return hasOtherReason;
  }

  public void setHasOtherReason(Boolean hasOtherReason) {
    this.hasOtherReason = hasOtherReason;
  }

  public String getEnvironmentalDisasterReason() {
    return environmentalDisasterReason;
  }

  public void setEnvironmentalDisasterReason(String environmentalDisasterReason) {
    this.environmentalDisasterReason = environmentalDisasterReason;
  }

  public String getSavingBarrelsReason() {
    return savingBarrelsReason;
  }

  public void setSavingBarrelsReason(String savingBarrelsReason) {
    this.savingBarrelsReason = savingBarrelsReason;
  }

  public String getProjectPlanningReason() {
    return projectPlanningReason;
  }

  public void setProjectPlanningReason(String projectPlanningReason) {
    this.projectPlanningReason = projectPlanningReason;
  }

  public String getOtherReason() {
    return otherReason;
  }

  public void setOtherReason(String otherReason) {
    this.otherReason = otherReason;
  }
}
