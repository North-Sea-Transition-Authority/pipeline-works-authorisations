package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.techdrawings.PipelineDrawingSummaryView;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings.PadTechnicalDrawingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;
import uk.co.ogauthority.pwa.validators.techdrawings.PipelineDrawingValidator;

@Service
public class PadTechnicalDrawingService implements ApplicationFormSectionService {

  private final PadTechnicalDrawingRepository padTechnicalDrawingRepository;
  private final PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;
  private final PadFileService padFileService;
  private final PadPipelineService padPipelineService;
  private final PipelineDrawingValidator pipelineDrawingValidator;
  private final SpringValidatorAdapter groupValidator;

  @Autowired
  public PadTechnicalDrawingService(
      PadTechnicalDrawingRepository padTechnicalDrawingRepository,
      PadTechnicalDrawingLinkService padTechnicalDrawingLinkService,
      PadFileService padFileService, PadPipelineService padPipelineService,
      PipelineDrawingValidator pipelineDrawingValidator,
      SpringValidatorAdapter groupValidator) {
    this.padTechnicalDrawingRepository = padTechnicalDrawingRepository;
    this.padTechnicalDrawingLinkService = padTechnicalDrawingLinkService;
    this.padFileService = padFileService;
    this.padPipelineService = padPipelineService;
    this.pipelineDrawingValidator = pipelineDrawingValidator;
    this.groupValidator = groupValidator;
  }

  public List<PadTechnicalDrawing> getDrawings(PwaApplicationDetail detail) {
    return padTechnicalDrawingRepository.getAllByPwaApplicationDetail(detail);
  }

  @Transactional
  public void addDrawing(PwaApplicationDetail detail, PipelineDrawingForm form) {
    var drawing = new PadTechnicalDrawing();
    // The form should be successfully validated at this point
    // This means it will contain a single file.
    PadFile file = padFileService.getPadFileByPwaApplicationDetailAndFileId(detail,
        form.getUploadedFileWithDescriptionForms().get(0).getUploadedFileId());
    drawing.setFile(file);
    drawing.setPwaApplicationDetail(detail);
    drawing.setReference(form.getReference());
    padTechnicalDrawingRepository.save(drawing);
    padTechnicalDrawingLinkService.linkDrawing(detail, form.getPadPipelineIds(), drawing);
  }

  public PipelineDrawingSummaryView getPipelineSummaryView(PwaApplicationDetail detail, Integer drawingId) {
    var drawing = padTechnicalDrawingRepository.findByPwaApplicationDetailAndId(detail, drawingId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Unable to find drawing with id (%d) of detail (%d)", drawingId, detail.getId())
        ));
    return getPipelineDrawingSummaryViewFromDrawing(detail, drawing);
  }

  public List<PipelineDrawingSummaryView> getPipelineDrawingSummaryViewList(PwaApplicationDetail detail) {
    var drawings = padTechnicalDrawingRepository.getAllByPwaApplicationDetail(detail);
    var links = padTechnicalDrawingLinkService.getLinksFromDrawingList(drawings);
    Map<PadTechnicalDrawing, List<PadTechnicalDrawingLink>> linkMap = links.stream()
        .collect(Collectors.groupingBy(PadTechnicalDrawingLink::getTechnicalDrawing));

    List<UploadedFileView> fileViews = padFileService.getUploadedFileViews(detail,
        ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL);

    var summaryList = new ArrayList<PipelineDrawingSummaryView>();

    linkMap.forEach((technicalDrawing, drawingLinks) -> {
      var summaryView = buildSummaryView(technicalDrawing, drawingLinks, fileViews);
      summaryList.add(summaryView);
    });

    summaryList.sort(Comparator.comparing(PipelineDrawingSummaryView::getReference));

    return summaryList;
  }

  private PipelineDrawingSummaryView buildSummaryView(PadTechnicalDrawing technicalDrawing,
                                                      List<PadTechnicalDrawingLink> drawingLinks,
                                                      List<UploadedFileView> fileViewList) {
    List<String> references = drawingLinks.stream()
        .map(drawingLink -> drawingLink.getPipeline().getPipelineRef())
        .collect(Collectors.toUnmodifiableList());

    UploadedFileView fileView = fileViewList.stream()
        .filter(uploadedFileView -> uploadedFileView.getFileId().equals(technicalDrawing.getFileId()))
        .findFirst()
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Unable to get UploadedFileView of file with ID: " + technicalDrawing.getFileId()));

    return new PipelineDrawingSummaryView(technicalDrawing, references, fileView);
  }

  @VisibleForTesting
  public PipelineDrawingSummaryView getPipelineDrawingSummaryViewFromDrawing(PwaApplicationDetail detail,
                                                                             PadTechnicalDrawing drawing) {
    var links = padTechnicalDrawingLinkService.getLinksFromDrawing(drawing);
    List<UploadedFileView> fileViews = padFileService.getUploadedFileViews(detail,
        ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL);
    return buildSummaryView(drawing, links, fileViews);
  }

  @Transactional
  public void removeDrawing(PwaApplicationDetail detail, Integer drawingId, WebUserAccount webUserAccount) {
    var drawing = padTechnicalDrawingRepository.findByPwaApplicationDetailAndId(detail, drawingId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Unable to find drawing with id (%d) of detail (%d)", drawingId, detail.getId())
        ));
    padTechnicalDrawingLinkService.unlinkDrawing(detail, drawing);
    padTechnicalDrawingRepository.delete(drawing);
    padFileService.processFileDeletion(drawing.getFile(), webUserAccount);
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return allPipelinesLinked(detail);
  }

  @Override
  @Deprecated
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    throw new ActionNotAllowedException("PadTechnicalDrawingService::validate should not be used");
  }

  public BindingResult validateDrawing(Object form, BindingResult bindingResult, ValidationType validationType,
                                       PwaApplicationDetail pwaApplicationDetail) {
    pipelineDrawingValidator.validate(form, bindingResult, pwaApplicationDetail);
    groupValidator.validate(form, bindingResult, FullValidation.class, MandatoryUploadValidation.class);
    return bindingResult;
  }

  private boolean allPipelinesLinked(PwaApplicationDetail pwaApplicationDetail) {
    var drawings = getDrawings(pwaApplicationDetail);
    var links = padTechnicalDrawingLinkService.getLinksFromDrawingList(drawings);
    var pipelines = padPipelineService.getPipelines(pwaApplicationDetail);

    Set<Integer> linkedPipelineIds = links.stream()
        .map(drawingLink -> drawingLink.getPipeline().getId())
        .collect(Collectors.toSet());

    return pipelines.stream()
        .allMatch(pipeline -> linkedPipelineIds.contains(pipeline.getId()));
  }

  public BindingResult validateSection(BindingResult bindingResult, PwaApplicationDetail pwaApplicationDetail) {

    if (!allPipelinesLinked(pwaApplicationDetail)) {
      bindingResult.reject("allPipelinesAdded" + FieldValidationErrorCodes.INVALID.getCode(),
          "Not all pipelines have been linked to a drawing");
    }

    return bindingResult;
  }
}
