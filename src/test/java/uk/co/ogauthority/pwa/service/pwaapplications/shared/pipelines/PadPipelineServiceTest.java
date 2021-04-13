package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
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
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.tasklist.PadPipelineDataCopierService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
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

  @Mock
  private PadPipelineDataCopierService padPipelineDataCopierService;

  @Mock
  private PipelineHeaderFormValidator pipelineHeaderFormValidator;

  @Mock
  private PadOptionConfirmedService padOptionConfirmedService;

  private PadPipelineService padPipelineService;

  private PwaApplicationDetail detail;

  private PipelineIdentFormValidator pipelineIdentFormValidator;

  @Mock
  private PadPipelineIdentService padPipelineIdentService;

  @Captor
  private ArgumentCaptor<PadPipeline> padPipelineArgumentCaptor;

  @Captor
  private ArgumentCaptor<List<PadPipeline>> padPipelineListArgCaptor;

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

    padPipelineService = new PadPipelineService(padPipelineRepository,
        pipelineService,
        pipelineDetailService,
        padPipelinePersisterService,
        pipelineHeaderFormValidator);

    mockValidatorPadPipelineService = new PadPipelineService(padPipelineRepository,
        pipelineService,
        pipelineDetailService,
        padPipelinePersisterService,
        pipelineHeaderFormValidator);

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
  public void updatePipeline_whenStatusIsOutOfUse() {
    var form = new PipelineHeaderForm();
    form.setWhyNotReturnedToShore("reason");
    form.setFromCoordinateForm(new CoordinateForm());
    form.setToCoordinateForm(new CoordinateForm());
    form.setTrenchedBuriedBackfilled(false);
    form.setPipelineMaterial(PipelineMaterial.DUPLEX);
    form.setPipelineInBundle(false);

    var padPipeline = new PadPipeline();
    padPipeline.setPipelineStatus(PipelineStatus.OUT_OF_USE_ON_SEABED);

    padPipelineService.updatePipeline(padPipeline, form);
    assertThat(padPipeline.getPipelineStatusReason()).isEqualTo("reason");
  }


  @Test
  public void updatePipeline_whenPipelineAlreadyExistsOnSeabedQuestionRequired() {
    var form = new PipelineHeaderForm();
    form.setAlreadyExistsOnSeabed(true);
    form.setPipelineInUse(true);
    form.setFromCoordinateForm(new CoordinateForm());
    form.setToCoordinateForm(new CoordinateForm());
    form.setTrenchedBuriedBackfilled(false);
    form.setPipelineMaterial(PipelineMaterial.DUPLEX);
    form.setPipelineInBundle(false);

    var padPipeline = new PadPipeline();

    padPipelineService.updatePipeline(padPipeline, form);
    assertThat(padPipeline.getAlreadyExistsOnSeabed()).isEqualTo(form.getAlreadyExistsOnSeabed());
    assertThat(padPipeline.getPipelineInUse()).isEqualTo(form.getPipelineInUse());
  }

  @Test
  public void canShowAlreadyExistsOnSeabedQuestions_cat2AppType_applicationPipeline() {
    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    when(pipelineDetailService.isPipelineConsented(pipeline)).thenReturn(false);

    var canShow = padPipelineService.canShowAlreadyExistsOnSeabedQuestions(padPipeline, PwaApplicationType.CAT_2_VARIATION);
    assertThat(canShow).isTrue();
  }

  @Test
  public void canShowAlreadyExistsOnSeabedQuestions_decomAppType_applicationPipeline() {
    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    when(pipelineDetailService.isPipelineConsented(pipeline)).thenReturn(false);

    var canShow = padPipelineService.canShowAlreadyExistsOnSeabedQuestions(padPipeline, PwaApplicationType.DECOMMISSIONING);
    assertThat(canShow).isTrue();
  }

  @Test
  public void canShowAlreadyExistsOnSeabedQuestions_validAppType_consentedPipeline() {
    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    when(pipelineDetailService.isPipelineConsented(pipeline)).thenReturn(true);

    var canShow = padPipelineService.canShowAlreadyExistsOnSeabedQuestions(padPipeline, PwaApplicationType.CAT_2_VARIATION);
    assertThat(canShow).isFalse();
  }

  @Test
  public void canShowAlreadyExistsOnSeabedQuestions_invalidAppType_applicationPipeline() {
    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    var canShow = padPipelineService.canShowAlreadyExistsOnSeabedQuestions(padPipeline, PwaApplicationType.INITIAL);
    assertThat(canShow).isFalse();
  }

  @Test
  public void getPipelineHeaderFormContext_pipelineNull() {
    var headerFormContext = padPipelineService.getPipelineHeaderFormContext(null);
    assertThat(headerFormContext).isEqualTo(PipelineHeaderFormContext.NON_CONSENTED_PIPELINE);
  }

  @Test
  public void getPipelineHeaderFormContext_nonConsentedPipeline() {
    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    when(pipelineDetailService.isPipelineConsented(pipeline)).thenReturn(false);
    var headerFormContext = padPipelineService.getPipelineHeaderFormContext(padPipeline);
    assertThat(headerFormContext).isEqualTo(PipelineHeaderFormContext.NON_CONSENTED_PIPELINE);
  }

  @Test
  public void getPipelineHeaderFormContext_consentedPipeline() {
    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    when(pipelineDetailService.isPipelineConsented(pipeline)).thenReturn(true);
    var headerFormContext = padPipelineService.getPipelineHeaderFormContext(padPipeline);
    assertThat(headerFormContext).isEqualTo(PipelineHeaderFormContext.CONSENTED_PIPELINE);
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
    pipelineDetail.setMaxExternalDiameter(BigDecimal.TEN);

    modifyPipelineForm.setPipelineStatus(PipelineStatus.OUT_OF_USE_ON_SEABED);
    modifyPipelineForm.setPipelineStatusReason("reason");

    var pipelineWithCopiedData = padPipelineService.copyDataToNewPadPipeline(detail, pipelineDetail,
        modifyPipelineForm);

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
    assertThat(pipelineWithCopiedData.getPipelineStatusReason()).isEqualTo(
        modifyPipelineForm.getPipelineStatusReason());
    assertThat(pipelineWithCopiedData.getMaxExternalDiameter()).isEqualTo(pipelineDetail.getMaxExternalDiameter());

  }

  @Test
  public void copyDataToNewPadPipeline_noReason_notOnSeabed() {
    var pipelineDetail = new PipelineDetail();
    modifyPipelineForm.setPipelineStatus(PipelineStatus.IN_SERVICE);
    modifyPipelineForm.setPipelineStatusReason("reason");
    var pipelineWithCopiedData = padPipelineService.copyDataToNewPadPipeline(detail, pipelineDetail,
        modifyPipelineForm);
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
  public void getPipelineOverviewMap_correctGrouping() {
    when(padPipelineRepository.getAllByPwaApplicationDetailAndIdIn(detail, List.of(padPipe1.getId())))
        .thenReturn(List.of(padPipe1));
    padPipe1.setPipelineStatus(PipelineStatus.IN_SERVICE);
    var result = padPipelineService.getPadPipelineMapForOverviews(detail, List.of(new PadPipelineOverview(padPipe1, 0L)));
    assertThat(result).containsExactly(
        entry(new PadPipelineId(padPipe1.getId()), padPipe1)
    );
  }

  @Test
  public void isValidationRequiredByStatus_statusSmokeTest() {
    PipelineStatus.stream().forEach(pipelineStatus -> {
      padPipe1.setPipelineStatus(pipelineStatus);
      switch (pipelineStatus) {
        case IN_SERVICE:
        case OUT_OF_USE_ON_SEABED:
          assertThat(padPipelineService.isValidationRequiredByStatus(padPipe1.getPipelineStatus())).isTrue();
          break;
        default:
          assertThat(padPipelineService.isValidationRequiredByStatus(padPipe1.getPipelineStatus())).isFalse();
      }
    });
  }

  @Test
  public void isPadPipelineValid_valid() {
    PipelineStatus.stream().forEach(pipelineStatus -> {
      padPipe1.setPipelineStatus(pipelineStatus);
      assertThat(padPipelineService.isPadPipelineValid(padPipe1, detail.getPwaApplicationType())).isTrue();
    });
  }

  @Test
  public void isPadPipelineValid_withError_invalid() {

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new ObjectError("", ""));
      return null;
    }).when(pipelineHeaderFormValidator).validate(any(), any(), any());

    PipelineStatus.stream().forEach(pipelineStatus -> {
      padPipe1.setPipelineStatus(pipelineStatus);
      var result = padPipelineService.isPadPipelineValid(padPipe1, detail.getPwaApplicationType());
      switch (pipelineStatus) {
        case IN_SERVICE:
        case OUT_OF_USE_ON_SEABED:
          assertThat(result).isFalse();
          break;
        default:
          assertThat(result).isTrue();
      }
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

}
