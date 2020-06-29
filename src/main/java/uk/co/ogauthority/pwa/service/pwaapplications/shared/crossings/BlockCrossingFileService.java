package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.BlockCrossingDocumentsController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.FileUploadStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.CrossedBlockOwner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadBlockCrossingFile;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.licence.PadCrossedBlockRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadBlockCrossingFileRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUploadService;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

@Service
public class BlockCrossingFileService implements ApplicationFormSectionService {

  private final PadBlockCrossingFileRepository padBlockCrossingFileRepository;
  private final PadCrossedBlockRepository padCrossedBlockRepository;
  private final FileUploadService fileUploadService;
  private final EntityManager entityManager;
  private final SpringValidatorAdapter groupValidator;
  private final PadFileService padFileService;

  @Autowired
  public BlockCrossingFileService(PadBlockCrossingFileRepository padBlockCrossingFileRepository,
                                  PadCrossedBlockRepository padCrossedBlockRepository,
                                  FileUploadService fileUploadService,
                                  EntityManager entityManager,
                                  SpringValidatorAdapter groupValidator,
                                  PadFileService padFileService) {
    this.padBlockCrossingFileRepository = padBlockCrossingFileRepository;
    this.padCrossedBlockRepository = padCrossedBlockRepository;
    this.fileUploadService = fileUploadService;
    this.entityManager = entityManager;

    this.groupValidator = groupValidator;
    this.padFileService = padFileService;
  }

  public boolean requiresFullValidation(PwaApplicationDetail pwaApplicationDetail) {
    // return 'true' for full validation if non holder organisations listed as crossed block owners
    return padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetailAndBlockOwnerIn(
        pwaApplicationDetail,
        List.of(CrossedBlockOwner.PORTAL_ORGANISATION)
    ) > 0;
  }

  /**
   * Create and persist a new block crossing file linked to app detail and uploaded file id.
   */
  private PadBlockCrossingFile createAndSaveBlockCrossingFile(PwaApplicationDetail pwaApplicationDetail,
                                                              String uploadedFileId) {
    var newFileLink = new PadBlockCrossingFile(
        pwaApplicationDetail,
        uploadedFileId,
        null,
        ApplicationFileLinkStatus.TEMPORARY
    );

    return padBlockCrossingFileRepository.save(newFileLink);
  }

