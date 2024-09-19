package uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.Instant;
import java.util.Set;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.datainfrastructure.DecommissioningConditionConverter;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.datainfrastructure.EnvironmentalConditionConverter;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity(name = "pad_env_and_decom")
public class PadEnvironmentalDecommissioning implements ChildEntity<Integer, PwaApplicationDetail> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_detail_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @Column(name = "trans_boundary_effect")
  private Boolean transboundaryEffect;
  private Instant emtSubmissionTimestamp;
  private Boolean emtHasSubmittedPermits;
  private String permitsSubmitted;
  private Boolean emtHasOutstandingPermits;
  private String permitsPendingSubmission;

  @Convert(converter = EnvironmentalConditionConverter.class)
  private Set<EnvironmentalCondition> environmentalConditions;

  @Convert(converter = DecommissioningConditionConverter.class)
  private Set<DecommissioningCondition> decommissioningConditions;

  //ChildEntity methods
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PwaApplicationDetail parentEntity) {
    setPwaApplicationDetail(parentEntity);
  }

  @Override
  public PwaApplicationDetail getParent() {
    return getPwaApplicationDetail();
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

  public Boolean getTransboundaryEffect() {
    return transboundaryEffect;
  }

  public void setTransboundaryEffect(Boolean transboundaryEffect) {
    this.transboundaryEffect = transboundaryEffect;
  }

  public Instant getEmtSubmissionTimestamp() {
    return emtSubmissionTimestamp;
  }

  public void setEmtSubmissionTimestamp(Instant emtSubmissionTimestamp) {
    this.emtSubmissionTimestamp = emtSubmissionTimestamp;
  }

  public Boolean getEmtHasSubmittedPermits() {
    return emtHasSubmittedPermits;
  }

  public void setEmtHasSubmittedPermits(Boolean emtHasSubmittedPermits) {
    this.emtHasSubmittedPermits = emtHasSubmittedPermits;
  }

  public String getPermitsSubmitted() {
    return permitsSubmitted;
  }

  public void setPermitsSubmitted(String permitsSubmitted) {
    this.permitsSubmitted = permitsSubmitted;
  }

  public Boolean getEmtHasOutstandingPermits() {
    return emtHasOutstandingPermits;
  }

  public void setEmtHasOutstandingPermits(Boolean emtHasOutstandingPermits) {
    this.emtHasOutstandingPermits = emtHasOutstandingPermits;
  }

  public String getPermitsPendingSubmission() {
    return permitsPendingSubmission;
  }

  public void setPermitsPendingSubmission(String permitsPendingSubmission) {
    this.permitsPendingSubmission = permitsPendingSubmission;
  }

  public Set<EnvironmentalCondition> getEnvironmentalConditions() {
    return environmentalConditions;
  }

  public void setEnvironmentalConditions(
      Set<EnvironmentalCondition> environmentalConditions) {
    this.environmentalConditions = environmentalConditions;
  }

  public Set<DecommissioningCondition> getDecommissioningConditions() {
    return decommissioningConditions;
  }

  public void setDecommissioningConditions(
      Set<DecommissioningCondition> decommissioningConditions) {
    this.decommissioningConditions = decommissioningConditions;
  }
}
