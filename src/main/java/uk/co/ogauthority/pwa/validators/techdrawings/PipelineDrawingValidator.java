package uk.co.ogauthority.pwa.validators.techdrawings;

import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings.PadTechnicalDrawingRepository;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingLinkService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PipelineDrawingValidationType;

@Service
public class PipelineDrawingValidator implements SmartValidator {

  private final PadPipelineService padPipelineService;
  private final PadTechnicalDrawingRepository padTechnicalDrawingRepository;
  private final PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;

  public PipelineDrawingValidator(
      PadPipelineService padPipelineService,
      PadTechnicalDrawingRepository padTechnicalDrawingRepository,
      PadTechnicalDrawingLinkService padTechnicalDrawingLinkService) {
    this.padPipelineService = padPipelineService;
    this.padTechnicalDrawingRepository = padTechnicalDrawingRepository;
    this.padTechnicalDrawingLinkService = padTechnicalDrawingLinkService;
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
    var existingDrawing = (PadTechnicalDrawing) validationHints[1];
    var validatorMode = (PipelineDrawingValidationType) validationHints[2];
    var pipelineList = padPipelineService.getByIdList(detail, form.getPadPipelineIds());

    // Drawing reference
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "reference",
        "reference" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "You must enter a drawing reference");

    boolean referenceAlreadyInUse;
    switch (validatorMode) {
      case ADD:
        referenceAlreadyInUse = padTechnicalDrawingRepository.getAllByPwaApplicationDetail(detail)
            .stream()
            .anyMatch(technicalDrawing -> technicalDrawing.getReference().equalsIgnoreCase(form.getReference()));
        break;
      case EDIT:
        referenceAlreadyInUse = padTechnicalDrawingRepository.getAllByPwaApplicationDetail(detail)
            .stream()
            .filter(technicalDrawing -> !technicalDrawing.getId().equals(existingDrawing.getId()))
            .anyMatch(technicalDrawing -> technicalDrawing.getReference().equalsIgnoreCase(form.getReference()));
        break;
      default:
        throw new ActionNotAllowedException("No implementation for " + validatorMode.name());
    }
    if (referenceAlreadyInUse) {
      errors.rejectValue("reference",
          "reference" + FieldValidationErrorCodes.INVALID.getCode(),
          "The drawing reference is already in use");
    }

    // File upload
    if (ListUtils.emptyIfNull(form.getUploadedFileWithDescriptionForms()).size() > 1) {
      errors.rejectValue("uploadedFileWithDescriptionForms",
          "uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode(),
          "You must only upload a single drawing");
    }

    // Pipeline references
    if (ListUtils.emptyIfNull(pipelineList).size() != ListUtils.emptyIfNull(form.getPadPipelineIds()).size()) {
      errors.rejectValue("padPipelineIds", "padPipelineIds" + FieldValidationErrorCodes.INVALID.getCode(),
          "Not all pipelines are valid");
    }
    ValidationUtils.rejectIfEmpty(errors, "padPipelineIds",
        "padPipelineIds" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "You must select at least one pipeline");
    if (!ListUtils.emptyIfNull(form.getPadPipelineIds()).isEmpty()) {
      var linkedPipelineIds = padTechnicalDrawingLinkService.getLinkedPipelineIds(detail);
      if (validatorMode.equals(PipelineDrawingValidationType.EDIT)) {
        linkedPipelineIds = linkedPipelineIds.stream()
            .filter(integer -> !integer.equals(existingDrawing.getId()))
            .collect(Collectors.toUnmodifiableList());
      }
      if (validatorMode.equals(PipelineDrawingValidationType.EDIT)) {
        var links = padTechnicalDrawingLinkService.getLinksFromDrawing(existingDrawing);
        linkedPipelineIds = linkedPipelineIds.stream()
            .filter(linkedPipelineId -> links.stream()
                .map(drawingLink -> drawingLink.getPipeline().getId())
                .noneMatch(drawingLinkPipelineId -> drawingLinkPipelineId.equals(linkedPipelineId)))
            .collect(Collectors.toUnmodifiableList());
      }
      boolean linkedPipelineOnApplication = linkedPipelineIds.stream()
          .anyMatch(linkedPipelineId -> form.getPadPipelineIds()
              .stream()
              .anyMatch(linkedPipelineId::equals));
      if (linkedPipelineOnApplication) {
        errors.rejectValue("padPipelineIds", "padPipelineIds" + FieldValidationErrorCodes.INVALID.getCode(),
            "One or more pipelines have already been added to another drawing");
      }
    }
  }
}
