package uk.co.ogauthority.pwa.model.entity.pipelines;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;

@Entity
@Table(name = "pipeline_detail_migr_huoo_data")
public class PipelineDetailMigrationHuooData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pipeline_detail_id")
  private PipelineDetail pipelineDetail;

  @Column(name = "organisation_role")
  @Enumerated(EnumType.STRING)
  private HuooRole huooRole;

  private Integer organisationUnitId;

  private String manualOrganisationName;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PipelineDetail getPipelineDetail() {
    return pipelineDetail;
  }

  public void setPipelineDetail(PipelineDetail pipelineDetail) {
    this.pipelineDetail = pipelineDetail;
  }

  public HuooRole getHuooRole() {
    return huooRole;
  }

  public void setHuooRole(HuooRole huooRole) {
    this.huooRole = huooRole;
  }

  public Integer getOrganisationUnitId() {
    return organisationUnitId;
  }

  public void setOrganisationUnitId(Integer organisationUnitId) {
    this.organisationUnitId = organisationUnitId;
  }

  public String getManualOrganisationName() {
    return manualOrganisationName;
  }

  public void setManualOrganisationName(String manualOrganisationName) {
    this.manualOrganisationName = manualOrganisationName;
  }
}
