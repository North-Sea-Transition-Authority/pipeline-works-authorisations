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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings.AdmiraltyChartDocumentsController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.FileUploadStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadAdmiraltyChartFile;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.AdmiraltyChartDocumentForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadAdmiraltyChartFileRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUploadService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

@Service
public class AdmiraltyChartFileService implements ApplicationFormSectionService {

  private final PadAdmiraltyChartFileRepository padAdmiraltyChartFileRepository;
  private final FileUploadService fileUploadService;
  private final EntityManager entityManager;
  private final SpringValidatorAdapter groupValidator;

  @Autowired
  public AdmiraltyChartFileService(
      PadAdmiraltyChartFileRepository padAdmiraltyChartFileRepository,
      FileUploadService fileUploadService, EntityManager entityManager,
      SpringValidatorAdapter groupValidator) {
    this.padAdmiraltyChartFileRepository = padAdmiraltyChartFileRepository;
    this.fileUploadService = fileUploadService;
    this.entityManager = entityManager;
    this.groupValidator = groupValidator;
  }

  public void mapDocumentsToForm(PwaApplicationDetail pwaApplicationDetail, AdmiraltyChartDocumentForm form) {
    var fileFormViewList = getUploadedFileListAsFormList(pwaApplicationDetail, ApplicationFileLinkStatus.FULL);
    form.setUploadedFileWithDescriptionForms(fileFormViewList);
  }

