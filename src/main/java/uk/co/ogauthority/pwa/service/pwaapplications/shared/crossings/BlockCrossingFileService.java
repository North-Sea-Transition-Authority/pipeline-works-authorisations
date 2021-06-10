package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.CrossedBlockOwner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.repository.licence.PadCrossedBlockRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.FileUploadUtils;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;

@Service
public class BlockCrossingFileService implements ApplicationFormSectionService {

  private final PadCrossedBlockRepository padCrossedBlockRepository;
  private final PadFileService padFileService;

  @Autowired
  public BlockCrossingFileService(PadCrossedBlockRepository padCrossedBlockRepository,
                                  PadFileService padFileService) {
    this.padCrossedBlockRepository = padCrossedBlockRepository;
    this.padFileService = padFileService;
  }

  public boolean requiresFullValidation(PwaApplicationDetail pwaApplicationDetail) {
    // return 'true' for full validation if non holder organisations listed as crossed block owners
    return padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetailAndBlockOwnerIn(
        pwaApplicationDetail,
        List.of(CrossedBlockOwner.PORTAL_ORGANISATION)
    ) > 0;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new CrossingDocumentsForm();
    padFileService.mapFilesToForm(form, detail, ApplicationDetailFilePurpose.BLOCK_CROSSINGS);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {

    List<Object> hints = new ArrayList<>();
    if (validationType.equals(ValidationType.FULL) && requiresFullValidation(pwaApplicationDetail)) {
      hints.add(MandatoryUploadValidation.class);
    }

    FileUploadUtils.validateFiles((CrossingDocumentsForm) form, bindingResult, hints);

    return bindingResult;

  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    // files copied in BlockCrossingService
  }
}
