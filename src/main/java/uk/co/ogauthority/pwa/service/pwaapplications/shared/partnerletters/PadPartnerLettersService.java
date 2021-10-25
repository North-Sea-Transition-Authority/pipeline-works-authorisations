package uk.co.ogauthority.pwa.service.pwaapplications.shared.partnerletters;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.partnerletters.PartnerLettersForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PartnerLettersView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.validators.partnerletters.PartnerLettersValidator;

/* Service providing simplified API for PartnerLettersService app form */
@Service
public class PadPartnerLettersService implements ApplicationFormSectionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PadPartnerLettersService.class);

  private final PwaApplicationDetailService applicationDetailService;
  private final PartnerLettersValidator partnerLettersValidator;
  private final PadFileService padFileService;

  private static final ApplicationDetailFilePurpose FILE_PURPOSE = ApplicationDetailFilePurpose.PARTNER_LETTERS;

  @Autowired
  public PadPartnerLettersService(PwaApplicationDetailService applicationDetailService,
                                  PartnerLettersValidator partnerLettersValidator,
                                  PadFileService padFileService) {
    this.applicationDetailService = applicationDetailService;
    this.partnerLettersValidator = partnerLettersValidator;
    this.padFileService = padFileService;
  }



  public void mapEntityToForm(PwaApplicationDetail applicationDetail, PartnerLettersForm form) {
    if (BooleanUtils.isTrue(applicationDetail.getPartnerLettersRequired())) {
      form.setPartnerLettersConfirmed(applicationDetail.getPartnerLettersConfirmed());
      padFileService.mapFilesToForm(form, applicationDetail, FILE_PURPOSE);
    }
    form.setPartnerLettersRequired(applicationDetail.getPartnerLettersRequired());

  }


  /**
   * From the form extract form data and file data which should be persisted.
   * Any linked files which are not part of official "save" action are discarded.
   */
  @Transactional
  public void saveEntityUsingForm(PwaApplicationDetail applicationDetail, PartnerLettersForm form, WebUserAccount user) {
    var uploadedFiles = padFileService.getAllByPwaApplicationDetailAndPurpose(applicationDetail, FILE_PURPOSE);
    applicationDetailService.updatePartnerLetters(applicationDetail, form);

    if (BooleanUtils.isTrue(form.getPartnerLettersRequired())) {
      padFileService.updateFiles(form, applicationDetail, FILE_PURPOSE,
          FileUpdateMode.DELETE_UNLINKED_FILES, user);

    } else if (!uploadedFiles.isEmpty()) {
      uploadedFiles.forEach(padFile -> padFileService.processFileDeletion(padFile, user));

    }
  }

  public PartnerLettersView getPartnerLettersView(PwaApplicationDetail pwaApplicationDetail) {

    List<UploadedFileView> uploadedFileViews = new ArrayList<>();
    if (BooleanUtils.isTrue(pwaApplicationDetail.getPartnerLettersRequired())) {
      uploadedFileViews = padFileService.getUploadedFileViews(pwaApplicationDetail, FILE_PURPOSE,
          ApplicationFileLinkStatus.FULL);
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
    if (validationType.equals(ValidationType.FULL)) {
      partnerLettersValidator.validate(form, bindingResult);
    }
    return bindingResult;
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    padFileService.copyPadFilesToPwaApplicationDetail(
        fromDetail,
        toDetail,
        ApplicationDetailFilePurpose.PARTNER_LETTERS,
        ApplicationFileLinkStatus.FULL
    );
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return !pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION);
  }
}
