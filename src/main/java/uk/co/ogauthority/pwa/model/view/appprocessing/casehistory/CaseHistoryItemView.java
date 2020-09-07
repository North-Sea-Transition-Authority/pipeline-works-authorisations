package uk.co.ogauthority.pwa.model.view.appprocessing.casehistory;

import java.time.Instant;
import java.util.Map;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.util.DateUtils;

public class CaseHistoryItemView {

  private final String headerText;

  private final Instant dateTime;
  private final String dateTimeDisplay;

  private final String personLabelText;
  private final PersonId personId;
  private String personName;

  private final Map<String, String> dataItems;

  public CaseHistoryItemView(String headerText,
                             Instant dateTime,
                             String personLabelText,
                             PersonId personId,
                             Map<String, String> dataItems) {
    this.headerText = headerText;
    this.dateTime = dateTime;
    this.dateTimeDisplay = DateUtils.formatDateTime(dateTime);
    this.personLabelText = personLabelText;
    this.personId = personId;
    this.dataItems = dataItems;
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

  public Map<String, String> getDataItems() {
    return dataItems;
  }

}
