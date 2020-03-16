package uk.co.ogauthority.pwa.model.entity.pwaapplications.initial;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity(name = "pad_data")
public class PadData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "application_detail_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @Column(name = "trans_boundary_effect")
  private Boolean transboundaryEffect;
  private Instant emtSubmissionTimestamp;
  private Boolean emtHasSubmittedPermits;
  private String permitsSubmitted;
  private Boolean emtHasOutstandingPermits;
  private String permitsPendingSubmission;
  private Boolean dischargeFundsAvailable;
  private Boolean acceptsOpolLiability;
  private String decommissioningPlans;
  private Boolean acceptsEolRegulations;
  private Boolean acceptsEolRemoval;
  private Boolean acceptsRemovalProposal;

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

  public Boolean getDischargeFundsAvailable() {
    return dischargeFundsAvailable;
  }

  public void setDischargeFundsAvailable(Boolean dischargeFundsAvailable) {
    this.dischargeFundsAvailable = dischargeFundsAvailable;
  }

  public Boolean getAcceptsOpolLiability() {
    return acceptsOpolLiability;
  }

  public void setAcceptsOpolLiability(Boolean acceptsOpolLiability) {
    this.acceptsOpolLiability = acceptsOpolLiability;
  }

  public String getDecommissioningPlans() {
    return decommissioningPlans;
  }

  public void setDecommissioningPlans(String decommissioningPlans) {
    this.decommissioningPlans = decommissioningPlans;
  }

  public Boolean getAcceptsEolRegulations() {
    return acceptsEolRegulations;
  }

  public void setAcceptsEolRegulations(Boolean acceptsEolRegulations) {
    this.acceptsEolRegulations = acceptsEolRegulations;
  }

  public Boolean getAcceptsEolRemoval() {
    return acceptsEolRemoval;
  }

  public void setAcceptsEolRemoval(Boolean acceptsEolRemoval) {
    this.acceptsEolRemoval = acceptsEolRemoval;
  }

  public Boolean getAcceptsRemovalProposal() {
    return acceptsRemovalProposal;
  }

  public void setAcceptsRemovalProposal(Boolean acceptsRemovalProposal) {
    this.acceptsRemovalProposal = acceptsRemovalProposal;
  }
}
