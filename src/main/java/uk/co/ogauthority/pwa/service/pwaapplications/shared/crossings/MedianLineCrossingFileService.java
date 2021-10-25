package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadMedianLineAgreementRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.util.FileUploadUtils;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;

@Service
public class MedianLineCrossingFileService implements ApplicationFormSectionService {

  private final PadMedianLineAgreementRepository padMedianLineAgreementRepository;
  private final PadFileService padFileService;

  @Autowired
  public MedianLineCrossingFileService(PadMedianLineAgreementRepository padMedianLineAgreementRepository,
                                       PadFileService padFileService) {
    this.padMedianLineAgreementRepository = padMedianLineAgreementRepository;
    this.padFileService = padFileService;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new CrossingDocumentsForm();
    padFileService.mapFilesToForm(form, detail, ApplicationDetailFilePurpose.MEDIAN_LINE_CROSSING);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  public boolean requiresMandatoryValidation(PwaApplicationDetail pwaApplicationDetail) {
    var optionalAgreement = padMedianLineAgreementRepository.findByPwaApplicationDetail(pwaApplicationDetail);
    if (optionalAgreement.isEmpty()) {
      return false;
    } else {
      return optionalAgreement.get().getAgreementStatus() != MedianLineStatus.NOT_CROSSED;
    }
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {

    List<Object> hints = new ArrayList<>();
    if (validationType.equals(ValidationType.FULL) && requiresMandatoryValidation(pwaApplicationDetail)) {
      hints.add(MandatoryUploadValidation.class);
    }

    FileUploadUtils.validateFiles((CrossingDocumentsForm) form, bindingResult, hints);

    return bindingResult;

  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    // files copied in PadMedianLineAgreementService
  }
}
