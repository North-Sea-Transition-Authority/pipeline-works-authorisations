package uk.co.ogauthority.pwa.service.workarea;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;

public class PwaApplicationWorkAreaItem {

  private static final DateTimeFormatter WORK_AREA_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/YYYY");
  private static final DateTimeFormatter WORK_AREA_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm");

  private final int pwaApplicationId;

  private final String padReference;

  private final String masterPwaReference;

  private final String applicationType;

  private final String padStatus;

  private final Instant padStatusSetInstant;

  private final boolean tipFlag;

  private final String viewApplicationUrl;

  private final List<String> orderedFieldList;

  private final Instant proposedStartInstant;

  private final String projectName;


  public PwaApplicationWorkAreaItem(ApplicationDetailSearchItem applicationDetailSearchItem,
                                    Function<ApplicationDetailSearchItem, String> viewApplicationUrlProducer) {

    this.viewApplicationUrl = viewApplicationUrlProducer.apply(applicationDetailSearchItem);

    this.pwaApplicationId = applicationDetailSearchItem.getPwaApplicationId();
    this.padReference = applicationDetailSearchItem.getPadReference();
    this.masterPwaReference = applicationDetailSearchItem.getPwaReference();
    this.padStatus = applicationDetailSearchItem.getPadStatus().getDisplayName();
    this.applicationType = applicationDetailSearchItem.getApplicationType().getDisplayName();
    this.padStatusSetInstant = applicationDetailSearchItem.getPadStatusTimestamp();
    this.tipFlag = applicationDetailSearchItem.isTipFlag();

    this.orderedFieldList = applicationDetailSearchItem.getPadFields().stream()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());

    this.projectName = applicationDetailSearchItem.getPadProjectName();
    this.proposedStartInstant = applicationDetailSearchItem.getPadProposedStart();

  }

  public int getPwaApplicationId() {
    return pwaApplicationId;
  }

  public String getPadReference() {
    return padReference;
  }

  public String getMasterPwaReference() {
    return masterPwaReference;
  }

  public String getApplicationType() {
    return applicationType;
  }

  public String getPadStatus() {
    return padStatus;
  }

  public boolean getIsTipFlag() {
    return tipFlag;
  }

  public String getViewApplicationUrl() {
    return viewApplicationUrl;
  }

  public List<String> getOrderedFieldList() {
    return orderedFieldList;
  }

  public Instant getProposedStartInstant() {
    return proposedStartInstant;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getFormattedProposedStartDate() {
    return formatInstant(this.proposedStartInstant, WORK_AREA_DATE_FORMAT);
  }

  public String getFormattedStatusSetDatetime() {
    return formatInstant(this.padStatusSetInstant, WORK_AREA_DATETIME_FORMAT);
  }

  public Instant getPadStatusSetInstant() {
    return padStatusSetInstant;
  }

  private String formatInstant(Instant instant, DateTimeFormatter formatter) {
    if (instant == null) {
      return null;
    }
    return instant.atZone(ZoneId.systemDefault()).toLocalDateTime().format(formatter);
  }

}
