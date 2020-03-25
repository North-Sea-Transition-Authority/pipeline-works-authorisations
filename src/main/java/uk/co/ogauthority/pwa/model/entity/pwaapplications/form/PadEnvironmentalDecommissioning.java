package uk.co.ogauthority.pwa.model.entity.pwaapplications.form;

import java.time.Instant;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import uk.co.ogauthority.pwa.model.entity.converters.DecommissioningConditionConverter;
import uk.co.ogauthority.pwa.model.entity.converters.EnvironmentalConditionConverter;
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity(name = "pad_env_and_decom")
public class PadEnvironmentalDecommissioning {

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
  private String decommissioningPlans;

  @Convert(converter = EnvironmentalConditionConverter.class)
  private Set<EnvironmentalCondition> environmentalConditions;

  @Convert(converter = DecommissioningConditionConverter.class)
  private Set<DecommissioningCondition> decommissioningConditions;

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

  public String getDecommissioningPlans() {
    return decommissioningPlans;
  }

  public void setDecommissioningPlans(String decommissioningPlans) {
    this.decommissioningPlans = decommissioningPlans;
  }

  public Set<DecommissioningCondition> getDecommissioningConditions() {
    return decommissioningConditions;
  }

  public void setDecommissioningConditions(
      Set<DecommissioningCondition> decommissioningConditions) {
    this.decommissioningConditions = decommissioningConditions;
  }
}