  /**
   * Return linked admiralty chart file if it exists for application detail else throw not found exception.
   */
  public PadAdmiraltyChartFile getAdmiraltyChartFile(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    return padAdmiraltyChartFileRepository.findByPwaApplicationDetailAndFileId(
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
   * Gets linked admiralty chart files as uploaded file forms.
   */
  private List<UploadFileWithDescriptionForm> getUploadedFileListAsFormList(PwaApplicationDetail pwaApplicationDetail,
                                                                            ApplicationFileLinkStatus applicationFileLinkStatus) {
    return getAdmiraltyChartFileViews(pwaApplicationDetail, applicationFileLinkStatus)
        .stream()
        .map(fileUploadService::createUploadFileWithDescriptionFormFromView)
        .collect(Collectors.toList());
  }

  /**
   * Get admiralty chart files with requested link status as standard uploaded file views.
   */
  public List<UploadedFileView> getAdmiraltyChartFileViews(PwaApplicationDetail pwaApplicationDetail,
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
            "FROM PadAdmiraltyChartFile pmlcf " +
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
        ufv -> ufv.setFileUrl(ReverseRouter.route(on(AdmiraltyChartDocumentsController.class).handleDownload(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            ufv.getFileId(),
            null
            ))
        ));

    return fileViews;

  }

  /**
   * Create and persist a new admiralty chart file linked to app detail and uploaded file id.
   */
  private PadAdmiraltyChartFile createAndSaveAdmiraltyChartFile(PwaApplicationDetail pwaApplicationDetail,
                                                                 String uploadedFileId) {
    var newFileLink = new PadAdmiraltyChartFile(
        pwaApplicationDetail,
        uploadedFileId,
        null,
        ApplicationFileLinkStatus.TEMPORARY
    );

    return padAdmiraltyChartFileRepository.save(newFileLink);
  }

  /**
   * Remove admiralty chart file link and delete uploaded file.
   */
  @Transactional
  void deleteAdmiraltyChartFilesAndLinkedUploads(Iterable<PadAdmiraltyChartFile> filesToBeRemoved,
                                                WebUserAccount user) {
    for (PadAdmiraltyChartFile fileToRemove : filesToBeRemoved) {
      var result = fileUploadService.deleteUploadedFile(fileToRemove.getFileId(), user);
      if (!result.isValid()) {
        throw new RuntimeException("Could not delete uploaded file with Id:" + fileToRemove.getFileId());
      }
    }
    padAdmiraltyChartFileRepository.deleteAll(filesToBeRemoved);
  }

  /**
   * Delete single uploaded file link.
   */
  @Transactional
  void deleteAdmiraltyChartFileLink(PadAdmiraltyChartFile fileToRemove) {
    padAdmiraltyChartFileRepository.delete(fileToRemove);
  }

  /**
   * For all uploaded files included in form, persist updated descriptions as provided on form.
   * For all linked files not included in form, remove link and delete uploaded file.
   */
  @Transactional
  public void updateOrDeleteLinkedFilesUsingForm(PwaApplicationDetail pwaApplicationDetail,
                                                 AdmiraltyChartDocumentForm form,
                                                 WebUserAccount user) {
    Map<String, UploadFileWithDescriptionForm> uploadedFilesMap = form.getUploadedFileWithDescriptionForms()
        .stream()
        .collect(Collectors.toMap(UploadFileWithDescriptionForm::getUploadedFileId, f -> f));

    var existingLinkedFiles = padAdmiraltyChartFileRepository.findAllByPwaApplicationDetail(pwaApplicationDetail);
    HashSet<PadAdmiraltyChartFile> filesToBeRemoved = new HashSet<>();
    var filesToUpdate = new HashSet<PadAdmiraltyChartFile>();

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

    padAdmiraltyChartFileRepository.saveAll(filesToUpdate);
    deleteAdmiraltyChartFilesAndLinkedUploads(filesToBeRemoved, user);
  }

  /**
   * Get all uploaded files as views where the file exists on form and with description as set on form.
   */
  private List<UploadedFileView> updateFormWithSuppliedUploadedFileViews(
      AdmiraltyChartDocumentForm form,
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
  public List<UploadedFileView> getUpdatedAdmiraltyChartFileViewsWhenFileOnForm(
      PwaApplicationDetail pwaApplicationDetail,
      AdmiraltyChartDocumentForm form) {

    return updateFormWithSuppliedUploadedFileViews(
        form,
        () -> getAdmiraltyChartFileViews(pwaApplicationDetail, ApplicationFileLinkStatus.ALL));
  }

  @Transactional
  public void deleteUploadedFileLink(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    PadAdmiraltyChartFile existingFile = getAdmiraltyChartFile(fileId,
        pwaApplicationDetail);
    deleteAdmiraltyChartFileLink(existingFile);
  }

  /**
   * Method which creates "temporary" link to application detail admiralty chart file
   * If form left unsaved, we know which files are deletable.
   */
  @Transactional
  public PadAdmiraltyChartFile createUploadedFileLink(String uploadedFileId,
                                                      PwaApplicationDetail pwaApplicationDetail) {
    return createAndSaveAdmiraltyChartFile(
        pwaApplicationDetail,
        uploadedFileId
    );
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new AdmiraltyChartDocumentForm();
    mapDocumentsToForm(detail, form);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    return !validate(form, bindingResult, ValidationType.FULL, detail).hasErrors();
  }

  public boolean isUploadRequired(PwaApplicationDetail pwaApplicationDetail) {
    var appType = pwaApplicationDetail.getPwaApplicationType();
    return appType.equals(PwaApplicationType.INITIAL) || appType.equals(PwaApplicationType.CAT_1_VARIATION);
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    List<Object> hints = new ArrayList<>();
    if (validationType.equals(ValidationType.FULL)) {
      hints.add(FullValidation.class);
      if (isUploadRequired(pwaApplicationDetail)) {
        hints.add(MandatoryUploadValidation.class);
      }
    } else {
      hints.add(PartialValidation.class);
    }
    groupValidator.validate(form, bindingResult, hints.toArray());

    if (((AdmiraltyChartDocumentForm) form).getUploadedFileWithDescriptionForms().size() > 1) {
      bindingResult.rejectValue("uploadedFileWithDescriptionForms",
          "uploadedFileWithDescriptionForms.exceededMaximumUpload",
              "You may only upload a single admiralty chart");
    }

    return bindingResult;
  }

  public boolean canUploadDocuments(PwaApplicationDetail detail) {
    switch (detail.getPwaApplicationType()) {
      case INITIAL:
      case CAT_1_VARIATION:
        return true;
      default:
        return false;
    }
  }
  
}
