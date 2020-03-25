package uk.co.ogauthority.pwa.controller.files;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;
import uk.co.ogauthority.pwa.model.form.files.ExampleMultipleUploadForm;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.files.UploadedFileRepository;
import uk.co.ogauthority.pwa.service.fileupload.FileUploadService;
import uk.co.ogauthority.pwa.service.util.FileDownloadUtils;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.ModelAndViewUtils;


/**
 * Basic controller to handle file upload and downloads. Destined for deletion when real implementation for user story is reached
 * Limitations:
 * no security. who can/cannot download files? this will require additional mapping table e.g application_case_notes
 * and services which support real functionality
 */
@Controller
@FileUploadFrontendController
public class FileUploadDownloadController {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadDownloadController.class);

  private final UploadedFileRepository uploadedFileRepository;
  private final FileUploadService fileUploadService;

  @Autowired
  public FileUploadDownloadController(UploadedFileRepository uploadedFileRepository,
                                      FileUploadService fileUploadService) {
    this.uploadedFileRepository = uploadedFileRepository;
    this.fileUploadService = fileUploadService;
    ;
  }

  @GetMapping("/files/download/{fileId}")
  @ResponseBody
  public ResponseEntity<Resource> handleFileDownloadRequest(@PathVariable String fileId,
                                                            AuthenticatedUserAccount user) {
    UploadedFile file = uploadedFileRepository.findById(fileId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Could not find uploaded file with id:" + fileId));
    return serveFile(file);
  }

  @PostMapping("/files/upload")
  @ResponseBody
  public FileUploadResult handleUpload(@RequestParam("file") MultipartFile file,
                                       AuthenticatedUserAccount user) {
    return fileUploadService.processUpload(file, user);
  }

  /**
   * Deals with individual file deleting.
   *
   * @param fileId the id of the file being downloaded
   * @param user   the logged in user
   * @return the object representing the downloaded file
   */
  @PostMapping("/files/delete/{fileId}")
  @ResponseBody
  public FileDeleteResult handleDelete(@PathVariable String fileId,
                                       AuthenticatedUserAccount user) {

    return fileUploadService.deleteUploadedFile(fileId, user);
  }


  @GetMapping("/files/all")
  @ResponseBody
  public ModelAndView renderUploadFiles(@ModelAttribute("form") ExampleMultipleUploadForm form,
                                        AuthenticatedUserAccount userAccount) {
    return getExampleFileUPloadModelAndView(new ExampleMultipleUploadForm(), null);
  }

  private ModelAndView getExampleFileUPloadModelAndView(ExampleMultipleUploadForm exampleForm,
                                                        BindingResult bindingResult) {
    List<UploadedFileView> uploadedFileViewList = fileUploadService.getAllUploadedFileViews();
    List<UploadFileWithDescriptionForm> uploadedFilesFormList = uploadedFileViewList.stream()
        .map(fileUploadService::createUploadFileWithDescriptionFormFromView)
        .collect(Collectors.toList());

    exampleForm.setUploadedFileWithDescriptionForms(uploadedFilesFormList);

    ModelAndView modelAndView = new ModelAndView("patterns/multipleFileUploadWithDescriptions");
    modelAndView.addObject("form", exampleForm);
    modelAndView.addObject("formSubmitUrl",
        ReverseRouter.route(on(FileUploadDownloadController.class).handleUpload(null, null)));

    modelAndView.addObject("uploadedFileViewList", uploadedFileViewList);
    modelAndView.addObject("cancelUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea()));

    modelAndView.addObject("uploadUrl",
        ReverseRouter.route(on(FileUploadDownloadController.class).handleUpload(null, null))
    );
    modelAndView.addObject("downloadUrl",
        ReverseRouter.route(on(FileUploadDownloadController.class).handleFileDownloadRequest(null, null)));
    modelAndView.addObject("deleteUrl",
        ReverseRouter.route(on(FileUploadDownloadController.class).handleDelete(null, null))
    );

    if (bindingResult == null) {
      modelAndView.addObject("errorList", Collections.emptyList());

    } else {
      ModelAndViewUtils.addFieldValidationErrors(modelAndView, bindingResult);
    }
    return modelAndView;

  }

  @PostMapping("/files/all")
  @ResponseBody
  public ModelAndView handleAllFilesPostFile(@ModelAttribute("form") @Valid ExampleMultipleUploadForm form,
                                             BindingResult bindingResult,
                                             AuthenticatedUserAccount userAccount) {
    return ControllerUtils.validateAndRedirect(
        bindingResult,
        getExampleFileUPloadModelAndView(form, bindingResult),
        () -> {
          throw new RuntimeException(
              "For a proper feature we need to save descriptions separately to file to allow updates without reuploading.");
        });
  }


  /**
   * Performs the download of an UploadedFile.
   *
   * @param uploadedFile file we want to trigger download for
   * @return the ResponseEntity object containing the downloaded file
   */
  private ResponseEntity<Resource> serveFile(UploadedFile uploadedFile) {


    Resource resource = FileDownloadUtils.fetchFileAsStream(uploadedFile.getFileName(), uploadedFile.getFileData());
    MediaType mediaType = MediaType.parseMediaType(uploadedFile.getContentType());
    return FileDownloadUtils.getResourceAsResponse(resource, mediaType, uploadedFile.getFileName(),
        uploadedFile.getFileSize());
  }
}

