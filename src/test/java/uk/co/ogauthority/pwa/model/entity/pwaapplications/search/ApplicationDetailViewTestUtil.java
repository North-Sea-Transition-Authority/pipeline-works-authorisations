package uk.co.ogauthority.pwa.model.entity.pwaapplications.search;

import java.time.Instant;
import java.util.List;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.enums.PwaResourceType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

public class ApplicationDetailViewTestUtil {

  private ApplicationDetailViewTestUtil() {
    throw new UnsupportedOperationException("No test util for you!");
  }


  public static ApplicationDetailView createDetailView(int masterPwaId,
                                                       PwaApplicationType applicationType,
                                                       int appId,
                                                       int appDetailId,
                                                       int versionNo,
                                                       boolean tipFlag,
                                                       Instant submittedTimestamp,
                                                       PwaApplicationStatus pwaApplicationStatus,
                                                       Instant confirmedSatisfactoryInstant) {

    var detailView = new ApplicationDetailView();
    detailView.setTipFlag(tipFlag);
    detailView.setPwaApplicationId(appId);
    detailView.setPwaApplicationDetailId(appDetailId);
    detailView.setVersionNo(versionNo);
    detailView.setPadSubmittedTimestamp(submittedTimestamp);
    detailView.setPwaId(masterPwaId);
    detailView.setPadStatus(pwaApplicationStatus);
    detailView.setApplicationType(applicationType);
    detailView.setPadConfirmedSatisfactoryTimestamp(confirmedSatisfactoryInstant);
    return detailView;
  }


  public static ApplicationDetailView createDraftDetailView(int masterPwaId,
                                                            PwaApplicationType applicationType,
                                                            int appId,
                                                            int appDetailId,
                                                            int versionNo,
                                                            boolean tipFlag) {

    return createDetailView(
        masterPwaId,
        applicationType,
        appId,
        appDetailId,
        versionNo,
        tipFlag,
        null,
        PwaApplicationStatus.DRAFT,
        null
    );
  }

  public static ApplicationDetailView createSubmittedReviewDetailView(int masterPwaId,
                                                                      PwaApplicationType applicationType,
                                                                      int appId,
                                                                      int appDetailId,
                                                                      int versionNo,
                                                                      boolean tipFlag,
                                                                      Instant submittedInstant,
                                                                      Instant confirmedSatisfactoryInstant) {

    return createDetailView(
        masterPwaId,
        applicationType,
        appId,
        appDetailId,
        versionNo,
        tipFlag,
        submittedInstant,
        PwaApplicationStatus.CASE_OFFICER_REVIEW,
        confirmedSatisfactoryInstant
    );
  }

  public static ApplicationDetailView createGenericDetailView() {

    var applicationDetailView = new ApplicationDetailView();
    applicationDetailView.setPadReference("PA/5/6");
    applicationDetailView.setApplicationType(PwaApplicationType.CAT_1_VARIATION);
    applicationDetailView.setCaseOfficerPersonId(1);
    applicationDetailView.setCaseOfficerName("Case Officer X");
    applicationDetailView.setSubmittedAsFastTrackFlag(true);
    applicationDetailView.setPadProposedStart(Instant.now());
    applicationDetailView.setResourceType(PwaResourceType.PETROLEUM);
    applicationDetailView.setPadFields(List.of("CAPTAIN", "PENGUIN"));
    applicationDetailView.setPadHolderNameList(List.of("ROYAL DUTCH SHELL"));
    applicationDetailView.setPwaHolderNameList(List.of("ROYAL DUTCH SHELL"));
    applicationDetailView.setVersionNo(1);

    return applicationDetailView;

  }

}
