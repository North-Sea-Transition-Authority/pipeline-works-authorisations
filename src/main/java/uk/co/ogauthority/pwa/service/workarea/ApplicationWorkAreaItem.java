package uk.co.ogauthority.pwa.service.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.controller.ApplicationLandingPageRouterController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationDisplayUtils;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.WorkAreaUtils;

/**
 * An abstract class grouping common work area attributes linked to PWA applications.
 */
public abstract class ApplicationWorkAreaItem {

  public static final String STATUS_LABEL = "Status";

  public static final String DEFAULT_APP_STATUS_SET_LABEL = "Status set";

  public static final String CASE_OFFICER_DISPLAY_LABEL = "Case officer";

  private final int pwaApplicationId;

  private final String pwaApplicationReference;

  private final String masterPwaReference;

  private final PwaApplicationType applicationType;

  private final PwaApplicationStatus applicationStatus;

  private final PwaResourceType resourceType;

  private final Instant padStatusSetInstant;

  private final boolean tipFlag;

  private final String projectName;

  private final Instant proposedStartInstant;

  private final List<String> orderedFieldList;

  private final List<String> orderedPwaHolderList;

  private final List<String> orderedPadHolderList;

  private final boolean submittedAsFastTrackFlag;

  private final Instant initialReviewApprovedInstant;

  private PersonId caseOfficerPersonId;
  private String caseOfficerName;

  private final boolean openUpdateRequestFlag;
  private final Instant openUpdateDeadlineTimestamp;

  private final String accessUrl;

  private final PublicNoticeStatus publicNoticeStatus;

  /**
   * Prefer other super constructor as it applies default access url logic.
   */
  private ApplicationWorkAreaItem(ApplicationDetailItemView applicationDetailItemView,
                                 String accessUrl) {
    this.pwaApplicationId = applicationDetailItemView.getPwaApplicationId();
    this.pwaApplicationReference = applicationDetailItemView.getPadReference();
    this.masterPwaReference = applicationDetailItemView.getPwaReference();
    this.applicationType = applicationDetailItemView.getApplicationType();
    this.resourceType = applicationDetailItemView.getResourceType();
    this.applicationStatus = applicationDetailItemView.getPadStatus();
    this.padStatusSetInstant = applicationDetailItemView.getPadStatusTimestamp();
    this.tipFlag = applicationDetailItemView.isTipFlag();
    this.projectName = applicationDetailItemView.getPadProjectName();
    this.proposedStartInstant = applicationDetailItemView.getPadProposedStart();
    this.orderedFieldList = applicationDetailItemView.getPadFields().stream()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());

