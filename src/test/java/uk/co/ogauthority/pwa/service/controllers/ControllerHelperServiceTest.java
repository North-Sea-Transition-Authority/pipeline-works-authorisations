package uk.co.ogauthority.pwa.service.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.model.form.appprocessing.casenotes.AddCaseNoteForm;
import uk.co.ogauthority.pwa.model.form.fds.ErrorItem;
import uk.co.ogauthority.pwa.service.controllers.typemismatch.TypeMismatchTestForm;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@ActiveProfiles("integration-test")
class ControllerHelperServiceTest {

  @Autowired
  private MessageSource messageSource;

  private ControllerHelperService controllerHelperService;

  private ModelAndView failedModelAndView;
  private ModelAndView passedModelAndView;

  @BeforeEach
  void setUp() {

    controllerHelperService = new ControllerHelperService(messageSource);

    failedModelAndView = new ModelAndView()
        .addObject("fail", true);

    passedModelAndView = new ModelAndView()
        .addObject("pass", true);

  }

  @Test
  void checkErrorsAndRedirect_noErrors() {

    var form = new TypeMismatchTestForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var result = controllerHelperService.checkErrorsAndRedirect(bindingResult, failedModelAndView, () -> passedModelAndView);

    assertThat(result).isEqualTo(passedModelAndView);

  }

  @Test
  void checkErrorsAndRedirect_errors() {

    var form = new TypeMismatchTestForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.rejectValue("integerField", "integerField.invalid", "Invalid value");
    bindingResult.rejectValue("stringField", "stringField.invalid", "Invalid string");

    var result = controllerHelperService.checkErrorsAndRedirect(bindingResult, failedModelAndView, () -> passedModelAndView);

    assertThat(result).isEqualTo(failedModelAndView);

    @SuppressWarnings("unchecked")
    var errorItemList = (List<ErrorItem>) result.getModel().get("errorList");

    assertThat(errorItemList)
        .extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(0, "integerField", "Invalid value"),
            tuple(1, "stringField", "Invalid string")
        );

  }

  @Test
  void checkErrorsAndRedirect_errorCodeHasOverrideFieldName_errorItemUsesOverrideFieldName() {

    var form = new AddCaseNoteForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.rejectValue(FileValidationUtils.FIELD_NAME, FileValidationUtils.ABOVE_LIMIT_ERROR_CODE);

    var result = controllerHelperService.checkErrorsAndRedirect(bindingResult, failedModelAndView, () -> passedModelAndView);

    @SuppressWarnings("unchecked")
    var errorItemList = (List<ErrorItem>) result.getModel().get("errorList");

    assertThat(errorItemList)
        .extracting(ErrorItem::getFieldName)
        .containsExactly(ControllerHelperService.UPLOADED_FILE_ERROR_ELEMENT_ID);

  }

}
