package uk.co.ogauthority.pwa.model.entity.pwaapplications.form;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.PwaApplicationFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity
@Table(name = "pad_project_information_files")
public class PadProjectInformationFile implements PwaApplicationFile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne
  @JoinColumn(referencedColumnName = "id", name = "application_detail_id")
  private PwaApplicationDetail pwaApplicationDetail;

  // not entity mapped to avoid selecting file data when not needed
  private String fileId;
  private String description;

  @Enumerated(EnumType.STRING)
  private ApplicationFileLinkStatus fileLinkStatus;

  public PadProjectInformationFile() {
  }

  public PadProjectInformationFile(
      PwaApplicationDetail pwaApplicationDetail, String fileId, String description, ApplicationFileLinkStatus applicationFileLinkStatus) {
    this.pwaApplicationDetail = pwaApplicationDetail;
    this.fileId = fileId;
    this.description = description;
    this.fileLinkStatus = applicationFileLinkStatus;
  }

  public int getId() {
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

  @Override
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

  public ApplicationFileLinkStatus getFileLinkStatus() {
    return fileLinkStatus;
  }

  public void setFileLinkStatus(ApplicationFileLinkStatus fileLinkStatus) {
    this.fileLinkStatus = fileLinkStatus;
  }
}
