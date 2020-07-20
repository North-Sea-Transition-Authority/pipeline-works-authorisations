package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.util.FieldUtils;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.ModifyPipelineController;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineFlexibility;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
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

  private PadPipelineService padPipelineService;

  private PwaApplicationDetail detail;

  private PipelineIdentFormValidator pipelineIdentFormValidator;

  @Mock
  private PadPipelineIdentService padPipelineIdentService;

  @Captor
  private ArgumentCaptor<PadPipeline> padPipelineArgumentCaptor;

  @Before
  public void setUp() {

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
        padPipelineIdentService, pipelineIdentFormValidator);

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

    verify(padPipelineRepository, times(1)).save(padPipelineArgumentCaptor.capture());
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
    verify(padPipelineRepository, times(1)).save(padPipelineArgumentCaptor.capture());
    var newPipeline = padPipelineArgumentCaptor.getValue();
    assertThat(newPipeline.getOtherPipelineMaterialUsed()).isEqualTo(form.getOtherPipelineMaterialUsed());
  }


  @Test
  public void isComplete_noPipes() {

    var detail = new PwaApplicationDetail();
    when(padPipelineRepository.countAllByPwaApplicationDetail(detail)).thenReturn(0L);

    assertThat(padPipelineService.isComplete(detail)).isFalse();

  }

  @Test
  public void isComplete_notAllPipesHaveIdents() {

    var detail = new PwaApplicationDetail();
    when(padPipelineRepository.countAllByPwaApplicationDetail(detail)).thenReturn(1L);
    when(padPipelineRepository.countAllWithNoIdentsByPwaApplicationDetail(detail)).thenReturn(1L);

    assertThat(padPipelineService.isComplete(detail)).isFalse();

  }

  @Test
  public void isComplete_allPipesHaveIdents() {

    var detail = new PwaApplicationDetail();
    when(padPipelineRepository.countAllByPwaApplicationDetail(detail)).thenReturn(1L);
    when(padPipelineRepository.countAllWithNoIdentsByPwaApplicationDetail(detail)).thenReturn(0L);

    assertThat(padPipelineService.isComplete(detail)).isTrue();

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

    when(pipelineService.getActivePipelineDetailsForApplicationMasterPwa(detail.getPwaApplication()))
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

    when(pipelineService.getActivePipelineDetailsForApplicationMasterPwa(detail.getPwaApplication()))
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
    when(padPipelineRepository.getMasterPipelineIdsOnApplication(detail)).thenReturn(List.of(1));
    var result = padPipelineService.getMasterPipelineIds(detail);
    assertThat(result).containsExactly(1);
  }

  @Test
  public void getPadPipelinesByMasterAndIds_serviceInteraction() {
    var master = detail.getMasterPwaApplication();
    var padPipeline = new PadPipeline();
    when(padPipelineRepository.getPadPipelineByMasterPwaAndPipelineIds(master, List.of(1)))
        .thenReturn(List.of(padPipeline));
    var result = padPipelineService.getPadPipelinesByMasterAndIds(master, List.of(1));
    assertThat(result).containsExactly(padPipeline);
  }

  @Test
  public void copyDataToNewPadPipeline_verifyDataMatches() {
    var padPipeline = new PadPipeline();
    var pipeline = new Pipeline();

    var fromCoordinatePair = new CoordinatePair(
        new LatitudeCoordinate(1, 1, BigDecimal.ZERO, LatitudeDirection.NORTH),
        new LongitudeCoordinate(1, 1, BigDecimal.ZERO, LongitudeDirection.EAST)
    );

    var toCoordinatePair = new CoordinatePair(
        new LatitudeCoordinate(2, 2, BigDecimal.ZERO, LatitudeDirection.SOUTH),
        new LongitudeCoordinate(2, 2, BigDecimal.ZERO, LongitudeDirection.WEST)
    );

    padPipeline.setBundleName("bundle");
    padPipeline.setPipelineRef("ref");
    padPipeline.setPipeline(pipeline);
    padPipeline.setComponentPartsDescription("comp desc");
    padPipeline.setFromCoordinates(fromCoordinatePair);
    padPipeline.setFromLocation("a");
    padPipeline.setLength(BigDecimal.ONE);
    padPipeline.setOtherPipelineMaterialUsed("materials used");
    padPipeline.setPipelineDesignLife(1);
    padPipeline.setPipelineFlexibility(PipelineFlexibility.FLEXIBLE);
    padPipeline.setPipelineInBundle(true);
    padPipeline.setPipelineMaterial(PipelineMaterial.CARBON_STEEL);
    padPipeline.setPipelineType(PipelineType.GAS_LIFT_PIPELINE);
    padPipeline.setProductsToBeConveyed("products");
    padPipeline.setToCoordinates(toCoordinatePair);
    padPipeline.setToLocation("b");
    padPipeline.setTrenchedBuriedBackfilled(true);
    padPipeline.setTrenchingMethodsDescription("trench desc");

    var pipelineWithCopiedData = padPipelineService.copyDataToNewPadPipeline(detail, padPipeline);

    // Ensure the pipeline returned is a different object to the one passed
    assertThat(pipelineWithCopiedData == padPipeline).isFalse();

    assertThat(pipelineWithCopiedData.getBundleName()).isEqualTo(padPipeline.getBundleName());
    assertThat(pipelineWithCopiedData.getPipelineRef()).isEqualTo(padPipeline.getPipelineRef());
    assertThat(pipelineWithCopiedData.getPipeline()).isEqualTo(padPipeline.getPipeline());
    assertThat(pipelineWithCopiedData.getComponentPartsDescription()).isEqualTo(padPipeline.getComponentPartsDescription());
    assertThat(pipelineWithCopiedData.getFromCoordinates()).isEqualTo(padPipeline.getFromCoordinates());
    assertThat(pipelineWithCopiedData.getFromLocation()).isEqualTo(padPipeline.getFromLocation());
    assertThat(pipelineWithCopiedData.getLength()).isEqualTo(padPipeline.getLength());
    assertThat(pipelineWithCopiedData.getOtherPipelineMaterialUsed()).isEqualTo(padPipeline.getOtherPipelineMaterialUsed());
    assertThat(pipelineWithCopiedData.getPipelineDesignLife()).isEqualTo(padPipeline.getPipelineDesignLife());
    assertThat(pipelineWithCopiedData.getPipelineFlexibility()).isEqualTo(padPipeline.getPipelineFlexibility());
    assertThat(pipelineWithCopiedData.getPipelineInBundle()).isEqualTo(padPipeline.getPipelineInBundle());
    assertThat(pipelineWithCopiedData.getPipelineMaterial()).isEqualTo(padPipeline.getPipelineMaterial());
    assertThat(pipelineWithCopiedData.getPipelineType()).isEqualTo(padPipeline.getPipelineType());
    assertThat(pipelineWithCopiedData.getProductsToBeConveyed()).isEqualTo(padPipeline.getProductsToBeConveyed());
    assertThat(pipelineWithCopiedData.getToCoordinates()).isEqualTo(padPipeline.getToCoordinates());
    assertThat(pipelineWithCopiedData.getToLocation()).isEqualTo(padPipeline.getToLocation());
    assertThat(pipelineWithCopiedData.getTrenchedBuriedBackfilled()).isEqualTo(padPipeline.getTrenchedBuriedBackfilled());
    assertThat(pipelineWithCopiedData.getTrenchingMethodsDescription()).isEqualTo(padPipeline.getTrenchingMethodsDescription());

  }

  @Test
  public void copyDataToNewPadPipeline_verifySaved() {
    var pipeline = new PadPipeline();

    var fromCoordinatePair = new CoordinatePair(
        new LatitudeCoordinate(1, 1, BigDecimal.ZERO, LatitudeDirection.NORTH),
        new LongitudeCoordinate(1, 1, BigDecimal.ZERO, LongitudeDirection.EAST)
    );

    var toCoordinatePair = new CoordinatePair(
        new LatitudeCoordinate(2, 2, BigDecimal.ZERO, LatitudeDirection.SOUTH),
        new LongitudeCoordinate(2, 2, BigDecimal.ZERO, LongitudeDirection.WEST)
    );

    pipeline.setFromCoordinates(fromCoordinatePair);
    pipeline.setToCoordinates(toCoordinatePair);

    var result = padPipelineService.copyDataToNewPadPipeline(detail, pipeline);

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

}
