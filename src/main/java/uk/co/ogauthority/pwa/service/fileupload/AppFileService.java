package uk.co.ogauthority.pwa.service.fileupload;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collection;
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
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.file.AppFileRepository;
import uk.co.ogauthority.pwa.util.FileServiceUtils;

/**
 * Service to handle file management for/linking files to PWA applications.
 */
@Service
public class AppFileService {

  private final FileUploadService fileUploadService;
  private final AppFileRepository appFileRepository;

  @Autowired
  public AppFileService(FileUploadService fileUploadService,
                        AppFileRepository appFileRepository) {
    this.fileUploadService = fileUploadService;
    this.appFileRepository = appFileRepository;
  }

  /**
   * Retrieve a list of file views for every file that is present on the provided file upload form.
   *
   * @param uploadForm containing files
   * @param application     to get files for
   * @param purpose    to get files for
   * @return list of files with populated descriptions
   */
  public List<UploadedFileView> getFilesLinkedToForm(UploadMultipleFilesWithDescriptionForm uploadForm,
                                                     PwaApplication application,
                                                     AppFilePurpose purpose) {

    var fileViewList = getUploadedFileViews(application, purpose, ApplicationFileLinkStatus.ALL);

    return FileServiceUtils.getFilesLinkedToForm(uploadForm, fileViewList);

  }

  /**
   * Populate a file upload form with fully linked application files which have a specific purpose.
   *
   * @param uploadForm to populate
   * @param application     we are getting files for
   * @param purpose    of the files we are getting
   */
  public void mapFilesToForm(UploadMultipleFilesWithDescriptionForm uploadForm,
                             PwaApplication application,
                             AppFilePurpose purpose) {

    List<UploadFileWithDescriptionForm> fileFormViewList = getUploadedFileViews(application, purpose,
        ApplicationFileLinkStatus.FULL).stream()
        .map(fileUploadService::createUploadFileWithDescriptionFormFromView)
        .collect(Collectors.toList());

    uploadForm.setUploadedFileWithDescriptionForms(fileFormViewList);

  }

  public void mapFileToForm(UploadMultipleFilesWithDescriptionForm uploadForm,
                             UploadedFileView uploadedFileView) {

    var uploadedFileForm = fileUploadService.createUploadFileWithDescriptionFormFromView(uploadedFileView);
    uploadForm.setUploadedFileWithDescriptionForms(List.of(uploadedFileForm));
  }

  /**
   * Upload a file and create a temporary link between the file and the application it was uploaded to, alongside the file purpose.
   *
   * @param file                 being uploaded
   * @param application to link it to
   * @param purpose              for the file
   * @param user                 uploading the file
   * @return a successful (or failed) upload result
   */
  @Transactional
  public FileUploadResult processInitialUpload(MultipartFile file,
                                               PwaApplication application,
                                               AppFilePurpose purpose,
                                               WebUserAccount user) {

    var result = fileUploadService.processUpload(file, user);

    if (result.isValid()) {
      String fileId = result.getFileId().orElseThrow();
      var appFile = new AppFile(application, fileId, purpose, ApplicationFileLinkStatus.TEMPORARY);
      appFileRepository.save(appFile);
    }

    return result;

  }

  /**
   * Fully link temporary files that are still present, update file descriptions, delete files that have been deleted onscreen.
   *
   * @param uploadForm           containing files to update
   * @param application          we are updating files for
   * @param updateMode           the mode used when updating files
   * @param purpose              of files being updated
   * @param user                 updating the files
   */
  @Transactional
  public void updateFiles(UploadMultipleFilesWithDescriptionForm uploadForm,
                          PwaApplication application,
                          AppFilePurpose purpose,
                          FileUpdateMode updateMode,
                          WebUserAccount user) {

    Map<String, UploadFileWithDescriptionForm> uploadedFileIdToFormMap = FileServiceUtils.getFileIdToFormMap(uploadForm);

    var existingLinkedFiles = appFileRepository.findAllByPwaApplicationAndPurpose(application, purpose);

    var filesToUpdate = new HashSet<AppFile>();
    var filesToRemove = new HashSet<AppFile>();

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

    appFileRepository.saveAll(filesToUpdate);

    if (updateMode == FileUpdateMode.DELETE_UNLINKED_FILES) {
      deleteAppFileLinksAndUploadedFiles(filesToRemove, user);
    }

  }