  /**
   * Return linked block crossing if it exists for application detail else throw not found exception.
   */
  public PadBlockCrossingFile getBlockCrossingFile(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    return padBlockCrossingFileRepository.findByPwaApplicationDetailAndFileId(
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
   * Remove block crossing file link and delete uploaded file.
   */
  @Transactional
  void deleteBlockCrossingFilesAndLinkedUploads(Iterable<PadBlockCrossingFile> filesToBeRemoved,
                                                WebUserAccount user) {
    for (PadBlockCrossingFile fileToRemove : filesToBeRemoved) {
      var result = fileUploadService.deleteUploadedFile(fileToRemove.getFileId(), user);
      if (!result.isValid()) {
        throw new RuntimeException("Could not delete uploaded file with Id:" + fileToRemove.getFileId());
      }
    }
    padBlockCrossingFileRepository.deleteAll(filesToBeRemoved);
  }

  /**
   * Delete single uploaded file link.
   */
  @Transactional
  void deleteBlockCrossingFileLink(PadBlockCrossingFile fileToRemove) {
    padBlockCrossingFileRepository.delete(fileToRemove);
  }


  /**
   * For all uploaded files included in form, persist updated descriptions as provided on form.
   * For all linked files not included in form, remove link and delete uploaded file.
   */
  @Transactional
  public void updateOrDeleteLinkedFilesUsingForm(PwaApplicationDetail pwaApplicationDetail,
                                                 CrossingDocumentsForm form,
                                                 WebUserAccount user) {
    Map<String, UploadFileWithDescriptionForm> uploadedFilesMap = form.getUploadedFileWithDescriptionForms()
        .stream()
        .collect(Collectors.toMap(UploadFileWithDescriptionForm::getUploadedFileId, f -> f));

    var existingLinkedFiles = padBlockCrossingFileRepository.findByPwaApplicationDetail(pwaApplicationDetail);
    var filesToBeRemoved = new HashSet<PadBlockCrossingFile>();
    var filesToUpdate = new HashSet<PadBlockCrossingFile>();

    // if file in uploaded set, update description and add to Save set
    // else file can be deleted so add to remove set
    existingLinkedFiles.forEach(existingFile -> {
      if (uploadedFilesMap.containsKey(existingFile.getFileId())) {
        var uploadedFile = uploadedFilesMap.get(existingFile.getFileId());
        existingFile.setDescription(uploadedFile.getUploadedFileDescription());
        existingFile.setFileLinkStatus(ApplicationFileLinkStatus.FULL);
        filesToUpdate.add(existingFile);
      } else {
        filesToBeRemoved.add(existingFile);
      }

    });

    padBlockCrossingFileRepository.saveAll(filesToUpdate);
    deleteBlockCrossingFilesAndLinkedUploads(filesToBeRemoved, user);


  }

  /**
   * Get all uploaded files as views where the file exists on form and with description as set on form.
   */
  private List<UploadedFileView> updateFormWithSuppliedUploadedFileViews(
      CrossingDocumentsForm form,
      Supplier<List<UploadedFileView>> viewListSupplier
  ) {
    Map<String, UploadFileWithDescriptionForm> formFilesMap = form.getUploadedFileWithDescriptionForms()
        .stream()
        .collect(Collectors.toMap(UploadFileWithDescriptionForm::getUploadedFileId, uf -> uf));

    var formFileViewList = viewListSupplier.get()
        .stream()
        .filter(fileView -> formFilesMap.containsKey(fileView.getFileId()))
        .collect(Collectors.toList());

    formFileViewList.forEach(fileView -> fileView.setFileDescription(
        formFilesMap.get(fileView.getFileId()).getUploadedFileDescription())
    );

    return formFileViewList;

  }


  /**
   * Gets linked block crossing files as uploaded file forms.
   */
  private List<UploadFileWithDescriptionForm> getUploadedFileListAsFormList(PwaApplicationDetail pwaApplicationDetail,
                                                                            ApplicationFileLinkStatus applicationFileLinkStatus) {
    return getBlockCrossingFileViews(pwaApplicationDetail, applicationFileLinkStatus)
        .stream()
        .map(fileUploadService::createUploadFileWithDescriptionFormFromView)
        .collect(Collectors.toList());
  }


  /**
   * Get block crossing files with requested link status as standard uploaded file views.
   */
  public List<UploadedFileView> getBlockCrossingFileViews(PwaApplicationDetail pwaApplicationDetail,
                                                          ApplicationFileLinkStatus fileLinkStatus) {

    var fileViews = entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.model.form.files.UploadedFileView(" +
            "  uf.fileId" +
            ", uf.fileName" +
            ", uf.fileSize" +
            ", pbcf.description" +
            ", uf.uploadDatetime" +
            ", '#' " + //link updated after construction as requires reverse router
            ") " +
            "FROM PadBlockCrossingFile pbcf " +
            "JOIN UploadedFile uf ON pbcf.fileId = uf.fileId " +
            "WHERE uf.status = :fileStatus " +
            "AND pbcf.pwaApplicationDetail = :pwaAppDetail " +
            "AND (pbcf.fileLinkStatus = :fileLinkStatus OR :fileLinkStatus = '" + ApplicationFileLinkStatus.ALL + "')",
        UploadedFileView.class)
        .setParameter("pwaAppDetail", pwaApplicationDetail)
        .setParameter("fileStatus", FileUploadStatus.CURRENT)
        .setParameter("fileLinkStatus", fileLinkStatus)
        .getResultList();

    fileViews.forEach(
        ufv -> ufv.setFileUrl(ReverseRouter.route(on(BlockCrossingDocumentsController.class).handleDownload(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            ufv.getFileId(),
            null
            ))
        ));

    return fileViews;

  }


  /**
   * Simplify api by providing pass through method to access file service.
   */
  public List<UploadedFileView> getUpdatedBlockCrossingFileViewsWhenFileOnForm(
      PwaApplicationDetail pwaApplicationDetail,
      CrossingDocumentsForm form) {

    return updateFormWithSuppliedUploadedFileViews(
        form,
        () -> getBlockCrossingFileViews(pwaApplicationDetail, ApplicationFileLinkStatus.ALL));

  }


  @Transactional
  public void deleteUploadedFileLink(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    PadBlockCrossingFile existingFile = getBlockCrossingFile(fileId,
        pwaApplicationDetail);
    deleteBlockCrossingFileLink(existingFile);
  }

  /**
   * Method which creates "temporary" link to application detail block crossing file
   * If form left unsaved, we know which files are deletable.
   */
  @Transactional
  public void createUploadedFileLink(String uploadedFileId, PwaApplicationDetail pwaApplicationDetail) {
    createAndSaveBlockCrossingFile(
        pwaApplicationDetail,
        uploadedFileId
    );
  }

  public int getDocumentUploadCount(PwaApplicationDetail pwaApplicationDetail) {
    return padBlockCrossingFileRepository.countAllByPwaApplicationDetailAndFileLinkStatus(pwaApplicationDetail,
        ApplicationFileLinkStatus.FULL);
  }


  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new CrossingDocumentsForm();
    padFileService.mapFilesToForm(form, detail, ApplicationFilePurpose.BLOCK_CROSSINGS);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    List<Object> hints = new ArrayList<>();
    if (validationType.equals(ValidationType.FULL)) {
      hints.add(FullValidation.class);
      if (requiresFullValidation(pwaApplicationDetail)) {
        hints.add(MandatoryUploadValidation.class);
      }
    } else {
      hints.add(PartialValidation.class);
    }
    groupValidator.validate(form, bindingResult, hints.toArray());
    return bindingResult;
  }
}
