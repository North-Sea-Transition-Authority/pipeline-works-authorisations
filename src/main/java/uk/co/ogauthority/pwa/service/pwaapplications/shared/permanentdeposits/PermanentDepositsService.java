package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PermanentDepositInfoFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PermanentDepositInformation;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PermanentDepositInformationRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;
import uk.co.ogauthority.pwa.validators.PermanentDepositsValidator;

/* Service providing simplified API for Permanent Deposit app form */
@Service
public class PermanentDepositsService implements ApplicationFormSectionService {

  private final PermanentDepositInformationRepository permanentDepositInformationRepository;
  private final PermanentDepositsFileService permanentDepositsFileService;
  private final PermanentDepositsEntityMappingService permanentDepositsEntityMappingService;
  private final PermanentDepositsValidator permanentDepositsValidator;
  private final SpringValidatorAdapter groupValidator;

  @Autowired
  public PermanentDepositsService(
      PermanentDepositInformationRepository permanentDepositInformationRepository,
      PermanentDepositsFileService permanentDepositsFileService,
      PermanentDepositsEntityMappingService permanentDepositsEntityMappingService,
      PermanentDepositsValidator permanentDepositsValidator,
      SpringValidatorAdapter groupValidator) {
    this.permanentDepositInformationRepository = permanentDepositInformationRepository;
    this.permanentDepositsFileService = permanentDepositsFileService;
    this.permanentDepositsEntityMappingService = permanentDepositsEntityMappingService;
    this.permanentDepositsValidator = permanentDepositsValidator;
    this.groupValidator = groupValidator;
  }

  public PermanentDepositInformation getPermanentDepositData(PwaApplicationDetail pwaApplicationDetail) {
    var permanentDepositInformation = permanentDepositInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PermanentDepositInformation());
    permanentDepositInformation.setPwaApplicationDetail(pwaApplicationDetail);
    return permanentDepositInformation;
  }


  /**
   * Map stored data to form including uploaded files depending on requested link status.
   *
   * @param permanentDepositInformation     stored data
   * @param form                      form to map to
   * @param applicationFileLinkStatus link status of uploaded files to be included in form.
   */
  public void mapEntityToForm(PermanentDepositInformation permanentDepositInformation,
                              PermanentDepositsForm form,
                              ApplicationFileLinkStatus applicationFileLinkStatus) {

    permanentDepositsEntityMappingService.mapDepositInformationDataToForm(permanentDepositInformation, form);

    // only attach files with matching link status to form
    //    var uploadedFilesWithDescriptionFormList = permanentDepositsFileService.getUploadedFileListAsFormList(
    //        padProjectInformation.getPwaApplicationDetail(),
    //        applicationFileLinkStatus
    //    );
    //
    //    form.setUploadedFileWithDescriptionForms(uploadedFilesWithDescriptionFormList);
  }


  /**
   * From the form extract form data and file data which should be persisted.
   * Any linked files which are not part of official "save" action are discarded.
   */
  @Transactional
  public void saveEntityUsingForm(PermanentDepositInformation permanentDepositInformation,
                                  PermanentDepositsForm form,
                                  WebUserAccount user) {
    permanentDepositsEntityMappingService.setEntityValuesUsingForm(permanentDepositInformation, form);
    //    permanentDepositsFileService.updateOrDeleteLinkedFilesUsingForm(
    //        padProjectInformation.getPwaApplicationDetail(),
    //        form,
    //        user
    //    );
    permanentDepositInformationRepository.save(permanentDepositInformation);

  }

  /**
   * Simplify api by providing pass through method to access file service.
   */
  public List<UploadedFileView> getUpdatedPermanentDepositFileViewsWhenFileOnForm(
      PwaApplicationDetail pwaApplicationDetail,
      PermanentDepositsForm form) {
    return permanentDepositsFileService.getUpdatedPermanentDepositFileViewsWhenFileOnForm(pwaApplicationDetail,
        form);

  }

  /**
   * Simplify api by providing pass through method to access file service.
   */
  public PermanentDepositInfoFile getPermanentDepositFile(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    return permanentDepositsFileService.getPermanentDepositInformationFile(fileId,
        pwaApplicationDetail);
  }


  @Transactional
  public void deleteUploadedFileLink(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    PermanentDepositInfoFile existingFile = permanentDepositsFileService.getPermanentDepositInformationFile(fileId,
        pwaApplicationDetail);
    permanentDepositsFileService.deletePermanentDepositInfoFileLink(existingFile);
  }

  /**
   * Method which creates "temporary" link to application detail Permanent Deposit.
   * If form left unsaved, we know which files are deletable.
   */
  @Transactional
  public void createUploadedFileLink(String uploadedFileId, PwaApplicationDetail pwaApplicationDetail) {
    permanentDepositsFileService.createAndSavePermanentDepositInfoFile(
        pwaApplicationDetail,
        uploadedFileId
    );
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {

    PermanentDepositInformation permanentDepositInformation = getPermanentDepositData(detail);
    var projectInformationForm = new PermanentDepositsForm();
    mapEntityToForm(permanentDepositInformation, projectInformationForm, ApplicationFileLinkStatus.FULL);
    BindingResult bindingResult = new BeanPropertyBindingResult(permanentDepositInformation, "form");
    permanentDepositsValidator.validate(projectInformationForm, bindingResult);

    return !bindingResult.hasErrors();
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    if (validationType.equals(ValidationType.PARTIAL)) {
      groupValidator.validate(form, bindingResult, PartialValidation.class);
    } else {
      groupValidator.validate(form, bindingResult, FullValidation.class);
      permanentDepositsValidator.validate(form, bindingResult, pwaApplicationDetail);
    }

    return bindingResult;

  }

  public boolean getIsPermanentDepositQuestionRequired(PwaApplicationDetail pwaApplicationDetail) {
    return true;
    //To Do: get proj info entity and check weather permanent deposit is being made.
  }


}