  private void updateFileDescriptionAndFullyLink(AppFile appFile, UploadFileWithDescriptionForm fileForm) {
    appFile.setDescription(fileForm.getUploadedFileDescription());
    appFile.setFileLinkStatus(ApplicationFileLinkStatus.FULL);
  }

  @Transactional
  void deleteAppFileLinksAndUploadedFiles(Iterable<AppFile> filesToBeRemoved,
                                          WebUserAccount user) {

    filesToBeRemoved.forEach(fileToRemove -> {
      var result = fileUploadService.deleteUploadedFile(fileToRemove.getFileId(), user);
      if (!result.isValid()) {
        throw new RuntimeException("Could not delete uploaded file with Id:" + fileToRemove.getFileId());
      }
    });

    appFileRepository.deleteAll(filesToBeRemoved);

  }


  public UploadedFile getUploadedFileById(String fileId) {
    return fileUploadService.getFileById(fileId);
  }

  /**
   * Get files for an application with a specified purpose and link status as uploaded file views.
   */
  public List<UploadedFileView> getUploadedFileViews(PwaApplication application,
                                                     AppFilePurpose purpose,
                                                     ApplicationFileLinkStatus fileLinkStatus) {

    var views = appFileRepository
        .findAllAsFileViewByAppAndPurposeAndFileLinkStatus(application, purpose, fileLinkStatus);

    views.forEach(view -> view.setFileUrl(getDownloadUrl(application, purpose, view.getFileId())));

    return views;

  }

  /**
   * Get files for an application with a specified purpose and link status as uploaded file views, .
   */
  public List<UploadedFileView> getUploadedFileViewsWithNoUrl(PwaApplication application,
                                                     AppFilePurpose purpose,
                                                     ApplicationFileLinkStatus fileLinkStatus) {

    return appFileRepository
        .findAllAsFileViewByAppAndPurposeAndFileLinkStatus(application, purpose, fileLinkStatus);

  }

  /**
   * Get a file for an application with a specified purpose and link status as an uploaded file view.
   */
  public UploadedFileView getUploadedFileView(PwaApplication application,
                                              String fileId,
                                              AppFilePurpose purpose,
                                              ApplicationFileLinkStatus fileLinkStatus) {

    var fileView = appFileRepository.findAsFileViewByAppAndFileIdAndPurposeAndFileLinkStatus(
        application, fileId, purpose, fileLinkStatus);
    fileView.setFileUrl(getDownloadUrl(application, purpose, fileView.getFileId()));
    return fileView;

  }

  private String getDownloadUrl(PwaApplication application, AppFilePurpose purpose, String fileId) {
    return ReverseRouter.route(on(purpose.getFileControllerClass()).handleDownload(
        application.getApplicationType(),
        application.getId(),
        fileId,
        null
    ));
  }

  /**
   * Delete an individual file for an application.
   *
   * @param appFile            file being deleted
   * @param user               deleting file
   * @param actionBeforeDelete a consumer to run if the result is valid, prior to deletion.
   * @return a successful (or failed) file delete result
   */
  @Transactional
  public FileDeleteResult processFileDeletionWithPreDeleteAction(AppFile appFile,
                                                                 WebUserAccount user,
                                                                 Consumer<AppFile> actionBeforeDelete) {
    var result = fileUploadService.deleteUploadedFile(appFile.getFileId(), user);

    if (result.isValid()) {
      actionBeforeDelete.accept(appFile);
      appFileRepository.delete(appFile);
    }

    return result;
  }

  /**
   * Delete an individual file for an application.
   *
   * @param appFile file being deleted
   * @param user    deleting file
   * @return a successful (or failed) file delete result
   */
  @Transactional
  public FileDeleteResult processFileDeletion(AppFile appFile,
                                              WebUserAccount user) {
    return processFileDeletionWithPreDeleteAction(appFile, user, appFileArg -> {
    });
  }

  public AppFile getAppFileByPwaApplicationAndFileId(PwaApplication application,
                                                     String fileId) {
    return appFileRepository.findByPwaApplicationAndFileId(application, fileId)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format(
            "Couldn't find an AppFile for app with ID: %s and fileId: %s",
            application.getId(),
            fileId)));
  }

  public List<AppFile> getFilesByIdIn(PwaApplication application,
                                      AppFilePurpose purpose,
                                      Collection<String> fileIds) {
    return appFileRepository.findAllByPwaApplicationAndPurposeAndFileIdIn(application, purpose, fileIds);
  }

}
