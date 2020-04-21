package uk.co.ogauthority.pwa.service.pwaapplications.shared.location;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.LocationDetailsController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.FileUploadStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.location.PadLocationDetailFile;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadLocationDetailFileRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUploadService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

@Service
public class PadLocationDetailFileService implements ApplicationFormSectionService {

  private final PadLocationDetailFileRepository padLocationDetailFileRepository;
  private final FileUploadService fileUploadService;
  private final EntityManager entityManager;
  private final SpringValidatorAdapter groupValidator;

  public PadLocationDetailFileService(
      PadLocationDetailFileRepository padLocationDetailFileRepository,
      FileUploadService fileUploadService, EntityManager entityManager,
      SpringValidatorAdapter groupValidator) {
    this.padLocationDetailFileRepository = padLocationDetailFileRepository;
    this.fileUploadService = fileUploadService;
    this.entityManager = entityManager;
    this.groupValidator = groupValidator;
  }


  public void mapDocumentsToForm(PwaApplicationDetail pwaApplicationDetail, LocationDetailsForm form) {
    var fileFormViewList = getUploadedFileListAsFormList(pwaApplicationDetail, ApplicationFileLinkStatus.FULL);
    form.setUploadedFileWithDescriptionForms(fileFormViewList);
  }

  /**
   * Create and persist a newlocation detail file linked to app detail and uploaded file id.
   */
  private PadLocationDetailFile createAndSaveLocationDetailFile(PwaApplicationDetail pwaApplicationDetail,
                                                                String uploadedFileId) {
    var newFileLink = new PadLocationDetailFile(
        pwaApplicationDetail,
        uploadedFileId,
        null,
        ApplicationFileLinkStatus.TEMPORARY
    );

    return padLocationDetailFileRepository.save(newFileLink);
  }

  /**
   * Return linked location detail file if it exists for application detail else throw not found exception.
   */
  public PadLocationDetailFile getLocationDetailFile(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    return padLocationDetailFileRepository.findByPwaApplicationDetailAndFileId(
        pwaApplicationDetail,
        fileId
    ).orElseThrow(() -> new PwaEntityNotFoundException(String.format(
        "file id %s not found for pwaAppDetailId:%s",
        fileId,
        pwaApplicationDetail.getId()
        ))
    );
  }


  /**
   * Remove location detail file link and delete uploaded file.
   */
  @Transactional
  void deleteLocationDetailFilesAndLinkedUploads(Iterable<PadLocationDetailFile> filesToBeRemoved,
                                                 WebUserAccount user) {
    for (PadLocationDetailFile fileToRemove : filesToBeRemoved) {
      var result = fileUploadService.deleteUploadedFile(fileToRemove.getFileId(), user);
      if (!result.isValid()) {
        throw new RuntimeException("Could not delete uploaded file with Id:" + fileToRemove.getFileId());
      }
    }
    padLocationDetailFileRepository.deleteAll(filesToBeRemoved);
  }

  /**
   * Delete single uploaded file link.
   */
  @Transactional
  void deleteLocationDetailFileLink(PadLocationDetailFile fileToRemove) {
    padLocationDetailFileRepository.delete(fileToRemove);
  }


  /**
   * For all uploaded files included in form, persist updated descriptions as provided on form.
   * For all linked files not included in form, remove link and delete uploaded file.
   */
  @Transactional
  public void updateOrDeleteLinkedFilesUsingForm(PwaApplicationDetail pwaApplicationDetail,
                                                 LocationDetailsForm form,
                                                 WebUserAccount user) {
    Map<String, UploadFileWithDescriptionForm> uploadedFilesMap = form.getUploadedFileWithDescriptionForms()
        .stream()
        .collect(Collectors.toMap(UploadFileWithDescriptionForm::getUploadedFileId, f -> f));

    var existingLinkedFiles = padLocationDetailFileRepository.findAllByPwaApplicationDetail(pwaApplicationDetail);
    HashSet<PadLocationDetailFile> filesToBeRemoved = new HashSet<>();
    var filesToUpdate = new HashSet<PadLocationDetailFile>();

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

    padLocationDetailFileRepository.saveAll(filesToUpdate);
    deleteLocationDetailFilesAndLinkedUploads(filesToBeRemoved, user);


  }

  /**
   * Get all uploaded files as views where the file exists on form and with description as set on form.
   */
  private List<UploadedFileView> updateFormWithSuppliedUploadedFileViews(
      LocationDetailsForm form,
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
   * Gets linked location detail files as uploaded file forms.
   */
  private List<UploadFileWithDescriptionForm> getUploadedFileListAsFormList(PwaApplicationDetail pwaApplicationDetail,
                                                                            ApplicationFileLinkStatus applicationFileLinkStatus) {
    return getLocationDetailFileViews(pwaApplicationDetail, applicationFileLinkStatus)
        .stream()
        .map(fileUploadService::createUploadFileWithDescriptionFormFromView)
        .collect(Collectors.toList());
  }


  /**
   * Get location detail files with requested link status as standard uploaded file views.
   */
  public List<UploadedFileView> getLocationDetailFileViews(PwaApplicationDetail pwaApplicationDetail,
                                                           ApplicationFileLinkStatus fileLinkStatus) {

    var fileViews = entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.model.form.files.UploadedFileView(" +
            "  uf.fileId" +
            ", uf.fileName" +
            ", uf.fileSize" +
            ", pldf.description" +
            ", uf.uploadDatetime" +
            ", '#' " + //link updated after construction as requires reverse router
            ") " +
            "FROM PadLocationDetailFile pldf " +
            "JOIN UploadedFile uf ON pldf.fileId = uf.fileId " +
            "WHERE uf.status = :fileStatus " +
            "AND pldf.pwaApplicationDetail = :pwaAppDetail " +
            "AND (pldf.fileLinkStatus = :fileLinkStatus OR :fileLinkStatus = '" + ApplicationFileLinkStatus.ALL + "')",
        UploadedFileView.class)
        .setParameter("pwaAppDetail", pwaApplicationDetail)
        .setParameter("fileStatus", FileUploadStatus.CURRENT)
        .setParameter("fileLinkStatus", fileLinkStatus)
        .getResultList();

    fileViews.forEach(
        ufv -> ufv.setFileUrl(ReverseRouter.route(on(LocationDetailsController.class).handleDownload(
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
  public List<UploadedFileView> getUpdatedLocationDetailFileViewsWhenFileOnForm(
      PwaApplicationDetail pwaApplicationDetail,
      LocationDetailsForm form) {

    return updateFormWithSuppliedUploadedFileViews(
        form,
        () -> getLocationDetailFileViews(pwaApplicationDetail, ApplicationFileLinkStatus.ALL));

  }


  @Transactional
  public void deleteUploadedFileLink(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    PadLocationDetailFile existingFile = getLocationDetailFile(fileId,
        pwaApplicationDetail);
    deleteLocationDetailFileLink(existingFile);
  }

  /**
   * Method which creates "temporary" link to application detail location detail file
   * If form left unsaved, we know which files are deletable.
   */
  @Transactional
  public void createUploadedFileLink(String uploadedFileId, PwaApplicationDetail pwaApplicationDetail) {
    createAndSaveLocationDetailFile(
        pwaApplicationDetail,
        uploadedFileId
    );
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    // Section is optional, so is always complete.
    return true;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    List<Object> hints = new ArrayList<>();
    if (validationType.equals(ValidationType.FULL)) {
      hints.add(FullValidation.class);
    } else {
      hints.add(PartialValidation.class);
    }
    groupValidator.validate(form, bindingResult, hints.toArray());
    return bindingResult;
  }
}

