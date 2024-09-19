package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult;


/* Service providing simplified API for Permanent Deposit Drawings app form */
@Service
public class DepositDrawingsService implements ApplicationFormSectionService {

  private final PadDepositDrawingRepository padDepositDrawingRepository;
  private final PadDepositDrawingLinkRepository padDepositDrawingLinkRepository;
  private final PermanentDepositsDrawingValidator permanentDepositsDrawingValidator;
  private final PadFileService padFileService;
  private final PermanentDepositService permanentDepositService;

  private static final ApplicationDetailFilePurpose FILE_PURPOSE = ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS;

  @Autowired
  public DepositDrawingsService(
      PadDepositDrawingRepository padDepositDrawingRepository,
      PadDepositDrawingLinkRepository padDepositDrawingLinkRepository,
      PermanentDepositsDrawingValidator permanentDepositsDrawingValidator,
      PadFileService padFileService,
      PermanentDepositService permanentDepositService) {
    this.padDepositDrawingRepository = padDepositDrawingRepository;
    this.padDepositDrawingLinkRepository = padDepositDrawingLinkRepository;
    this.permanentDepositsDrawingValidator = permanentDepositsDrawingValidator;
    this.padFileService = padFileService;
    this.permanentDepositService = permanentDepositService;
  }


  public void mapEntityToForm(PwaApplicationDetail detail, PadDepositDrawing depositDrawing,
                              PermanentDepositDrawingForm form) {
    var depositDrawingLinks = padDepositDrawingLinkRepository.getAllByPadDepositDrawing(depositDrawing);
    form.setReference(depositDrawing.getReference());
    form.setSelectedDeposits(depositDrawingLinks
        .stream()
        .map(depositDrawingLink -> depositDrawingLink.getPadPermanentDeposit().getId().toString())
        .collect(Collectors.toSet()));

    if (depositDrawing.getFile() != null) {
      var file = padFileService.getUploadedFileView(detail, depositDrawing.getFile().getFileId(),
          FILE_PURPOSE, ApplicationFileLinkStatus.FULL);
      form.setUploadedFileWithDescriptionForms(List.of(
          new UploadFileWithDescriptionForm(file.getFileId(), file.getFileDescription(), file.getFileUploadedTime())));
    }
  }

  @Transactional
  public void addDrawing(PwaApplicationDetail detail, PermanentDepositDrawingForm form, WebUserAccount webUserAccount) {
    padFileService.updateFiles(
        form, detail, FILE_PURPOSE, FileUpdateMode.KEEP_UNLINKED_FILES, webUserAccount);
    saveDrawingAndLinks(detail, form, new PadDepositDrawing());
  }


  private void saveDrawingAndLinks(PwaApplicationDetail detail, PermanentDepositDrawingForm form,
                                   PadDepositDrawing drawing) {
    // Validated form will always have 1 file
    PadFile file = padFileService.getPadFileByPwaApplicationDetailAndFileId(detail,
        form.getUploadedFileWithDescriptionForms().get(0).getUploadedFileId());
    drawing.setFile(file);
    drawing.setPwaApplicationDetail(detail);
    drawing.setReference(form.getReference());
    drawing = padDepositDrawingRepository.save(drawing);

    for (String padPermanentDepositId : form.getSelectedDeposits()) {
      var padPermanentDeposit = permanentDepositService.getDepositById(Integer.parseInt(padPermanentDepositId))
          .orElseThrow(() -> new PwaEntityNotFoundException(
              String.format("Couldn't find padPermanentDeposit with ID: %s", padPermanentDepositId)));

      var drawingLink = new PadDepositDrawingLink(padPermanentDeposit, drawing);
      padDepositDrawingLinkRepository.save(drawingLink);
    }
  }


  @Transactional
  public List<PermanentDepositDrawingView> getDepositDrawingSummaryViews(PwaApplicationDetail pwaApplicationDetail) {

    // get all drawings for detail
    var drawings = padDepositDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);

    // get all links for the detail's drawings
    var links = padDepositDrawingLinkRepository.getAllByPadDepositDrawingIn(drawings);

    // map drawings that have links to a list of their links
    Map<PadDepositDrawing, List<PadDepositDrawingLink>> linkMap = links.stream()
        .collect(Collectors.groupingBy(PadDepositDrawingLink::getPadDepositDrawing));

    // for each drawing that has no links, manually add it to the drawing map
    drawings.forEach(d -> linkMap.putIfAbsent(d, List.of()));

    List<UploadedFileView> fileViews = padFileService.getUploadedFileViews(pwaApplicationDetail,
        FILE_PURPOSE,
        ApplicationFileLinkStatus.FULL);

