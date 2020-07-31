package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
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

  private static final ApplicationFilePurpose FILE_PURPOSE = ApplicationFilePurpose.PIPELINE_DRAWINGS;

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

  public PadTechnicalDrawing getDrawing(PwaApplicationDetail detail, Integer drawingId) {
    return padTechnicalDrawingRepository.findByPwaApplicationDetailAndId(detail, drawingId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Unable to find drawing (%d) of detail (%d)", drawingId, detail.getId())));
  }

  public Optional<PadTechnicalDrawing> getDrawingLinkedToPadFile(PwaApplicationDetail detail, PadFile padFile) {
    return padTechnicalDrawingRepository.findByPwaApplicationDetailAndFile(detail, padFile);
  }

  public void unlinkFile(PadTechnicalDrawing drawing) {
    drawing.setFile(null);
    padTechnicalDrawingRepository.save(drawing);
  }

  public void mapDrawingToForm(PwaApplicationDetail detail, PadTechnicalDrawing drawing, PipelineDrawingForm form) {

    var pipelineIds = padTechnicalDrawingLinkService.getLinksFromDrawingList(List.of(drawing))
        .stream()
        .map(drawingLink -> drawingLink.getPipeline().getId())
        .collect(Collectors.toUnmodifiableList());

    if (drawing.getFile() != null) {
      var file = padFileService.getUploadedFileView(detail, drawing.getFileId(),
          FILE_PURPOSE,
          ApplicationFileLinkStatus.FULL);
      form.setUploadedFileWithDescriptionForms(List.of(
          new UploadFileWithDescriptionForm(file.getFileId(), file.getFileDescription(), file.getFileUploadedTime())));
    }

    form.setReference(drawing.getReference());
    form.setPadPipelineIds(pipelineIds);
  }

  @Transactional
  public void addDrawing(PwaApplicationDetail detail, PipelineDrawingForm form) {
    var drawing = new PadTechnicalDrawing();
    saveDrawingAndLink(detail, form, drawing);
  }

  @Transactional
  void saveDrawingAndLink(PwaApplicationDetail detail, PipelineDrawingForm form,
                          PadTechnicalDrawing drawing) {
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
    return getPipelineDrawingSummaryViewsFromDrawingList(detail, drawings);
  }

  @VisibleForTesting
  public List<PipelineDrawingSummaryView> getPipelineDrawingSummaryViewsFromDrawingList(PwaApplicationDetail detail,
                                                                                        List<PadTechnicalDrawing> drawings) {
    var links = padTechnicalDrawingLinkService.getLinksFromDrawingList(drawings);
    Map<PadTechnicalDrawing, List<PadTechnicalDrawingLink>> linkMap = links.stream()
        .collect(Collectors.groupingBy(PadTechnicalDrawingLink::getTechnicalDrawing));

    List<UploadedFileView> fileViews = padFileService.getUploadedFileViews(detail,
        FILE_PURPOSE,
        ApplicationFileLinkStatus.FULL);

    var summaryList = new ArrayList<PipelineDrawingSummaryView>();

    linkMap.forEach((technicalDrawing, drawingLinks) -> {
      var summaryView = buildSummaryView(technicalDrawing, drawingLinks, fileViews);
      summaryList.add(summaryView);
    });

    summaryList.sort(Comparator.comparing(PipelineDrawingSummaryView::getReference));

    return summaryList;
  }

  public List<PipelineOverview> getUnlinkedApplicationPipelineOverviews(PwaApplicationDetail detail) {
    var overviewList = padPipelineService.getApplicationPipelineOverviews(detail);
    var linkedPipelinesIds = padTechnicalDrawingLinkService.getLinkedPipelineIds(detail);
    return overviewList.stream()
        .filter(pipelineOverview -> linkedPipelinesIds.stream()
            .noneMatch(pipelineIdDto -> pipelineOverview.getPadPipelineId().equals(pipelineIdDto.getPadPipelineId())))
        .collect(Collectors.toUnmodifiableList());
  }

  public List<PipelineOverview> getUnlinkedAndSpecificApplicationPipelineOverviews(PwaApplicationDetail detail,
                                                                                   List<Integer> ids) {
    var overviewList = padPipelineService.getApplicationPipelineOverviews(detail);
    var linkedPipelinesIds = padTechnicalDrawingLinkService.getLinkedPipelineIds(detail);
    return overviewList.stream()
        .filter(overview -> {
          var isExcluded = ids.stream()
              .anyMatch(id -> id.equals(overview.getPadPipelineId()));
          var isNotLinked = linkedPipelinesIds.stream()
              .noneMatch(pipelineIdDto -> pipelineIdDto.getPadPipelineId().equals(overview.getPadPipelineId()));
          return isNotLinked || isExcluded;
        })
        .collect(Collectors.toUnmodifiableList());
  }

  private PipelineDrawingSummaryView buildSummaryView(PadTechnicalDrawing technicalDrawing,
                                                      List<PadTechnicalDrawingLink> drawingLinks,
                                                      List<UploadedFileView> fileViewList) {
    List<String> references = drawingLinks.stream()
        .map(drawingLink -> drawingLink.getPipeline().getPipelineRef())
        .collect(Collectors.toUnmodifiableList());

    if (technicalDrawing.getFile() != null) {
      UploadedFileView fileView = fileViewList.stream()
          .filter(uploadedFileView -> uploadedFileView.getFileId().equals(technicalDrawing.getFileId()))
          .findFirst()
          .orElseThrow(() -> new PwaEntityNotFoundException(
              "Unable to get UploadedFileView of file with ID: " + technicalDrawing.getFileId()));
      return new PipelineDrawingSummaryView(technicalDrawing, references, fileView);
    }
    return new PipelineDrawingSummaryView(technicalDrawing, references);
  }

  @VisibleForTesting
  public PipelineDrawingSummaryView getPipelineDrawingSummaryViewFromDrawing(PwaApplicationDetail detail,
                                                                             PadTechnicalDrawing drawing) {
    var links = padTechnicalDrawingLinkService.getLinksFromDrawing(drawing);
    List<UploadedFileView> fileViews = padFileService.getUploadedFileViews(detail,
        FILE_PURPOSE,
        ApplicationFileLinkStatus.FULL);
    return buildSummaryView(drawing, links, fileViews);
  }

  @Transactional
  public void removeDrawing(PwaApplicationDetail detail, Integer drawingId, WebUserAccount webUserAccount) {
    var drawing = getDrawing(detail, drawingId);
    padTechnicalDrawingLinkService.unlinkDrawing(detail, drawing);
    padTechnicalDrawingRepository.delete(drawing);
    if (drawing.getFile() != null) {
      padFileService.processFileDeletion(drawing.getFile(), webUserAccount);
    }
  }

  @Transactional
  public void updateDrawing(PwaApplicationDetail detail, Integer drawingId, WebUserAccount webUserAccount,
                            PipelineDrawingForm form) {
    var drawing = getDrawing(detail, drawingId);
    padTechnicalDrawingLinkService.unlinkDrawing(detail, drawing);
    saveDrawingAndLink(detail, form, drawing);
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return drawingsValid(detail);
  }

  public PipelineDrawingValidationFactory getValidationFactory(PwaApplicationDetail detail) {

    var drawings = getDrawings(detail);
    var isComplete = drawingsValid(detail);

    return new PipelineDrawingValidationFactory(
        isComplete,
        drawings,
        this::isDrawingLinkedToFile,
        drawing -> "This drawing does not have an uploaded file"
    );
  }

  @Override
  @Deprecated
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    throw new ActionNotAllowedException("PadTechnicalDrawingService::validate should not be used");
  }

  public BindingResult validateDrawing(Object form, BindingResult bindingResult, ValidationType validationType,
                                       PwaApplicationDetail pwaApplicationDetail) {
    pipelineDrawingValidator.validate(form, bindingResult, pwaApplicationDetail, null,
        PipelineDrawingValidationType.ADD);
    groupValidator.validate(form, bindingResult, FullValidation.class, MandatoryUploadValidation.class);
    return bindingResult;
  }

  public BindingResult validateEdit(Object form, BindingResult bindingResult, ValidationType validationType,
                                    PwaApplicationDetail pwaApplicationDetail, Integer drawingId) {
    var drawing = getDrawing(pwaApplicationDetail, drawingId);
    pipelineDrawingValidator.validate(form, bindingResult, pwaApplicationDetail, drawing,
        PipelineDrawingValidationType.EDIT);
    groupValidator.validate(form, bindingResult, FullValidation.class, MandatoryUploadValidation.class);
    return bindingResult;
  }

  @VisibleForTesting
  public boolean drawingsValid(PwaApplicationDetail pwaApplicationDetail) {
    var drawings = getDrawings(pwaApplicationDetail);

    boolean allDrawingsLinkedToFile = drawings.stream()
        .allMatch(this::isDrawingLinkedToFile);

    return allDrawingsLinkedToFile && allPipelinesLinked(pwaApplicationDetail, drawings);
  }

  private boolean isDrawingLinkedToFile(PadTechnicalDrawing drawing) {
    return drawing.getFile() != null;
  }

  @VisibleForTesting
  public boolean allPipelinesLinked(PwaApplicationDetail pwaApplicationDetail, List<PadTechnicalDrawing> drawings) {
    var links = padTechnicalDrawingLinkService.getLinksFromDrawingList(drawings);
    var pipelines = padPipelineService.getPipelines(pwaApplicationDetail);

    Set<Integer> linkedPipelineIds = links.stream()
        .map(drawingLink -> drawingLink.getPipeline().getId())
        .collect(Collectors.toSet());

    return pipelines.stream()
        .allMatch(pipeline -> linkedPipelineIds.contains(pipeline.getId()));
  }

  /**
   * A drawing will only become unlinked once all pipelines have been removed.
   * Could have an effect if someone is mid-editing drawing. Will this be an issue?
   *
   * @param pwaApplicationDetail The current application detail
   */
  @Transactional
  public void cleanUnlinkedDrawings(PwaApplicationDetail pwaApplicationDetail) {
    var drawings = padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
    Map<PadTechnicalDrawing, List<PadTechnicalDrawingLink>> linkMap =
        padTechnicalDrawingLinkService.getLinksFromDrawingList(drawings)
            .stream()
            .collect(Collectors.groupingBy(PadTechnicalDrawingLink::getTechnicalDrawing));

    var drawingsToDelete = drawings.stream()
        .filter(drawing -> linkMap.keySet().stream()
            .noneMatch(groupedDrawing -> drawing.getId().equals(groupedDrawing.getId())))
        .collect(Collectors.toUnmodifiableList());

    if (!drawingsToDelete.isEmpty()) {
      padTechnicalDrawingRepository.deleteAll(drawingsToDelete);
    }
  }

  public BindingResult validateSection(BindingResult bindingResult, PwaApplicationDetail pwaApplicationDetail) {

    if (!getValidationFactory(pwaApplicationDetail).isComplete()) {
      bindingResult.reject("allPipelinesAdded" + FieldValidationErrorCodes.INVALID.getCode(),
          "All pipelines must be linked to a drawing and all drawings must have a file uploaded");
    }

    return bindingResult;
  }

  @Override
  public void cleanupData(PwaApplicationDetail detail) {

    List<Integer> padFileIdsOnDrawings = getDrawings(detail).stream()
        .map(drawing -> drawing.getFile().getId())
        .collect(Collectors.toList());

    padFileService.cleanupFiles(detail, FILE_PURPOSE, padFileIdsOnDrawings);

  }
}
