package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawingLink;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositDrawingsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPermanentDepositRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.permanentdepositdrawings.PadDepositDrawingLinkRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.permanentdepositdrawings.PadDepositDrawingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskInfo;

import javax.transaction.Transactional;


/* Service providing simplified API for Permanent Deposit Drawings app form */
@Service
public class DepositDrawingsService implements ApplicationFormSectionService {

  private final PadDepositDrawingRepository padDepositDrawingRepository;
  private final PadPermanentDepositRepository padPermanentDepositRepository;
  private final PadDepositDrawingLinkRepository padDepositDrawingLinkRepository;
  private final SpringValidatorAdapter groupValidator;
  private final PadFileService padFileService;

  @Autowired
  public DepositDrawingsService(
      PadDepositDrawingRepository padDepositDrawingRepository,
      PadPermanentDepositRepository padPermanentDepositRepository,
      PadDepositDrawingLinkRepository padDepositDrawingLinkRepository,
      SpringValidatorAdapter groupValidator,
      PadFileService padFileService) {
    this.padDepositDrawingRepository = padDepositDrawingRepository;
    this.padPermanentDepositRepository = padPermanentDepositRepository;
    this.padDepositDrawingLinkRepository = padDepositDrawingLinkRepository;
    this.groupValidator = groupValidator;
    this.padFileService = padFileService;
  }

  @Transactional
  public void addDrawing(PwaApplicationDetail detail, PermanentDepositDrawingsForm form) {
    var drawing = new PadDepositDrawing();
    // Validated form will always have 1 file
    PadFile file = padFileService.getPadFileByPwaApplicationDetailAndFileId(detail,
        form.getUploadedFileWithDescriptionForms().get(0).getUploadedFileId());
    drawing.setFile(file);
    drawing.setPwaApplicationDetail(detail);
    drawing.setReference(form.getReference());
    padDepositDrawingRepository.save(drawing);

    var drawingLink = new PadDepositDrawingLink();
    drawingLink.setPadDepositDrawingId(drawing);
    for (String padPermanentDepositId: form.getSelectedDeposits()) {
      if (padPermanentDepositId != "") {
        var padPermanentDeposit = padPermanentDepositRepository.findById(Integer.parseInt(padPermanentDepositId))
            .orElseThrow(() -> new PwaEntityNotFoundException(String.format("Couldn't find padPermanentDeposit with ID: %s", padPermanentDepositId)));
        drawingLink.setPermanentDepositInfo(padPermanentDeposit);
        padDepositDrawingLinkRepository.save(drawingLink);
      }
    }
  }




  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return false;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult,
                                ValidationType validationType, PwaApplicationDetail pwaApplicationDetail) {
    return null;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return false;
  }

  @Override
  public List<TaskInfo> getTaskInfoList(PwaApplicationDetail pwaApplicationDetail) {
    return null;
  }
}

