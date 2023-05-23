package uk.co.ogauthority.pwa.service.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;
import uk.co.ogauthority.pwa.controller.ApplicationLandingPageRouterController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationDisplayUtils;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

public class ApplicationWorkAreaItemTestUtil {


  public static void test_getSummaryColumn_whenFieldsExist(ApplicationDetailItemView applicationDetailSearchItem,
                                                           Function<ApplicationDetailItemView, ApplicationWorkAreaItem> workAreaItemFunction) {

    applicationDetailSearchItem.setPadFields(List.of("FIELD 1", "FIELD 2"));

    var applicationWorkAreaItem = workAreaItemFunction.apply(applicationDetailSearchItem);

    assertThat(applicationWorkAreaItem.getSummaryColumn()).containsExactly(
        WorkAreaColumnItemView.createLabelledItem(
            "Project name", applicationWorkAreaItem.getProjectName()),
        WorkAreaColumnItemView.createLabelledItem(
            "Proposed start date",
            applicationWorkAreaItem.getProposedStartDateDisplay()),
        WorkAreaColumnItemView.createLabelledItem(
            "Fields",
            "FIELD 1, FIELD 2")
    );

  }

  public static void test_getSummaryColumn_whenNoFields(ApplicationDetailItemView applicationDetailSearchItem,
                                                        Function<ApplicationDetailItemView, ApplicationWorkAreaItem> workAreaItemFunction) {

    applicationDetailSearchItem.setPadFields(List.of());

    var applicationWorkAreaItem = workAreaItemFunction.apply(applicationDetailSearchItem);

    assertThat(applicationWorkAreaItem.getSummaryColumn()).containsExactly(
        WorkAreaColumnItemView.createLabelledItem(
            "Project name", applicationWorkAreaItem.getProjectName()),
        WorkAreaColumnItemView.createLabelledItem(
            "Proposed start date",
            applicationWorkAreaItem.getProposedStartDateDisplay())
    );

  }

