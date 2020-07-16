package uk.co.ogauthority.pwa.validators.techdrawings;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings.PadTechnicalDrawingRepository;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadPipelineKeyDto;
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

    // Validate that the drawing reference is valid, and unique.
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

    // Ensure that a file has been uploaded, and is limited to a single file.
    if (ListUtils.emptyIfNull(form.getUploadedFileWithDescriptionForms()).size() > 1) {
      errors.rejectValue("uploadedFileWithDescriptionForms",
          "uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode(),
          "You must only upload a single drawing");
    }

    validatePipelines(errors, form, pipelineList, detail, existingDrawing, validatorMode);
  }

  public void validatePipelines(Errors errors, PipelineDrawingForm form, List<PadPipeline> pipelineList,
                                PwaApplicationDetail detail, PadTechnicalDrawing existingDrawing,
                                PipelineDrawingValidationType validatorMode) {

    ValidationUtils.rejectIfEmpty(errors, "padPipelineIds",
        "padPipelineIds" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "You must select at least one pipeline");

    // Check to see if a ID passed into the PadPipelineIds list is a valid selectable pipeline.
    boolean idNotLinkedToPipeline = ListUtils.emptyIfNull(form.getPadPipelineIds()).stream()
        .anyMatch(pipelineId -> ListUtils.emptyIfNull(pipelineList)
            .stream()
            .noneMatch(pipeline -> pipeline.getId().equals(pipelineId)));
    if (idNotLinkedToPipeline) {
      errors.rejectValue("padPipelineIds", "padPipelineIds" + FieldValidationErrorCodes.INVALID.getCode(),
          "Not all pipelines are valid");
    }

    // Ensure that all selected pipelines are not currently linked to another drawing.
    if (!ListUtils.emptyIfNull(form.getPadPipelineIds()).isEmpty()) {
      List<PadPipelineKeyDto> linkedPadPipelineKeyDtos = padTechnicalDrawingLinkService.getLinkedPipelineIds(detail);
      if (validatorMode.equals(PipelineDrawingValidationType.EDIT)) {
        // Remove previously linked pipelines from the linkedPadPipelineKeyDtos list.
        var links = padTechnicalDrawingLinkService.getLinksFromDrawing(existingDrawing);
        linkedPadPipelineKeyDtos = linkedPadPipelineKeyDtos.stream()
            .filter(pipelineIdDto -> links.stream()
                .map(drawingLink -> drawingLink.getPipeline().getId())
                .noneMatch(padPipelineId -> padPipelineId.equals(pipelineIdDto.getPadPipelineId())))
            .collect(Collectors.toUnmodifiableList());
      }

      // Check if any IDs in padPipelineIds overlap with other linked pipelines
      boolean linkedPipelineOnApplication = linkedPadPipelineKeyDtos.stream()
          .map(PadPipelineKeyDto::getPadPipelineId)
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
