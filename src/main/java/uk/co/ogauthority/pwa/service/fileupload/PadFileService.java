package uk.co.ogauthority.pwa.service.fileupload;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.file.PadFileRepository;

/**
 * Service to handle file management for/linking files to PWA applications.
 */
@Service
public class PadFileService {

  private final FileUploadService fileUploadService;
  private final PadFileRepository padFileRepository;

  @Autowired
  public PadFileService(FileUploadService fileUploadService,
                        PadFileRepository padFileRepository) {
    this.fileUploadService = fileUploadService;
    this.padFileRepository = padFileRepository;
  }

  /**
   * Retrieve a list of file views for every file that is present on the provided file upload form.
   * @param uploadForm containing files
   * @param detail to get files for
   * @param purpose to get files for
   * @return list of files with populated descriptions
   */
  public List<UploadedFileView> getFilesLinkedToForm(UploadMultipleFilesWithDescriptionForm uploadForm,
                                                     PwaApplicationDetail detail,
                                                     ApplicationFilePurpose purpose) {

    Map<String, UploadFileWithDescriptionForm> fileIdToFormMap = getFileIdToFormMap(uploadForm);

    var formFileViewList = getUploadedFileViews(detail, purpose, ApplicationFileLinkStatus.ALL).stream()
        .filter(fileView -> fileIdToFormMap.containsKey(fileView.getFileId()))
        .collect(Collectors.toList());

    return formFileViewList.stream()
        .peek(fileView -> fileView.setFileDescription(fileIdToFormMap.get(fileView.getFileId()).getUploadedFileDescription()))
        .collect(Collectors.toList());

  }

  /**
   * Populate a file upload form with fully linked application files which have a specific purpose.
   * @param uploadForm to populate
   * @param detail we are getting files for
   * @param purpose of the files we are getting
   */
  public void mapFilesToForm(UploadMultipleFilesWithDescriptionForm uploadForm,
                             PwaApplicationDetail detail,
                             ApplicationFilePurpose purpose) {

    List<UploadFileWithDescriptionForm> fileFormViewList = getUploadedFileViews(detail, purpose, ApplicationFileLinkStatus.FULL).stream()
        .map(fileUploadService::createUploadFileWithDescriptionFormFromView)
        .collect(Collectors.toList());

    uploadForm.setUploadedFileWithDescriptionForms(fileFormViewList);

  }

  /**
   * Upload a file and create a temporary link between the file and the application it was uploaded to, alongside the file purpose.
   * @param file being uploaded
   * @param pwaApplicationDetail to link it to
   * @param purpose for the file
   * @param user uploading the file
   * @return a successful (or failed) upload result
   */
  @Transactional
  public FileUploadResult processInitialUpload(MultipartFile file,
                                               PwaApplicationDetail pwaApplicationDetail,
                                               ApplicationFilePurpose purpose,
                                               WebUserAccount user) {

    var result = fileUploadService.processUpload(file, user);

    if (result.isValid()) {
      String fileId = result.getFileId().orElseThrow();
      var appFile = new PadFile(pwaApplicationDetail, fileId, purpose, ApplicationFileLinkStatus.TEMPORARY);
      padFileRepository.save(appFile);
    }

    return result;

  }

  /**
   * Fully link temporary files that are still present, update file descriptions, delete files that have been deleted onscreen.
   * @param uploadForm containing files to update
   * @param pwaApplicationDetail we are updating files for
   * @param updateMode the mode used when updating files
   * @param purpose of files being updated
   * @param user updating the files
   */
  @Transactional
  public void updateFiles(UploadMultipleFilesWithDescriptionForm uploadForm,
                          PwaApplicationDetail pwaApplicationDetail,
                          ApplicationFilePurpose purpose,
                          FileUpdateMode updateMode,
                          WebUserAccount user) {

    Map<String, UploadFileWithDescriptionForm> uploadedFileIdToFormMap = getFileIdToFormMap(uploadForm);

    var existingLinkedFiles = padFileRepository.findAllByPwaApplicationDetailAndPurpose(pwaApplicationDetail, purpose);

    var filesToUpdate = new HashSet<PadFile>();
    var filesToRemove = new HashSet<PadFile>();

    // if file is still in list of uploaded files, update description and add to update set
    // else file can be deleted so add to remove set
    existingLinkedFiles.forEach(existingFile -> {

      if (uploadedFileIdToFormMap.containsKey(existingFile.getFileId())) {

        updateFileDescriptionAndFullyLink(existingFile, uploadedFileIdToFormMap.get(existingFile.getFileId()));
        filesToUpdate.add(existingFile);

      } else {
        filesToRemove.add(existingFile);
      }

    });

    padFileRepository.saveAll(filesToUpdate);

    if (updateMode == FileUpdateMode.DELETE_UNLINKED_FILES) {
      deleteAppFileLinksAndUploadedFiles(filesToRemove, user);
    }

  }

