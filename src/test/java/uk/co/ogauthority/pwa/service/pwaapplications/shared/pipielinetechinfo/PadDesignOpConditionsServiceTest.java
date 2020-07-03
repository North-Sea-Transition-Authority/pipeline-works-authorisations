package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipielinetechinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadDesignOpConditions;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.DesignOpConditionsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadDesignOpConditionsRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadDesignOpConditionsMappingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadDesignOpConditionsService;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInputValidator;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PadDesignOpConditionsValidator;


@RunWith(MockitoJUnitRunner.class)
public class PadDesignOpConditionsServiceTest {

  private PadDesignOpConditionsService padDesignOpConditionsService;

  private PadDesignOpConditionsMappingService padDesignOpConditionsMappingService;

  @Mock
  private PadDesignOpConditionsRepository padDesignOpConditionsRepository;


  private PadDesignOpConditionsValidator validator;

  private EntityAndFormBuilder entityAndFormBuilder;

  private PwaApplicationDetail pwaApplicationDetail;


  @Before
  public void setUp() {
    validator = new PadDesignOpConditionsValidator(new MinMaxInputValidator());
    padDesignOpConditionsMappingService = new PadDesignOpConditionsMappingService();
    padDesignOpConditionsService = new PadDesignOpConditionsService(
        padDesignOpConditionsMappingService, padDesignOpConditionsRepository, validator);
    pwaApplicationDetail = new PwaApplicationDetail();
    entityAndFormBuilder = new EntityAndFormBuilder();
  }

  // Entity/Form  Retrieval/Mapping tests
  @Test
  public void getDesignOpConditionsEntity_existingNotFound() {
    var actualDesignOpConditions = padDesignOpConditionsService.getDesignOpConditionsEntity(pwaApplicationDetail);
    var expectedDesignOpConditions = new PadDesignOpConditions(pwaApplicationDetail);
    assertThat(actualDesignOpConditions).isEqualTo(expectedDesignOpConditions);
  }

  @Test
  public void mapEntityToForm_full() {
    var actualForm = new DesignOpConditionsForm();
    var entity = entityAndFormBuilder.createValidEntity();
    padDesignOpConditionsService.mapEntityToForm(actualForm, entity);
    assertThat(actualForm).isEqualTo(entityAndFormBuilder.createValidForm());
  }

  @Test
  public void mapFormToEntity_full() {
    var actualEntity = new PadDesignOpConditions();
    var form = entityAndFormBuilder.createValidForm();
    padDesignOpConditionsService.saveEntityUsingForm(form, actualEntity);
    assertThat(actualEntity).isEqualTo(entityAndFormBuilder.createValidEntity());
    verify(padDesignOpConditionsRepository, times(1)).save(any(PadDesignOpConditions.class));
  }


  // Validation / Checking tests
  @Test
  public void isComplete_valid() {
    when(padDesignOpConditionsRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(entityAndFormBuilder.createValidEntity()));
    var isValid = padDesignOpConditionsService.isComplete(pwaApplicationDetail);
    assertTrue(isValid);
  }

  @Test
  public void isComplete_invalid() {
    when(padDesignOpConditionsRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.empty());
    var isValid = padDesignOpConditionsService.isComplete(pwaApplicationDetail);
    assertFalse(isValid);
  }

  @Test
  public void validate_fullValidation_valid() {
    var bindingResult = new BeanPropertyBindingResult(null, "empty");
    padDesignOpConditionsService.validate(entityAndFormBuilder.createValidForm(), bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  public void validate_invalid() {
    var form = entityAndFormBuilder.createBlankForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padDesignOpConditionsService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    Assertions.assertTrue(bindingResult.hasErrors());
  }






}