package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadAreaService;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PwaAreaLinksView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.diff.DiffService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaDetailAreaService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class AreaInformationSummaryServiceTest {
  private final String FIELD_NAME = "FIELD";
  private final String LINK_DESC = "LINK DESC";


  @Mock
  private TaskListService taskListService;

  @Mock
  private DiffService diffService;

  @Mock
  private PadAreaService padAreaService;

  @Mock
  private MasterPwaDetailAreaService masterPwaDetailAreaService;

  private AreaInformationSummaryService areaInformationSummaryService;


  private PwaApplicationDetail pwaApplicationDetail;

  private PwaAreaLinksView linkedFieldView;
  private PwaAreaLinksView notLinkedFieldView;

  @BeforeEach
  void setUp() throws Exception {

    linkedFieldView = createFieldLinksView(true, FIELD_NAME);
    notLinkedFieldView = createFieldLinksView(false, LINK_DESC);

    areaInformationSummaryService = new AreaInformationSummaryService(
        taskListService,
        padAreaService,
        masterPwaDetailAreaService,
        diffService
    );

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }

  @Test
  void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(Set.of(
        ApplicationTask.FIELD_INFORMATION,
        ApplicationTask.CARBON_STORAGE_INFORMATION), pwaApplicationDetail))
        .thenReturn(true);
    assertThat(areaInformationSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  void canSummarise_whenTaskNotShown() {
    assertThat(areaInformationSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  void summariseSection_PetroleumTemplate() {
    when(masterPwaDetailAreaService.getCurrentMasterPwaDetailAreaLinksView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(notLinkedFieldView);
    when(padAreaService.getApplicationAreaLinksView(pwaApplicationDetail)).thenReturn(notLinkedFieldView);

    var appSummary = areaInformationSummaryService.summariseSection(pwaApplicationDetail, null);

    assertThat(appSummary.getTemplatePath()).isEqualTo("pwaApplication/applicationSummarySections/fieldInformationSummary.ftl");
  }

  @Test
  void summariseSection_HydrogenTemplate() {
    var application = pwaApplicationDetail.getPwaApplication();
    application.setResourceType(PwaResourceType.HYDROGEN);
    pwaApplicationDetail.setPwaApplication(application);

    when(masterPwaDetailAreaService.getCurrentMasterPwaDetailAreaLinksView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(notLinkedFieldView);
    when(padAreaService.getApplicationAreaLinksView(pwaApplicationDetail)).thenReturn(notLinkedFieldView);

    var appSummary = areaInformationSummaryService.summariseSection(pwaApplicationDetail, null);

    assertThat(appSummary.getTemplatePath()).isEqualTo("pwaApplication/applicationSummarySections/fieldInformationSummary.ftl");
  }

  @Test
  void summariseSection_CcusTemplate() {
    var application = pwaApplicationDetail.getPwaApplication();
    application.setResourceType(PwaResourceType.CCUS);
    pwaApplicationDetail.setPwaApplication(application);

    when(masterPwaDetailAreaService.getCurrentMasterPwaDetailAreaLinksView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(notLinkedFieldView);
    when(padAreaService.getApplicationAreaLinksView(pwaApplicationDetail)).thenReturn(notLinkedFieldView);

    var appSummary = areaInformationSummaryService.summariseSection(pwaApplicationDetail, null);

    assertThat(appSummary.getTemplatePath()).isEqualTo("pwaApplication/applicationSummarySections/storageAreaInformationSummary.ftl");
  }

  @Test
  void summariseSection_verifyServiceInteractions() {

    when(masterPwaDetailAreaService.getCurrentMasterPwaDetailAreaLinksView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(notLinkedFieldView);
    when(padAreaService.getApplicationAreaLinksView(pwaApplicationDetail)).thenReturn(notLinkedFieldView);

    var appSummary = areaInformationSummaryService.summariseSection(pwaApplicationDetail, null);

    assertThat(appSummary.getTemplateModel()).hasSize(7);
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", ApplicationTask.FIELD_INFORMATION.getDisplayName()));
    assertThat(appSummary.getTemplateModel()).containsKey("showAreaNames");
    assertThat(appSummary.getTemplateModel()).containsKey("hideAreaNamesOnLoad");
    assertThat(appSummary.getTemplateModel()).containsKey("showPwaLinkedToDesc");
    assertThat(appSummary.getTemplateModel()).containsKey("hidePwaLinkedToDescOnLoad");
    assertThat(appSummary.getTemplateModel()).containsKey("areaLinks");
    assertThat(appSummary.getTemplateModel()).containsKey("areaLinkQuestions");

    assertThat(appSummary.getSidebarSectionLinks()).contains(
        SidebarSectionLink.createAnchorLink(ApplicationTask.FIELD_INFORMATION.getDisplayName(), "#areaInformation")
    );

    verify(diffService, times(1)).diff(notLinkedFieldView, notLinkedFieldView, Set.of("linkedAreaNames"));
    verify(diffService, times(1))
        .diffComplexLists(
            eq(notLinkedFieldView.getLinkedAreaNames()),
            eq(notLinkedFieldView.getLinkedAreaNames()),
            any(),
            any());

  }

  @Test
  void summariseSection_whenAppIsNotLinkedToFields_andConsentIsNotLinkedToFields() {

    when(masterPwaDetailAreaService.getCurrentMasterPwaDetailAreaLinksView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(notLinkedFieldView);
    when(padAreaService.getApplicationAreaLinksView(pwaApplicationDetail)).thenReturn(notLinkedFieldView);

    var appSummary = areaInformationSummaryService.summariseSection(pwaApplicationDetail, null);

    assertThat(appSummary.getTemplateModel()).contains(entry("showAreaNames", false));
    assertThat(appSummary.getTemplateModel()).contains(entry("hideAreaNamesOnLoad", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("showPwaLinkedToDesc", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("hidePwaLinkedToDescOnLoad",false));

  }

  @Test
  void summariseSection_whenAppIsLinkedToFields_andConsentIsNotLinkedToFields() {

    when(masterPwaDetailAreaService.getCurrentMasterPwaDetailAreaLinksView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(notLinkedFieldView);
    when(padAreaService.getApplicationAreaLinksView(pwaApplicationDetail)).thenReturn(linkedFieldView);

    var appSummary = areaInformationSummaryService.summariseSection(pwaApplicationDetail, null);

    assertThat(appSummary.getTemplateModel()).contains(entry("showAreaNames", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("hideAreaNamesOnLoad", false));
    assertThat(appSummary.getTemplateModel()).contains(entry("showPwaLinkedToDesc", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("hidePwaLinkedToDescOnLoad",true));

  }

  @Test
  void summariseSection_whenAppIsLinkedToFields_andConsentIsLinkedToFields() {

    when(masterPwaDetailAreaService.getCurrentMasterPwaDetailAreaLinksView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(linkedFieldView);
    when(padAreaService.getApplicationAreaLinksView(pwaApplicationDetail)).thenReturn(linkedFieldView);

    var appSummary = areaInformationSummaryService.summariseSection(pwaApplicationDetail, null);

    assertThat(appSummary.getTemplateModel()).contains(entry("showAreaNames", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("hideAreaNamesOnLoad", false));
    assertThat(appSummary.getTemplateModel()).contains(entry("showPwaLinkedToDesc", false));
    assertThat(appSummary.getTemplateModel()).contains(entry("hidePwaLinkedToDescOnLoad",true));

  }

  @Test
  void summariseSection_whenAppIsNotLinkedToFields_andConsentIsLinkedToFields() {

    when(masterPwaDetailAreaService.getCurrentMasterPwaDetailAreaLinksView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(linkedFieldView);
    when(padAreaService.getApplicationAreaLinksView(pwaApplicationDetail)).thenReturn(notLinkedFieldView);

    var appSummary = areaInformationSummaryService.summariseSection(pwaApplicationDetail, null);

    assertThat(appSummary.getTemplateModel()).contains(entry("showAreaNames", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("hideAreaNamesOnLoad", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("showPwaLinkedToDesc", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("hidePwaLinkedToDescOnLoad",false));

  }



  private PwaAreaLinksView createFieldLinksView(boolean isLinkedToField, String fieldNameOrLinkDesc){
    return new PwaAreaLinksView(
        isLinkedToField,
        isLinkedToField ? null : fieldNameOrLinkDesc,
        List.of(new StringWithTag(fieldNameOrLinkDesc))
    );


  }


}
