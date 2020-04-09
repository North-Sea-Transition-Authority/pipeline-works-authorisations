package uk.co.ogauthority.pwa.service.pwaapplications.initial;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import javax.validation.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadEnvironmentalDecommissioning;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.EnvironmentalDecommissioningForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.initial.PadEnvironmentalDecommissioningRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.EnvironmentalDecommissioningValidator;

@RunWith(MockitoJUnitRunner.class)
public class PadEnvironmentalDecommissioningServiceTest {

  @Mock
  private PadEnvironmentalDecommissioningRepository padEnvironmentalDecommissioningRepository;

  @Mock
  private EnvironmentalDecommissioningValidator validator;

  private SpringValidatorAdapter groupValidator;

  private PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;
  private PwaApplicationDetail pwaApplicationDetail;
  private Instant instant;

  @Before
  public void setUp() {
    groupValidator = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());
    padEnvironmentalDecommissioningService = new PadEnvironmentalDecommissioningService(
        padEnvironmentalDecommissioningRepository, validator, groupValidator);
    instant = Instant.now();
  }

  @Test
  public void testGetEnvDecomData_NoneSaved() {
    when(padEnvironmentalDecommissioningRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.empty());
    PadEnvironmentalDecommissioning padEnvironmentalDecommissioning = padEnvironmentalDecommissioningService.getEnvDecomData(
        pwaApplicationDetail);
    assertThat(padEnvironmentalDecommissioning.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(padEnvironmentalDecommissioning.getId()).isNull();
  }

  @Test
  public void testGetEnvDecomData_PreExisting() {
    var existingData = new PadEnvironmentalDecommissioning();
    when(padEnvironmentalDecommissioningRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.of(existingData));
    PadEnvironmentalDecommissioning padEnvironmentalDecommissioning = padEnvironmentalDecommissioningService.getEnvDecomData(
        pwaApplicationDetail);
    assertThat(padEnvironmentalDecommissioning).isEqualTo(existingData);
  }

  @Test
  public void mapEntityToForm() {
    var form = new EnvironmentalDecommissioningForm();
    var entity = buildEntity();
    padEnvironmentalDecommissioningService.mapEntityToForm(entity, form);
    assertThat(entity.getTransboundaryEffect()).isEqualTo(form.getTransboundaryEffect());
    assertThat(entity.getEmtHasSubmittedPermits()).isEqualTo(form.getEmtHasSubmittedPermits());
    assertThat(entity.getPermitsSubmitted()).isEqualTo(form.getPermitsSubmitted());
    assertThat(entity.getEmtHasOutstandingPermits()).isEqualTo(form.getEmtHasOutstandingPermits());
    assertThat(entity.getPermitsPendingSubmission()).isEqualTo(form.getPermitsPendingSubmission());
    assertThat(entity.getDecommissioningPlans()).isEqualTo(form.getDecommissioningPlans());
    assertThat(LocalDate.ofInstant(entity.getEmtSubmissionTimestamp(), ZoneId.systemDefault()))
        .isEqualTo(LocalDate.of(form.getEmtSubmissionYear(), form.getEmtSubmissionMonth(), form.getEmtSubmissionDay()));
    assertThat(entity.getEnvironmentalConditions()).isEqualTo(form.getEnvironmentalConditions());
    assertThat(entity.getDecommissioningConditions()).isEqualTo(form.getDecommissioningConditions());
  }

  @Test
  public void saveEntityUsingForm_AllExpanded() {
    var form = buildForm();
    var entity = new PadEnvironmentalDecommissioning();
    padEnvironmentalDecommissioningService.saveEntityUsingForm(entity, form);
    assertThat(entity.getTransboundaryEffect()).isEqualTo(form.getTransboundaryEffect());
    assertThat(entity.getEmtHasSubmittedPermits()).isEqualTo(form.getEmtHasSubmittedPermits());
    assertThat(entity.getPermitsSubmitted()).isEqualTo(form.getPermitsSubmitted());
    assertThat(entity.getEmtHasOutstandingPermits()).isEqualTo(form.getEmtHasOutstandingPermits());
    assertThat(entity.getPermitsPendingSubmission()).isEqualTo(form.getPermitsPendingSubmission());
    assertThat(entity.getDecommissioningPlans()).isEqualTo(form.getDecommissioningPlans());
    assertThat(LocalDate.ofInstant(entity.getEmtSubmissionTimestamp(), ZoneId.systemDefault()))
        .isEqualTo(LocalDate.of(2020, 3, 18));
    assertThat(entity.getEnvironmentalConditions()).isEqualTo(form.getEnvironmentalConditions());
    assertThat(entity.getDecommissioningConditions()).isEqualTo(form.getDecommissioningConditions());
  }

  @Test
  public void saveEntityUsingForm_NoneExpanded() {
    var form = buildForm();
    form.setEmtHasOutstandingPermits(false);
    form.setEmtHasSubmittedPermits(false);
    var entity = new PadEnvironmentalDecommissioning();
    padEnvironmentalDecommissioningService.saveEntityUsingForm(entity, form);
    assertThat(entity.getTransboundaryEffect()).isEqualTo(form.getTransboundaryEffect());
    assertThat(entity.getEmtHasSubmittedPermits()).isEqualTo(form.getEmtHasSubmittedPermits());
    assertThat(entity.getPermitsSubmitted()).isNull();
    assertThat(entity.getEmtHasOutstandingPermits()).isEqualTo(form.getEmtHasOutstandingPermits());
    assertThat(entity.getPermitsPendingSubmission()).isNull();
    assertThat(entity.getDecommissioningPlans()).isEqualTo(form.getDecommissioningPlans());
    assertThat(entity.getEmtSubmissionTimestamp()).isNull();
    assertThat(entity.getEnvironmentalConditions()).isEqualTo(form.getEnvironmentalConditions());
    assertThat(entity.getDecommissioningConditions()).isEqualTo(form.getDecommissioningConditions());
  }

  @Test
  public void saveEntityUsingForm_NullValues() {
    var form = new EnvironmentalDecommissioningForm();
    var entity = new PadEnvironmentalDecommissioning();
    padEnvironmentalDecommissioningService.saveEntityUsingForm(entity, form);
    assertThat(entity.getTransboundaryEffect()).isNull();
    assertThat(entity.getEmtHasSubmittedPermits()).isNull();
    assertThat(entity.getPermitsSubmitted()).isNull();
    assertThat(entity.getEmtHasOutstandingPermits()).isNull();
    assertThat(entity.getPermitsPendingSubmission()).isNull();
    assertThat(entity.getDecommissioningPlans()).isNull();
    assertThat(entity.getEmtSubmissionTimestamp()).isNull();
    assertThat(entity.getEnvironmentalConditions()).isNull();
    assertThat(entity.getDecommissioningConditions()).isNull();
  }

  @Test
  public void validate_partial_fail() {

    var form = new EnvironmentalDecommissioningForm();
    form.setPermitsSubmitted(ValidatorTestUtils.over4000Chars());
    form.setDecommissioningPlans(ValidatorTestUtils.over4000Chars());
    form.setPermitsPendingSubmission(ValidatorTestUtils.over4000Chars());

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    padEnvironmentalDecommissioningService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(
        entry("permitsSubmitted", Set.of("Length")),
        entry("decommissioningPlans", Set.of("Length")),
        entry("permitsPendingSubmission", Set.of("Length"))
    );

    verifyNoInteractions(validator);

  }

  @Test
  public void validate_partial_pass() {

    var form = new EnvironmentalDecommissioningForm();
    form.setPermitsSubmitted(ValidatorTestUtils.exactly4000chars());
    form.setDecommissioningPlans(ValidatorTestUtils.exactly4000chars());
    form.setPermitsPendingSubmission(ValidatorTestUtils.exactly4000chars());

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    padEnvironmentalDecommissioningService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).isEmpty();

    verifyNoInteractions(validator);

  }

  @Test
  public void validate_full_fail() {

    var form = new EnvironmentalDecommissioningForm();
    form.setPermitsSubmitted(ValidatorTestUtils.over4000Chars());
    form.setPermitsPendingSubmission(ValidatorTestUtils.over4000Chars());

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    padEnvironmentalDecommissioningService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(
        entry("transboundaryEffect", Set.of("NotNull")),
        entry("emtHasSubmittedPermits", Set.of("NotNull")),
        entry("permitsSubmitted", Set.of("Length")),
        entry("emtHasOutstandingPermits", Set.of("NotNull")),
        entry("permitsPendingSubmission", Set.of("Length")),
        entry("decommissioningPlans", Set.of("NotNull"))
    );

    verify(validator, times(1)).validate(form, bindingResult);

  }

  @Test
  public void validate_full_pass() {

    var form = new EnvironmentalDecommissioningForm();
    form.setTransboundaryEffect(true);
    form.setEmtHasSubmittedPermits(false);
    form.setEmtHasOutstandingPermits(true);
    form.setPermitsPendingSubmission(ValidatorTestUtils.exactly4000chars());
    form.setDecommissioningPlans(ValidatorTestUtils.exactly4000chars());

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    padEnvironmentalDecommissioningService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).isEmpty();

    verify(validator, times(1)).validate(form, bindingResult);

  }

  private PadEnvironmentalDecommissioning buildEntity() {
    var entity = new PadEnvironmentalDecommissioning();
    entity.setTransboundaryEffect(true);
    entity.setEmtHasSubmittedPermits(true);
    entity.setPermitsSubmitted("Submitted permits");
    entity.setEmtHasOutstandingPermits(true);
    entity.setPermitsPendingSubmission("Pending permits");
    entity.setDecommissioningPlans("Decom plans");
    entity.setEmtSubmissionTimestamp(instant);
    entity.setEnvironmentalConditions(EnumSet.allOf(EnvironmentalCondition.class));
    entity.setDecommissioningConditions(EnumSet.allOf(DecommissioningCondition.class));
    return entity;
  }

  private EnvironmentalDecommissioningForm buildForm() {
    var form = new EnvironmentalDecommissioningForm();
    form.setTransboundaryEffect(true);
    form.setEmtHasSubmittedPermits(true);
    form.setPermitsSubmitted("Submitted text");
    form.setEmtHasOutstandingPermits(true);
    form.setPermitsPendingSubmission("Pending text");
    form.setDecommissioningPlans("Decom text");
    form.setEmtSubmissionYear(2020);
    form.setEmtSubmissionMonth(3);
    form.setEmtSubmissionDay(18);
    form.setEnvironmentalConditions(EnumSet.allOf(EnvironmentalCondition.class));
    form.setDecommissioningConditions(EnumSet.allOf(DecommissioningCondition.class));
    return form;
  }
}