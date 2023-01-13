package uk.co.ogauthority.pwa.features.application.tasks.campaignworks;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.projectextension.MaxCompletionPeriod;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInputValidator;

@RunWith(MockitoJUnitRunner.class)
public class WorkScheduleFormValidatorTest {

  private TwoFieldDateInputValidator twoFieldDateInputValidator = new TwoFieldDateInputValidator();

  @Mock
  private PadPipelineService padPipelineService;

  private WorkScheduleFormValidator validator;

  private List<PadPipeline> padPipelineList;
  private List<Integer> padPipelineIdList;
  private PadPipeline pipe1;
  private PadPipeline pipe2;

  private WorkScheduleForm validForm;
  private CampaignWorkScheduleValidationHint campaignWorkScheduleValidationHint;
  private Object[] defaultHints;

  private Clock clock = Clock.fixed(Clock.systemUTC().instant(), ZoneId.systemDefault());


  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setup() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL);

    campaignWorkScheduleValidationHint = new CampaignWorkScheduleValidationHint(
        LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault()),
       null,
        pwaApplicationDetail.getPwaApplicationType()
    );

    defaultHints = new Object[]{
        pwaApplicationDetail,
        campaignWorkScheduleValidationHint
    };

    validator = new WorkScheduleFormValidator(twoFieldDateInputValidator, padPipelineService);

    pipe1 = new PadPipeline(pwaApplicationDetail);
    pipe1.setId(10);

    pipe2 = new PadPipeline(pwaApplicationDetail);
    pipe2.setId(20);
    padPipelineList = List.of(pipe1, pipe2);
    padPipelineIdList = List.of(pipe1.getId(), pipe2.getId());
    when(padPipelineService.getByIdList(pwaApplicationDetail, padPipelineIdList)).thenReturn(padPipelineList);

    validForm = new WorkScheduleForm();
    validForm.setWorkStart(new TwoFieldDateInput(2020, 2));
    validForm.setWorkEnd(new TwoFieldDateInput(2021, 1));
    validForm.setPadPipelineIds(padPipelineIdList);

  }


  @Test
  public void validate_emptyForm() {
    var form = new WorkScheduleForm();

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator(validator, form, bindingResult, defaultHints);

    var codes = ValidatorTestUtils.extractErrors(bindingResult);
    var messages = ValidatorTestUtils.extractErrorMessages(bindingResult);

    assertThat(codes).containsOnly(
        entry("workStart", Set.of("workStart.required")),
        entry("workEnd", Set.of("workEnd.required")),
        entry("padPipelineIds", Set.of("padPipelineIds.required"))
    );

    assertThat(messages).contains(
        entry("workStart", Set.of("Enter a work start date")),
        entry("workEnd", Set.of("Enter a work end date"))
    );

  }

  @Test
  public void validate_validPipelineIdsAdded() {
    var bindingResult = new BeanPropertyBindingResult(validForm, "form");
    ValidationUtils.invokeValidator(validator, validForm, bindingResult, defaultHints);

    var codes = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(codes).doesNotContainKeys("padPipelineIds");

  }

  @Test
  public void validate_invalidPipelineIdsAdded() {
    var form = new WorkScheduleForm();
    form.setPadPipelineIds(List.of(9999999));

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator(validator, form, bindingResult, defaultHints);

    var codes = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(codes).contains(entry("padPipelineIds", Set.of("padPipelineIds.invalid")));

  }

  @Test
  public void validate_workEndBeforeWorkStart() {
    var form = new WorkScheduleForm();
    form.setWorkStart(new TwoFieldDateInput(2019, 6));
    form.setWorkEnd(new TwoFieldDateInput(2019, 5));

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator(validator, form, bindingResult, defaultHints);

    var codes = ValidatorTestUtils.extractErrors(bindingResult);
    var messages = ValidatorTestUtils.extractErrorMessages(bindingResult);

    assertThat(codes).contains(
        entry("workEnd.month", Set.of("month.afterDate")),
        entry("workEnd.year", Set.of("year.afterDate"))
    );

    assertThat(messages).contains(
        entry("workEnd.month", Set.of("")),
        entry("workEnd.year", Set.of("Work end must be the same as or after work start"))
    );

  }

  @Test
  public void validate_workEndDateBeyondLimit() {
    var form = new WorkScheduleForm();
    var latestValidEndDate = campaignWorkScheduleValidationHint.getLatestWorkEndDateHint().getDate();
    var validStartDate = latestValidEndDate.minusMonths(1);
    var invalidEndDate = latestValidEndDate.plusMonths(1);

    form.setWorkStart(new TwoFieldDateInput(validStartDate.getYear(), validStartDate.getMonthValue()));
    form.setWorkEnd(new TwoFieldDateInput(invalidEndDate.getYear(), invalidEndDate.getMonthValue()));

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator(validator, form, bindingResult, defaultHints);

    var codes = ValidatorTestUtils.extractErrors(bindingResult);
    var messages = ValidatorTestUtils.extractErrorMessages(bindingResult);

    assertThat(codes).contains(
        entry("workEnd.month", Set.of("month.beforeDate")),
        entry("workEnd.year", Set.of("year.beforeDate"))
    );

    assertThat(messages.get("workEnd.year"))
        .contains("Work end must be the same as or before " + latestValidEndDate.format(CampaignWorkScheduleValidationHint.DATETIME_FORMATTER));
    assertThat(messages.get("workEnd.month")).contains("");

  }

  @Test
  public void validate_workEndAfter_ShorterProposedWorks() {
    var form = new WorkScheduleForm();
    var latestValidEndDate = campaignWorkScheduleValidationHint.getLatestWorkEndDateHint().getDate();
    var validStartDate = latestValidEndDate.minusMonths(1);
    var invalidEndDate = latestValidEndDate.plusMonths(1);

    form.setWorkStart(new TwoFieldDateInput(validStartDate.getYear(), validStartDate.getMonthValue()));
    form.setWorkEnd(new TwoFieldDateInput(invalidEndDate.getYear(), invalidEndDate.getMonthValue()));

    var campaignWorkScheduleValidationHint = new CampaignWorkScheduleValidationHint(
        LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault()),
        LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault()).plusMonths(8),
        pwaApplicationDetail.getPwaApplicationType()
    );
    defaultHints[1] = campaignWorkScheduleValidationHint;

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator(validator, form, bindingResult, defaultHints);

    var codes = ValidatorTestUtils.extractErrors(bindingResult);
    var messages = ValidatorTestUtils.extractErrorMessages(bindingResult);

    assertThat(codes).contains(
        entry("workEnd.month", Set.of("month.beforeDate")),
        entry("workEnd.year", Set.of("year.beforeDate"))
    );

    assertThat(messages.get("workEnd.year"))
        .contains("Work end must be the same as or before " +
            LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault()).plusMonths(8)
                .format(CampaignWorkScheduleValidationHint.DATETIME_FORMATTER));
    assertThat(messages.get("workEnd.month")).contains("");
  }

  @Test
  public void validate_workEndAfter_OptionsVariation() {
    var form = new WorkScheduleForm();
    var latestValidEndDate = campaignWorkScheduleValidationHint.getLatestWorkEndDateHint().getDate();
    var validStartDate = latestValidEndDate.minusMonths(1);
    var invalidEndDate = latestValidEndDate.plusMonths(1);

    form.setWorkStart(new TwoFieldDateInput(validStartDate.getYear(), validStartDate.getMonthValue()));
    form.setWorkEnd(new TwoFieldDateInput(invalidEndDate.getYear(), invalidEndDate.getMonthValue()));

    var campaignWorkScheduleValidationHint = new CampaignWorkScheduleValidationHint(
        LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault()),
        null,
        PwaApplicationType.OPTIONS_VARIATION
    );
    defaultHints[1] = campaignWorkScheduleValidationHint;

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator(validator, form, bindingResult, defaultHints);

    var codes = ValidatorTestUtils.extractErrors(bindingResult);
    var messages = ValidatorTestUtils.extractErrorMessages(bindingResult);

    assertThat(codes).contains(
        entry("workEnd.month", Set.of("month.beforeDate")),
        entry("workEnd.year", Set.of("year.beforeDate"))
    );

    assertThat(messages.get("workEnd.year"))
        .contains("Work end must be the same as or before " +
            LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault())
                .plusMonths(MaxCompletionPeriod.OPTIONS_VARIATION.getMaxMonthsCompletion())
                .format(CampaignWorkScheduleValidationHint.DATETIME_FORMATTER));
    assertThat(messages.get("workEnd.month")).contains("");
  }

  @Test
  public void validate_workStartDateBeforeEarliestStartDate() {
    var form = new WorkScheduleForm();
    var earliest = campaignWorkScheduleValidationHint.getEarliestDate();
    var invalidStartDate = earliest.minusMonths(1);
    var validEndDate = earliest.plusMonths(1);

    form.setWorkStart(new TwoFieldDateInput(invalidStartDate.getYear(), invalidStartDate.getMonthValue()));
    form.setWorkEnd(new TwoFieldDateInput(validEndDate.getYear(), validEndDate.getMonthValue()));

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator(validator, form, bindingResult, defaultHints);

    var codes = ValidatorTestUtils.extractErrors(bindingResult);
    var messages = ValidatorTestUtils.extractErrorMessages(bindingResult);

    assertThat(codes).contains(
        entry("workStart.month", Set.of("month.afterDate")),
        entry("workStart.year", Set.of("year.afterDate"))
    );

    assertThat(messages.get("workStart.month")).contains("");

    assertThat(messages.get("workStart.year"))
        .contains("Work start must be the same as or after Project information proposed start of works date " +
            "(" + earliest.format(CampaignWorkScheduleValidationHint.DATETIME_FORMATTER)+")");

  }

  @Test
  public void validate_workStartAndWorkEndYearTooLarge() {
    var form = new WorkScheduleForm();
    form.setWorkStart(new TwoFieldDateInput(4001, 6));
    form.setWorkEnd(new TwoFieldDateInput(4001, 7));

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator(validator, form, bindingResult, defaultHints);

    var codes = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(codes).contains(
        entry("workStart.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("workEnd.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }

  @Test
  public void validate_workStartAndWorkEndYearTooSmall() {
    var form = new WorkScheduleForm();
    form.setWorkStart(new TwoFieldDateInput(999, 6));
    form.setWorkEnd(new TwoFieldDateInput(999, 7));

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    ValidationUtils.invokeValidator(validator, form, bindingResult, defaultHints);

    var codes = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(codes).contains(
        entry("workStart.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode())),
        entry("workEnd.year", Set.of("year" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }
}