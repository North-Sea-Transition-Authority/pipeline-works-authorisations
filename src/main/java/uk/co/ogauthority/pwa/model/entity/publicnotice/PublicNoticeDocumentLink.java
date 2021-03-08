package uk.co.ogauthority.pwa.model.entity.publicnotice;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;

@Entity
@Table(name = "public_notice_document_links")
public class PublicNoticeDocumentLink {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "public_notice_document_id")
  private PublicNoticeDocument publicNoticeDocument;

  @OneToOne
  @JoinColumn(name = "af_id")
  private AppFile appFile;

  public PublicNoticeDocumentLink() {
  }

  public PublicNoticeDocumentLink(
      PublicNoticeDocument publicNoticeDocument, AppFile appFile) {
    this.publicNoticeDocument = publicNoticeDocument;
    this.appFile = appFile;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PublicNoticeDocument getPublicNoticeDocument() {
    return publicNoticeDocument;
  }

  public void setPublicNoticeDocument(
      PublicNoticeDocument publicNoticeDocument) {
    this.publicNoticeDocument = publicNoticeDocument;
  }

  public AppFile getAppFile() {
    return appFile;
  }

  public void setAppFile(AppFile appFile) {
    this.appFile = appFile;
  }



}
