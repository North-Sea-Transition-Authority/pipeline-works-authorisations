package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PhysicalPipelineState;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.util.CleanupUtils;

@Service
public class PadTechnicalDrawingService {

  private final PadTechnicalDrawingRepository padTechnicalDrawingRepository;
  private final PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;
  private final PadFileService padFileService;
  private final PadPipelineService padPipelineService;
  private final PipelineDrawingValidator pipelineDrawingValidator;

  private static final ApplicationDetailFilePurpose FILE_PURPOSE = ApplicationDetailFilePurpose.PIPELINE_DRAWINGS;

  @Autowired
  public PadTechnicalDrawingService(
      PadTechnicalDrawingRepository padTechnicalDrawingRepository,
      PadTechnicalDrawingLinkService padTechnicalDrawingLinkService,
      PadFileService padFileService, PadPipelineService padPipelineService,
      PipelineDrawingValidator pipelineDrawingValidator) {
    this.padTechnicalDrawingRepository = padTechnicalDrawingRepository;
    this.padTechnicalDrawingLinkService = padTechnicalDrawingLinkService;
    this.padFileService = padFileService;
    this.padPipelineService = padPipelineService;
    this.pipelineDrawingValidator = pipelineDrawingValidator;
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
        .filter(pipelineOverview -> isDrawingRequiredForPipeline(pipelineOverview.getPipelineStatus()))
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

  public boolean isDrawingRequiredForPipeline(PipelineStatus pipelineStatus) {
    return pipelineStatus.getPhysicalPipelineState() == PhysicalPipelineState.ON_SEABED;
  }

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

  public BindingResult validateDrawing(Object form, BindingResult bindingResult, ValidationType validationType,
                                       PwaApplicationDetail pwaApplicationDetail) {
    var validationHints = new PadTechnicalDrawingValidationHints(
        pwaApplicationDetail, null, PipelineDrawingValidationType.ADD);
    pipelineDrawingValidator.validate(form, bindingResult, validationHints);
    return bindingResult;
  }

  public BindingResult validateEdit(Object form, BindingResult bindingResult, ValidationType validationType,
                                    PwaApplicationDetail pwaApplicationDetail, Integer drawingId) {
    var drawing = getDrawing(pwaApplicationDetail, drawingId);
    var validationHints = new PadTechnicalDrawingValidationHints(
        pwaApplicationDetail, drawing, PipelineDrawingValidationType.EDIT);
    pipelineDrawingValidator.validate(form, bindingResult, validationHints);
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
    var pipelines = padPipelineService.getPipelines(pwaApplicationDetail).stream()
        .filter(pipeline -> isDrawingRequiredForPipeline(pipeline.getPipelineStatus()))
        .collect(Collectors.toList());

    Set<Integer> linkedPipelineIds = links.stream()
        .map(drawingLink -> drawingLink.getPipeline().getId())
        .collect(Collectors.toSet());

    return pipelines.stream()
        .allMatch(pipeline -> linkedPipelineIds.contains(pipeline.getId()));
  }

  @Transactional
  public void removePadPipelineFromDrawings(PadPipeline padPipeline) {

    var pwaApplicationDetail = padPipeline.getPwaApplicationDetail();
    padTechnicalDrawingLinkService.removeAllPipelineLinks(pwaApplicationDetail, padPipeline);

    var drawings = padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
    Map<PadTechnicalDrawing, List<PadTechnicalDrawingLink>> linkMap =
        padTechnicalDrawingLinkService.getLinksFromDrawingList(drawings)
            .stream()
            .collect(Collectors.groupingBy(PadTechnicalDrawingLink::getTechnicalDrawing));

    var drawingsToDelete = CleanupUtils.getUnlinkedKeys(drawings, linkMap,
        (drawing, drawing2) -> drawing.getId().equals(drawing2.getId()));

    if (!drawingsToDelete.isEmpty()) {
      padTechnicalDrawingRepository.deleteAll(drawingsToDelete);
    }
  }

  public BindingResult validateSection(BindingResult bindingResult, PwaApplicationDetail pwaApplicationDetail) {

    if (!getValidationFactory(pwaApplicationDetail).isComplete()) {
      bindingResult.reject(PipelineSchematicsErrorCode.TECHNICAL_DRAWINGS.getErrorCode(),
          "All pipelines must be linked to a drawing and all drawings must have a file uploaded");
    }

    return bindingResult;
  }

  public void cleanupData(PwaApplicationDetail detail) {

    List<Integer> padFileIdsOnDrawings = getDrawings(detail).stream()
        .map(drawing -> drawing.getFile().getId())
        .collect(Collectors.toList());

    padFileService.cleanupFiles(detail, FILE_PURPOSE, padFileIdsOnDrawings);

  }

  public Map<PipelineId, PipelineDrawingSummaryView> getPipelineDrawingViewsMap(PwaApplicationDetail pwaApplicationDetail) {

    var drawingLinks =  padTechnicalDrawingLinkService.getLinksFromAppDetail(pwaApplicationDetail);
    List<UploadedFileView> fileViews = padFileService.getUploadedFileViews(pwaApplicationDetail,
        FILE_PURPOSE,
        ApplicationFileLinkStatus.FULL);

    Map<PipelineId, PipelineDrawingSummaryView> pipelineIdDrawingViewMap = new HashMap<>();
    drawingLinks.forEach(drawingLink -> {
      var summaryView = buildSummaryView(drawingLink.getTechnicalDrawing(), drawingLinks, fileViews);
      pipelineIdDrawingViewMap.put(drawingLink.getPipeline().getPipelineId(), summaryView);
    });

    return pipelineIdDrawingViewMap;
  }


}
