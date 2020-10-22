package uk.co.ogauthority.pwa.model.view.appprocessing.casehistory;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.util.DateUtils;

public class CaseHistoryItemView {

  private final String headerText;

  private final Instant dateTime;
  private final String dateTimeDisplay;

  private final String personLabelText;
  private final PersonId personId;
  private String personEmailLabel;

  // set after initial construction
  private String personName;
  private String personEmail;

  private final Map<String, String> dataItems;

  private List<UploadedFileView> uploadedFileViews;
  private String fileDownloadUrl;

  private CaseHistoryItemView(Builder builder) {
    this.headerText = builder.headerText;
    this.dateTime = builder.dateTime;
    this.dateTimeDisplay = DateUtils.formatDateTime(builder.dateTime);
    this.personLabelText = builder.personLabelText;
    this.personId = builder.personId;
    this.dataItems = builder.dataItems;
    this.uploadedFileViews = builder.uploadedFileViews;
    this.fileDownloadUrl = builder.fileDownloadUrl;
    this.personEmailLabel = builder.personEmailLabel;
  }

  public String getHeaderText() {
    return headerText;
  }

  public Instant getDateTime() {
    return dateTime;
  }

  public String getDateTimeDisplay() {
    return dateTimeDisplay;
  }

  public String getPersonLabelText() {
    return personLabelText;
  }

  public PersonId getPersonId() {
    return personId;
  }

  public String getPersonName() {
    return personName;
  }

  public void setPersonName(String personName) {
    this.personName = personName;
  }

  public String getPersonEmail() {
    return personEmail;
  }

  public void setPersonEmail(String personEmail) {
    this.personEmail = personEmail;
  }

  public String getPersonEmailLabel() {
    return personEmailLabel;
  }

  public void setPersonEmailLabel(String personEmailLabel) {
    this.personEmailLabel = personEmailLabel;
  }

  public Map<String, String> getDataItems() {
    return dataItems;
  }

  public List<UploadedFileView> getUploadedFileViews() {
    return uploadedFileViews;
  }

  public String getFileDownloadUrl() {
    return fileDownloadUrl;
  }

  /**
   * Use builder pattern for case note views due to large variability in how they might need to be constructed
   * and reasonably large number of optional/customisable attributes.
   */
  public static class Builder {

    //Required Params
    private String headerText;
    private Instant dateTime;
    private PersonId personId;
    private String personLabelText;

    // optional/customisable params
    private String personEmailLabel;
    private Map<String, String> dataItems;
    private List<UploadedFileView> uploadedFileViews;
    private String fileDownloadUrl;

    public Builder(String headerText,
                   Instant dateTime,
                   PersonId createdByPersonId) {
      this.headerText = headerText;
      this.dateTime = dateTime;
      this.personId = createdByPersonId;
      this.personLabelText = "Created by";
      this.uploadedFileViews = Collections.emptyList();
      this.dataItems = new LinkedHashMap<>();
    }

    public Builder addDataItem(String label, String value) {
      this.dataItems.put(label, value);
      return this;
    }

    public Builder setPersonLabelText(String personLabelText) {
      this.personLabelText = personLabelText;
      return this;
    }

    public Builder setUploadedFileViews(List<UploadedFileView> uploadedFileViews,
                                        String fileDownloadUrl) {
      this.uploadedFileViews = uploadedFileViews;
      this.fileDownloadUrl = fileDownloadUrl;
      return this;
    }

    public Builder setPersonEmailLabel(String personEmailLabel) {
      this.personEmailLabel = personEmailLabel;
      return this;
    }

    public CaseHistoryItemView build() {
      return new CaseHistoryItemView(this);
    }
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CaseHistoryItemView that = (CaseHistoryItemView) o;
    return Objects.equals(headerText, that.headerText)
        && Objects.equals(dateTime, that.dateTime)
        && Objects.equals(dateTimeDisplay, that.dateTimeDisplay)
        && Objects.equals(personLabelText, that.personLabelText)
        && Objects.equals(personId, that.personId)
        && Objects.equals(personEmailLabel, that.personEmailLabel)
        && Objects.equals(personName, that.personName)
        && Objects.equals(personEmail, that.personEmail)
        && Objects.equals(dataItems, that.dataItems)
        && Objects.equals(uploadedFileViews, that.uploadedFileViews)
        && Objects.equals(fileDownloadUrl, that.fileDownloadUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(headerText, dateTime, dateTimeDisplay, personLabelText, personId, personEmailLabel, personName,
        personEmail, dataItems, uploadedFileViews, fileDownloadUrl);
  }

  @Override
  public String toString() {
    return "CaseHistoryItemView{" +
        "headerText='" + headerText + '\'' +
        ", dateTime=" + dateTime +
        ", dateTimeDisplay='" + dateTimeDisplay + '\'' +
        ", personLabelText='" + personLabelText + '\'' +
        ", personId=" + personId +
        ", personEmailLabel='" + personEmailLabel + '\'' +
        ", personName='" + personName + '\'' +
        ", personEmail='" + personEmail + '\'' +
        ", dataItems=" + dataItems +
        ", uploadedFileViews=" + uploadedFileViews +
        ", fileDownloadUrl='" + fileDownloadUrl + '\'' +
        '}';
  }
}
