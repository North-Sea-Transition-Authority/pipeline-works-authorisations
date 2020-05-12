package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.ProjectInformationController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.FileUploadStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PermanentDepositInfoFile;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PermanentDepositInfoFileRepository;
import uk.co.ogauthority.pwa.service.fileupload.FileUploadService;
import uk.co.ogauthority.pwa.service.fileupload.PwaApplicationFileService;




/**
 * Retrieving, persisting and converting of project information file records.
 */
@Service
public class PermanentDepositsFileService {

  private final PermanentDepositInfoFileRepository permanentDepositInfoFileRepository;
  private final FileUploadService fileUploadService;
  private final EntityManager entityManager;
  private final PwaApplicationFileService pwaApplicationFileService;

  @Autowired
  public PermanentDepositsFileService(
          PermanentDepositInfoFileRepository permanentDepositInfoFileRepository,
      FileUploadService fileUploadService, EntityManager entityManager,
      PwaApplicationFileService pwaApplicationFileService) {
    this.permanentDepositInfoFileRepository = permanentDepositInfoFileRepository;
    this.fileUploadService = fileUploadService;
    this.entityManager = entityManager;
    this.pwaApplicationFileService = pwaApplicationFileService;
  }


  /**
   * Create and persist a new Permanent Deposit information file linked to app detail and uploaded file id.
   */
  @Transactional
  PermanentDepositInfoFile createAndSavePermanentDepositInfoFile(PwaApplicationDetail pwaApplicationDetail,
                                                                String uploadedFileId) {
    var newFileLink = new PermanentDepositInfoFile(
        pwaApplicationDetail,
        uploadedFileId,
        null,
        ApplicationFileLinkStatus.TEMPORARY
    );

    return permanentDepositInfoFileRepository.save(newFileLink);
  }