  public static void test_getHolderColumn_whenInitialType(ApplicationDetailItemView applicationDetailSearchItem,
                                                          Function<ApplicationDetailItemView, ApplicationWorkAreaItem> workAreaItemFunction) {

    applicationDetailSearchItem.setApplicationType(PwaApplicationType.INITIAL);
    applicationDetailSearchItem.setPadHolderNameList(List.of("PAD HOLDER 1", "PAD HOLDER 2"));

    var applicationWorkAreaItem = workAreaItemFunction.apply(applicationDetailSearchItem);

    assertThat(applicationWorkAreaItem.getHolderColumn()).containsExactly(
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, "PAD HOLDER 1"),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, "PAD HOLDER 2")
    );

  }

  public static void test_getHolderColumn_whenNotInitialType(ApplicationDetailItemView applicationDetailSearchItem,
                                                             Function<ApplicationDetailItemView, ApplicationWorkAreaItem> workAreaItemFunction) {

    applicationDetailSearchItem.setApplicationType(PwaApplicationType.CAT_1_VARIATION);
    applicationDetailSearchItem.setPwaHolderNameList(List.of("PWA HOLDER 1", "PWA HOLDER 2"));

    var applicationWorkAreaItem = workAreaItemFunction.apply(applicationDetailSearchItem);

    assertThat(applicationWorkAreaItem.getHolderColumn()).containsExactly(
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, "PWA HOLDER 1"),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, "PWA HOLDER 2")
    );

  }

  public static void test_getApplicationColumn_whenApplicationNotCompleteOrInitial(ApplicationDetailItemView applicationDetailSearchItem,
                                                               Function<ApplicationDetailItemView, ApplicationWorkAreaItem> workAreaItemFunction) {


    applicationDetailSearchItem.setApplicationType(PwaApplicationType.INITIAL);
    applicationDetailSearchItem.setPadStatus(PwaApplicationStatus.DRAFT);
    applicationDetailSearchItem.setResourceType(PwaResourceType.PETROLEUM);
    applicationDetailSearchItem.setPadReference("PAD REFERENCE");
    applicationDetailSearchItem.setPwaReference("PAD REFERENCE");

    var applicationWorkAreaItem = workAreaItemFunction.apply(applicationDetailSearchItem);

    assertThat(applicationWorkAreaItem.getApplicationColumn()).containsExactly(
        WorkAreaColumnItemView.createLinkItem("PAD REFERENCE", applicationWorkAreaItem.getAccessUrl()),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, PwaApplicationDisplayUtils.getApplicationTypeDisplay(PwaApplicationType.CAT_1_VARIATION, PwaResourceType.PETROLEUM))
    );

  }

  public static void test_getApplicationColumn_whenApplicationCompleteOrNotInitial(ApplicationDetailItemView applicationDetailSearchItem,
                                                               Function<ApplicationDetailItemView, ApplicationWorkAreaItem> workAreaItemFunction) {


    applicationDetailSearchItem.setApplicationType(PwaApplicationType.CAT_1_VARIATION);
    applicationDetailSearchItem.setPadStatus(PwaApplicationStatus.COMPLETE);
    applicationDetailSearchItem.setResourceType(PwaResourceType.PETROLEUM);
    applicationDetailSearchItem.setPadReference("PAD REFERENCE");
    applicationDetailSearchItem.setPwaReference("PWA REFERENCE");

    var applicationWorkAreaItem = workAreaItemFunction.apply(applicationDetailSearchItem);

    assertThat(applicationWorkAreaItem.getApplicationColumn()).containsExactly(
        WorkAreaColumnItemView.createLinkItem("PAD REFERENCE", applicationWorkAreaItem.getAccessUrl()),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, PwaApplicationDisplayUtils.getApplicationTypeDisplay(PwaApplicationType.CAT_1_VARIATION, PwaResourceType.PETROLEUM)),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, "PWA REFERENCE")
    );

  }

  public static void testGetApplicationColumnWhenUpdateRequestWithinDeadline(ApplicationDetailItemView applicationDetailSearchItem,
                                                                             Function<ApplicationDetailItemView, ApplicationWorkAreaItem> workAreaItemFunction) {

    applicationDetailSearchItem.setApplicationType(PwaApplicationType.CAT_1_VARIATION);
    applicationDetailSearchItem.setResourceType(PwaResourceType.PETROLEUM);
    applicationDetailSearchItem.setOpenUpdateRequestFlag(true);
    applicationDetailSearchItem.setOpenUpdateDeadlineTimestamp(Instant.now().plus(5, ChronoUnit.DAYS));

    applicationDetailSearchItem.setPadReference("PAD REFERENCE");
    applicationDetailSearchItem.setPwaReference("PWA REFERENCE");

    var applicationWorkAreaItem = workAreaItemFunction.apply(applicationDetailSearchItem);

    assertThat(applicationWorkAreaItem.getApplicationColumn()).containsExactly(
        WorkAreaColumnItemView.createLinkItem("PAD REFERENCE", applicationWorkAreaItem.getAccessUrl()),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, PwaApplicationDisplayUtils.getApplicationTypeDisplay(PwaApplicationType.CAT_1_VARIATION, PwaResourceType.PETROLEUM)),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, "PWA REFERENCE"),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.DEFAULT,
            "UPDATE DUE IN 5 DAYS")
    );

  }

  public static void testGetApplicationColumnWhenUpdateRequestDueToday(ApplicationDetailItemView applicationDetailSearchItem,
                                                                       Function<ApplicationDetailItemView, ApplicationWorkAreaItem> workAreaItemFunction) {

    applicationDetailSearchItem.setApplicationType(PwaApplicationType.CAT_1_VARIATION);
    applicationDetailSearchItem.setResourceType(PwaResourceType.PETROLEUM);
    applicationDetailSearchItem.setOpenUpdateRequestFlag(true);
    applicationDetailSearchItem.setOpenUpdateDeadlineTimestamp(Instant.now());

    applicationDetailSearchItem.setPadReference("PAD REFERENCE");
    applicationDetailSearchItem.setPwaReference("PWA REFERENCE");

    var applicationWorkAreaItem = workAreaItemFunction.apply(applicationDetailSearchItem);

    assertThat(applicationWorkAreaItem.getApplicationColumn()).containsExactly(
        WorkAreaColumnItemView.createLinkItem("PAD REFERENCE", applicationWorkAreaItem.getAccessUrl()),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, PwaApplicationDisplayUtils.getApplicationTypeDisplay(PwaApplicationType.CAT_1_VARIATION, PwaResourceType.PETROLEUM)),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, "PWA REFERENCE"),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.DEFAULT, "UPDATE DUE TODAY")
    );

  }

  public static void testGetApplicationColumnWhenUpdateRequestOverdue(ApplicationDetailItemView applicationDetailSearchItem,
                                                                      Function<ApplicationDetailItemView, ApplicationWorkAreaItem> workAreaItemFunction) {

    applicationDetailSearchItem.setApplicationType(PwaApplicationType.CAT_1_VARIATION);
    applicationDetailSearchItem.setResourceType(PwaResourceType.PETROLEUM);
    applicationDetailSearchItem.setOpenUpdateRequestFlag(true);
    applicationDetailSearchItem.setOpenUpdateDeadlineTimestamp(Instant.now().minus(1, ChronoUnit.DAYS));

    applicationDetailSearchItem.setPadReference("PAD REFERENCE");
    applicationDetailSearchItem.setPwaReference("PWA REFERENCE");

    var applicationWorkAreaItem = workAreaItemFunction.apply(applicationDetailSearchItem);

    assertThat(applicationWorkAreaItem.getApplicationColumn()).containsExactly(
        WorkAreaColumnItemView.createLinkItem("PAD REFERENCE", applicationWorkAreaItem.getAccessUrl()),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, PwaApplicationDisplayUtils.getApplicationTypeDisplay(PwaApplicationType.CAT_1_VARIATION, PwaResourceType.PETROLEUM)),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, "PWA REFERENCE"),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.DEFAULT, "UPDATE OVERDUE")
    );

  }

  public static void test_getAccessUrl_assertDefaultAccessUrl(ApplicationDetailItemView applicationDetailSearchItem,
                                                              Function<ApplicationDetailItemView, ApplicationWorkAreaItem> workAreaItemFunction) {
    var applicationWorkAreaItem = workAreaItemFunction.apply(applicationDetailSearchItem);

    assertThat(applicationWorkAreaItem.getAccessUrl()).isEqualTo(
        ReverseRouter.route(
            on(ApplicationLandingPageRouterController.class).route(applicationDetailSearchItem.getPwaApplicationId(), null))
    );
  }


}
