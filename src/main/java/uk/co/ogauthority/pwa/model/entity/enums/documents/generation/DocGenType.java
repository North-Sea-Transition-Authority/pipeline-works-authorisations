package uk.co.ogauthority.pwa.model.entity.enums.documents.generation;

import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;

public enum DocGenType {

  FULL(FileDocumentType.CONSENT_DOCUMENT),
  PREVIEW(FileDocumentType.CONSENT_PREVIEW);

  private final FileDocumentType fileDocumentType;

  DocGenType(FileDocumentType fileDocumentType) {
    this.fileDocumentType = fileDocumentType;
  }

  public FileDocumentType getFileDocumentType() {
    return fileDocumentType;
  }
}
