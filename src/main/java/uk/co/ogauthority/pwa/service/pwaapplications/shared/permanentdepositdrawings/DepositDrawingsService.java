package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositDrawingForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositDrawingView;
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

  private final PadDepositDrawingRepository padDepositDrawingRepository;
  private final PadDepositDrawingLinkRepository padDepositDrawingLinkRepository;
  private final PermanentDepositsDrawingValidator permanentDepositsDrawingValidator;
  private final SpringValidatorAdapter groupValidator;
  private final PadFileService padFileService;
  private final PermanentDepositService permanentDepositService;

  private static final ApplicationFilePurpose FILE_PURPOSE = ApplicationFilePurpose.DEPOSIT_DRAWINGS;

  @Autowired
  public DepositDrawingsService(
      PadDepositDrawingRepository padDepositDrawingRepository,
      PadDepositDrawingLinkRepository padDepositDrawingLinkRepository,
      PermanentDepositsDrawingValidator permanentDepositsDrawingValidator,
      SpringValidatorAdapter groupValidator,
      PadFileService padFileService,
      PermanentDepositService permanentDepositService) {
    this.padDepositDrawingRepository = padDepositDrawingRepository;
    this.padDepositDrawingLinkRepository = padDepositDrawingLinkRepository;
    this.permanentDepositsDrawingValidator = permanentDepositsDrawingValidator;
    this.groupValidator = groupValidator;
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


  public List<PermanentDepositDrawingView> getDepositDrawingSummaryViews(PwaApplicationDetail pwaApplicationDetail) {
    var drawings = padDepositDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
    var links = padDepositDrawingLinkRepository.getAllByPadDepositDrawingIn(drawings);
    Map<PadDepositDrawing, List<PadDepositDrawingLink>> linkMap = links.stream()
        .collect(Collectors.groupingBy(PadDepositDrawingLink::getPadDepositDrawing));
    for (var drawing : drawings) {
      if (!linkMap.containsKey(drawing)) {
        linkMap.put(drawing, List.of());
      }
    }

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


  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    for (var deposit : permanentDepositService.getPermanentDeposits(detail)) {
      if (padDepositDrawingLinkRepository.findByPadPermanentDeposit(deposit).isEmpty()) {
        return false;
      }
    }

    var depositDrawings = padDepositDrawingRepository.getAllByPwaApplicationDetail(detail);
    for (var depositDrawing : depositDrawings) {
      var form = new PermanentDepositDrawingForm();
      mapEntityToForm(detail, depositDrawing, form);

      BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
      validateDrawingEdit(form, bindingResult, ValidationType.FULL, detail, depositDrawing.getId());
      if (bindingResult.hasErrors()) {
        return false;
      }
    }

    return depositDrawings.size() > 0;
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
}

