package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.tasklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.util.FieldUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.ModifyPipelineController;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDtoTestUtils;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineHeaderFormContext;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.form.fds.ErrorItem;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.ModifyPipelineForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineBundlePairDto;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.options.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelinePersisterService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineHeaderFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineIdentDataFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineIdentFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.CoordinateUtils;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineTaskListServiceTest {

  private static final int PIPELINE_ID = 100;

  @Mock
  private PadPipelineRepository padPipelineRepository;

  @Mock
  private PipelineService pipelineService;

  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private PadPipelinePersisterService padPipelinePersisterService;

  @Mock
  private PadPipelineDataCopierService padPipelineDataCopierService;

  @Mock
  private PipelineHeaderFormValidator pipelineHeaderFormValidator;

  @Mock
  private PadOptionConfirmedService padOptionConfirmedService;

  @Mock
  private PadPipelineIdentService padPipelineIdentService;

  @Mock
  private PipelineIdentFormValidator mockValidator;

  @Mock
  private PadPipelineService padPipelineService;

  @Captor
  private ArgumentCaptor<PadPipeline> padPipelineArgumentCaptor;

  @Captor
  private ArgumentCaptor<List<PadPipeline>> padPipelineListArgCaptor;

  private PadPipelineTaskListService padPipelineTaskListService;

  private PwaApplicationDetail detail;

  private PipelineIdentFormValidator pipelineIdentFormValidator;


  private PadPipeline padPipe1;
  private Pipeline pipe1;
  private PadPipelineIdent ident;
  private ModifyPipelineForm modifyPipelineForm;

  @Before
  public void setUp() {

    modifyPipelineForm = new ModifyPipelineForm();

    // mimic save of new pipeline behaviour.
    when(pipelineService.createApplicationPipeline(any())).thenAnswer(invocation -> {
      var app = (PwaApplication) invocation.getArgument(0);
      var pipeline = new Pipeline(app);
      pipeline.setId(PIPELINE_ID);
      return pipeline;
    });

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pipelineIdentFormValidator = new PipelineIdentFormValidator(new PipelineIdentDataFormValidator(),
        new CoordinateFormValidator());

    padPipelineTaskListService = new PadPipelineTaskListService(
        padPipelineService,
        padPipelineIdentService,
        padOptionConfirmedService,
        padPipelineRepository,
        pipelineIdentFormValidator,
        padPipelineDataCopierService);

    padPipe1 = new PadPipeline();
    padPipe1.setId(1);
    padPipe1.setPipelineInBundle(false);
    padPipe1.setPipelineRef("TEMPORARY 1");
    padPipe1.setPipelineStatus(PipelineStatus.IN_SERVICE);

    pipe1 = new Pipeline();
    pipe1.setId(1);
    padPipe1.setPipeline(pipe1);

    ident = new PadPipelineIdent();
    ident.setPadPipeline(padPipe1);

    when(padPipelineRepository.getAllByPwaApplicationDetailAndIdIn(detail, List.of(1)))
        .thenReturn(List.of(padPipe1));

  }

  @Test
  public void isComplete() {

    // no errors on validate
    doAnswer(invocation -> invocation.getArgument(1)).when(mockValidator).validate(any(), any(), any());

    when(padPipelineRepository.findAllPipelinesAsSummaryDtoByPwaApplicationDetail(detail)).thenReturn(List.of(
        PadPipelineSummaryDtoTestUtils.generateFrom(padPipe1)
    ));

    when(padPipelineIdentService.getAllIdentsByPadPipelineIds(eq(List.of(new PadPipelineId(1))))).thenReturn(
        List.of(ident));

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

    when(padPipelineRepository.getAllByPwaApplicationDetail(detail)).thenReturn(List.of(pipeline1, pipeline2));

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

    when(padPipelineRepository.getAllByPwaApplicationDetail(detail)).thenReturn(List.of(pipeline1, pipeline2));

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

    when(padPipelineRepository.getAllByPwaApplicationDetail(detail)).thenReturn(List.of(pipeline1, pipeline2));

    padPipelineTaskListService.cleanupData(detail);

    verify(padPipelineRepository, times(1)).saveAll(padPipelineListArgCaptor.capture());

    var updatedPipeList = padPipelineListArgCaptor.getValue();

    assertThat(updatedPipeList).isEmpty();

  }

  @Test
  public void getValidationResult_noErrors() {

    // no errors on validate
    doAnswer(invocation -> invocation.getArgument(1)).when(mockValidator).validate(any(), any(), any());

    when(padPipelineRepository.findAllPipelinesAsSummaryDtoByPwaApplicationDetail(detail)).thenReturn(List.of(
        PadPipelineSummaryDtoTestUtils.generateFrom(padPipe1)
    ));

    when(padPipelineIdentService.getAllIdentsByPadPipelineIds(eq(List.of(new PadPipelineId(1))))).thenReturn(
        List.of(ident));

    var validationResult = padPipelineTaskListService.getValidationResult(detail);

    assertThat(validationResult.isSectionComplete()).isTrue();
    assertThat(validationResult.getErrorItems()).isEmpty();
    assertThat(validationResult.getSectionIncompleteError()).isNull();
    assertThat(validationResult.getIdPrefix()).isEqualTo("pipeline-");
    assertThat(validationResult.getInvalidObjectIds()).isEmpty();

  }

  @Test
  public void getValidationResult_errors_noPipelines() {

    when(padPipelineRepository.findAllPipelinesAsSummaryDtoByPwaApplicationDetail(detail)).thenReturn(List.of());

    var validationResult = padPipelineTaskListService.getValidationResult(detail);

    assertThat(validationResult.isSectionComplete()).isFalse();
    assertThat(validationResult.getErrorItems()).isEmpty();
    assertThat(validationResult.getSectionIncompleteError()).isEqualTo(
        "At least one pipeline must be added with valid header information. Each pipeline must have at least one valid ident.");
    assertThat(validationResult.getIdPrefix()).isEqualTo("pipeline-");
    assertThat(validationResult.getInvalidObjectIds()).isEmpty();

  }

  @Test
  public void getValidationResult_errors_pipelineExists_noIdentsOnIt() {

    when(padPipelineRepository.findAllPipelinesAsSummaryDtoByPwaApplicationDetail(detail)).thenReturn(List.of(
        PadPipelineSummaryDtoTestUtils.generateFrom(padPipe1)
    ));

    when(padPipelineIdentService.getAllIdentsByPadPipelineIds(eq(List.of(new PadPipelineId(1))))).thenReturn(List.of());

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
  public void getValidationResult_errors_pipelineExists_identValidationFails() {

    when(padPipelineRepository.findAllPipelinesAsSummaryDtoByPwaApplicationDetail(detail)).thenReturn(List.of(
        PadPipelineSummaryDtoTestUtils.generateFrom(padPipe1)
    ));

    when(padPipelineIdentService.getAllIdentsByPadPipelineIds(eq(List.of(new PadPipelineId(1))))).thenReturn(
        List.of(ident));

    // force error when validating ident
    doAnswer(invocation -> {
      ((BindingResult) invocation.getArgument(1)).rejectValue("length",
          "length.invalid", "fake");
      return invocation;
    }).when(mockValidator).validate(any(), any(), any());

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
  public void doesPipelineHaveTasks_false() {
    EnumSet.of(PipelineStatus.RETURNED_TO_SHORE, PipelineStatus.NEVER_LAID).forEach(pipelineStatus -> {
      padPipe1.setPipelineStatus(pipelineStatus);
      var dto = createPadPipelineSummaryDto(padPipe1);
      var result = padPipelineTaskListService.doesPipelineHaveTasks(dto);
      assertThat(result).isFalse();
    });
  }

  @Test
  public void doesPipelineHaveTasks() {
    var statusEnums = EnumSet.allOf(PipelineStatus.class);
    statusEnums.forEach(pipelineStatus -> {

      padPipe1.setPipelineStatus(pipelineStatus);
      var dto = createPadPipelineSummaryDto(padPipe1);
      var result = padPipelineTaskListService.doesPipelineHaveTasks(dto);

      boolean expectedResult = List.of(PipelineStatus.IN_SERVICE, PipelineStatus.OUT_OF_USE_ON_SEABED).contains(pipelineStatus);

      assertThat(result).isEqualTo(expectedResult);

    });
  }


  private PadPipelineSummaryDto createPadPipelineSummaryDto(PadPipeline padPipeline) {
    return new PadPipelineSummaryDto(
        padPipeline.getId(),
        padPipeline.getPipeline().getId(),
        padPipeline.getPipelineType(),
        padPipeline.getPipelineRef(),
        padPipeline.getTemporaryRef(),
        padPipeline.getLength(),
        padPipeline.getComponentPartsDescription(),
        padPipeline.getProductsToBeConveyed(),
        1L,
        padPipeline.getFromLocation(),
        1, 1, BigDecimal.ZERO, LatitudeDirection.NORTH,
        1, 1, BigDecimal.ZERO, LongitudeDirection.EAST,
        padPipeline.getToLocation(),
        1, 1, BigDecimal.ZERO, LatitudeDirection.NORTH,
        1, 1, BigDecimal.ZERO, LongitudeDirection.EAST,
        padPipeline.getMaxExternalDiameter(),
        padPipeline.getPipelineInBundle(),
        padPipeline.getBundleName(),
        padPipeline.getPipelineFlexibility(),
        padPipeline.getPipelineMaterial(),
        padPipeline.getOtherPipelineMaterialUsed(),
        padPipeline.getTrenchedBuriedBackfilled(),
        padPipeline.getTrenchingMethodsDescription(),
        padPipeline.getPipelineStatus(),
        padPipeline.getPipelineStatusReason(),
        padPipeline.getAlreadyExistsOnSeabed(),
        padPipeline.getPipelineInUse()
    );
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

}
