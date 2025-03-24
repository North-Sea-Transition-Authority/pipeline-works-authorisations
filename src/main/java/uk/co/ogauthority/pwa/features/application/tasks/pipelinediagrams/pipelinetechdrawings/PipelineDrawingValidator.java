package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

@Service
public class PipelineDrawingValidator implements SmartValidator {

  private final PadPipelineService padPipelineService;
  private final PadTechnicalDrawingRepository padTechnicalDrawingRepository;
  private final PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;
  private final PadTechnicalDrawingService padTechnicalDrawingService;

  public PipelineDrawingValidator(
      PadPipelineService padPipelineService,
      PadTechnicalDrawingRepository padTechnicalDrawingRepository,
      PadTechnicalDrawingLinkService padTechnicalDrawingLinkService,
      @Lazy PadTechnicalDrawingService padTechnicalDrawingService) {
    this.padPipelineService = padPipelineService;
    this.padTechnicalDrawingRepository = padTechnicalDrawingRepository;
    this.padTechnicalDrawingLinkService = padTechnicalDrawingLinkService;
    this.padTechnicalDrawingService = padTechnicalDrawingService;
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
    var technicalDrawingValidationHints = (PadTechnicalDrawingValidationHints) validationHints[0];
    var detail = technicalDrawingValidationHints.getPwaApplicationDetail();
    var existingDrawing = technicalDrawingValidationHints.getExistingDrawing();
    var validatorMode = technicalDrawingValidationHints.getValidationType();
    var pipelineList = padPipelineService.getByIdList(detail, form.getPadPipelineIds());

    // Validate that the drawing reference is valid, and unique.
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "reference",
        "reference" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Enter a drawing reference");

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

    FileValidationUtils.validator()
        .withMinimumNumberOfFiles(1, "Upload at least one file")
        .withMaximumNumberOfFiles(1, "Upload a single drawing only")
            .validate(errors, form.getUploadedFiles());

    var pipelinesRequiringDrawings = pipelineList.stream()
        .filter(padPipeline -> padTechnicalDrawingService.isDrawingRequiredForPipeline(padPipeline.getPipelineStatus()))
        .collect(Collectors.toList());
    validatePipelines(errors, form, pipelinesRequiringDrawings, detail, existingDrawing, validatorMode);
  }

  private void validatePipelines(Errors errors, PipelineDrawingForm form, List<PadPipeline> pipelineList,
                                PwaApplicationDetail detail, PadTechnicalDrawing existingDrawing,
                                PipelineDrawingValidationType validatorMode) {

    ValidationUtils.rejectIfEmpty(errors, "padPipelineIds",
        "padPipelineIds" + FieldValidationErrorCodes.REQUIRED.getCode(),
        "Select at least one pipeline");

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
