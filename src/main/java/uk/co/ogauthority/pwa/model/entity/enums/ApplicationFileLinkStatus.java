package uk.co.ogauthority.pwa.model.entity.enums;

/**
 * Indicates if an uploaded file link has been saved officially or has only just been uploaded.
 */
public enum ApplicationFileLinkStatus {
  TEMPORARY, //Uploaded files which have not been actively "Saved" against a form page
  FULL, // Uploaded files which have been actively "saved" against a form page
  ALL // When querying based on link status, get both full and temporary linked files
}
