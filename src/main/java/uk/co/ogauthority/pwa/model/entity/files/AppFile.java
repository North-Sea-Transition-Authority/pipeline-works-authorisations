package uk.co.ogauthority.pwa.model.entity.files;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;

@Entity
@Table(name = "app_files")
public class AppFile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", name = "pa_id")
  private PwaApplication pwaApplication;

  // not entity mapped to avoid selecting file data when not needed
  private String fileId;

  private String description;

  @Enumerated(EnumType.STRING)
  private AppFilePurpose purpose;

  @Enumerated(EnumType.STRING)
  private ApplicationFileLinkStatus fileLinkStatus;

  public AppFile() {
  }

  public AppFile(PwaApplication pwaApplication, String fileId,
                 AppFilePurpose purpose,
                 ApplicationFileLinkStatus fileLinkStatus) {
    this.pwaApplication = pwaApplication;
    this.fileId = fileId;
    this.purpose = purpose;
    this.fileLinkStatus = fileLinkStatus;
  }

  public Integer getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public PwaApplication getPwaApplication() {
    return pwaApplication;
  }

  public void setPwaApplication(PwaApplication pwaApplication) {
    this.pwaApplication = pwaApplication;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public AppFilePurpose getPurpose() {
    return purpose;
  }

  public void setPurpose(AppFilePurpose purpose) {
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
    AppFile appFile = (AppFile) o;
    return Objects.equals(id, appFile.id)
        && Objects.equals(pwaApplication, appFile.pwaApplication)
        && Objects.equals(fileId, appFile.fileId)
        && Objects.equals(description, appFile.description)
        && purpose == appFile.purpose
        && fileLinkStatus == appFile.fileLinkStatus;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pwaApplication, fileId, description, purpose, fileLinkStatus);
  }
}