    return linkMap.entrySet().stream()
        .map(entrySet -> buildSummaryView(entrySet.getKey(), entrySet.getValue(), fileViews))
        .sorted(Comparator.comparing(PermanentDepositDrawingView::getReference))
        .collect(Collectors.toList());
  }

  public PermanentDepositDrawingView getDepositDrawingView(Integer depositDrawingId,
                                                           PwaApplicationDetail pwaApplicationDetail) {
    var depositDrawing = padDepositDrawingRepository.findById(depositDrawingId)
        .orElseThrow(() -> getDrawingNotFoundException(depositDrawingId));
    var depositDrawingLinks = padDepositDrawingLinkRepository.getAllByPadDepositDrawing(depositDrawing);

    List<UploadedFileView> fileViews = new ArrayList<>();
    if (depositDrawing.getFile() != null) {
      fileViews.add(padFileService.getUploadedFileView(pwaApplicationDetail, depositDrawing.getFile().getFileId(),
          FILE_PURPOSE, ApplicationFileLinkStatus.FULL));
    }

    return buildSummaryView(depositDrawing, depositDrawingLinks, fileViews);
  }


  private PermanentDepositDrawingView buildSummaryView(PadDepositDrawing depositDrawing,
                                                       List<PadDepositDrawingLink> drawingLinks,
                                                       List<UploadedFileView> fileViewList) {
    Set<String> depositReferences = drawingLinks.stream()
        .map(drawingLink -> drawingLink.getPadPermanentDeposit().getReference())
        .collect(Collectors.toUnmodifiableSet());

    if (fileViewList.size() > 0 && depositDrawing.getFile() != null) {
      UploadedFileView fileView = fileViewList.stream()
          .filter(uploadedFileView -> uploadedFileView.getFileId().equals(depositDrawing.getFile().getFileId()))
          .findFirst()
          .orElseThrow(() -> new PwaEntityNotFoundException(
              "Unable to get UploadedFileView of file with ID: " + depositDrawing.getFile().getFileId()));
      return new PermanentDepositDrawingView(depositDrawing.getId(), depositDrawing.getReference(), depositReferences,
          fileView);
    }

    return new PermanentDepositDrawingView(depositDrawing.getId(), depositDrawing.getReference(), depositReferences);
  }

  @Transactional
  public void editDepositDrawing(int depositDrawingId, PwaApplicationDetail detail,
                                 PermanentDepositDrawingForm form, WebUserAccount webUserAccount) {
    padFileService.updateFiles(
        form,
        detail,
        FILE_PURPOSE,
        FileUpdateMode.KEEP_UNLINKED_FILES,
        webUserAccount);

    var depositDrawing = padDepositDrawingRepository.findById(depositDrawingId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Unable to find permanent deposit drawing with ID: " + depositDrawingId));
    List<PadDepositDrawingLink> depositDrawingLinks = padDepositDrawingLinkRepository.getAllByPadDepositDrawing(
        depositDrawing);
    padDepositDrawingLinkRepository.deleteAll(depositDrawingLinks);

    saveDrawingAndLinks(detail, form, depositDrawing);
  }

  @Transactional
  public void removeDepositFromDrawing(PadPermanentDeposit padPermanentDeposit) {
    removeDepositsFromDrawings(List.of(padPermanentDeposit));
  }

  @Transactional
  public void removeDepositsFromDrawings(Collection<PadPermanentDeposit> permanentDeposits) {
    List<PadDepositDrawingLink> links = padDepositDrawingLinkRepository.getAllByPadPermanentDepositIn(
        permanentDeposits);
    padDepositDrawingLinkRepository.deleteAll(links);
  }

  @Transactional
  public void removeDrawingAndFile(int depositDrawingId, WebUserAccount webUserAccount) {
    var depositDrawing = padDepositDrawingRepository.findById(depositDrawingId)
        .orElseThrow(() -> getDrawingNotFoundException(depositDrawingId));
    List<PadDepositDrawingLink> depositDrawingLinks = padDepositDrawingLinkRepository.getAllByPadDepositDrawing(
        depositDrawing);
    var padFile = depositDrawing.getFile();
    padDepositDrawingLinkRepository.deleteAll(depositDrawingLinks);
    padDepositDrawingRepository.delete(depositDrawing);
    if (padFile != null) {
      padFileService.processFileDeletion(depositDrawing.getFile(), webUserAccount);
    }
  }

  public Optional<PadDepositDrawing> getDrawingLinkedToPadFile(PwaApplicationDetail applicationDetail,
                                                               PadFile padFile) {
    return padDepositDrawingRepository.findByPwaApplicationDetailAndFile(applicationDetail, padFile);
  }

  public void unlinkFile(PadDepositDrawing depositDrawing) {
    depositDrawing.setFile(null);
    padDepositDrawingRepository.save(depositDrawing);
  }

  public PadDepositDrawing getDepositDrawing(Integer depositDrawingId) {
    return padDepositDrawingRepository.findById(depositDrawingId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find permanent deposit drawing with ID: %s", depositDrawingId)));
  }

  public SummaryScreenValidationResult getDepositDrawingSummaryScreenValidationResult(PwaApplicationDetail detail) {

    var deposits = permanentDepositService.getPermanentDeposits(detail);
    var allDepositsHaveDrawings = !deposits.isEmpty();
    for (var deposit : deposits) {
      if (padDepositDrawingLinkRepository.getAllByPadPermanentDeposit(deposit).isEmpty()) {
        allDepositsHaveDrawings = false;
        break;
      }
    }

    Map<String, String> invalidDrawingIdToDescriptorMap = new LinkedHashMap<>();
    var depositDrawings = padDepositDrawingRepository.getAllByPwaApplicationDetail(detail);
    for (var depositDrawing : depositDrawings) {
      var form = new PermanentDepositDrawingForm();
      mapEntityToForm(detail, depositDrawing, form);

      BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
      validateDrawingEdit(form, bindingResult, detail, depositDrawing.getId());
      if (bindingResult.hasErrors()) {
        invalidDrawingIdToDescriptorMap.put(
            String.valueOf(depositDrawing.getId()), depositDrawing.getReference());
      }
    }

    var sectionComplete = invalidDrawingIdToDescriptorMap.isEmpty() && allDepositsHaveDrawings;
    String sectionIncompleteError = !sectionComplete
        ? "Ensure that all deposits are linked to a deposit drawing" : null;

    return new SummaryScreenValidationResult(invalidDrawingIdToDescriptorMap,
        "deposit-drawing",
        "has errors",
        sectionComplete,
        sectionIncompleteError);

  }


  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return getDepositDrawingSummaryScreenValidationResult(detail).isSectionComplete();
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult,
                                ValidationType validationType, PwaApplicationDetail pwaApplicationDetail) {
    permanentDepositsDrawingValidator.validate(form, bindingResult, this, pwaApplicationDetail);
    return bindingResult;
  }

  public BindingResult validateDrawingEdit(Object form, BindingResult bindingResult,
                                           PwaApplicationDetail pwaApplicationDetail,
                                           Integer padDepositDrawingId) {
    permanentDepositsDrawingValidator.validate(form, bindingResult, this, pwaApplicationDetail, padDepositDrawingId);
    return bindingResult;
  }


  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return permanentDepositService.hasPermanentDepositBeenMade(pwaApplicationDetail)
        && permanentDepositService.canShowInTaskList(pwaApplicationDetail);
  }


  public boolean isDrawingReferenceUnique(String drawingRef, Integer padDepositDrawingId,
                                          PwaApplicationDetail pwaApplicationDetail) {
    var existingDrawings = padDepositDrawingRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(
        pwaApplicationDetail, drawingRef);
    return existingDrawings.isEmpty()
        || (existingDrawings.get().getId() != null && existingDrawings.get().getId().equals(padDepositDrawingId));
  }

  private PwaEntityNotFoundException getDrawingNotFoundException(int depositDrawingId) {
    return new PwaEntityNotFoundException(
        String.format("Couldn't find permanent deposit drawing with ID: %s", depositDrawingId));
  }

  @Override
  public void cleanupData(PwaApplicationDetail detail) {

    List<Integer> padFileIdsOnDrawings = padDepositDrawingRepository.getAllByPwaApplicationDetail(detail).stream()
        .map(drawing -> drawing.getFile().getId())
        .collect(Collectors.toList());

    padFileService.cleanupFiles(detail, FILE_PURPOSE, padFileIdsOnDrawings);

  }

  /**
   * See {@link PermanentDepositService#copySectionInformation}.
   */
  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    // assumed to done in core per deposit service
  }

  public List<PadDepositDrawing> getAllDepositDrawingsForDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padDepositDrawingRepository.findByPwaApplicationDetail(pwaApplicationDetail);
  }

  public Map<PadPermanentDeposit, List<PadDepositDrawingLink>> getDepositAndDrawingLinksMapForDeposits(
      Collection<PadPermanentDeposit> deposits) {
    return padDepositDrawingLinkRepository.getAllByPadPermanentDepositIn(deposits)
        .stream()
        .collect(Collectors.groupingBy(PadDepositDrawingLink::getPadPermanentDeposit));
  }

  void saveDepositDrawings(List<PadDepositDrawing> padDepositDrawings) {
    padDepositDrawingRepository.saveAll(padDepositDrawings);
  }

  public List<PadDepositDrawingLink> getAllDepositDrawingLinksByDetailPermanentDeposits(PwaApplicationDetail pwaApplicationDetail) {
    return padDepositDrawingLinkRepository.findByPadPermanentDeposit_PwaApplicationDetail(pwaApplicationDetail);
  }

  void saveDepositDrawingLinks(List<PadDepositDrawingLink> padDepositDrawingLinks) {
    padDepositDrawingLinkRepository.saveAll(padDepositDrawingLinks);
  }
}

