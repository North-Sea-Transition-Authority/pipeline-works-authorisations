package uk.co.ogauthority.pwa.validators.techdrawings;

import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings.PadTechnicalDrawingRepository;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;

@Service
public class PipelineDrawingValidator implements SmartValidator {

  private final PadPipelineService padPipelineService;
  private final PadTechnicalDrawingRepository padTechnicalDrawingRepository;

  public PipelineDrawingValidator(
      PadPipelineService padPipelineService,
      PadTechnicalDrawingRepository padTechnicalDrawingRepository) {
    this.padPipelineService = padPipelineService;
    this.padTechnicalDrawingRepository = padTechnicalDrawingRepository;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(PipelineDrawingForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    throw new ActionNotAllowedException("Use the varargs validate method");
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (PipelineDrawingForm) target;
    var detail = (PwaApplicationDetail) validationHints[0];
    var pipelineList = padPipelineService.getByIdList(detail, form.getPadPipelineIds());
    if (ListUtils.emptyIfNull(pipelineList).size() != ListUtils.emptyIfNull(form.getPadPipelineIds()).size()) {
      errors.rejectValue("padPipelineIds", "padPipelineIds" + FieldValidationErrorCodes.INVALID.getCode(),
          "Not all pipelines are valid");
    }
    ValidationUtils.rejectIfEmpty(errors, "padPipelineIds",
        "padPipelineIds" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "You must select at least one pipeline");
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "reference",
        "reference" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "You must enter a drawing reference");
    if (ListUtils.emptyIfNull(form.getUploadedFileWithDescriptionForms()).size() > 1) {
      errors.rejectValue("uploadedFileWithDescriptionForms",
          "uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode(),
          "You must only upload a single drawing");
    }

    boolean referenceAlreadyInUse = padTechnicalDrawingRepository.getAllByPwaApplicationDetail(detail)
        .stream()
        .anyMatch(technicalDrawing -> technicalDrawing.getReference().equalsIgnoreCase(form.getReference()));

    if (referenceAlreadyInUse) {
      errors.rejectValue("uploadedFileWithDescriptionForms",
          "reference" + FieldValidationErrorCodes.INVALID.getCode(),
          "The drawing reference is already in use");
    }
  }
}
