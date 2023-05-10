package uk.co.ogauthority.pwa.domain.pwa.application.model;

import static uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationResourceType.*;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowSubject;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowType;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.OrganisationUnitIdConverter;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.service.documents.DocumentSource;

@Entity
@Table(name = "pwa_applications")
public class PwaApplication implements WorkflowSubject, DocumentSource {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pwa_id")
  private MasterPwa masterPwa;

  @Enumerated(EnumType.STRING)
  private PwaApplicationType applicationType;

  @Enumerated(EnumType.STRING)
  private PwaApplicationResourceType resourceType = PETROLEUM;

  private String appReference;

  private String consentReference;

  private Integer variationNo;

  @Enumerated(EnumType.STRING)
  private PwaApplicationDecision decision;

  private Instant decisionTimestamp;

  @Column(name = "app_created_timestamp")
  private Instant applicationCreatedTimestamp;

  @Basic // this annotation allows the Jpa metamodel to pick up the field, but leaves default behaviour intact.
  // Suitable as OrganisationUnitId just wraps a basic class.
  @Column(name = "applicant_ou_id")
  @Convert(converter = OrganisationUnitIdConverter.class)
  private OrganisationUnitId applicantOrganisationUnitId;

  public PwaApplication() {
  }

  public PwaApplication(MasterPwa masterPwa, PwaApplicationType applicationType, Integer variationNo) {
    this.masterPwa = masterPwa;
    this.applicationType = applicationType;
    this.variationNo = variationNo;
  }

  public PwaApplication(MasterPwa masterPwa,
                        PwaApplicationType applicationType,
                        PwaApplicationResourceType resourceType,
                        Integer variationNo) {
    this.masterPwa = masterPwa;
    this.applicationType = applicationType;
    this.resourceType = resourceType;
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

  public PwaApplicationResourceType getResourceType() {
    return resourceType;
  }

  public void setResourceType(PwaApplicationResourceType resourceType) {
    this.resourceType = resourceType;
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

  public Instant getApplicationCreatedTimestamp() {
    return applicationCreatedTimestamp;
  }

  public void setApplicationCreatedTimestamp(Instant applicationCreatedTimestamp) {
    this.applicationCreatedTimestamp = applicationCreatedTimestamp;
  }

  public OrganisationUnitId getApplicantOrganisationUnitId() {
    return applicantOrganisationUnitId;
  }

  public void setApplicantOrganisationUnitId(OrganisationUnitId applicantOrganisationUnit) {
    this.applicantOrganisationUnitId = applicantOrganisationUnit;
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
        && Objects.equals(decisionTimestamp, that.decisionTimestamp)
        && Objects.equals(applicantOrganisationUnitId, that.applicantOrganisationUnitId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, masterPwa, applicationType, appReference, consentReference, variationNo, decision,
        decisionTimestamp, applicantOrganisationUnitId);
  }

  @Override
  public Integer getBusinessKey() {
    return getId();
  }

  @Override
  public WorkflowType getWorkflowType() {
    return WorkflowType.PWA_APPLICATION;
  }

  @Override
  public Object getSource() {
    return this;
  }

  @Override
  public DocumentSpec getDocumentSpec() {
    return applicationType.getConsentDocumentSpec();
  }

}