    this.orderedPwaHolderList = applicationDetailItemView.getPwaHolderNameList().stream()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());

    this.orderedPadHolderList = applicationDetailItemView.getPadHolderNameList().stream()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());

    this.submittedAsFastTrackFlag = applicationDetailItemView.wasSubmittedAsFastTrack();
    this.initialReviewApprovedInstant = applicationDetailItemView.getPadInitialReviewApprovedTimestamp();
    this.accessUrl = accessUrl;
    this.publicNoticeStatus = applicationDetailItemView.getPublicNoticeStatus();

    if (applicationDetailItemView.getCaseOfficerPersonId() != null) {
      this.caseOfficerPersonId = new PersonId(applicationDetailItemView.getCaseOfficerPersonId());
      this.caseOfficerName = applicationDetailItemView.getCaseOfficerName();
    }

    this.openUpdateRequestFlag = applicationDetailItemView.getOpenUpdateRequestFlag();
    this.openUpdateDeadlineTimestamp = applicationDetailItemView.getOpenUpdateDeadlineTimestamp();

  }

  /**
   * This is the preferred super constructor as it applies default access url logic.
   */
  public ApplicationWorkAreaItem(ApplicationDetailItemView applicationDetailItemView) {
    this(
        applicationDetailItemView,
        ReverseRouter.route(on(ApplicationLandingPageRouterController.class).route(applicationDetailItemView.getPwaApplicationId(), null))
    );
  }

  public int getPwaApplicationId() {
    return pwaApplicationId;
  }

  public String getApplicationReference() {
    return pwaApplicationReference;
  }

  public String getMasterPwaReference() {
    return masterPwaReference;
  }

  public String getApplicationTypeDisplay() {
    return applicationType.getDisplayName();
  }

  public String getApplicationStatusDisplay() {
    return applicationStatus.getDisplayName();
  }

  public String getFormattedStatusSetDatetime() {
    return Optional.ofNullable(this.padStatusSetInstant)
        .map(WorkAreaUtils.WORK_AREA_DATETIME_FORMAT::format)
        .orElse(null);
  }

  public boolean isTipFlag() {
    return tipFlag;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getProposedStartDateDisplay() {
    return Optional.ofNullable(this.proposedStartInstant)
        .map(WorkAreaUtils.WORK_AREA_DATE_FORMAT::format)
        .orElse(null);
  }

  public List<String> getOrderedFieldList() {
    return orderedFieldList;
  }

  public String getAccessUrl() {
    return accessUrl;
  }

  public boolean wasSubmittedAsFastTrack() {
    return submittedAsFastTrackFlag;
  }

  public boolean isFastTrackAccepted() {
    return submittedAsFastTrackFlag && initialReviewApprovedInstant != null;
  }

  public String getFastTrackLabelText() {

    if (!submittedAsFastTrackFlag) {
      return null;
    }

    return isFastTrackAccepted()
        ? ApplicationTask.FAST_TRACK.getDisplayName() + " accepted"
        : ApplicationTask.FAST_TRACK.getDisplayName();

  }

  public PersonId getCaseOfficerPersonId() {
    return caseOfficerPersonId;
  }

  public String getCaseOfficerName() {
    return caseOfficerName;
  }

  public PublicNoticeStatus getPublicNoticeStatus() {
    return publicNoticeStatus;
  }

  /**
   * Provide a default implementation for the application column that can be overridden if required.
   */
  public List<WorkAreaColumnItemView> getApplicationColumn() {

    var columnItemList = new ArrayList<WorkAreaColumnItemView>();

    columnItemList.add(
        WorkAreaColumnItemView.createLinkItem(
            this.pwaApplicationReference,
            this.accessUrl
        ));

    columnItemList.add(
        WorkAreaColumnItemView.createTagItem(
            WorkAreaColumnItemView.TagType.NONE,
            PwaApplicationDisplayUtils.getApplicationTypeDisplay(applicationType, resourceType)
        ));

    if (this.applicationType != PwaApplicationType.INITIAL || this.applicationStatus == PwaApplicationStatus.COMPLETE) {
      columnItemList.add(
          WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, this.masterPwaReference)
      );
    }

    if (this.openUpdateRequestFlag) {

      var periodTillUpdateDue = Period.between(LocalDate.now(), DateUtils.instantToLocalDate(openUpdateDeadlineTimestamp));
      String updateDueTimeText;
      if (periodTillUpdateDue.isNegative()) {
        updateDueTimeText = "UPDATE OVERDUE";
      } else if (periodTillUpdateDue.isZero()) {
        updateDueTimeText = "UPDATE DUE TODAY";
      } else {
        updateDueTimeText = "UPDATE DUE IN " + periodTillUpdateDue.getDays() + " DAYS";
      }

      columnItemList.add(WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.DEFAULT, updateDueTimeText));
    }

    if (PublicNoticeStatus.APPLICANT_UPDATE.equals(this.publicNoticeStatus)) {
      columnItemList.add(WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.DEFAULT, "PUBLIC NOTICE UPDATE REQUESTED"));
    }

    return columnItemList;

  }

  /**
   * Provide a default implementation for the Holder column that can be overridden if required.
   */
  public List<WorkAreaColumnItemView> getHolderColumn() {

    var columnItemList = new ArrayList<WorkAreaColumnItemView>();
    // When INITIAL type use app info, otherwise always use consent model info.
    if (PwaApplicationType.INITIAL.equals(this.applicationType)) {
      // suppress simplification warning so ordering intent is clear
      //noinspection SimplifyStreamApiCallChains
      this.orderedPadHolderList.stream().forEachOrdered(holderName -> columnItemList.add(
          WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, holderName)
      ));

    } else {
      // suppress simplification warning so ordering intent is clear
      //noinspection SimplifyStreamApiCallChains
      this.orderedPwaHolderList.stream().forEachOrdered(holderName -> columnItemList.add(
          WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, holderName)
      ));
    }

    return columnItemList;

  }

  /**
   * Provide a default implementation for the summary column that can be overridden if required.
   */
  public List<WorkAreaColumnItemView> getSummaryColumn() {

    var columnItemList = new ArrayList<WorkAreaColumnItemView>();

    columnItemList.add(WorkAreaColumnItemView.createLabelledItem("Project name", this.projectName));
    columnItemList.add(
        WorkAreaColumnItemView.createLabelledItem("Proposed start date", this.getProposedStartDateDisplay()));

    if (!this.orderedFieldList.isEmpty()) {
      columnItemList.add(
          WorkAreaColumnItemView.createLabelledItem(
              "Fields",
              StringUtils.join(this.orderedFieldList, ", ")
          ));
    }

    return columnItemList;

  }

  protected Optional<WorkAreaColumnItemView> createFastTrackColumnItem() {
    if (this.wasSubmittedAsFastTrack()) {
      return Optional.of(
          WorkAreaColumnItemView.createTagItem(
              this.isFastTrackAccepted() ? WorkAreaColumnItemView.TagType.SUCCESS : WorkAreaColumnItemView.TagType.DANGER,
              this.getFastTrackLabelText()
          )
      );
    }
    return Optional.empty();

  }

  /**
   * Application status column must be implemented by each ApplicationWorkAreaItem class. Contains details
   * which is suitable for the type in which the ApplicationWorkAreItem is being used.
   */
  public abstract List<WorkAreaColumnItemView> getApplicationStatusColumn();


}
