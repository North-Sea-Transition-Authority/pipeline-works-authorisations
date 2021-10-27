package uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineMaterial;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineId;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineOverview;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineRepository;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdent;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.importconsented.controller.ModifyPipelineController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber.RegulatorPipelineNumberTaskService;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDtoTestUtils;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.fds.ErrorItem;
import uk.co.ogauthority.pwa.model.location.CoordinatePairTestUtil;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.options.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResultTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInputValidator;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineTaskListServiceTest {

  private static final int PAD_PIPELINE_1_ID = 1;

  @Mock
  private PadPipelineRepository padPipelineRepository;

  @Mock
  private PadPipelineDataCopierService padPipelineDataCopierService;

  @Mock
  private PadOptionConfirmedService padOptionConfirmedService;

  @Mock
  private PadPipelineIdentService padPipelineIdentService;

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private RegulatorPipelineNumberTaskService regulatorPipelineNumberTaskService;

  @Mock
  private DecimalInputValidator decimalInputValidator;

  @Captor
  private ArgumentCaptor<List<PadPipeline>> padPipelineListArgCaptor;

  private PadPipelineTaskListService padPipelineTaskListService;

  private PwaApplicationDetail detail;


  private PadPipeline padPipe1;
  private Pipeline pipe1;
  private PadPipelineIdent ident;

  @Before
  public void setUp() {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    padPipelineTaskListService = new PadPipelineTaskListService(
        padPipelineService,
        padPipelineIdentService,
        padOptionConfirmedService,
        padPipelineRepository,
        regulatorPipelineNumberTaskService,
        padPipelineDataCopierService);

    padPipe1 = new PadPipeline();
    padPipe1.setId(PAD_PIPELINE_1_ID);
    padPipe1.setPipelineInBundle(false);
    padPipe1.setPipelineRef("TEMPORARY 1");
    padPipe1.setPipelineStatus(PipelineStatus.IN_SERVICE);
    padPipe1.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    padPipe1.setFromLocation("Location A");
    padPipe1.setFromCoordinates(CoordinatePairTestUtil.getDefaultCoordinate());
    padPipe1.setToLocation("Location B");
    padPipe1.setToCoordinates(CoordinatePairTestUtil.getDefaultCoordinate());

    pipe1 = new Pipeline();
    pipe1.setId(1);
    padPipe1.setPipeline(pipe1);

    ident = new PadPipelineIdent();
    ident.setPadPipeline(padPipe1);

    when(padPipelineService.isValidationRequiredByStatus(any())).thenReturn(true);

  }

  private PadPipelineTaskListService getTaskListServiceWithMockedIdentValidator(){
    return new PadPipelineTaskListService(
        padPipelineService,
        padPipelineIdentService,
        padOptionConfirmedService,
        padPipelineRepository,
        regulatorPipelineNumberTaskService,
        padPipelineDataCopierService);
  }

  @Test
  public void isComplete_whenValidatorCreatesNoErrors() {

    // Use mock ident validator instead of real one.
    padPipelineTaskListService = getTaskListServiceWithMockedIdentValidator();

    mockPipeline();

    when(padPipelineService.isValidationRequiredByStatus(any())).thenReturn(true);
    when(padPipelineIdentService.getSummaryScreenValidationResult(any()))
        .thenReturn(SummaryScreenValidationResultTestUtils.completeResult());

    var validationResult = padPipelineTaskListService.getValidationResult(detail);
    var isComplete = padPipelineTaskListService.isComplete(detail);

    assertThat(isComplete).isEqualTo(validationResult.isSectionComplete());

  }

  @Test
  public void canImportConsentedPipelines_applicationTypeSmokeTest() {
    PwaApplicationType.stream()
        .forEach(pwaApplicationType -> {
          var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(pwaApplicationType);
          var result = padPipelineTaskListService.canImportConsentedPipelines(detail);

          var allowed = Arrays.asList(
              ModifyPipelineController.class.getAnnotation(PwaApplicationTypeCheck.class).types()
          ).contains(pwaApplicationType);

          assertThat(result).isEqualTo(allowed);
        });
  }

  @Test
  public void cleanupData_hiddenData() {

    var pipeline1 = new PadPipeline();
    pipeline1.setTrenchedBuriedBackfilled(false);
    pipeline1.setTrenchingMethodsDescription("desc");
    pipeline1.setPipelineMaterial(PipelineMaterial.CARBON_STEEL);
    pipeline1.setOtherPipelineMaterialUsed("other");
    pipeline1.setPipelineStatus(PipelineStatus.IN_SERVICE);

    var pipeline2 = new PadPipeline();
    pipeline2.setTrenchedBuriedBackfilled(false);
    pipeline2.setTrenchingMethodsDescription("desc");
    pipeline2.setPipelineMaterial(PipelineMaterial.DUPLEX);
    pipeline2.setOtherPipelineMaterialUsed("other");
    pipeline2.setPipelineStatus(PipelineStatus.OUT_OF_USE_ON_SEABED);

    when(padPipelineService.getPipelines(detail)).thenReturn(List.of(pipeline1, pipeline2));
    when(padPipelineService.isValidationRequiredByStatus(any())).thenReturn(true);

    padPipelineTaskListService.cleanupData(detail);

    assertThat(pipeline1.getTrenchingMethodsDescription()).isNull();
    assertThat(pipeline1.getOtherPipelineMaterialUsed()).isNull();

    assertThat(pipeline2.getTrenchingMethodsDescription()).isNull();
    assertThat(pipeline2.getOtherPipelineMaterialUsed()).isNull();

    verify(padPipelineRepository, times(1)).saveAll(eq(List.of(pipeline1, pipeline2)));

  }

  @Test
  public void cleanupData_noHiddenData() {

    var pipeline1 = new PadPipeline();
    pipeline1.setTrenchedBuriedBackfilled(true);
    pipeline1.setTrenchingMethodsDescription("desc");
    pipeline1.setPipelineMaterial(PipelineMaterial.OTHER);
    pipeline1.setOtherPipelineMaterialUsed("other");
    pipeline1.setPipelineStatus(PipelineStatus.IN_SERVICE);

    var pipeline2 = new PadPipeline();
    pipeline2.setTrenchedBuriedBackfilled(true);
    pipeline2.setTrenchingMethodsDescription("desc");
    pipeline2.setPipelineMaterial(PipelineMaterial.OTHER);
    pipeline2.setOtherPipelineMaterialUsed("other");
    pipeline2.setPipelineStatus(PipelineStatus.OUT_OF_USE_ON_SEABED);

    when(padPipelineService.getPipelines(detail)).thenReturn(List.of(pipeline1, pipeline2));
    when(padPipelineService.isValidationRequiredByStatus(any())).thenReturn(true);

    padPipelineTaskListService.cleanupData(detail);

    assertThat(pipeline1.getTrenchingMethodsDescription()).isNotNull();
    assertThat(pipeline1.getOtherPipelineMaterialUsed()).isNotNull();

    assertThat(pipeline2.getTrenchingMethodsDescription()).isNotNull();
    assertThat(pipeline2.getOtherPipelineMaterialUsed()).isNotNull();

    verify(padPipelineRepository, times(1)).saveAll(eq(List.of(pipeline1, pipeline2)));

  }

  @Test
  public void cleanupData_dataNotRequired_notCleaned() {

    var pipeline1 = new PadPipeline();
    pipeline1.setPipelineStatus(PipelineStatus.RETURNED_TO_SHORE);

    var pipeline2 = new PadPipeline();
    pipeline2.setPipelineStatus(PipelineStatus.NEVER_LAID);

    when(padPipelineService.getPipelines(detail)).thenReturn(List.of(pipeline1, pipeline2));
    when(padPipelineService.isValidationRequiredByStatus(any())).thenReturn(false);

    padPipelineTaskListService.cleanupData(detail);

    verify(padPipelineRepository, times(1)).saveAll(padPipelineListArgCaptor.capture());

    var updatedPipeList = padPipelineListArgCaptor.getValue();

    assertThat(updatedPipeList).isEmpty();

  }

  @Test
  public void getValidationResult_noErrors() {

    // Use mock ident validator instead of real one.
    padPipelineTaskListService = getTaskListServiceWithMockedIdentValidator();

    mockPipeline();

    when(padPipelineService.isPadPipelineValid(any(), any())).thenReturn(true);
    when(padPipelineService.isValidationRequiredByStatus(any())).thenReturn(true);
    when(padPipelineIdentService.getSummaryScreenValidationResult(any()))
        .thenReturn(SummaryScreenValidationResultTestUtils.completeResult());

    var validationResult = padPipelineTaskListService.getValidationResult(detail);

    assertThat(validationResult.isSectionComplete()).isTrue();
    assertThat(validationResult.getErrorItems()).isEmpty();
    assertThat(validationResult.getSectionIncompleteError()).isNull();
    assertThat(validationResult.getIdPrefix()).isEqualTo("pipeline-");
    assertThat(validationResult.getInvalidObjectIds()).isEmpty();

  }

  @Test
  public void getValidationResult_errors_noPipelines() {

    var validationResult = padPipelineTaskListService.getValidationResult(detail);

    assertThat(validationResult.isSectionComplete()).isFalse();
    assertThat(validationResult.getErrorItems()).isEmpty();
    assertThat(validationResult.getSectionIncompleteError()).isEqualTo(
        "At least one pipeline must be added with valid header information. Each pipeline must have at least one valid ident.");
    assertThat(validationResult.getIdPrefix()).isEqualTo("pipeline-");
    assertThat(validationResult.getInvalidObjectIds()).isEmpty();

  }

  private void mockPipeline() {
    var overview = PadPipelineOverview.from(
        PadPipelineSummaryDtoTestUtils.generateFrom(padPipe1), true);
    when(padPipelineService.getApplicationPipelineOverviews(detail)).thenReturn(List.of(overview));

    var padPipelineId = new PadPipelineId(PAD_PIPELINE_1_ID);
    when(padPipelineService.getPadPipelineMapForOverviews(detail, List.of(overview)))
        .thenReturn(Map.of(padPipelineId, padPipe1));
  }

  @Test
  public void getValidationResult_errors_pipelineExists_identSummaryIncomplete() {

    mockPipeline();
    when(padPipelineIdentService.getSummaryScreenValidationResult(padPipe1))
        .thenReturn(SummaryScreenValidationResultTestUtils.incompleteResult());

    var validationResult = padPipelineTaskListService.getValidationResult(detail);

    assertThat(validationResult.isSectionComplete()).isFalse();
    assertThat(validationResult.getErrorItems())
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(1, "pipeline-1", "TEMPORARY 1 - Production Flowline must have all sections completed")
        );
    assertThat(validationResult.getSectionIncompleteError()).isEqualTo(
        "At least one pipeline must be added with valid header information. Each pipeline must have at least one valid ident.");
    assertThat(validationResult.getIdPrefix()).isEqualTo("pipeline-");
    assertThat(validationResult.getInvalidObjectIds()).containsExactly("1");

  }


  @Test
  public void copySectionInformation_serviceInteractions() {
    var newDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100, 100);
    padPipelineTaskListService.copySectionInformation(detail, newDetail);
    verify(padPipelineDataCopierService, times(1))
        .copyAllPadPipelineData(eq(detail), eq(newDetail), any());
  }

  @Test
  public void canShowInTaskList_notOptionsVariation() {
    var notOptions = EnumSet.allOf(PwaApplicationType.class);
    notOptions.remove(PwaApplicationType.OPTIONS_VARIATION);

    for (PwaApplicationType type : notOptions) {
      detail.getPwaApplication().setApplicationType(type);
      assertThat(padPipelineTaskListService.canShowInTaskList(detail)).isTrue();
    }

  }

  @Test
  public void canShowInTaskList_OptionsVariation_optionsNotComplete() {
    when(padOptionConfirmedService.approvedOptionConfirmed(detail)).thenReturn(false);

    detail.getPwaApplication().setApplicationType(PwaApplicationType.OPTIONS_VARIATION);

    assertThat(padPipelineTaskListService.canShowInTaskList(detail)).isFalse();

  }

  @Test
  public void canShowInTaskList_OptionsVariation_optionsComplete() {
    when(padOptionConfirmedService.approvedOptionConfirmed(detail)).thenReturn(true);

    detail.getPwaApplication().setApplicationType(PwaApplicationType.OPTIONS_VARIATION);

    assertThat(padPipelineTaskListService.canShowInTaskList(detail)).isTrue();

  }

  @Test
  public void getSortedPipelineTaskListItems_whenNoPipelines() {
    var context = new PwaApplicationContext(detail, null, Set.of(PwaApplicationPermission.EDIT));

    var taskListItems = padPipelineTaskListService.getSortedPipelineTaskListItems(context);

    assertThat(taskListItems).isEmpty();

  }

  @Test
  public void getSortedPipelineTaskListItems_whenPipelineIsNotRequiredToBeValidated() {

    var context = new PwaApplicationContext(detail, null, Set.of(PwaApplicationPermission.EDIT));
    padPipe1.setPipelineStatus(PipelineStatus.RETURNED_TO_SHORE);

    mockPipeline();
    when(padPipelineService.getPipelines(detail)).thenReturn(List.of(padPipe1));

    when(padPipelineService.isValidationRequiredByStatus(any())).thenReturn(false);

    var taskListItems = padPipelineTaskListService.getSortedPipelineTaskListItems(context);

    assertThat(taskListItems).hasSize(1);

    assertThat(taskListItems.get(0).getTaskList()).isEmpty();

  }

  @Test
  public void getSortedPipelineTaskListItems_whenRegulatorPipelineServiceDoesNotProvideTask() {
    var context = new PwaApplicationContext(detail, null, Set.of(PwaApplicationPermission.EDIT));

    mockPipeline();
    when(padPipelineService.getPipelines(detail)).thenReturn(List.of(padPipe1));

    var taskListItems = padPipelineTaskListService.getSortedPipelineTaskListItems(context);

    assertThat(taskListItems).hasSize(1);

    assertThat(taskListItems.get(0).getTaskList()).hasSize(2)
        .anySatisfy(taskListEntry -> assertThat(taskListEntry.getTaskName()).containsIgnoringCase("header"))
        .anySatisfy(taskListEntry -> assertThat(taskListEntry.getTaskName()).containsIgnoringCase("ident"));

  }

  @Test
  public void getSortedPipelineTaskListItems_whenRegulatorPipelineServiceProvidesTask() {
    var context = new PwaApplicationContext(detail, null, Set.of(PwaApplicationPermission.EDIT));

    mockPipeline();
    when(padPipelineService.getPipelines(detail)).thenReturn(List.of(padPipe1));

    var taskListEntry = new TaskListEntry("example", "route", true, 1);
    when(regulatorPipelineNumberTaskService.getTaskListEntry(any(),any())).thenReturn(Optional.of(taskListEntry));

    var taskListItems = padPipelineTaskListService.getSortedPipelineTaskListItems(context);

    assertThat(taskListItems).hasSize(1);

    assertThat(taskListItems.get(0).getTaskList()).hasSize(3)
        .contains(taskListEntry);

  }

  @Test
  public void getAvailableMailMergeFields() {

    PwaApplicationType.stream().forEach(appType -> {

      var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(appType);

      var mergeFields = padPipelineTaskListService.getAvailableMailMergeFields(detail.getPwaApplicationType());

      var expectedMergeFields = new ArrayList<MailMergeFieldMnem>();

      if (MailMergeFieldMnem.PL_NUMBER_LIST.appTypeIsSupported(appType)) {
        expectedMergeFields.add(MailMergeFieldMnem.PL_NUMBER_LIST);
      }

      assertThat(mergeFields).containsExactlyInAnyOrderElementsOf(expectedMergeFields);

    });

  }

  @Test
  public void resolveMailMergeFields() {

    var pipe1 = new PadPipeline();
    pipe1.setPipelineNumber("PL889");
    var overView1 = new PadPipelineOverview(pipe1);

    var pipe2 = new PadPipeline();
    pipe2.setPipelineNumber("PL13");
    var overview2 = new PadPipelineOverview(pipe2);

    when(padPipelineService.getApplicationPipelineOverviews(any())).thenReturn(List.of(overView1, overview2));

    PwaApplicationType.stream().forEach(appType -> {

      var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(appType);

      var mergeFieldsMap = padPipelineTaskListService.resolveMailMergeFields(detail);

      var expectedMergeFieldsMap = new HashMap<MailMergeFieldMnem, String>();

      if (MailMergeFieldMnem.PL_NUMBER_LIST.appTypeIsSupported(appType)) {
        expectedMergeFieldsMap.put(MailMergeFieldMnem.PL_NUMBER_LIST, "PL13, PL889");
      }

      assertThat(mergeFieldsMap).containsExactlyInAnyOrderEntriesOf(expectedMergeFieldsMap);

    });

  }

}