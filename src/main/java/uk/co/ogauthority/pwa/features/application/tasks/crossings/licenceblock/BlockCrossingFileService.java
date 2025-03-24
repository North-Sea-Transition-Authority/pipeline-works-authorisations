package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.CrossingOwner;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.formhelpers.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Service
public class BlockCrossingFileService implements ApplicationFormSectionService {

  private final PadCrossedBlockRepository padCrossedBlockRepository;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public BlockCrossingFileService(PadCrossedBlockRepository padCrossedBlockRepository,
                                  PadFileManagementService padFileManagementService) {
    this.padCrossedBlockRepository = padCrossedBlockRepository;
    this.padFileManagementService = padFileManagementService;
  }

  public boolean requiresFullValidation(PwaApplicationDetail pwaApplicationDetail) {
    // return 'true' for full validation if non holder organisations listed as crossed block owners
    return padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetailAndBlockOwnerIn(
        pwaApplicationDetail,
        List.of(CrossingOwner.PORTAL_ORGANISATION)
    ) > 0;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new CrossingDocumentsForm();
    padFileManagementService.mapFilesToForm(form, detail, FileDocumentType.BLOCK_CROSSINGS);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    var castedForm = (CrossingDocumentsForm) form;

    if (validationType.equals(ValidationType.FULL) && requiresFullValidation(pwaApplicationDetail)) {
      FileValidationUtils.validator()
          .withMinimumNumberOfFiles(1, "Upload at least one file")
          .validate(bindingResult, castedForm.getUploadedFiles());
    } else {
      FileValidationUtils.validator()
          .validate(bindingResult, castedForm.getUploadedFiles());
    }

    return bindingResult;

  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    // files copied in BlockCrossingService
  }
}
