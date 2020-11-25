package uk.co.ogauthority.pwa.service.pwaapplications.options;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;

import java.util.EnumSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.options.PadConfirmationOfOption;
import uk.co.ogauthority.pwa.model.form.pwaapplications.options.ConfirmOptionForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.options.PadConfirmationOfOptionRepository;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.options.ConfirmOptionFormValidator;

@RunWith(MockitoJUnitRunner.class)
public class PadConfirmationOfOptionServiceTest {

  @Mock
  private ApproveOptionsService approveOptionsService;

  @Mock
  private PadConfirmationOfOptionRepository padConfirmationOfOptionRepository;

  @Mock
  private ConfirmOptionFormValidator confirmOptionFormValidator;

  private PadConfirmationOfOptionService padConfirmationOfOptionService;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    padConfirmationOfOptionService = new PadConfirmationOfOptionService(
        approveOptionsService,
        padConfirmationOfOptionRepository,
        confirmOptionFormValidator
    );

    // always return input binding result
    doAnswer(invocation -> invocation.getArgument(1))
        .when(confirmOptionFormValidator).validate(any(), any(), any());
    when(confirmOptionFormValidator.supports(any())).thenReturn(true);

  }

  @Test
  public void canShowInTaskList_whenOptionsNotApproved() {

    assertThat(padConfirmationOfOptionService.canShowInTaskList(pwaApplicationDetail)).isFalse();

  }

  @Test
  public void canShowInTaskList_whenOptionsApproved() {
    when(approveOptionsService.optionsApproved(pwaApplicationDetail.getPwaApplication())).thenReturn(true);

    assertThat(padConfirmationOfOptionService.canShowInTaskList(pwaApplicationDetail)).isTrue();

  }

  @Test
  public void isComplete_whenValidationPasses() {

    var confirmation = new PadConfirmationOfOption();
    when(padConfirmationOfOptionRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(Optional.of(confirmation));

    assertThat(padConfirmationOfOptionService.isComplete(pwaApplicationDetail)).isTrue();

    verify(confirmOptionFormValidator, times(1))
        .validate(any(), any(), eq(ValidationType.FULL));

  }

  @Test
  public void isComplete_whenValidationFails() {

    var confirmation = new PadConfirmationOfOption();
    when(padConfirmationOfOptionRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(Optional.of(confirmation));

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.rejectValue("confirmedOptionType", REQUIRED.errorCode("confirmedOptionType"));
      return invocation;
    }).when(confirmOptionFormValidator).validate(any(), any(), eq(ValidationType.FULL));

    assertThat(padConfirmationOfOptionService.isComplete(pwaApplicationDetail)).isFalse();

  }

  @Test
  public void mapEntityToForm_whenWorkCompleteAsPerOptions() {
    var form = new ConfirmOptionForm();
    var confirmation = new PadConfirmationOfOption();
    confirmation.setChosenOptionDesc("SOMETHING");
    confirmation.setConfirmedOptionType(WORK_COMPLETE_AS_PER_OPTIONS);

    padConfirmationOfOptionService.mapEntityToForm(form, confirmation);

    assertThat(form.getConfirmedOptionType()).isEqualTo(confirmation.getConfirmedOptionType());
    assertThat(form.getOptionCompletedDescription()).isEqualTo(confirmation.getChosenOptionDesc());
  }

  @Test
  public void mapFormToEntity_whenWorkCompleteAsPerOptions() {
    var confirmation = new PadConfirmationOfOption();
    var form = new ConfirmOptionForm();
    form.setOptionCompletedDescription("SOMETHING");
    form.setConfirmedOptionType(WORK_COMPLETE_AS_PER_OPTIONS);

    padConfirmationOfOptionService.mapFormToEntity(form, confirmation);

    assertThat(confirmation.getChosenOptionDesc()).isEqualTo(form.getOptionCompletedDescription());
    assertThat(confirmation.getConfirmedOptionType()).isEqualTo(form.getConfirmedOptionType());

  }

  @Test
  public void mapFormToEntity_whenDescriptionNotCollected() {
    var noDescriptionOptions = EnumSet.complementOf(EnumSet.of(ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS));

    for(ConfirmedOptionType noDescOption : noDescriptionOptions){
      var confirmation = new PadConfirmationOfOption();
      confirmation.setChosenOptionDesc("SOMETHING");
      var form = new ConfirmOptionForm();
      form.setConfirmedOptionType(noDescOption);
      form.setOptionCompletedDescription("IGNORED STRING FROM HIDDEN CONTENT");

      padConfirmationOfOptionService.mapFormToEntity(form, confirmation);

      assertThat(confirmation.getChosenOptionDesc()).isNull();
      assertThat(confirmation.getConfirmedOptionType()).isEqualTo(form.getConfirmedOptionType());

    }

  }

  @Test
  public void findPadConfirmationOfOption_whenFound() {
    var confirmation = new PadConfirmationOfOption();
    when(padConfirmationOfOptionRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(Optional.of(confirmation));

    assertThat(padConfirmationOfOptionService.findPadConfirmationOfOption(pwaApplicationDetail))
        .contains(confirmation);
  }

  @Test
  public void findPadConfirmationOfOption_whenNotFound() {

    assertThat(padConfirmationOfOptionService.findPadConfirmationOfOption(pwaApplicationDetail)).isEmpty();
  }

  @Test
  public void getOrCreatePadConfirmationOfOption_whenFound() {
    var confirmation = new PadConfirmationOfOption();
    when(padConfirmationOfOptionRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(Optional.of(confirmation));

    assertThat(padConfirmationOfOptionService.getOrCreatePadConfirmationOfOption(pwaApplicationDetail))
        .isSameAs(confirmation);

  }

  @Test
  public void getOrCreatePadConfirmationOfOption_whenNotFound() {
    assertThat(padConfirmationOfOptionService.getOrCreatePadConfirmationOfOption(pwaApplicationDetail)).isNotNull();
  }

  @Test
  public void savePadConfirmation_serviceInteractions() {
    var confirmation = new PadConfirmationOfOption();

    padConfirmationOfOptionService.savePadConfirmation(confirmation);

    verify(padConfirmationOfOptionRepository, times(1)).save(confirmation);

  }


  @Test
  public void validate_whenValidationPasses() {
    var form = new ConfirmOptionForm();

    var sourceBindingResult = new BeanPropertyBindingResult(form, "form");

    var result = padConfirmationOfOptionService.validate(form, sourceBindingResult, ValidationType.FULL,
        pwaApplicationDetail);

    assertThat(result.hasErrors()).isFalse();
    verify(confirmOptionFormValidator, times(1))
        .validate(eq(form), eq(sourceBindingResult), eq(ValidationType.FULL));

  }

  @Test
  public void validate_whenValidationFails() {
    var form = new ConfirmOptionForm();

    var sourceBindingResult = new BeanPropertyBindingResult(form, "form");
    sourceBindingResult.reject("confirmedOptionType");

    var result = padConfirmationOfOptionService.validate(form, sourceBindingResult, ValidationType.FULL,
        pwaApplicationDetail);

    assertThat(result.hasErrors()).isTrue();
    verify(confirmOptionFormValidator, times(1))
        .validate(eq(form), eq(sourceBindingResult), eq(ValidationType.FULL));

  }
}