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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadFieldService;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PwaFieldLinksView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.diff.DiffService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaDetailFieldService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class FieldInformationSummaryServiceTest {

  private final String TEMPLATE = "TEMPLATE";
  private final String FIELD_NAME = "FIELD";
  private final String LINK_DESC = "LINK DESC";


  @Mock
  private TaskListService taskListService;

  @Mock
  private DiffService diffService;

  @Mock
  private PadFieldService padFieldService;

  @Mock
  private MasterPwaDetailFieldService masterPwaDetailFieldService;

  private FieldInformationSummaryService fieldInformationSummaryService;


  private PwaApplicationDetail pwaApplicationDetail;

  private PwaFieldLinksView linkedFieldView;
  private PwaFieldLinksView notLinkedFieldView;

  @Before
  public void setUp() throws Exception {

    linkedFieldView = createFieldLinksView(true, FIELD_NAME);
    notLinkedFieldView = createFieldLinksView(false, LINK_DESC);

    fieldInformationSummaryService = new FieldInformationSummaryService(
        taskListService,
        padFieldService,
        masterPwaDetailFieldService,
        diffService
    );

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);
  }

  @Test
  public void canSummarise_whenHasTaskShown() {
    when(taskListService.anyTaskShownForApplication(Set.of(ApplicationTask.FIELD_INFORMATION), pwaApplicationDetail))
        .thenReturn(true);
    assertThat(fieldInformationSummaryService.canSummarise(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenTaskNotShown() {
    assertThat(fieldInformationSummaryService.canSummarise(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void summariseSection_verifyServiceInteractions() {

    when(masterPwaDetailFieldService.getCurrentMasterPwaDetailFieldLinksView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(notLinkedFieldView);
    when(padFieldService.getApplicationFieldLinksView(pwaApplicationDetail)).thenReturn(notLinkedFieldView);

    var appSummary = fieldInformationSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);

    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).hasSize(7);
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", ApplicationTask.FIELD_INFORMATION.getDisplayName()));
    assertThat(appSummary.getTemplateModel()).containsKey("showFieldNames");
    assertThat(appSummary.getTemplateModel()).containsKey("hideFieldNamesOnLoad");
    assertThat(appSummary.getTemplateModel()).containsKey("showPwaLinkedToDesc");
    assertThat(appSummary.getTemplateModel()).containsKey("hidePwaLinkedToDescOnLoad");
    assertThat(appSummary.getTemplateModel()).containsKey("fieldLinks");
    assertThat(appSummary.getTemplateModel()).containsKey("fieldLinkQuestions");

    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(ApplicationTask.FIELD_INFORMATION.getDisplayName(), "#fieldInformation")
    );

    verify(diffService, times(1)).diff(notLinkedFieldView, notLinkedFieldView, Set.of("linkedFieldNames"));
    verify(diffService, times(1))
        .diffComplexLists(
            eq(notLinkedFieldView.getLinkedFieldNames()),
            eq(notLinkedFieldView.getLinkedFieldNames()),
            any(),
            any());

  }

  @Test
  public void summariseSection_whenAppIsNotLinkedToFields_andConsentIsNotLinkedToFields() {

    when(masterPwaDetailFieldService.getCurrentMasterPwaDetailFieldLinksView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(notLinkedFieldView);
    when(padFieldService.getApplicationFieldLinksView(pwaApplicationDetail)).thenReturn(notLinkedFieldView);

    var appSummary = fieldInformationSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);

    assertThat(appSummary.getTemplateModel()).contains(entry("showFieldNames", false));
    assertThat(appSummary.getTemplateModel()).contains(entry("hideFieldNamesOnLoad", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("showPwaLinkedToDesc", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("hidePwaLinkedToDescOnLoad",false));

  }

  @Test
  public void summariseSection_whenAppIsLinkedToFields_andConsentIsNotLinkedToFields() {

    when(masterPwaDetailFieldService.getCurrentMasterPwaDetailFieldLinksView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(notLinkedFieldView);
    when(padFieldService.getApplicationFieldLinksView(pwaApplicationDetail)).thenReturn(linkedFieldView);

    var appSummary = fieldInformationSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);

    assertThat(appSummary.getTemplateModel()).contains(entry("showFieldNames", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("hideFieldNamesOnLoad", false));
    assertThat(appSummary.getTemplateModel()).contains(entry("showPwaLinkedToDesc", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("hidePwaLinkedToDescOnLoad",true));

  }

  @Test
  public void summariseSection_whenAppIsLinkedToFields_andConsentIsLinkedToFields() {

    when(masterPwaDetailFieldService.getCurrentMasterPwaDetailFieldLinksView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(linkedFieldView);
    when(padFieldService.getApplicationFieldLinksView(pwaApplicationDetail)).thenReturn(linkedFieldView);

    var appSummary = fieldInformationSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);

    assertThat(appSummary.getTemplateModel()).contains(entry("showFieldNames", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("hideFieldNamesOnLoad", false));
    assertThat(appSummary.getTemplateModel()).contains(entry("showPwaLinkedToDesc", false));
    assertThat(appSummary.getTemplateModel()).contains(entry("hidePwaLinkedToDescOnLoad",true));

  }

  @Test
  public void summariseSection_whenAppIsNotLinkedToFields_andConsentIsLinkedToFields() {

    when(masterPwaDetailFieldService.getCurrentMasterPwaDetailFieldLinksView(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(linkedFieldView);
    when(padFieldService.getApplicationFieldLinksView(pwaApplicationDetail)).thenReturn(notLinkedFieldView);

    var appSummary = fieldInformationSummaryService.summariseSection(pwaApplicationDetail, TEMPLATE);

    assertThat(appSummary.getTemplateModel()).contains(entry("showFieldNames", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("hideFieldNamesOnLoad", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("showPwaLinkedToDesc", true));
    assertThat(appSummary.getTemplateModel()).contains(entry("hidePwaLinkedToDescOnLoad",false));

  }



  private PwaFieldLinksView createFieldLinksView(boolean isLinkedToField, String fieldNameOrLinkDesc){
    return new PwaFieldLinksView(
        isLinkedToField,
        isLinkedToField ? null : fieldNameOrLinkDesc,
        List.of(new StringWithTag(fieldNameOrLinkDesc))
    );


  }


}