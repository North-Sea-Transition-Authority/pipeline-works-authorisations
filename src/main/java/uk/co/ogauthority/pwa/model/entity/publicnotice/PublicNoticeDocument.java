package uk.co.ogauthority.pwa.model.entity.publicnotice;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeDocumentType;

@Entity
@Table(name = "public_notice_documents")
public class PublicNoticeDocument {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "public_notice_id")
  @ManyToOne
  private PublicNotice publicNotice;

  private Integer version;

  @Enumerated(EnumType.STRING)
  private PublicNoticeDocumentType documentType;

  private String comments;

  private Instant createdTimestamp;

  public PublicNoticeDocument() {
  }

  public PublicNoticeDocument(PublicNotice publicNotice, Integer version,
                              PublicNoticeDocumentType documentType, Instant createdTimestamp) {
    this.publicNotice = publicNotice;
    this.version = version;
    this.documentType = documentType;
    this.createdTimestamp = createdTimestamp;
  }


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PublicNotice getPublicNotice() {
    return publicNotice;
  }

  public void setPublicNotice(PublicNotice publicNotice) {
    this.publicNotice = publicNotice;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public PublicNoticeDocumentType getDocumentType() {
    return documentType;
  }

  public void setDocumentType(PublicNoticeDocumentType documentType) {
    this.documentType = documentType;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public PublicNoticeDocument setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PublicNoticeDocument that = (PublicNoticeDocument) o;
    return Objects.equals(id, that.id)
        && Objects.equals(publicNotice, that.publicNotice)
        && Objects.equals(version, that.version)
        && documentType == that.documentType
        && Objects.equals(comments, that.comments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, publicNotice, version, documentType, comments);
  }
}
