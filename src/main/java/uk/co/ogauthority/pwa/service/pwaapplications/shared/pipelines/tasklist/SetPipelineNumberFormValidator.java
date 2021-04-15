package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.tasklist;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;

@Service
public class SetPipelineNumberFormValidator implements SmartValidator {

  private static final String PIPELINE_NUM_ATTR = "pipelineNumber";

  // java 8 and onwards supports regex group names
  private static final String VALID_FORMAT_REGEX = "^(?<prefix>PL|PLU)(?<number>[0-9]{1,5})(?<bundle>\\.[0-9]{1,3})?$";

  private final PadPipelineService padPipelineService;

  @Autowired
  public SetPipelineNumberFormValidator(PadPipelineService padPipelineService) {
    this.padPipelineService = padPipelineService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(SetPipelineNumberForm.class);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (SetPipelineNumberForm) target;
    var padPipeline = Arrays.stream(validationHints)
        .filter(o -> o instanceof PadPipeline)
        .map(o -> (PadPipeline) o)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Expected to find PadPipeline as validation hint"));

    var pipelineNumberValidationConfig = Arrays.stream(validationHints)
        .filter(o -> o instanceof SetPipelineNumberValidationConfig)
        .map(o -> (SetPipelineNumberValidationConfig) o)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Expected to find validation config as validation hint"));

    ValidationUtils.rejectIfEmpty(
        errors,
        PIPELINE_NUM_ATTR,
        FieldValidationErrorCodes.REQUIRED.errorCode(PIPELINE_NUM_ATTR),
        "Enter a pipeline number"
    );

    // Run through checks and avoid doing more checks once an error has been found.
    // Could this validation flow be formalised? e.g ExitOnErrorValidationPipeline.begin().then().then().finally();
    if (!errors.hasErrors()) {
      pipelineNumberFormatCheck(errors, form, pipelineNumberValidationConfig);
    }

    if (!errors.hasErrors()) {
      pipelineNumberRangeCheck(errors, form, pipelineNumberValidationConfig);
    }

    if (!errors.hasErrors()) {
      checkPipelineNumberUniqueness(errors, form, padPipeline);
    }

  }

  private void checkPipelineNumberUniqueness(Errors errors,
                                             SetPipelineNumberForm form,
                                             PadPipeline padPipeline){
    var foundPadPipelines = padPipelineService.findSubmittedOrDraftPipelinesWithPipelineNumber(form.getPipelineNumber());
    var pipelineIdsWithMatchingNumber = foundPadPipelines
        .stream()
        .map(PadPipeline::getPipelineId)
        .collect(toSet());

    var currentPipelineFound =  pipelineIdsWithMatchingNumber.contains(padPipeline.getPipelineId());

    if(!currentPipelineFound && !pipelineIdsWithMatchingNumber.isEmpty()) {
      var appRefsWithMatchingPipelineNumber = foundPadPipelines.stream()
          .map(padPipeline1 -> padPipeline1.getPwaApplicationDetail().getPwaApplicationRef())
          .distinct()
          .sorted(Comparator.comparing(s -> s))
          .collect(toList());

      errors.rejectValue(
          PIPELINE_NUM_ATTR,
          FieldValidationErrorCodes.NOT_UNIQUE.errorCode(PIPELINE_NUM_ATTR),
          "Enter a unique pipeline number. Applications where the last submitted or current draft version use this number are " +
              String.join(", ", appRefsWithMatchingPipelineNumber)
      );
    }

  }

  private void pipelineNumberRangeCheck(Errors errors,
                                        SetPipelineNumberForm form,
                                        SetPipelineNumberValidationConfig config){

    var pattern = Pattern.compile(VALID_FORMAT_REGEX);
    Matcher matcher = pattern.matcher(form.getPipelineNumber());
    // puts matcher into required state to extract groups. Without this throws illegal state exception.
    matcher.matches();
    // groups "prefix", "number" , "bundle"

    var extractedPipelineNumber = Integer.parseInt(matcher.group("number"));

    if(!config.getPipelineNumberRange().contains(extractedPipelineNumber)){
      var validRange = config.getPipelineNumberRange();
      errors.rejectValue(
          PIPELINE_NUM_ATTR,
          FieldValidationErrorCodes.OUT_OF_TARGET_RANGE.errorCode(PIPELINE_NUM_ATTR),
          String.format(
              "Enter a pipeline number between %s and %s",
              validRange.getMinimum(),
              validRange.getMaximum()
          )
      );
    }

  }

  private void pipelineNumberFormatCheck(Errors errors, SetPipelineNumberForm form, SetPipelineNumberValidationConfig config){
    var pattern = Pattern.compile(VALID_FORMAT_REGEX);
    Matcher matcher = pattern.matcher(form.getPipelineNumber());

    if (!matcher.matches()) {
      var minNumberValue = config.getPipelineNumberRange().getMinimum();
      errors.rejectValue(
          PIPELINE_NUM_ATTR,
          FieldValidationErrorCodes.INVALID.errorCode(PIPELINE_NUM_ATTR),
          String.format(
              "Enter a pipeline number in a valid format. For example PL%s or PLU%s.1",
              minNumberValue,
              minNumberValue
          )
      );
    }
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new UnsupportedOperationException("Must use validate method with hints");
  }
}
