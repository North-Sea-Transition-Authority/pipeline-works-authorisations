package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.AdmiraltyChartDocumentForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

@Service
public class AdmiraltyChartFileService implements ApplicationFormSectionService {

  private final PadFileService padFileService;
  private final SpringValidatorAdapter groupValidator;

  @Autowired
  public AdmiraltyChartFileService(PadFileService padFileService, SpringValidatorAdapter groupValidator) {
    this.padFileService = padFileService;
    this.groupValidator = groupValidator;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new AdmiraltyChartDocumentForm();
    padFileService.mapFilesToForm(form, detail, ApplicationFilePurpose.ADMIRALTY_CHART);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  public boolean isUploadRequired(PwaApplicationDetail pwaApplicationDetail) {
    var appType = pwaApplicationDetail.getPwaApplicationType();
    return appType.equals(PwaApplicationType.INITIAL) || appType.equals(PwaApplicationType.CAT_1_VARIATION);
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    List<Object> hints = new ArrayList<>();
    if (validationType.equals(ValidationType.FULL)) {
      hints.add(FullValidation.class);
      if (isUploadRequired(pwaApplicationDetail)) {
        hints.add(MandatoryUploadValidation.class);
      }
    } else {
      hints.add(PartialValidation.class);
    }
    groupValidator.validate(form, bindingResult, hints.toArray());

    if (((AdmiraltyChartDocumentForm) form).getUploadedFileWithDescriptionForms().size() > 1) {
      bindingResult.rejectValue("uploadedFileWithDescriptionForms",
          "uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.EXCEEDED_MAXIMUM_FILE_UPLOAD_COUNT.getCode(),
          "You must provide a single admiralty chart");
    }

    return bindingResult;
  }

  public boolean canUploadDocuments(PwaApplicationDetail detail) {
    switch (detail.getPwaApplicationType()) {
      case INITIAL:
      case CAT_1_VARIATION:
        return true;
      default:
        return false;
    }
  }

}
