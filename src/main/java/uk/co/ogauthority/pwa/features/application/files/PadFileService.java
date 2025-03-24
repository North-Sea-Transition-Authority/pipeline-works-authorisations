package uk.co.ogauthority.pwa.features.application.files;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
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

  @Autowired
  public PadFileService(PadFileRepository padFileRepository,
                        EntityCopyingService entityCopyingService) {
    this.padFileRepository = padFileRepository;
    this.entityCopyingService = entityCopyingService;
  }

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
