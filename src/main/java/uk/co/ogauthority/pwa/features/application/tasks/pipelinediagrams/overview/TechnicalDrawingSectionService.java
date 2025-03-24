package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.overview;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartDocumentForm;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartFileService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PipelineSchematicsErrorCode;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.umbilical.UmbilicalCrossSectionService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Service
public class TechnicalDrawingSectionService implements ApplicationFormSectionService {

  private final AdmiraltyChartFileService admiraltyChartFileService;
  private final PadTechnicalDrawingService padTechnicalDrawingService;
  private final UmbilicalCrossSectionService umbilicalCrossSectionService;
  private final PadOptionConfirmedService padOptionConfirmedService;

  private final List<MailMergeFieldMnem> techDrawingMailMergeFields =
      List.of(MailMergeFieldMnem.PL_DRAWING_REF_LIST, MailMergeFieldMnem.ADMIRALTY_CHART_REF);
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public TechnicalDrawingSectionService(
      AdmiraltyChartFileService admiraltyChartFileService,
      PadTechnicalDrawingService padTechnicalDrawingService,
      UmbilicalCrossSectionService umbilicalCrossSectionService,
      PadOptionConfirmedService padOptionConfirmedService,
      PadFileManagementService padFileManagementService
  ) {
    this.admiraltyChartFileService = admiraltyChartFileService;
    this.padTechnicalDrawingService = padTechnicalDrawingService;
    this.umbilicalCrossSectionService = umbilicalCrossSectionService;
    this.padOptionConfirmedService = padOptionConfirmedService;
    this.padFileManagementService = padFileManagementService;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    // do not do additional type checks as this is covered by the controller markup
    return !PwaApplicationType.OPTIONS_VARIATION.equals(pwaApplicationDetail.getPwaApplicationType())
        || padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail);
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return admiraltyChartFileService.isComplete(detail)
        && padTechnicalDrawingService.isComplete(detail)
        && umbilicalCrossSectionService.isComplete(detail);
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    var admiraltyForm = new AdmiraltyChartDocumentForm();
    if (admiraltyChartFileService.canUploadDocuments(pwaApplicationDetail)) {
      padFileManagementService.mapFilesToForm(admiraltyForm, pwaApplicationDetail, FileDocumentType.ADMIRALTY_CHART);
      admiraltyChartFileService.validate(admiraltyForm, bindingResult, validationType, pwaApplicationDetail);
    }
    padTechnicalDrawingService.validateSection(bindingResult, pwaApplicationDetail);
    return bindingResult;
  }

  public TechnicalDrawingsSectionValidationSummary getValidationSummary(BindingResult bindingResult) {

    if (!bindingResult.hasErrors()) {
      return TechnicalDrawingsSectionValidationSummary.createValidSummary();
    }

    var errorCodes = bindingResult.getAllErrors().stream()
        .map(DefaultMessageSourceResolvable::getCodes)
        .filter(Objects::nonNull)
        .flatMap(Arrays::stream)
        .collect(Collectors.toSet());

    if (errorCodes.contains(PipelineSchematicsErrorCode.TECHNICAL_DRAWINGS.getErrorCode())
        && errorCodes.contains(PipelineSchematicsErrorCode.ADMIRALTY_CHART.getErrorCode())) {
      return TechnicalDrawingsSectionValidationSummary.createInvalidSummary(
          "An admiralty chart must be provided, and all pipelines must be linked to a drawing");

    } else if (errorCodes.contains(PipelineSchematicsErrorCode.TECHNICAL_DRAWINGS.getErrorCode())) {
      return TechnicalDrawingsSectionValidationSummary.createInvalidSummary("All pipelines must be linked to a drawing");

    } else if (errorCodes.contains(PipelineSchematicsErrorCode.ADMIRALTY_CHART.getErrorCode())) {
      return TechnicalDrawingsSectionValidationSummary.createInvalidSummary("An admiralty chart must be provided");
    }

    return TechnicalDrawingsSectionValidationSummary.createInvalidSummary("");
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    padFileManagementService.copyUploadedFiles(fromDetail, toDetail, FileDocumentType.ADMIRALTY_CHART);
    padFileManagementService.copyUploadedFiles(fromDetail, toDetail, FileDocumentType.UMBILICAL_CROSS_SECTION);
  }

  @Override
  public void cleanupData(PwaApplicationDetail detail) {
    padTechnicalDrawingService.cleanupData(detail);
  }

  @Override
  public List<MailMergeFieldMnem> getAvailableMailMergeFields(PwaApplicationType pwaApplicationType) {

    return techDrawingMailMergeFields.stream()
        .filter(mailMergeField -> mailMergeField.appTypeIsSupported(pwaApplicationType))
        .collect(Collectors.toList());

  }

  @Override
  public Map<MailMergeFieldMnem, String> resolveMailMergeFields(PwaApplicationDetail pwaApplicationDetail) {

    var availableMergeFields = getAvailableMailMergeFields(pwaApplicationDetail.getPwaApplicationType());

    EnumMap<MailMergeFieldMnem, String> map = new EnumMap<>(MailMergeFieldMnem.class);

    if (availableMergeFields.contains(MailMergeFieldMnem.PL_DRAWING_REF_LIST)) {

      var drawingRefs = padTechnicalDrawingService.getDrawings(pwaApplicationDetail)
          .stream()
          .map(PadTechnicalDrawing::getReference)
          .sorted()
          .collect(Collectors.joining(", "));

      map.put(MailMergeFieldMnem.PL_DRAWING_REF_LIST, drawingRefs);

    }

    if (availableMergeFields.contains(MailMergeFieldMnem.ADMIRALTY_CHART_REF)) {

      admiraltyChartFileService.getAdmiraltyChartFile(pwaApplicationDetail)
          .ifPresent(file -> map.put(MailMergeFieldMnem.ADMIRALTY_CHART_REF, file.getFileDescription()));

    }

    return map;

  }

}
