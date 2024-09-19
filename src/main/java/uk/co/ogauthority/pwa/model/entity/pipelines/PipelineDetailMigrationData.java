package uk.co.ogauthority.pwa.model.entity.pipelines;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "pipeline_detail_migration_data")
public class PipelineDetailMigrationData {

  @Id
  private Integer id;
  private String pipelineDetailId;
  private Instant commissionedDate;
  private Instant abandonedDate;
  private String fileReference;
  private String pipeMaterial;
  private String materialGrade;
  private String trenchDepth;
  private String systemIdentifier;
  private String psig;
  private String notes;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public String getPipelineDetailId() {
    return pipelineDetailId;
  }

  public void setPipelineDetailId(String pipelineDetailId) {
    this.pipelineDetailId = pipelineDetailId;
  }


  public Instant getCommissionedDate() {
    return commissionedDate;
  }

  public void setCommissionedDate(Instant commissionedDate) {
    this.commissionedDate = commissionedDate;
  }


  public Instant getAbandonedDate() {
    return abandonedDate;
  }

  public void setAbandonedDate(Instant abandonedDate) {
    this.abandonedDate = abandonedDate;
  }


  public String getFileReference() {
    return fileReference;
  }

  public void setFileReference(String fileReference) {
    this.fileReference = fileReference;
  }


  public String getPipeMaterial() {
    return pipeMaterial;
  }

  public void setPipeMaterial(String pipeMaterial) {
    this.pipeMaterial = pipeMaterial;
  }


  public String getMaterialGrade() {
    return materialGrade;
  }

  public void setMaterialGrade(String materialGrade) {
    this.materialGrade = materialGrade;
  }


  public String getTrenchDepth() {
    return trenchDepth;
  }

  public void setTrenchDepth(String trenchDepth) {
    this.trenchDepth = trenchDepth;
  }


  public String getSystemIdentifier() {
    return systemIdentifier;
  }

  public void setSystemIdentifier(String systemIdentifier) {
    this.systemIdentifier = systemIdentifier;
  }


  public String getPsig() {
    return psig;
  }

  public void setPsig(String psig) {
    this.psig = psig;
  }


  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

}
