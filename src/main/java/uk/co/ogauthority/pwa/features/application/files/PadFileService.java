package uk.co.ogauthority.pwa.features.application.files;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadDepositDrawingRepository;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingRepository;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.CopiedEntityIdTuple;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;

/**
 * Service to handle file management for/linking files to PWA applications.
 */
@Service
public class PadFileService {

  private final PadFileRepository padFileRepository;
  private final EntityCopyingService entityCopyingService;
  private final FileService fileService;
  private final PadTechnicalDrawingRepository padTechnicalDrawingRepository;
  private final PadDepositDrawingRepository padDepositDrawingRepository;

  @Autowired
  public PadFileService(PadFileRepository padFileRepository,
                        EntityCopyingService entityCopyingService,
                        FileService fileService,
                        PadTechnicalDrawingRepository padTechnicalDrawingRepository,
                        PadDepositDrawingRepository padDepositDrawingRepository) {
    this.padFileRepository = padFileRepository;
    this.entityCopyingService = entityCopyingService;
    this.fileService = fileService;
    this.padTechnicalDrawingRepository = padTechnicalDrawingRepository;
    this.padDepositDrawingRepository = padDepositDrawingRepository;
  }

  @Transactional
  public void savePadFileIfValid(
      FileUploadResponse response,
      PwaApplicationDetail pwaApplicationDetail,
      ApplicationDetailFilePurpose purpose) {
    if (response.getError() == null) {
      String fileId = String.valueOf(response.getFileId());
      var appFile = new PadFile(pwaApplicationDetail, fileId, purpose, ApplicationFileLinkStatus.FULL);
      padFileRepository.save(appFile);
    }
  }

  /**
   * Copy files from with a specified purpose and link status from one application detail to another.
   */
  public Set<CopiedEntityIdTuple<Integer, PadFile>> copyPadFilesToPwaApplicationDetail(PwaApplicationDetail fromDetail,
                                                                                       PwaApplicationDetail toDetail,
                                                                                       ApplicationDetailFilePurpose purpose,
                                                                                       ApplicationFileLinkStatus fileLinkStatus) {


    return entityCopyingService.duplicateEntitiesAndSetParent(
        () -> padFileRepository.findAllCurrentFilesByAppDetailAndFilePurposeAndFileLinkStatus(
            fromDetail,
            purpose,
            fileLinkStatus
        ),
        toDetail,
        PadFile.class
    );

  }

  /**
   * Delete an individual file for an application.
   *
   * @param padFile file being deleted
   */
  @Transactional
  public void processFileDeletion(PadFile padFile) {
    padFileRepository.delete(padFile);
  }

  public List<PadFile> getAllByFileId(String fileId) {
    return padFileRepository.findAllByFileId(fileId);
  }

  /**
   * Delete an uploaded file and the pad_files bridge row linking it to the given application detail, so a
   * file removed via the file-upload widget doesn't leave a pad_files row pointing at a file that no longer
   * exists. Scoped to the given detail only, so a file id that (due to historic data corruption) is also
   * referenced by a pad_files row on a different application detail version is left untouched.
   *
   * <p>pad_technical_drawings.pf_id and pad_deposit_drawings.pf_id are the only foreign keys onto pad_files,
   * so a drawing being edited can still reference this pad_files row (the widget removes a file from the
   * form before it's resubmitted) - that link is cleared first so the bridge row can be deleted without
   * violating those constraints.</p>
   */
  @Transactional
  public FileDeleteResponse deleteFileAndPadFileLinks(PwaApplicationDetail pwaApplicationDetail, UploadedFile uploadedFile) {
    padFileRepository.findByPwaApplicationDetailAndFileId(pwaApplicationDetail, String.valueOf(uploadedFile.getId()))
        .ifPresent(padFile -> {
          padTechnicalDrawingRepository.findByPwaApplicationDetailAndFile(pwaApplicationDetail, padFile)
              .ifPresent(drawing -> {
                drawing.setFile(null);
                padTechnicalDrawingRepository.save(drawing);
              });
          padDepositDrawingRepository.findByPwaApplicationDetailAndFile(pwaApplicationDetail, padFile)
              .ifPresent(drawing -> {
                drawing.setFile(null);
                padDepositDrawingRepository.save(drawing);
              });
          padFileRepository.delete(padFile);
        });
    return fileService.delete(uploadedFile);
  }

  public PadFile getPadFileByPwaApplicationDetailAndFileId(PwaApplicationDetail detail,
                                                           String fileId) {
    return padFileRepository.findByPwaApplicationDetailAndFileId(detail, fileId)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format(
            "Couldn't find a PadFile for app detail with ID: %s and fileId: %s",
            detail.getId(),
            fileId)));
  }

  public List<PadFile> getAllByPwaApplicationDetailAndPurpose(PwaApplicationDetail detail,
                                                              ApplicationDetailFilePurpose purpose) {
    return padFileRepository.findAllByPwaApplicationDetailAndPurpose(detail, purpose);
  }

  /**
   * Remove PadFiles that are linked to a detail and purpose and are not in a specified list.
   *
   * @param detail            detail for app to cleanup files for
   * @param purpose           of files we're looking at
   * @param excludePadFileIds list of ids for PadFiles we don't want to remove
   */
  @Transactional
  public void cleanupFiles(PwaApplicationDetail detail,
                           ApplicationDetailFilePurpose purpose,
                           List<Integer> excludePadFileIds) {

    List<PadFile> filesToCleanup;

    if (excludePadFileIds.isEmpty()) {
      filesToCleanup = padFileRepository.findAllByPwaApplicationDetailAndPurpose(detail, purpose);
    } else {
      filesToCleanup = padFileRepository.findAllByAppDetailAndFilePurposeAndIdNotIn(detail, purpose, excludePadFileIds);
    }

    padFileRepository.deleteAll(filesToCleanup);

  }
}
