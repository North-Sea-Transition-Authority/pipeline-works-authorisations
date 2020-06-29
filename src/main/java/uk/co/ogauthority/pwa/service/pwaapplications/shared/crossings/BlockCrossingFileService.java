package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.CrossedBlockOwner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.repository.licence.PadCrossedBlockRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

@Service
public class BlockCrossingFileService implements ApplicationFormSectionService {

  private final PadCrossedBlockRepository padCrossedBlockRepository;
  private final SpringValidatorAdapter groupValidator;
  private final PadFileService padFileService;

  @Autowired
  public BlockCrossingFileService(PadCrossedBlockRepository padCrossedBlockRepository,
                                  SpringValidatorAdapter groupValidator,
                                  PadFileService padFileService) {
    this.padCrossedBlockRepository = padCrossedBlockRepository;

    this.groupValidator = groupValidator;
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
    padFileService.mapFilesToForm(form, detail, ApplicationFilePurpose.BLOCK_CROSSINGS);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    List<Object> hints = new ArrayList<>();
    if (validationType.equals(ValidationType.FULL)) {
      hints.add(FullValidation.class);
      if (requiresFullValidation(pwaApplicationDetail)) {
        hints.add(MandatoryUploadValidation.class);
      }
    } else {
      hints.add(PartialValidation.class);
    }
    groupValidator.validate(form, bindingResult, hints.toArray());
    return bindingResult;
  }
}
