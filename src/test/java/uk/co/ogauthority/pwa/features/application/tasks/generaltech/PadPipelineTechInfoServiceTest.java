package uk.co.ogauthority.pwa.features.application.tasks.generaltech;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


@ExtendWith(MockitoExtension.class)
class PadPipelineTechInfoServiceTest {

  private PadPipelineTechInfoService padPipelineTechInfoService;

  @Mock
  private PipelineTechInfoMappingService pipelineTechInfoMappingService;

  @Mock
  private PadPipelineTechInfoRepository padPipelineTechInfoRepository;

  @Mock
  private PipelineTechInfoValidator validator;

  @Mock
  private EntityCopyingService entityCopyingService;

  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {
    padPipelineTechInfoService = new PadPipelineTechInfoService(
        padPipelineTechInfoRepository,
        pipelineTechInfoMappingService,
        validator,
        entityCopyingService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }

  private PipelineTechInfoForm createValidForm() {
    var form = new PipelineTechInfoForm();
    form.setEstimatedAssetLife(5);
    form.setPipelineDesignedToStandards(true);
    form.setPipelineStandardsDescription("description");
    form.setCorrosionDescription("description");
    form.setPlannedPipelineTieInPoints(true);
    form.setTieInPointsDescription("description");
    return form;
  }

  private PadPipelineTechInfo createValidEntity() {
    var entity = new PadPipelineTechInfo();
    entity.setEstimatedAssetLife(5);
    entity.setPipelineDesignedToStandards(true);
    entity.setPipelineStandardsDescription("description");
    entity.setCorrosionDescription("description");
    entity.setPlannedPipelineTieInPoints(true);
    entity.setTieInPointsDescription("description");
    return entity;
  }

  @Test
  void getPipelineTechInfoEntity_existingNotFound() {
    var padPipelineTechInfo = padPipelineTechInfoService.getPipelineTechInfoEntity(pwaApplicationDetail);
    var expectedPadPipelineTechInfo = new PadPipelineTechInfo();
    expectedPadPipelineTechInfo.setPwaApplicationDetail(pwaApplicationDetail);
    assertThat(padPipelineTechInfo).isEqualTo(expectedPadPipelineTechInfo);
  }

  @Test
  void saveEntityUsingForm() {
    padPipelineTechInfoService.saveEntityUsingForm(new PipelineTechInfoForm(), new PadPipelineTechInfo());
    verify(padPipelineTechInfoRepository, times(1)).save(any(PadPipelineTechInfo.class));
  }

  @Test
  void getGeneralTechInfoView() {
    var entity = createValidEntity();
    when(padPipelineTechInfoRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(entity));
    var view = padPipelineTechInfoService.getGeneralTechInfoView(pwaApplicationDetail);

    assertThat(view.getEstimatedAssetLife()).isEqualTo(entity.getEstimatedAssetLife());
    assertThat(view.getPipelineDesignedToStandards()).isEqualTo(entity.getPipelineDesignedToStandards());
    assertThat(view.getPipelineStandardsDescription()).isEqualTo(entity.getPipelineStandardsDescription());
    assertThat(view.getCorrosionDescription()).isEqualTo(entity.getCorrosionDescription());
    assertThat(view.getPlannedPipelineTieInPoints()).isEqualTo(entity.getPlannedPipelineTieInPoints());
    assertThat(view.getTieInPointsDescription()).isEqualTo(entity.getTieInPointsDescription());
  }


  @Test
  void isComplete_valid() {
    when(padPipelineTechInfoRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(createValidEntity()));
    var isValid = padPipelineTechInfoService.isComplete(pwaApplicationDetail);
    assertTrue(isValid);
  }

  @Test
  void validate_anyType_withErrors_bindingResultHasErrors() {

    var bindingResult = new BeanPropertyBindingResult(new PipelineTechInfoForm(), "form");

    ControllerTestUtils.mockSmartValidatorErrors(validator, List.of("estimatedAssetLife"));

    Arrays.stream(ValidationType.values()).forEach(validationType -> {
      padPipelineTechInfoService.validate(createValidForm(), bindingResult, validationType, pwaApplicationDetail);
      assertTrue(bindingResult.hasErrors());
    });
  }

  @Test
  void validate_anyType_noErrors_bindingResultHasNoErrors() {

    var bindingResult = new BeanPropertyBindingResult(new PipelineTechInfoForm(), "form");

    Arrays.stream(ValidationType.values()).forEach(validationType -> {
      padPipelineTechInfoService.validate(createValidForm(), bindingResult, validationType, pwaApplicationDetail);
      assertFalse(bindingResult.hasErrors());
    });

  }

  @Test
  void cleanupData_hiddenData() {

    var techInfo = new PadPipelineTechInfo();

    techInfo.setPipelineDesignedToStandards(false);
    techInfo.setPipelineStandardsDescription("standards");

    techInfo.setPlannedPipelineTieInPoints(false);
    techInfo.setTieInPointsDescription("tie");

    when(padPipelineTechInfoRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(techInfo));

    padPipelineTechInfoService.cleanupData(pwaApplicationDetail);

    assertThat(techInfo.getPipelineStandardsDescription()).isNull();

    assertThat(techInfo.getTieInPointsDescription()).isNull();

    verify(padPipelineTechInfoRepository, times(1)).save(techInfo);

  }

  @Test
  void cleanupData_noHiddenData() {

    var techInfo = new PadPipelineTechInfo();

    techInfo.setPipelineDesignedToStandards(true);
    techInfo.setPipelineStandardsDescription("standards");

    techInfo.setPlannedPipelineTieInPoints(true);
    techInfo.setTieInPointsDescription("tie");

    when(padPipelineTechInfoRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(techInfo));

    padPipelineTechInfoService.cleanupData(pwaApplicationDetail);

    assertThat(techInfo.getPipelineStandardsDescription()).isNotNull();

    assertThat(techInfo.getTieInPointsDescription()).isNotNull();

    verify(padPipelineTechInfoRepository, times(1)).save(techInfo);

  }


}
