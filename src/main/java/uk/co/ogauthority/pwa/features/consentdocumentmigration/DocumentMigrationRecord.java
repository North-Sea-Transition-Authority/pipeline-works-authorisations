package uk.co.ogauthority.pwa.features.consentdocumentmigration;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "consent_document_migration_progress")
public class DocumentMigrationRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private String filename;
  private String pwaReference;
  private String fieldName;
  private String consentDoc;
  private String consentDate;
  private String consentType;
  private String incorrectPwaReference;
  private String action;
  private Boolean fileLocated;
  private Boolean destinationRecordExists;
  private Boolean migrationSuccessful;


  public DocumentMigrationRecord() {
    fileLocated = false;
    destinationRecordExists = false;
    migrationSuccessful = false;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getPwaReference() {
    return pwaReference;
  }

  public void setPwaReference(String pwaReference) {
    this.pwaReference = pwaReference;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getConsentDoc() {
    return consentDoc;
  }

  public void setConsentDoc(String consentDoc) {
    this.consentDoc = consentDoc;
  }

  public String getConsentDate() {
    return consentDate;
  }

  public void setConsentDate(String consentDate) {
    this.consentDate = consentDate;
  }

  public String getConsentType() {
    return consentType;
  }

  public void setConsentType(String consentType) {
    this.consentType = consentType;
  }

  public String getIncorrectPwaReference() {
    return incorrectPwaReference;
  }

  public void setIncorrectPwaReference(String incorrectPwaReference) {
    this.incorrectPwaReference = incorrectPwaReference;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public Boolean getFileLocated() {
    return fileLocated;
  }

  public void setFileLocated(Boolean fileLocated) {
    this.fileLocated = fileLocated;
  }

  public Boolean getDestinationRecordExists() {
    return destinationRecordExists;
  }

  public void setDestinationRecordExists(Boolean destinationRecordExists) {
    this.destinationRecordExists = destinationRecordExists;
  }

  public Boolean getMigrationSuccessful() {
    return migrationSuccessful;
  }

  public void setMigrationSuccessful(Boolean migrationSuccessful) {
    this.migrationSuccessful = migrationSuccessful;
  }
}
