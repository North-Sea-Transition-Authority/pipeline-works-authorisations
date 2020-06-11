package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings;

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
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawingLink;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositDrawingForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositDrawingView;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPermanentDepositRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.permanentdepositdrawings.PadDepositDrawingLinkRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.permanentdepositdrawings.PadDepositDrawingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;
import uk.co.ogauthority.pwa.validators.PermanentDepositsDrawingValidator;


/* Service providing simplified API for Permanent Deposit Drawings app form */
@Service
public class DepositDrawingsService implements ApplicationFormSectionService {

  private final PermanentDepositService permanentDepositService;
  private final PadDepositDrawingRepository padDepositDrawingRepository;
  private final PadPermanentDepositRepository padPermanentDepositRepository;
  private final PadDepositDrawingLinkRepository padDepositDrawingLinkRepository;
  private final PermanentDepositsDrawingValidator permanentDepositsDrawingValidator;
  private final SpringValidatorAdapter groupValidator;
  private final PadFileService padFileService;

  @Autowired
  public DepositDrawingsService(
      PermanentDepositService permanentDepositService,
      PadDepositDrawingRepository padDepositDrawingRepository,
      PadPermanentDepositRepository padPermanentDepositRepository,
      PadDepositDrawingLinkRepository padDepositDrawingLinkRepository,
      PermanentDepositsDrawingValidator permanentDepositsDrawingValidator,
      SpringValidatorAdapter groupValidator,
      PadFileService padFileService) {
    this.permanentDepositService = permanentDepositService;
    this.padDepositDrawingRepository = padDepositDrawingRepository;
    this.padPermanentDepositRepository = padPermanentDepositRepository;
    this.padDepositDrawingLinkRepository = padDepositDrawingLinkRepository;
    this.permanentDepositsDrawingValidator = permanentDepositsDrawingValidator;
    this.groupValidator = groupValidator;
    this.padFileService = padFileService;
  }



  public void mapEntityToForm(PwaApplicationDetail detail, PadDepositDrawing depositDrawing, PermanentDepositDrawingForm form) {
    var depositDrawingLinks = padDepositDrawingLinkRepository.getAllByPadDepositDrawing(depositDrawing);
    form.setReference(depositDrawing.getReference());
    form.setSelectedDeposits(depositDrawingLinks
        .stream()
        .map(depositDrawingLink -> depositDrawingLink.getPadPermanentDeposit().getId().toString())
        .collect(Collectors.toSet()));

    if (depositDrawing.getFile() != null) {
      var file = padFileService.getUploadedFileView(detail, depositDrawing.getFile().getFileId(),
          ApplicationFilePurpose.DEPOSIT_DRAWINGS, ApplicationFileLinkStatus.FULL);
      form.setUploadedFileWithDescriptionForms(List.of(
          new UploadFileWithDescriptionForm(file.getFileId(), file.getFileDescription(), file.getFileUploadedTime())));
    }
  }

  @Transactional
  public void addDrawing(PwaApplicationDetail detail, PermanentDepositDrawingForm form, WebUserAccount webUserAccount) {
    padFileService.updateFiles(
        form, detail, ApplicationFilePurpose.DEPOSIT_DRAWINGS, FileUpdateMode.KEEP_UNLINKED_FILES, webUserAccount);
    saveDrawingAndLinks(detail, form, new PadDepositDrawing());
  }


  private void saveDrawingAndLinks(PwaApplicationDetail detail, PermanentDepositDrawingForm form, PadDepositDrawing drawing) {
    // Validated form will always have 1 file
    PadFile file = padFileService.getPadFileByPwaApplicationDetailAndFileId(detail,
        form.getUploadedFileWithDescriptionForms().get(0).getUploadedFileId());
    drawing.setFile(file);
    drawing.setPwaApplicationDetail(detail);
    drawing.setReference(form.getReference());
    drawing = padDepositDrawingRepository.save(drawing);

    for (String padPermanentDepositId: form.getSelectedDeposits()) {
      var padPermanentDeposit = permanentDepositService.getDepositById(Integer.parseInt(padPermanentDepositId))
          .orElseThrow(() -> new PwaEntityNotFoundException(
              String.format("Couldn't find padPermanentDeposit with ID: %s", padPermanentDepositId)));

      var drawingLink = new PadDepositDrawingLink(padPermanentDeposit, drawing);
      padDepositDrawingLinkRepository.save(drawingLink);
    }
  }



