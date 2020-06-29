package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.PipelineCrossingDocumentsController;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.FileUploadStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineCrossingRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.file.PadPipelineCrossingFileRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUploadService;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

@Service
public class PipelineCrossingFileService implements ApplicationFormSectionService {
  
  private final PadPipelineCrossingFileRepository padPipelineCrossingFileRepository;
  private final PadPipelineCrossingRepository padPipelineCrossingRepository;
  private final FileUploadService fileUploadService;
  private final EntityManager entityManager;
  private final SpringValidatorAdapter groupValidator;
  private final PadFileService padFileService;

  @Autowired
  public PipelineCrossingFileService(
      PadPipelineCrossingFileRepository padPipelineCrossingFileRepository,
      PadPipelineCrossingRepository padPipelineCrossingRepository,
      FileUploadService fileUploadService, EntityManager entityManager,
      SpringValidatorAdapter groupValidator, PadFileService padFileService) {
    this.padPipelineCrossingFileRepository = padPipelineCrossingFileRepository;
    this.padPipelineCrossingRepository = padPipelineCrossingRepository;
    this.fileUploadService = fileUploadService;
    this.entityManager = entityManager;
    this.groupValidator = groupValidator;
    this.padFileService = padFileService;
  }

  public boolean requiresFullValidation(PwaApplicationDetail pwaApplicationDetail) {
    return padPipelineCrossingRepository.countAllByPwaApplicationDetailAndPipelineFullyOwnedByOrganisation(
        pwaApplicationDetail, false) > 0;
  }

  /**
   * Gets linked pipeline crossing files as uploaded file forms.
   */
  private List<UploadFileWithDescriptionForm> getUploadedFileListAsFormList(PwaApplicationDetail pwaApplicationDetail,
                                                                            ApplicationFileLinkStatus applicationFileLinkStatus) {
    return getPipelineCrossingFileViews(pwaApplicationDetail, applicationFileLinkStatus)
        .stream()
        .map(fileUploadService::createUploadFileWithDescriptionFormFromView)
        .collect(Collectors.toList());
  }


  /**
   * Get pipeline crossing files with requested link status as standard uploaded file views.
   */
  public List<UploadedFileView> getPipelineCrossingFileViews(PwaApplicationDetail pwaApplicationDetail,
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
            "FROM PadPipelineCrossingFile pbcf " +
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
        ufv -> ufv.setFileUrl(ReverseRouter.route(on(PipelineCrossingDocumentsController.class).handleDownload(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            ufv.getFileId(),
            null
            ))
        ));

    return fileViews;

  }


  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new CrossingDocumentsForm();
    padFileService.mapFilesToForm(form, detail, ApplicationFilePurpose.PIPELINE_CROSSINGS);
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
