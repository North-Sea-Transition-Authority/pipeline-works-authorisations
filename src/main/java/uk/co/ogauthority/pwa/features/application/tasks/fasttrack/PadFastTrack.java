package uk.co.ogauthority.pwa.features.application.tasks.fasttrack;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity(name = "pad_fast_track_information")
public class PadFastTrack implements ChildEntity<Integer, PwaApplicationDetail> {

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

  //ChildEntity methods
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PwaApplicationDetail parentEntity) {
    this.pwaApplicationDetail = parentEntity;
  }

  @Override
  public PwaApplicationDetail getParent() {
    return this.pwaApplicationDetail;
  }

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
