package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.techdrawings.PipelineDrawingSummaryView;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings.PadTechnicalDrawingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
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

  public List<PipelineDrawingSummaryView> getPipelineDrawingSummaryViews(PwaApplicationDetail detail) {
    var drawings = padTechnicalDrawingRepository.getAllByPwaApplicationDetail(detail);
    var links = padTechnicalDrawingLinkService.getLinksFromDrawingList(drawings);
    Map<PadTechnicalDrawing, List<PadTechnicalDrawingLink>> linkMap = links.stream()
        .collect(Collectors.groupingBy(PadTechnicalDrawingLink::getTechnicalDrawing));

    List<UploadedFileView> fileViews = padFileService.getUploadedFileViews(detail,
        ApplicationFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL);

    var summaryList = new ArrayList<PipelineDrawingSummaryView>();

    for (PadTechnicalDrawing technicalDrawing : linkMap.keySet()) {
      List<PipelineOverview> overviews = linkMap.get(technicalDrawing)
          .stream()
          .map(drawingLink -> new PipelineOverview(drawingLink.getPipeline(), List.of()))
          .collect(Collectors.toUnmodifiableList());

      UploadedFileView fileView = fileViews.stream()
          .filter(uploadedFileView -> uploadedFileView.getFileId().equals(technicalDrawing.getFileId()))
          .findFirst()
          .orElseThrow(() -> new PwaEntityNotFoundException(
              "Unable to get UploadedFileView of file with ID: " + technicalDrawing.getFileId()));
      summaryList.add(new PipelineDrawingSummaryView(technicalDrawing, overviews, fileView));
    }

    summaryList.sort(Comparator.comparing(PipelineDrawingSummaryView::getReference));

    return summaryList;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var drawings = getDrawings(detail);
    List<Integer> linkedPipelineIds = padTechnicalDrawingLinkService.getLinksFromDrawingList(drawings)
        .stream()
        .map(padTechnicalDrawingLink -> padTechnicalDrawingLink.getPipeline().getId())
        .collect(Collectors.toUnmodifiableList());

    return padPipelineService.getPipelines(detail)
        .stream()
        .map(PadPipeline::getId)
        .allMatch(linkedPipelineIds::contains);
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    pipelineDrawingValidator.validate(form, bindingResult, pwaApplicationDetail);
    groupValidator.validate(form, bindingResult, FullValidation.class, MandatoryUploadValidation.class);
    return bindingResult;
  }
}
