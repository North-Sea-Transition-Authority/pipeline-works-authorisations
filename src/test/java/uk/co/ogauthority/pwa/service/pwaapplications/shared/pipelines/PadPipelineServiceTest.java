package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.ModifyPipelineController;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDtoTestUtils;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
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
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineBundlePairDto;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.CoordinateUtils;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineServiceTest {

  private static final int PIPELINE_ID = 100;

  @Mock
  private PadPipelineRepository padPipelineRepository;

  @Mock
  private PipelineService pipelineService;

  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private PadPipelinePersisterService padPipelinePersisterService;

  private PadPipelineService padPipelineService;

  private PwaApplicationDetail detail;

  private PipelineIdentFormValidator pipelineIdentFormValidator;

  @Mock
  private PadPipelineIdentService padPipelineIdentService;

  @Captor
  private ArgumentCaptor<PadPipeline> padPipelineArgumentCaptor;

  @Mock
  private PipelineIdentFormValidator mockValidator;

  private PadPipelineService mockValidatorPadPipelineService;

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

    padPipelineService = new PadPipelineService(padPipelineRepository, pipelineService, pipelineDetailService,
        padPipelineIdentService, pipelineIdentFormValidator, padPipelinePersisterService);

    mockValidatorPadPipelineService = new PadPipelineService(padPipelineRepository, pipelineService,
        pipelineDetailService, padPipelineIdentService, mockValidator, padPipelinePersisterService);

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

  }

  @Test
  public void addPipeline() throws IllegalAccessException {
    var form = new PipelineHeaderForm();

    form.setFromLocation("from");
    var fromCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(55, 55, BigDecimal.valueOf(55.55), LatitudeDirection.NORTH),
            new LongitudeCoordinate(12, 12, BigDecimal.valueOf(12), LongitudeDirection.EAST)
        ), fromCoordinateForm
    );
    form.setFromCoordinateForm(fromCoordinateForm);

    form.setToLocation("to");
    var toCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(46, 46, BigDecimal.valueOf(46), LatitudeDirection.SOUTH),
            new LongitudeCoordinate(6, 6, BigDecimal.valueOf(6.66), LongitudeDirection.WEST)
        ), toCoordinateForm
    );
    form.setToCoordinateForm(toCoordinateForm);

    form.setLength(BigDecimal.valueOf(65.66));
    form.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    form.setComponentPartsDescription("component parts");
    form.setProductsToBeConveyed("products");
    form.setTrenchedBuriedBackfilled(true);
    form.setTrenchingMethods("trench methods");
    form.setPipelineMaterial(PipelineMaterial.CARBON_STEEL);

    padPipelineService.addPipeline(detail, form);

    verify(padPipelinePersisterService, times(1)).savePadPipelineAndMaterialiseIdentData(
        padPipelineArgumentCaptor.capture());
    verify(pipelineService, times(1)).createApplicationPipeline(detail.getPwaApplication());

    var newPadPipeline = padPipelineArgumentCaptor.getValue();
    assertThat(newPadPipeline.getPipeline().getId()).isEqualTo(PIPELINE_ID);
    assertThat(newPadPipeline.getFromLocation()).isEqualTo(form.getFromLocation());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "fromLatitudeDegrees")).isEqualTo(
        form.getFromCoordinateForm().getLatitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "fromLatitudeMinutes")).isEqualTo(
        form.getFromCoordinateForm().getLatitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "fromLatitudeSeconds")).isEqualTo(
        form.getFromCoordinateForm().getLatitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "fromLatitudeDirection")).isEqualTo(
        form.getFromCoordinateForm().getLatitudeDirection());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "fromLongitudeDegrees")).isEqualTo(
        form.getFromCoordinateForm().getLongitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "fromLongitudeMinutes")).isEqualTo(
        form.getFromCoordinateForm().getLongitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "fromLongitudeSeconds")).isEqualTo(
        form.getFromCoordinateForm().getLongitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "fromLongitudeDirection")).isEqualTo(
        form.getFromCoordinateForm().getLongitudeDirection());

    assertThat(newPadPipeline.getToLocation()).isEqualTo(form.getToLocation());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "toLatitudeDegrees")).isEqualTo(
        form.getToCoordinateForm().getLatitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "toLatitudeMinutes")).isEqualTo(
        form.getToCoordinateForm().getLatitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "toLatitudeSeconds")).isEqualTo(
        form.getToCoordinateForm().getLatitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "toLatitudeDirection")).isEqualTo(
        form.getToCoordinateForm().getLatitudeDirection());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "toLongitudeDegrees")).isEqualTo(
        form.getToCoordinateForm().getLongitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "toLongitudeMinutes")).isEqualTo(
        form.getToCoordinateForm().getLongitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "toLongitudeSeconds")).isEqualTo(
        form.getToCoordinateForm().getLongitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newPadPipeline, "toLongitudeDirection")).isEqualTo(
        form.getToCoordinateForm().getLongitudeDirection());

    assertThat(newPadPipeline.getLength()).isEqualTo(form.getLength());
    assertThat(newPadPipeline.getPipelineType()).isEqualTo(form.getPipelineType());
    assertThat(newPadPipeline.getProductsToBeConveyed()).isEqualTo(form.getProductsToBeConveyed());
    assertThat(newPadPipeline.getComponentPartsDescription()).isEqualTo(form.getComponentPartsDescription());
    assertThat(form.getTrenchedBuriedBackfilled()).isEqualTo(form.getTrenchedBuriedBackfilled());
    assertThat(form.getTrenchingMethods()).isEqualTo(form.getTrenchingMethods());

    assertThat(newPadPipeline.getPipelineStatus()).isEqualTo(PipelineStatus.IN_SERVICE);

  }

  @Test
  public void addPipeline_otherMaterialSelected() {
    var form = new PipelineHeaderForm();

    form.setPipelineType(PipelineType.HYDRAULIC_JUMPER);
    form.setPipelineMaterial(PipelineMaterial.OTHER);
    form.setOtherPipelineMaterialUsed("other material");
    var fromCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(55, 55, BigDecimal.valueOf(55.55), LatitudeDirection.NORTH),
            new LongitudeCoordinate(12, 12, BigDecimal.valueOf(12), LongitudeDirection.EAST)
        ), fromCoordinateForm
    );
    form.setFromCoordinateForm(fromCoordinateForm);

    form.setToLocation("to");
    var toCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(46, 46, BigDecimal.valueOf(46), LatitudeDirection.SOUTH),
            new LongitudeCoordinate(6, 6, BigDecimal.valueOf(6.66), LongitudeDirection.WEST)
        ), toCoordinateForm
    );
    form.setToCoordinateForm(toCoordinateForm);
    form.setTrenchedBuriedBackfilled(false);

    padPipelineService.addPipeline(detail, form);
    verify(padPipelinePersisterService, times(1)).savePadPipelineAndMaterialiseIdentData(
        padPipelineArgumentCaptor.capture());
    var newPipeline = padPipelineArgumentCaptor.getValue();
    assertThat(newPipeline.getOtherPipelineMaterialUsed()).isEqualTo(form.getOtherPipelineMaterialUsed());
  }

  @Test
  public void addPipeline_pipelineReference_isSequential() {
    var form = new PipelineHeaderForm();
    form.setFromCoordinateForm(new CoordinateForm());
    form.setToCoordinateForm(new CoordinateForm());
    form.setTrenchedBuriedBackfilled(false);
    form.setPipelineMaterial(PipelineMaterial.DUPLEX);
    form.setPipelineInBundle(false);

    when(padPipelineRepository.getMaxTemporaryNumberByPwaApplicationDetail(detail)).thenReturn(2);

    var padPipeline = padPipelineService.addPipeline(detail, form);
    assertThat(padPipeline.getTemporaryNumber()).isEqualTo(3);
    assertThat(padPipeline.getPipelineRef()).isEqualTo("TEMPORARY 3");
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

    var validationResult = mockValidatorPadPipelineService.getValidationResult(detail);
    var isComplete = mockValidatorPadPipelineService.isComplete(detail);

    assertThat(isComplete).isEqualTo(validationResult.isSectionComplete());

  }

  @Test
  public void getPipelines() {
    var pipelinesMocked = new ArrayList<PadPipeline>();
    var PadPipeline = new PadPipeline();
    PadPipeline.setId(1);
    PadPipeline.setPipelineRef("l1");
    pipelinesMocked.add(PadPipeline);
    PadPipeline = new PadPipeline();
    PadPipeline.setId(2);
    PadPipeline.setPipelineRef("l2");
    pipelinesMocked.add(PadPipeline);

    var pipeLinesExpected = new HashMap<String, String>();
    pipeLinesExpected.put("1", "l1");
    pipeLinesExpected.put("2", "l2");

    var detail = new PwaApplicationDetail();
    when(padPipelineRepository.getAllByPwaApplicationDetail(detail)).thenReturn(pipelinesMocked);

    assertThat(padPipelineService.getPipelineReferenceMap(detail)).isEqualTo(pipeLinesExpected);
  }

  @Test
  public void getApplicationOrConsentedPipelineNumberLookup_whenNoConsentedPipelines() {
    var pipeline = new Pipeline();
    pipeline.setId(1);

    var padPipeline = new PadPipeline(detail);
    padPipeline.setPipeline(pipeline);
    padPipeline.setId(10);
    padPipeline.setPipelineRef("PIPELINE_1");

    when(padPipelineRepository.getAllByPwaApplicationDetail(detail)).thenReturn(List.of(padPipeline));

    assertThat(padPipelineService.getApplicationOrConsentedPipelineNumberLookup(detail))
        .containsExactly(
            entry(new PipelineId(1), "PIPELINE_1")

        );

  }

  @Test
  public void getApplicationOrConsentedPipelineNumberLookup_whenPadPipelineImportedFromConsentedModel() {

    var pipeline = new Pipeline();
    pipeline.setId(1);

    var padPipeline = new PadPipeline(detail);
    padPipeline.setPipeline(pipeline);
    padPipeline.setId(10);
    padPipeline.setPipelineRef("APP_PIPELINE_1");

    var pipelineDetail = new PipelineDetail(pipeline);
    pipelineDetail.setPipelineNumber("CONSENTED_PIPELINE_1");


    when(padPipelineRepository.getAllByPwaApplicationDetail(detail)).thenReturn(List.of(padPipeline));

    when(pipelineDetailService.getActivePipelineDetailsForApplicationMasterPwa(detail.getPwaApplication()))
        .thenReturn(List.of(pipelineDetail));

    assertThat(padPipelineService.getApplicationOrConsentedPipelineNumberLookup(detail))
        .containsExactly(
            entry(PipelineId.from(pipeline), "APP_PIPELINE_1")

        );

  }

  @Test
  public void getApplicationOrConsentedPipelineNumberLookup_whenNoAppPipelines() {

    var pipeline = new Pipeline();
    pipeline.setId(1);
    var pipelineDetail = new PipelineDetail(pipeline);
    pipelineDetail.setPipelineNumber("CONSENTED_PIPELINE_1");

    when(pipelineDetailService.getActivePipelineDetailsForApplicationMasterPwa(detail.getPwaApplication()))
        .thenReturn(List.of(pipelineDetail));

    assertThat(padPipelineService.getApplicationOrConsentedPipelineNumberLookup(detail))
        .containsExactly(
            entry(PipelineId.from(pipeline), "CONSENTED_PIPELINE_1")

        );

  }

  @Test
  public void getPipelineBundleNamesByDetail_repositoryInteraction() {
    var bundlePairDto = new PipelineBundlePairDto(1, "bundle");
    List<PipelineBundlePairDto> list = List.of(bundlePairDto);
    when(padPipelineRepository.getBundleNamesByPwaApplicationDetail(detail)).thenReturn(list);
    var result = padPipelineService.getPipelineBundleNamesByDetail(detail);
    assertThat(result).containsExactly(bundlePairDto);
  }

  @Test
  public void getAvailableBundleNamesForApplication_noImportedBundles() {
    var consentedBundlePairDto = new PipelineBundlePairDto(1, "bundle");
    var applicationBundlePairDto = new PipelineBundlePairDto(2, "other bundle");
    when(pipelineDetailService.getSimilarPipelineBundleNamesByDetail(detail))
        .thenReturn(List.of(consentedBundlePairDto));
    when(padPipelineRepository.getBundleNamesByPwaApplicationDetail(detail)).thenReturn(
        List.of(applicationBundlePairDto));
    var result = padPipelineService.getAvailableBundleNamesForApplication(detail);
    assertThat(result).containsExactlyInAnyOrder("bundle", "other bundle");
  }

  @Test
  public void getAvailableBundleNamesForApplication_hasImportedBundles() {
    var consentedBundlePairDto = new PipelineBundlePairDto(1, "bundle");
    var importedApplicationBundlePairDto = new PipelineBundlePairDto(1, "other bundle");
    var applicationBundlePairDto = new PipelineBundlePairDto(2, "test bundle");
    when(pipelineDetailService.getSimilarPipelineBundleNamesByDetail(detail))
        .thenReturn(List.of(consentedBundlePairDto));
    when(padPipelineRepository.getBundleNamesByPwaApplicationDetail(detail))
        .thenReturn(List.of(applicationBundlePairDto, importedApplicationBundlePairDto));
    var result = padPipelineService.getAvailableBundleNamesForApplication(detail);
    assertThat(result).containsExactlyInAnyOrder("other bundle", "test bundle");
  }

  @Test
  public void getAvailableBundleNamesForApplication_noBundles() {
    when(pipelineDetailService.getSimilarPipelineBundleNamesByDetail(detail)).thenReturn(List.of());
    when(padPipelineRepository.getBundleNamesByPwaApplicationDetail(detail)).thenReturn(List.of());
    var result = padPipelineService.getAvailableBundleNamesForApplication(detail);
    assertThat(result).isEmpty();
  }

  @Test
  public void canImportConsentedPipelines_applicationTypeSmokeTest() {
    PwaApplicationType.stream()
        .forEach(pwaApplicationType -> {
          var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(pwaApplicationType);
          var result = padPipelineService.canImportConsentedPipelines(detail);

          var allowed = Arrays.asList(
              ModifyPipelineController.class.getAnnotation(PwaApplicationTypeCheck.class).types()
          ).contains(pwaApplicationType);

          assertThat(result).isEqualTo(allowed);
        });
  }

  @Test
  public void getMasterPipelineIds_serviceInteraction() {
    var pipelineId = new PipelineId(1);
    when(padPipelineRepository.getMasterPipelineIdsOnApplication(detail)).thenReturn(Set.of(pipelineId));
    Set<PipelineId> result = padPipelineService.getMasterPipelineIds(detail);
    assertThat(result).containsExactly(pipelineId);
  }

  @Test
  public void copyDataToNewPadPipeline_verifyDataMatches() {
    var pipelineDetail = new PipelineDetail();
    var pipeline = new Pipeline();

    var fromCoordinatePair = new CoordinatePair(
        new LatitudeCoordinate(1, 1, BigDecimal.ZERO, LatitudeDirection.NORTH),
        new LongitudeCoordinate(1, 1, BigDecimal.ZERO, LongitudeDirection.EAST)
    );

    var toCoordinatePair = new CoordinatePair(
        new LatitudeCoordinate(2, 2, BigDecimal.ZERO, LatitudeDirection.SOUTH),
        new LongitudeCoordinate(2, 2, BigDecimal.ZERO, LongitudeDirection.WEST)
    );

    // TODO: PWA-682 - Set added fields
    pipelineDetail.setBundleName("bundle");
    pipelineDetail.setPipelineNumber("ref");
    pipelineDetail.setPipeline(pipeline);
    pipelineDetail.setComponentPartsDesc("comp desc");
    pipelineDetail.setFromCoordinates(fromCoordinatePair);
    pipelineDetail.setFromLocation("a");
    pipelineDetail.setLength(BigDecimal.ONE);
    pipelineDetail.setPipelineInBundle(true);
    pipelineDetail.setPipelineType(PipelineType.GAS_LIFT_PIPELINE);
    pipelineDetail.setProductsToBeConveyed("products");
    pipelineDetail.setToCoordinates(toCoordinatePair);
    pipelineDetail.setToLocation("b");

    modifyPipelineForm.setPipelineStatus(PipelineStatus.OUT_OF_USE_ON_SEABED);
    modifyPipelineForm.setPipelineStatusReason("reason");

    var pipelineWithCopiedData = padPipelineService.copyDataToNewPadPipeline(detail, pipelineDetail, modifyPipelineForm);

    // TODO: PWA-682 - Assert added fields
    assertThat(pipelineWithCopiedData.getBundleName()).isEqualTo(pipelineDetail.getBundleName());
    assertThat(pipelineWithCopiedData.getPipeline()).isEqualTo(pipelineDetail.getPipeline());
    assertThat(pipelineWithCopiedData.getFromCoordinates()).isEqualTo(pipelineDetail.getFromCoordinates());
    assertThat(pipelineWithCopiedData.getFromLocation()).isEqualTo(pipelineDetail.getFromLocation());
    assertThat(pipelineWithCopiedData.getLength()).isEqualTo(pipelineDetail.getLength());
    assertThat(pipelineWithCopiedData.getPipelineInBundle()).isEqualTo(pipelineDetail.getPipelineInBundle());
    assertThat(pipelineWithCopiedData.getPipelineType()).isEqualTo(pipelineDetail.getPipelineType());
    assertThat(pipelineWithCopiedData.getProductsToBeConveyed()).isEqualTo(pipelineDetail.getProductsToBeConveyed());
    assertThat(pipelineWithCopiedData.getToCoordinates()).isEqualTo(pipelineDetail.getToCoordinates());
    assertThat(pipelineWithCopiedData.getToLocation()).isEqualTo(pipelineDetail.getToLocation());
    assertThat(pipelineWithCopiedData.getComponentPartsDescription()).isEqualTo(pipelineDetail.getComponentPartsDesc());
    assertThat(pipelineWithCopiedData.getPipelineRef()).isEqualTo(pipelineDetail.getPipelineNumber());
    assertThat(pipelineWithCopiedData.getPipelineStatus()).isEqualTo(modifyPipelineForm.getPipelineStatus());
    assertThat(pipelineWithCopiedData.getPipelineStatusReason()).isEqualTo(modifyPipelineForm.getPipelineStatusReason());

  }

  @Test
  public void copyDataToNewPadPipeline_noReason_notOnSeabed() {
    var pipelineDetail = new PipelineDetail();
    modifyPipelineForm.setPipelineStatus(PipelineStatus.IN_SERVICE);
    modifyPipelineForm.setPipelineStatusReason("reason");
    var pipelineWithCopiedData = padPipelineService.copyDataToNewPadPipeline(detail, pipelineDetail, modifyPipelineForm);
    assertThat(pipelineWithCopiedData.getPipelineStatus()).isEqualTo(modifyPipelineForm.getPipelineStatus());
    assertThat(pipelineWithCopiedData.getPipelineStatusReason()).isNull();
  }

  @Test
  public void copyDataToNewPadPipeline_verifySaved() {
    var pipelineDetail = new PipelineDetail();

    var fromCoordinatePair = new CoordinatePair(
        new LatitudeCoordinate(1, 1, BigDecimal.ZERO, LatitudeDirection.NORTH),
        new LongitudeCoordinate(1, 1, BigDecimal.ZERO, LongitudeDirection.EAST)
    );

    var toCoordinatePair = new CoordinatePair(
        new LatitudeCoordinate(2, 2, BigDecimal.ZERO, LatitudeDirection.SOUTH),
        new LongitudeCoordinate(2, 2, BigDecimal.ZERO, LongitudeDirection.WEST)
    );

    pipelineDetail.setFromCoordinates(fromCoordinatePair);
    pipelineDetail.setToCoordinates(toCoordinatePair);

    var result = padPipelineService.copyDataToNewPadPipeline(detail, pipelineDetail, modifyPipelineForm);

    var captor = ArgumentCaptor.forClass(PadPipeline.class);
    verify(padPipelineRepository, times(1)).save(captor.capture());
    assertThat(captor.getValue()).isEqualTo(result);
  }

  @Test
  public void cleanupData_hiddenData() {

    var pipeline1 = new PadPipeline();
    pipeline1.setTrenchedBuriedBackfilled(false);
    pipeline1.setTrenchingMethodsDescription("desc");
    pipeline1.setPipelineMaterial(PipelineMaterial.CARBON_STEEL);
    pipeline1.setOtherPipelineMaterialUsed("other");

    var pipeline2 = new PadPipeline();
    pipeline2.setTrenchedBuriedBackfilled(false);
    pipeline2.setTrenchingMethodsDescription("desc");
    pipeline2.setPipelineMaterial(PipelineMaterial.DUPLEX);
    pipeline2.setOtherPipelineMaterialUsed("other");

    when(padPipelineRepository.getAllByPwaApplicationDetail(detail)).thenReturn(List.of(pipeline1, pipeline2));

    padPipelineService.cleanupData(detail);

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

    var pipeline2 = new PadPipeline();
    pipeline2.setTrenchedBuriedBackfilled(true);
    pipeline2.setTrenchingMethodsDescription("desc");
    pipeline2.setPipelineMaterial(PipelineMaterial.OTHER);
    pipeline2.setOtherPipelineMaterialUsed("other");

    when(padPipelineRepository.getAllByPwaApplicationDetail(detail)).thenReturn(List.of(pipeline1, pipeline2));

    padPipelineService.cleanupData(detail);

    assertThat(pipeline1.getTrenchingMethodsDescription()).isNotNull();
    assertThat(pipeline1.getOtherPipelineMaterialUsed()).isNotNull();

    assertThat(pipeline2.getTrenchingMethodsDescription()).isNotNull();
    assertThat(pipeline2.getOtherPipelineMaterialUsed()).isNotNull();

    verify(padPipelineRepository, times(1)).saveAll(eq(List.of(pipeline1, pipeline2)));

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

    var validationResult = mockValidatorPadPipelineService.getValidationResult(detail);

    assertThat(validationResult.isSectionComplete()).isTrue();
    assertThat(validationResult.getErrorItems()).isEmpty();
    assertThat(validationResult.getSectionIncompleteError()).isNull();
    assertThat(validationResult.getIdPrefix()).isEqualTo("pipeline-");
    assertThat(validationResult.getInvalidObjectIds()).isEmpty();

  }

  @Test
  public void getValidationResult_errors_noPipelines() {

    when(padPipelineRepository.findAllPipelinesAsSummaryDtoByPwaApplicationDetail(detail)).thenReturn(List.of());

    var validationResult = mockValidatorPadPipelineService.getValidationResult(detail);

    assertThat(validationResult.isSectionComplete()).isFalse();
    assertThat(validationResult.getErrorItems()).isEmpty();
    assertThat(validationResult.getSectionIncompleteError()).isEqualTo(
        "At least one pipeline must be added. Each pipeline must have at least one valid ident.");
    assertThat(validationResult.getIdPrefix()).isEqualTo("pipeline-");
    assertThat(validationResult.getInvalidObjectIds()).isEmpty();

  }

  @Test
  public void getValidationResult_errors_pipelineExists_noIdentsOnIt() {

    when(padPipelineRepository.findAllPipelinesAsSummaryDtoByPwaApplicationDetail(detail)).thenReturn(List.of(
        PadPipelineSummaryDtoTestUtils.generateFrom(padPipe1)
    ));

    when(padPipelineIdentService.getAllIdentsByPadPipelineIds(eq(List.of(new PadPipelineId(1))))).thenReturn(List.of());

    var validationResult = mockValidatorPadPipelineService.getValidationResult(detail);

    assertThat(validationResult.isSectionComplete()).isFalse();
    assertThat(validationResult.getErrorItems())
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(1, "pipeline-1", "TEMPORARY 1 - Production Flowline is not complete")
        );
    assertThat(validationResult.getSectionIncompleteError()).isEqualTo(
        "At least one pipeline must be added. Each pipeline must have at least one valid ident.");
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

    var validationResult = mockValidatorPadPipelineService.getValidationResult(detail);

    assertThat(validationResult.isSectionComplete()).isFalse();
    assertThat(validationResult.getErrorItems())
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(1, "pipeline-1", "TEMPORARY 1 - Production Flowline is not complete")
        );
    assertThat(validationResult.getSectionIncompleteError()).isEqualTo(
        "At least one pipeline must be added. Each pipeline must have at least one valid ident.");
    assertThat(validationResult.getIdPrefix()).isEqualTo("pipeline-");
    assertThat(validationResult.getInvalidObjectIds()).containsExactly("1");

  }

  @Test
  public void doesPipelineHaveTasks_false() {
    EnumSet.of(PipelineStatus.RETURNED_TO_SHORE, PipelineStatus.NEVER_LAID).forEach(pipelineStatus -> {
      padPipe1.setPipelineStatus(pipelineStatus);
      var dto = createPadPipelineSummaryDto(padPipe1);
      var result = padPipelineService.doesPipelineHaveTasks(dto);
      assertThat(result).isFalse();
    });
  }

  @Test
  public void doesPipelineHaveTasks_true() {
    var statusEnums = EnumSet.allOf(PipelineStatus.class);
    statusEnums.removeAll(EnumSet.of(PipelineStatus.RETURNED_TO_SHORE, PipelineStatus.NEVER_LAID));
    statusEnums.forEach(pipelineStatus -> {
      padPipe1.setPipelineStatus(pipelineStatus);
      var dto = createPadPipelineSummaryDto(padPipe1);
      var result = padPipelineService.doesPipelineHaveTasks(dto);
      assertThat(result).isTrue();
    });
  }

  private PadPipelineSummaryDto createPadPipelineSummaryDto(PadPipeline padPipeline) {
    return new PadPipelineSummaryDto(
        padPipeline.getId(),
        padPipeline.getPipeline().getId(),
        padPipeline.getPipelineType(),
        padPipeline.getPipelineRef(),
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
        padPipeline.getPipelineStatusReason());
  }

}
