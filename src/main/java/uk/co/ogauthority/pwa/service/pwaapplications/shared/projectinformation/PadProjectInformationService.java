package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;

import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformationFile;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;

/* Service providing simplified API for project information app form */
@Service
public class PadProjectInformationService {

  private final PadProjectInformationRepository padProjectInformationRepository;
  private final ProjectInformationFileService projectInformationFileService;
  private final ProjectInformationEntityMappingService projectInformationEntityMappingService;

  @Autowired
  public PadProjectInformationService(
      PadProjectInformationRepository padProjectInformationRepository,
      ProjectInformationFileService projectInformationFileService,
      ProjectInformationEntityMappingService projectInformationEntityMappingService) {
    this.padProjectInformationRepository = padProjectInformationRepository;
    this.projectInformationFileService = projectInformationFileService;
    this.projectInformationEntityMappingService = projectInformationEntityMappingService;
  }

  public PadProjectInformation getPadProjectInformationData(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadProjectInformation());
    projectInformation.setPwaApplicationDetail(pwaApplicationDetail);
    return projectInformation;
  }

  /**
   * Map stored data to form including uploaded files depending on requested link status.
   *
   * @param padProjectInformation     stored data
   * @param form                      form to map to
   * @param applicationFileLinkStatus link status of uploaded files to be included in form.
   */
  public void mapEntityToForm(PadProjectInformation padProjectInformation,
                              ProjectInformationForm form,
                              ApplicationFileLinkStatus applicationFileLinkStatus) {

    projectInformationEntityMappingService.mapProjectInformationDataToForm(padProjectInformation, form);

    // only attach files with matching link status to form
    var uploadedFilesWithDescriptionFormList = projectInformationFileService.getUploadedFileListAsFormList(
        padProjectInformation.getPwaApplicationDetail(),
        applicationFileLinkStatus
    );

    form.setUploadedFileWithDescriptionForms(uploadedFilesWithDescriptionFormList);
  }


  /**
   * From the form extract form data and file data which should be persisted.
   * Any linked files which are not part of official "save" action are discarded.
   */
  @Transactional
  public void saveEntityUsingForm(PadProjectInformation padProjectInformation,
                                  ProjectInformationForm form,
                                  WebUserAccount user) {
    projectInformationEntityMappingService.setEntityValuesUsingForm(padProjectInformation, form);
    projectInformationFileService.updateOrDeleteLinkedFilesUsingForm(
        padProjectInformation.getPwaApplicationDetail(),
        form,
        user
    );
    padProjectInformationRepository.save(padProjectInformation);

  }

  /**
   * Simplify api by providing pass through method to access file service.
   */
  public List<UploadedFileView> getUpdatedProjectInformationFileViewsWhenFileOnForm(
      PwaApplicationDetail pwaApplicationDetail,
      ProjectInformationForm form) {
    return projectInformationFileService.getUpdatedProjectInformationFileViewsWhenFileOnForm(pwaApplicationDetail,
        form);

  }

  /**
   * Simplify api by providing pass through method to access file service.
   */
  public PadProjectInformationFile getProjectInformationFile(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    return projectInformationFileService.getProjectInformationFile(fileId,
        pwaApplicationDetail);
  }


  @Transactional
  public void deleteUploadedFileLink(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    PadProjectInformationFile existingFile = projectInformationFileService.getProjectInformationFile(fileId,
        pwaApplicationDetail);
    projectInformationFileService.deleteProjectInformationFileLink(existingFile);
  }

  /**
   * Method which creates "temporary" link to application detail project information.
   * If form left unsaved, we know which files are deletable.
   */
  @Transactional
  public void createUploadedFileLink(String uploadedFileId, PwaApplicationDetail pwaApplicationDetail) {
    projectInformationFileService.createAndSaveProjectInformationFile(
        pwaApplicationDetail,
        uploadedFileId
    );
  }

}
