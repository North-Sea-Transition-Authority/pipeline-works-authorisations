package uk.co.ogauthority.pwa.model.entity.pwaapplications;

import java.time.Instant;
import java.util.Optional;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwa;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationDecision;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@Entity(name = "pwa_applications")
public class PwaApplication {

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
}
