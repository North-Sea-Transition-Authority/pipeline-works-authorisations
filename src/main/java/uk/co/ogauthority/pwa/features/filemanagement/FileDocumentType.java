package uk.co.ogauthority.pwa.features.filemanagement;

import java.util.Optional;
import java.util.Set;

public enum FileDocumentType {

  PROJECT_LAYOUT();

  private final Set<String> allowedExtensions;

  FileDocumentType() {
    this.allowedExtensions = null;
  }

  FileDocumentType(Set<String> allowedExtensions) {
    this.allowedExtensions = allowedExtensions;
  }

  public Optional<Set<String>> getAllowedExtensions() {
    return Optional.ofNullable(allowedExtensions);
  }
}
