package uk.co.ogauthority.pwa.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadProperties;
import uk.co.ogauthority.pwa.controller.files.FileUploadFrontendController;

@ControllerAdvice(annotations = FileUploadFrontendController.class)
public class FileUploadFrontendControllerAdvice {

  private final FileUploadProperties fileUploadProperties;

  @Autowired
  public FileUploadFrontendControllerAdvice(FileUploadProperties fileUploadProperties) {
    this.fileUploadProperties = fileUploadProperties;
  }

  /**
   * As part of a ControllerAdvice these properties will be added in any request's Model accepted by controllers
   * with the @FileUploadFrontendController.
   * @param model the Model to which the file upload properties will be added to
   */
  @ModelAttribute
  public void addCommonModelAttributes(Model model) {
    String fileUploadAllowedExtensions = String.join(",", fileUploadProperties.getAllowedExtensions());
    model.addAttribute("fileuploadAllowedExtensions", fileUploadAllowedExtensions);
    model.addAttribute("fileuploadMaxUploadSize", String.valueOf(fileUploadProperties.getMaxFileSize()));
  }
}
