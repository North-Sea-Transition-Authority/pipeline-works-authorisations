package uk.co.ogauthority.pwa.features.application.files;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity
@Table(name = "pad_files")
public class PadFile implements ChildEntity<Integer, PwaApplicationDetail> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", name = "pad_id")
  private PwaApplicationDetail pwaApplicationDetail;

  // not entity mapped to avoid selecting file data when not needed
  private String fileId;

  private String description;

  @Enumerated(EnumType.STRING)
  private ApplicationDetailFilePurpose purpose;

  @Enumerated(EnumType.STRING)
  private ApplicationFileLinkStatus fileLinkStatus;

  public PadFile() {
  }

  public PadFile(PwaApplicationDetail pwaApplicationDetail, String fileId,
                 ApplicationDetailFilePurpose purpose,
                 ApplicationFileLinkStatus fileLinkStatus) {
    this.pwaApplicationDetail = pwaApplicationDetail;
    this.fileId = fileId;
    this.purpose = purpose;
    this.fileLinkStatus = fileLinkStatus;
  }

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

  public void setId(int id) {
    this.id = id;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public UUID getFileId() {
    return UUID.fromString(fileId);
  }

  public void setFileId(UUID fileId) {
    this.fileId = String.valueOf(fileId);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ApplicationDetailFilePurpose getPurpose() {
    return purpose;
  }

  public void setPurpose(ApplicationDetailFilePurpose purpose) {
    this.purpose = purpose;
  }

  public ApplicationFileLinkStatus getFileLinkStatus() {
    return fileLinkStatus;
  }

  public void setFileLinkStatus(ApplicationFileLinkStatus fileLinkStatus) {
    this.fileLinkStatus = fileLinkStatus;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadFile padFile = (PadFile) o;
    return id == padFile.id
        && Objects.equals(pwaApplicationDetail, padFile.pwaApplicationDetail)
        && Objects.equals(fileId, padFile.fileId)
        && Objects.equals(description, padFile.description)
        && purpose == padFile.purpose
        && fileLinkStatus == padFile.fileLinkStatus;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pwaApplicationDetail, fileId, description, purpose, fileLinkStatus);
  }
}
