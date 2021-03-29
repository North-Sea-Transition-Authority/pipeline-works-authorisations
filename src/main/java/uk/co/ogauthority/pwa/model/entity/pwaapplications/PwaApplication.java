package uk.co.ogauthority.pwa.model.entity.pwaapplications;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationDecision;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowSubject;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;

@Entity
@Table(name = "pwa_applications")
public class PwaApplication implements WorkflowSubject {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pwa_id")
  private MasterPwa masterPwa;

  @Enumerated(EnumType.STRING)
  private PwaApplicationType applicationType;

  private String appReference;

  private String consentReference;

  private Integer variationNo;

  @Enumerated(EnumType.STRING)
  private PwaApplicationDecision decision;

  private Instant decisionTimestamp;

  public PwaApplication() {
  }

  public PwaApplication(MasterPwa masterPwa, PwaApplicationType applicationType, Integer variationNo) {
    this.masterPwa = masterPwa;
    this.applicationType = applicationType;
    this.variationNo = variationNo;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public MasterPwa getMasterPwa() {
    return masterPwa;
  }

  public void setMasterPwa(MasterPwa masterPwa) {
    this.masterPwa = masterPwa;
  }

  public PwaApplicationType getApplicationType() {
    return applicationType;
  }

  public void setApplicationType(PwaApplicationType applicationType) {
    this.applicationType = applicationType;
  }

  public String getAppReference() {
    return appReference;
  }

  public void setAppReference(String appReference) {
    this.appReference = appReference;
  }

  public String getConsentReference() {
    return consentReference;
  }

  public void setConsentReference(String consentReference) {
    this.consentReference = consentReference;
  }

  public Integer getVariationNo() {
    return variationNo;
  }

  public void setVariationNo(Integer variationNo) {
    this.variationNo = variationNo;
  }

  public Optional<PwaApplicationDecision> getDecision() {
    return Optional.ofNullable(decision);
  }

  public void setDecision(PwaApplicationDecision decision) {
    this.decision = decision;
  }

  public Optional<Instant> getDecisionTimestamp() {
    return Optional.ofNullable(decisionTimestamp);
  }

  public void setDecisionTimestamp(Instant decisionTimestamp) {
    this.decisionTimestamp = decisionTimestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PwaApplication that = (PwaApplication) o;
    return Objects.equals(id, that.id)
        && Objects.equals(masterPwa, that.masterPwa)
        && Objects.equals(applicationType, that.applicationType)
        && Objects.equals(appReference, that.appReference)
        && Objects.equals(consentReference, that.consentReference)
        && Objects.equals(variationNo, that.variationNo)
        && Objects.equals(decision, that.decision)
        && Objects.equals(decisionTimestamp, that.decisionTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, masterPwa, applicationType, appReference, consentReference, variationNo, decision,
        decisionTimestamp);
  }

  @Override
  public Integer getBusinessKey() {
    return getId();
  }

  @Override
  public WorkflowType getWorkflowType() {
    return WorkflowType.PWA_APPLICATION;
  }
}
