package uk.co.ogauthority.pwa.model.entity.pwaapplications.form;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity(name = "pad_fast_track_information")
public class PadFastTrack {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "application_detail_id")
  @OneToOne
  private PwaApplicationDetail pwaApplicationDetail;

  private Boolean avoidEnvironmentalDisaster;
  private Boolean savingBarrels;
  private Boolean projectPlanning;
  private Boolean hasOtherReason;

  private String environmentalDisasterReason;
  private String savingBarrelsReason;
  private String projectPlanningReason;
  private String otherReason;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

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
