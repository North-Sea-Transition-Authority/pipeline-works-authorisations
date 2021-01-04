package uk.co.ogauthority.pwa.service.workarea;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

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

  public static void test_getApplicationColumn_whenInitialType(ApplicationDetailItemView applicationDetailSearchItem,
                                                               Function<ApplicationDetailItemView, ApplicationWorkAreaItem> workAreaItemFunction) {


    applicationDetailSearchItem.setApplicationType(PwaApplicationType.INITIAL);
    applicationDetailSearchItem.setPadReference("PAD REFERENCE");
    applicationDetailSearchItem.setPwaReference("PWA REFERENCE");

    var applicationWorkAreaItem = workAreaItemFunction.apply(applicationDetailSearchItem);

    assertThat(applicationWorkAreaItem.getApplicationColumn()).containsExactly(
        WorkAreaColumnItemView.createLinkItem("PAD REFERENCE", applicationWorkAreaItem.getAccessUrl()),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, PwaApplicationType.INITIAL.getDisplayName())
    );

  }

  public static void test_getApplicationColumn_whenNotInitialType(ApplicationDetailItemView applicationDetailSearchItem,
                                                               Function<ApplicationDetailItemView, ApplicationWorkAreaItem> workAreaItemFunction) {


    applicationDetailSearchItem.setApplicationType(PwaApplicationType.CAT_1_VARIATION);
    applicationDetailSearchItem.setPadReference("PAD REFERENCE");
    applicationDetailSearchItem.setPwaReference("PWA REFERENCE");

    var applicationWorkAreaItem = workAreaItemFunction.apply(applicationDetailSearchItem);

    assertThat(applicationWorkAreaItem.getApplicationColumn()).containsExactly(
        WorkAreaColumnItemView.createLinkItem("PAD REFERENCE", applicationWorkAreaItem.getAccessUrl()),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, PwaApplicationType.CAT_1_VARIATION.getDisplayName()),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, "PWA REFERENCE")
    );

  }

  public static void test_getApplicationColumn_whenUpdateRequest(ApplicationDetailItemView applicationDetailSearchItem,
                                                                 Function<ApplicationDetailItemView, ApplicationWorkAreaItem> workAreaItemFunction) {

    applicationDetailSearchItem.setApplicationType(PwaApplicationType.CAT_1_VARIATION);
    applicationDetailSearchItem.setOpenUpdateRequestFlag(true);

    applicationDetailSearchItem.setPadReference("PAD REFERENCE");
    applicationDetailSearchItem.setPwaReference("PWA REFERENCE");

    var applicationWorkAreaItem = workAreaItemFunction.apply(applicationDetailSearchItem);

    assertThat(applicationWorkAreaItem.getApplicationColumn()).containsExactly(
        WorkAreaColumnItemView.createLinkItem("PAD REFERENCE", applicationWorkAreaItem.getAccessUrl()),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, PwaApplicationType.CAT_1_VARIATION.getDisplayName()),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, "PWA REFERENCE"),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.DEFAULT, "UPDATE REQUESTED")
    );

  }


}
