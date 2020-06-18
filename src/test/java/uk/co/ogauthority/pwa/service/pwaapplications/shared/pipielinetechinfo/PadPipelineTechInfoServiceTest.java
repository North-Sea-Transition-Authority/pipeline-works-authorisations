package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipielinetechinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineTechInfo;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineTechInfoForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadPipelineTechInfoRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadPipelineTechInfoService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PipelineTechInfoMappingService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PipelineTechInfoValidator;


@RunWith(MockitoJUnitRunner.class)
public class PadPipelineTechInfoServiceTest {

  private PadPipelineTechInfoService padPipelineTechInfoService;

  @Mock
  private PipelineTechInfoMappingService pipelineTechInfoMappingService;

  @Mock
  private PadPipelineTechInfoRepository padPipelineTechInfoRepository;

  @Mock
  private SpringValidatorAdapter springValidatorAdapter;

  @Mock
  private PipelineTechInfoValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;


  @Before
  public void setUp() {
    padPipelineTechInfoService = new PadPipelineTechInfoService(padPipelineTechInfoRepository,
        pipelineTechInfoMappingService, springValidatorAdapter, validator);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }

  private PipelineTechInfoForm createValidForm() {
    var form = new PipelineTechInfoForm();
    form.setEstimatedFieldLife(5);
    form.setPipelineDesignedToStandards(true);
    form.setPipelineStandardsDescription("description");
    form.setCorrosionDescription("description");
    form.setPlannedPipelineTieInPoints(true);
    form.setTieInPointsDescription("description");
    return form;
  }

  private PadPipelineTechInfo createValidEntity() {
    var entity = new PadPipelineTechInfo();
    entity.setEstimatedFieldLife(5);
    entity.setPipelineDesignedToStandards(true);
    entity.setPipelineStandardsDescription("description");
    entity.setCorrosionDescription("description");
    entity.setPlannedPipelineTieInPoints(true);
    entity.setTieInPointsDescription("description");
    return entity;
  }



  @Test
  public void getPipelineTechInfoEntity_existingNotFound() {
    var padPipelineTechInfo = padPipelineTechInfoService.getPipelineTechInfoEntity(pwaApplicationDetail);
    var expectedPadPipelineTechInfo = new PadPipelineTechInfo();
    expectedPadPipelineTechInfo.setPwaApplicationDetail(pwaApplicationDetail);
    assertThat(padPipelineTechInfo).isEqualTo(expectedPadPipelineTechInfo);
  }

  @Test
  public void saveEntityUsingForm() {
    padPipelineTechInfoService.saveEntityUsingForm(new PipelineTechInfoForm(), new PadPipelineTechInfo());
    verify(padPipelineTechInfoRepository, times(1)).save(any(PadPipelineTechInfo.class));
  }


  @Test
  public void isComplete_valid() {
    when(padPipelineTechInfoRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(createValidEntity()));
    var isValid = padPipelineTechInfoService.isComplete(pwaApplicationDetail);
    assertTrue(isValid);
  }

  @Test
  public void validate_fullValidation_valid() {
    var bindingResult = new BeanPropertyBindingResult(null, "empty");
    padPipelineTechInfoService.validate(createValidForm(), bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertFalse(bindingResult.hasErrors());
  }




}