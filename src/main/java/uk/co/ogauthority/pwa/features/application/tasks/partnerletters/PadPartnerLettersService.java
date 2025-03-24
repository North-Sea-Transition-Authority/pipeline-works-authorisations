package uk.co.ogauthority.pwa.features.application.tasks.partnerletters;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

/* Service providing simplified API for PartnerLettersService app form */
@Service
public class PadPartnerLettersService implements ApplicationFormSectionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PadPartnerLettersService.class);
  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.PARTNER_LETTERS;

  private final PwaApplicationDetailService applicationDetailService;
  private final PartnerLettersValidator partnerLettersValidator;
  private final PadFileManagementService padFileManagementService;


  @Autowired
  public PadPartnerLettersService(PwaApplicationDetailService applicationDetailService,
                                  PartnerLettersValidator partnerLettersValidator,
                                  PadFileManagementService padFileManagementService) {
    this.applicationDetailService = applicationDetailService;
    this.partnerLettersValidator = partnerLettersValidator;
    this.padFileManagementService = padFileManagementService;
  }

  public void mapEntityToForm(PwaApplicationDetail applicationDetail, PartnerLettersForm form) {
    if (BooleanUtils.isTrue(applicationDetail.getPartnerLettersRequired())) {
      form.setPartnerLettersConfirmed(applicationDetail.getPartnerLettersConfirmed());
      padFileManagementService.mapFilesToForm(form, applicationDetail, FileDocumentType.PARTNER_LETTERS);
    }
    form.setPartnerLettersRequired(applicationDetail.getPartnerLettersRequired());

  }

  /**
   * From the form extract form data and file data which should be persisted.
   * Any linked files which are not part of official "save" action are discarded.
   */
  @Transactional
  public void saveEntityUsingForm(PwaApplicationDetail applicationDetail, PartnerLettersForm form, WebUserAccount user) {
    var uploadedFiles = padFileManagementService.getUploadedFiles(applicationDetail, DOCUMENT_TYPE);
    applicationDetailService.updatePartnerLetters(applicationDetail, form);

    if (BooleanUtils.isTrue(form.getPartnerLettersRequired())) {
      padFileManagementService.saveFiles(form, applicationDetail, FileDocumentType.PARTNER_LETTERS);

    } else if (!uploadedFiles.isEmpty()) {
      uploadedFiles.forEach(padFileManagementService::deleteUploadedFile);
    }
  }

  public PartnerLettersView getPartnerLettersView(PwaApplicationDetail pwaApplicationDetail) {

    List<UploadedFileView> uploadedFileViews = new ArrayList<>();
    if (BooleanUtils.isTrue(pwaApplicationDetail.getPartnerLettersRequired())) {
      uploadedFileViews = padFileManagementService.getUploadedFileViews(pwaApplicationDetail, DOCUMENT_TYPE);
    }

    return new PartnerLettersView(
        pwaApplicationDetail.getPartnerLettersRequired(), pwaApplicationDetail.getPartnerLettersConfirmed(), uploadedFileViews);
  }


  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new PartnerLettersForm();
    mapEntityToForm(detail, form);
    BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
    validate(form, bindingResult, ValidationType.FULL, detail);
    return !bindingResult.hasErrors();
  }


  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    partnerLettersValidator.validate(form, bindingResult, validationType);
    return bindingResult;
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    padFileManagementService.copyUploadedFiles(fromDetail, toDetail, DOCUMENT_TYPE);
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return !pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION);
  }
}
