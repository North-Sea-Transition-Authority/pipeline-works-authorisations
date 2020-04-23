package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.CableCrossingDocumentsController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.FileUploadStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadAdmiralityChartFile;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadAdmiralityChartFileRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUploadService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

@Service
public class AdmiralityChartFileService implements ApplicationFormSectionService {

  private final PadAdmiralityChartFileRepository padAdmiralityChartFileRepository;
  private final FileUploadService fileUploadService;
  private final EntityManager entityManager;
  private final SpringValidatorAdapter groupValidator;

  @Autowired
  public AdmiralityChartFileService(
      PadAdmiralityChartFileRepository padCableCrossingFileRepository,
      FileUploadService fileUploadService, EntityManager entityManager,
      SpringValidatorAdapter groupValidator) {
    this.padAdmiralityChartFileRepository = padCableCrossingFileRepository;
    this.fileUploadService = fileUploadService;
    this.entityManager = entityManager;
    this.groupValidator = groupValidator;
  }

  public void mapDocumentsToForm(PwaApplicationDetail pwaApplicationDetail, CrossingDocumentsForm form) {
    var fileFormViewList = getUploadedFileListAsFormList(pwaApplicationDetail, ApplicationFileLinkStatus.FULL);
    form.setUploadedFileWithDescriptionForms(fileFormViewList);
  }

  /**
   * Return linked cable crossing file if it exists for application detail else throw not found exception.
   */
  public PadAdmiralityChartFile getCableCrossingFile(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    return padAdmiralityChartFileRepository.findByPwaApplicationDetailAndFileId(
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
   * Gets linked cable crossing files as uploaded file forms.
   */
  private List<UploadFileWithDescriptionForm> getUploadedFileListAsFormList(PwaApplicationDetail pwaApplicationDetail,
                                                                            ApplicationFileLinkStatus applicationFileLinkStatus) {
    return getCableCrossingFileViews(pwaApplicationDetail, applicationFileLinkStatus)
        .stream()
        .map(fileUploadService::createUploadFileWithDescriptionFormFromView)
        .collect(Collectors.toList());
  }

  /**
   * Get cable crossing files with requested link status as standard uploaded file views.
   */
  public List<UploadedFileView> getCableCrossingFileViews(PwaApplicationDetail pwaApplicationDetail,
                                                          ApplicationFileLinkStatus fileLinkStatus) {

    var fileViews = entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.model.form.files.UploadedFileView(" +
            "  uf.fileId" +
            ", uf.fileName" +
            ", uf.fileSize" +
            ", pmlcf.description" +
            ", uf.uploadDatetime" +
            ", '#' " + //link updated after construction as requires reverse router
            ") " +
            "FROM PadAdmiralityChartFile pmlcf " +
            "JOIN UploadedFile uf ON pmlcf.fileId = uf.fileId " +
            "WHERE uf.status = :fileStatus " +
            "AND pmlcf.pwaApplicationDetail = :pwaAppDetail " +
            "AND (pmlcf.fileLinkStatus = :fileLinkStatus OR :fileLinkStatus = '" + ApplicationFileLinkStatus.ALL + "')",
        UploadedFileView.class)
        .setParameter("pwaAppDetail", pwaApplicationDetail)
        .setParameter("fileStatus", FileUploadStatus.CURRENT)
        .setParameter("fileLinkStatus", fileLinkStatus)
        .getResultList();

    fileViews.forEach(
        ufv -> ufv.setFileUrl(ReverseRouter.route(on(CableCrossingDocumentsController.class).handleDownload(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            ufv.getFileId(),
            null
            ))
        ));

    return fileViews;

  }

  /**
   * Create and persist a new cable crossing file linked to app detail and uploaded file id.
   */
  private PadAdmiralityChartFile createAndSaveCableCrossingFile(PwaApplicationDetail pwaApplicationDetail,
                                                              String uploadedFileId) {
    var newFileLink = new PadAdmiralityChartFile(
        pwaApplicationDetail,
        uploadedFileId,
        null,
        ApplicationFileLinkStatus.TEMPORARY
    );

    return padAdmiralityChartFileRepository.save(newFileLink);
  }

  /**
   * Remove cable crossing file link and delete uploaded file.
   */
  @Transactional
  void deleteCableCrossingFilesAndLinkedUploads(Iterable<PadAdmiralityChartFile> filesToBeRemoved,
                                                WebUserAccount user) {
    for (PadAdmiralityChartFile fileToRemove : filesToBeRemoved) {
      var result = fileUploadService.deleteUploadedFile(fileToRemove.getFileId(), user);
      if (!result.isValid()) {
        throw new RuntimeException("Could not delete uploaded file with Id:" + fileToRemove.getFileId());
      }
    }
    padAdmiralityChartFileRepository.deleteAll(filesToBeRemoved);
  }

  /**
   * Delete single uploaded file link.
   */
  @Transactional
  void deleteCableCrossingFileLink(PadAdmiralityChartFile fileToRemove) {
    padAdmiralityChartFileRepository.delete(fileToRemove);
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

    var existingLinkedFiles = padAdmiralityChartFileRepository.findAllByPwaApplicationDetail(pwaApplicationDetail);
    HashSet<PadAdmiralityChartFile> filesToBeRemoved = new HashSet<>();
    var filesToUpdate = new HashSet<PadAdmiralityChartFile>();

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

    padAdmiralityChartFileRepository.saveAll(filesToUpdate);
    deleteCableCrossingFilesAndLinkedUploads(filesToBeRemoved, user);
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
   * Simplify api by providing pass through method to access file service.
   */
  public List<UploadedFileView> getUpdatedCableCrossingFileViewsWhenFileOnForm(
      PwaApplicationDetail pwaApplicationDetail,
      CrossingDocumentsForm form) {

    return updateFormWithSuppliedUploadedFileViews(
        form,
        () -> getCableCrossingFileViews(pwaApplicationDetail, ApplicationFileLinkStatus.ALL));
  }

  @Transactional
  public void deleteUploadedFileLink(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    PadAdmiralityChartFile existingFile = getCableCrossingFile(fileId,
        pwaApplicationDetail);
    deleteCableCrossingFileLink(existingFile);
  }

  /**
   * Method which creates "temporary" link to application detail cable crossing file
   * If form left unsaved, we know which files are deletable.
   */
  @Transactional
  public PadAdmiralityChartFile createUploadedFileLink(String uploadedFileId,
                                                     PwaApplicationDetail pwaApplicationDetail) {
    return createAndSaveCableCrossingFile(
        pwaApplicationDetail,
        uploadedFileId
    );
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new CrossingDocumentsForm();
    mapDocumentsToForm(detail, form);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  private boolean requiresFullValidation(PwaApplicationDetail pwaApplicationDetail) {
    var appType = pwaApplicationDetail.getPwaApplicationType();
    if (appType.equals(PwaApplicationType.INITIAL) || appType.equals(PwaApplicationType.CAT_1_VARIATION)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
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
