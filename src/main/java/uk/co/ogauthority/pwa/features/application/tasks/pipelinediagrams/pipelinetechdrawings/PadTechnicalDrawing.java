package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;
import uk.co.ogauthority.pwa.service.entitycopier.ParentEntity;

@Entity
@Table(name = "pad_technical_drawings")
public class PadTechnicalDrawing implements ChildEntity<Integer, PwaApplicationDetail>, ParentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pad_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @ManyToOne
  @JoinColumn(name = "pf_id")
  private PadFile file;

  private String reference;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PadTechnicalDrawing() {
  }

  public PadTechnicalDrawing(Integer id,
                             PwaApplicationDetail pwaApplicationDetail,
                             PadFile file, String reference) {
    this.id = id;
    this.pwaApplicationDetail = pwaApplicationDetail;
    this.file = file;
    this.reference = reference;
  }

  //ParentEntity Methods
  @Override
  public Object getIdAsParent() {
    return this.id;
  }

  //ChildEntity Methods
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

  // generated methods

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public PadFile getFile() {
    return file;
  }

  public UUID getFileId() {
    return file.getFileId();
  }

  public void setFile(PadFile file) {
    this.file = file;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getReference() {
    return reference;
  }

  public String getFileDescription() {
    return file.getDescription();
  }

}