  /**
   * Return linked PermanentDepositInfoFile if it exists for application detail else throw not found exception.
   */
  PermanentDepositInfoFile getPermanentDepositInformationFile(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    return permanentDepositInfoFileRepository.findByPwaApplicationDetailAndFileId(
        pwaApplicationDetail,
        fileId
    )
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format(
            "file id %s not found for pwaAppDetailId:%s",
            fileId,
            pwaApplicationDetail.getId()
            ))
        );
  }


  /**
   * Remove Permanent Deposit file link and delete uploaded file.
   */
  @Transactional
  void deletePermanentDepositInformationFilesAndLinkedUploads(Iterable<PermanentDepositInfoFile> filesToBeRemoved,
                                                     WebUserAccount user) {
    for (PermanentDepositInfoFile fileToRemove : filesToBeRemoved) {
      var result = fileUploadService.deleteUploadedFile(fileToRemove.getFileId(), user);
      if (!result.isValid()) {
        throw new RuntimeException("Could not delete uploaded file with Id:" + fileToRemove.getFileId());
      }
    }
    permanentDepositInfoFileRepository.deleteAll(filesToBeRemoved);
  }

  /**
   * Delete single uploaded file link.
   */
  @Transactional
  void deletePermanentDepositInfoFileLink(PermanentDepositInfoFile fileToRemove) {
    permanentDepositInfoFileRepository.delete(fileToRemove);
  }


  /**
   * For all uploaded files included in form, persist updated descriptions as provided on form.
   * For all linked files not included in form, remove link and delete uploaded file.
   */
  @Transactional
  void updateOrDeleteLinkedFilesUsingForm(PwaApplicationDetail pwaApplicationDetail,
                                          PermanentDepositsForm form,
                                          WebUserAccount user) {
    Map<String, UploadFileWithDescriptionForm> uploadedFilesMap = form.getUploadedFileWithDescriptionForms()
        .stream()
        .collect(Collectors.toMap(UploadFileWithDescriptionForm::getUploadedFileId, f -> f));

    var existingLinkedFiles = permanentDepositInfoFileRepository.findByPwaApplicationDetail(pwaApplicationDetail);
    var filesToBeRemoved = new HashSet<PermanentDepositInfoFile>();
    var filesToUpdate = new HashSet<PermanentDepositInfoFile>();

    // if file in uploaded set, update description and add to Save set
    // else file can be deleted so add to remove set
    existingLinkedFiles.forEach(existingPermanentDepositInfoFile -> {
      if (uploadedFilesMap.containsKey(existingPermanentDepositInfoFile.getFileId())) {
        var uploadedFile = uploadedFilesMap.get(existingPermanentDepositInfoFile.getFileId());
        existingPermanentDepositInfoFile.setDescription(uploadedFile.getUploadedFileDescription());
        existingPermanentDepositInfoFile.setFileLinkStatus(ApplicationFileLinkStatus.FULL);
        filesToUpdate.add(existingPermanentDepositInfoFile);
      } else {
        filesToBeRemoved.add(existingPermanentDepositInfoFile);
      }

    });

    permanentDepositInfoFileRepository.saveAll(filesToUpdate);
    deletePermanentDepositInformationFilesAndLinkedUploads(filesToBeRemoved, user);


  }

  /**
   * Get all uploaded files as views where the file exists on form and with description as set on form.
   */
  List<UploadedFileView> getUpdatedPermanentDepositFileViewsWhenFileOnForm(
      PwaApplicationDetail pwaApplicationDetail,
      PermanentDepositsForm form
  ) {
    Map<String, UploadFileWithDescriptionForm> formFilesMap = form.getUploadedFileWithDescriptionForms()
        .stream()
        .collect(Collectors.toMap(UploadFileWithDescriptionForm::getUploadedFileId, uf -> uf));

    var formFileViewList = getPermanentDepositsFileViews(pwaApplicationDetail, ApplicationFileLinkStatus.ALL)
        .stream()
        .filter(fileView -> formFilesMap.containsKey(fileView.getFileId()))
        .collect(Collectors.toList());

    formFileViewList.forEach(fileView -> fileView.setFileDescription(
        formFilesMap.get(fileView.getFileId()).getUploadedFileDescription())
    );

    return formFileViewList;

  }


  /**
   * Gets linked project info files as uploaded file forms.
   */
  List<UploadFileWithDescriptionForm> getUploadedFileListAsFormList(PwaApplicationDetail pwaApplicationDetail,
                                                                    ApplicationFileLinkStatus applicationFileLinkStatus) {
    return getPermanentDepositsFileViews(pwaApplicationDetail, applicationFileLinkStatus)
        .stream()
        .map(fileUploadService::createUploadFileWithDescriptionFormFromView)
        .collect(Collectors.toList());
  }


  /**
   * Get project info files with requested link status as standard uploaded file views.
   */
  List<UploadedFileView> getPermanentDepositsFileViews(PwaApplicationDetail pwaApplicationDetail,
                                                        ApplicationFileLinkStatus fileLinkStatus) {

    var fileViews = entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.model.form.files.UploadedFileView(" +
            "  uf.fileId" +
            ", uf.fileName" +
            ", uf.fileSize" +
            ", pif.description" +
            ", uf.uploadDatetime" +
            ", '#' " + //link updated after construction as requires reverse router
            ") " +
            "FROM PermanentDepositInfoFile pif " +
            "JOIN UploadedFile uf ON pif.fileId = uf.fileId " +
            "WHERE uf.status = :fileStatus " +
            "AND pif.pwaApplicationDetail = :pwaAppDetail " +
            "AND (pif.fileLinkStatus = :fileLinkStatus OR :fileLinkStatus = '" + ApplicationFileLinkStatus.ALL + "')",
        UploadedFileView.class)
        .setParameter("pwaAppDetail", pwaApplicationDetail)
        .setParameter("fileStatus", FileUploadStatus.CURRENT)
        .setParameter("fileLinkStatus", fileLinkStatus)
        .getResultList();

    fileViews.forEach(ufv -> ufv.setFileUrl(ReverseRouter.route(on(ProjectInformationController.class).handleDownload(
        pwaApplicationDetail.getPwaApplicationType(),
        pwaApplicationDetail.getMasterPwaApplicationId(),
        ufv.getFileId(),
        null
        ))
    ));

    return fileViews;

  }

}