  private Map<String, UploadFileWithDescriptionForm> getFileIdToFormMap(UploadMultipleFilesWithDescriptionForm uploadForm) {
    return uploadForm.getUploadedFileWithDescriptionForms().stream()
        .collect(Collectors.toMap(UploadFileWithDescriptionForm::getUploadedFileId, f -> f));
  }

  private void updateFileDescriptionAndFullyLink(PadFile padFile, UploadFileWithDescriptionForm fileForm) {
    padFile.setDescription(fileForm.getUploadedFileDescription());
    padFile.setFileLinkStatus(ApplicationFileLinkStatus.FULL);
  }

  @Transactional
  void deleteAppFileLinksAndUploadedFiles(Iterable<PadFile> filesToBeRemoved,
                                          WebUserAccount user) {

    filesToBeRemoved.forEach(fileToRemove -> {
      var result = fileUploadService.deleteUploadedFile(fileToRemove.getFileId(), user);
      if (!result.isValid()) {
        throw new RuntimeException("Could not delete uploaded file with Id:" + fileToRemove.getFileId());
      }
    });

    padFileRepository.deleteAll(filesToBeRemoved);

  }

  public UploadedFile getUploadedFileById(String fileId) {
    return fileUploadService.getFileById(fileId);
  }

  /**
   * Get files for an application with a specified purpose and link status as uploaded file views.
   */
  public List<UploadedFileView> getUploadedFileViews(PwaApplicationDetail pwaApplicationDetail,
                                                      ApplicationFilePurpose purpose,
                                                      ApplicationFileLinkStatus fileLinkStatus) {

    return padFileRepository.findAllAsFileViewByAppDetailAndPurposeAndFileLinkStatus(
        pwaApplicationDetail, purpose, fileLinkStatus).stream()
        .peek(ufv -> ufv.setFileUrl(getDownloadUrl(pwaApplicationDetail, purpose, ufv.getFileId())))
        .collect(Collectors.toList());

  }

  /**
   * Get a file for an application with a specified purpose and link status as an uploaded file view.
   */
  public UploadedFileView getUploadedFileView(PwaApplicationDetail pwaApplicationDetail,
                                                     String fileId,
                                                     ApplicationFilePurpose purpose,
                                                     ApplicationFileLinkStatus fileLinkStatus) {

    var fileView =  padFileRepository.findAsFileViewByAppDetailAndFileIdAndPurposeAndFileLinkStatus(
        pwaApplicationDetail, fileId, purpose, fileLinkStatus);
    fileView.setFileUrl(getDownloadUrl(pwaApplicationDetail, purpose, fileView.getFileId()));
    return fileView;

  }

  private String getDownloadUrl(PwaApplicationDetail detail, ApplicationFilePurpose purpose, String fileId) {
    return ReverseRouter.route(on(purpose.getFileControllerClass()).handleDownload(
        detail.getPwaApplicationType(),
        detail.getMasterPwaApplicationId(),
        fileId,
        null
    ));
  }

  /**
   * Delete an individual file for an application.
   * @param padFile file being deleted
   * @param user deleting file
   * @param actionBeforeDelete a consumer to run if the result is valid, prior to deletion.
   * @return a successful (or failed) file delete result
   */
  @Transactional
  public FileDeleteResult processFileDeletion(PadFile padFile,
                                              WebUserAccount user,
                                              Consumer<PadFile> actionBeforeDelete) {

    var result = fileUploadService.deleteUploadedFile(padFile.getFileId(), user);

    if (result.isValid()) {
      actionBeforeDelete.accept(padFile);
      padFileRepository.delete(padFile);
    }

    return result;

  }

  /**
   * Delete an individual file for an application.
   * @param padFile file being deleted
   * @param user deleting file
   * @return a successful (or failed) file delete result
   */
  @Transactional
  public FileDeleteResult processFileDeletion(PadFile padFile,
                                              WebUserAccount user) {

    var result = fileUploadService.deleteUploadedFile(padFile.getFileId(), user);

    if (result.isValid()) {
      padFileRepository.delete(padFile);
    }

    return result;

  }

  public PadFile getPadFileByPwaApplicationDetailAndFileId(PwaApplicationDetail detail,
                                             String fileId) {
    return padFileRepository.findByPwaApplicationDetailAndFileId(detail, fileId)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format(
            "Couldn't find a PadFile for app detail with ID: %s and fileId: %s",
            detail.getId(),
            fileId)));
  }

}
