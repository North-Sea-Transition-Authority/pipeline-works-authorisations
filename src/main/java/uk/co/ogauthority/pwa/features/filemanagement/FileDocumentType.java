package uk.co.ogauthority.pwa.features.filemanagement;

import java.util.Optional;
import java.util.Set;

public enum FileDocumentType {
  //app files
  CASE_NOTES(),
  CONSENT_DOCUMENT(),
  CONSENT_PREVIEW(),
  CONSULTATION_RESPONSE(),
  PUBLIC_NOTICE(),

  //pad files
  ADMIRALTY_CHART(Set.of("bmp", "gif", "jpeg", "jpg", "png",  "tif", "tiff")),
  BLOCK_CROSSINGS(),
  CABLE_CROSSINGS(),
  CARBON_STORAGE_CROSSINGS(),
  DEPOSIT_DRAWINGS(Set.of("bmp", "gif", "jpeg", "jpg", "png",  "tif", "tiff")),
  LOCATION_DETAILS(),
  MEDIAN_LINE_CROSSING(),
  OPTIONS_TEMPLATE(),
  PARTNER_LETTERS(),
  PIPELINE_CROSSINGS(),
  PIPELINE_DRAWINGS(Set.of("bmp", "gif", "jpeg", "jpg", "png",  "tif", "tiff")),
  PROJECT_EXTENSION(Set.of("bmp", "gif", "jpeg", "jpg", "png",  "tif", "tiff")),
  PROJECT_INFORMATION(),
  SUPPLEMENTARY_DOCUMENTS(),
  UMBILICAL_CROSS_SECTION(Set.of("bmp", "gif", "jpeg", "jpg", "png",  "tif", "tiff"))
  ;

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