  public List<PermanentDepositDrawingView> getDepositDrawingSummaryViews(PwaApplicationDetail pwaApplicationDetail) {
    var drawings = padDepositDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
    var links = padDepositDrawingLinkRepository.getAllByPadDepositDrawingIn(drawings);
    Map<PadDepositDrawing, List<PadDepositDrawingLink>> linkMap = links.stream()
        .collect(Collectors.groupingBy(PadDepositDrawingLink::getPadDepositDrawing));

    List<UploadedFileView> fileViews = padFileService.getUploadedFileViews(pwaApplicationDetail,
        ApplicationFilePurpose.DEPOSIT_DRAWINGS,
        ApplicationFileLinkStatus.FULL);

    return linkMap.entrySet().stream()
        .map(entrySet -> buildSummaryView(entrySet.getKey(), entrySet.getValue(), fileViews))
        .sorted(Comparator.comparing(PermanentDepositDrawingView::getReference))
        .collect(Collectors.toList());
  }

  public PermanentDepositDrawingView getDepositDrawingView(Integer depositDrawingId, PwaApplicationDetail pwaApplicationDetail) {
    var depositDrawing = padDepositDrawingRepository.findById(depositDrawingId)
        .orElseThrow(() -> getDrawingNotFoundException(depositDrawingId));
    var depositDrawingLinks = padDepositDrawingLinkRepository.getAllByPadDepositDrawing(depositDrawing);

    UploadedFileView fileViews = padFileService.getUploadedFileView(pwaApplicationDetail, depositDrawing.getFile().getFileId(),
        ApplicationFilePurpose.DEPOSIT_DRAWINGS, ApplicationFileLinkStatus.FULL);

    return buildSummaryView(depositDrawing, depositDrawingLinks, List.of(fileViews));
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
      return new PermanentDepositDrawingView(depositDrawing.getId(), depositDrawing.getReference(), depositReferences, fileView);
    }

    return new PermanentDepositDrawingView(depositDrawing.getId(), depositDrawing.getReference(), depositReferences);
  }

  @Transactional
  public void editDepositDrawing(int depositDrawingId, PwaApplicationDetail detail,
                                 PermanentDepositDrawingForm form, WebUserAccount webUserAccount) {
    padFileService.updateFiles(
        form,
        detail,
        ApplicationFilePurpose.DEPOSIT_DRAWINGS,
        FileUpdateMode.KEEP_UNLINKED_FILES,
        webUserAccount);

    var depositDrawing = padDepositDrawingRepository.findById(depositDrawingId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Unable to find permanent deposit drawing with ID: " + depositDrawingId));
    List<PadDepositDrawingLink> depositDrawingLinks = padDepositDrawingLinkRepository.getAllByPadDepositDrawing(depositDrawing);
    padDepositDrawingLinkRepository.deleteAll(depositDrawingLinks);

    saveDrawingAndLinks(detail, form, depositDrawing);
  }

  public void removeDrawingAndFile(int depositDrawingId, WebUserAccount webUserAccount) {
    var depositDrawing = padDepositDrawingRepository.findById(depositDrawingId)
        .orElseThrow(() -> getDrawingNotFoundException(depositDrawingId));
    List<PadDepositDrawingLink> depositDrawingLinks = padDepositDrawingLinkRepository.getAllByPadDepositDrawing(depositDrawing);
    padDepositDrawingLinkRepository.deleteAll(depositDrawingLinks);
    padDepositDrawingRepository.delete(depositDrawing);
    padFileService.processFileDeletion(depositDrawing.getFile(), webUserAccount);
  }

  public Optional<PadDepositDrawing> getDrawingLinkedToPadFile(PwaApplicationDetail applicationDetail, PadFile padFile) {
    return padDepositDrawingRepository.findByPwaApplicationDetailAndAndFile(applicationDetail, padFile);
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




  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return false;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult,
                                ValidationType validationType, PwaApplicationDetail pwaApplicationDetail) {
    permanentDepositsDrawingValidator.validate(form, bindingResult, this, pwaApplicationDetail);
    groupValidator.validate(form, bindingResult, FullValidation.class, MandatoryUploadValidation.class);
    return bindingResult;
  }

  public BindingResult validateDrawingEdit(Object form, BindingResult bindingResult, ValidationType validationType,
                                           PwaApplicationDetail pwaApplicationDetail, Integer padDepositDrawingId) {
    permanentDepositsDrawingValidator.validate(form, bindingResult, this, pwaApplicationDetail, padDepositDrawingId);
    return bindingResult;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return permanentDepositService.hasPermanentDepositBeenMade(pwaApplicationDetail);
  }



  public boolean isDrawingReferenceUnique(String drawingRef, Integer padDepositDrawingId, PwaApplicationDetail pwaApplicationDetail) {
    var existingDrawings = padDepositDrawingRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(
        pwaApplicationDetail, drawingRef);
    return existingDrawings.isEmpty()
        || (existingDrawings.get().getId() != null && existingDrawings.get().getId().equals(padDepositDrawingId));
  }

  private PwaEntityNotFoundException getDrawingNotFoundException(int depositDrawingId) {
    return new PwaEntityNotFoundException(
        String.format("Couldn't find permanent deposit drawing with ID: %s", depositDrawingId));
  }


}

