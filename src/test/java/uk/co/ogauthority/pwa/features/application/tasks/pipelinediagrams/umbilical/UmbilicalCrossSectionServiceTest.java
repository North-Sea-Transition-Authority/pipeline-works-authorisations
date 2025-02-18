package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.umbilical;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.util.fileupload.FileUploadTestUtil;

@ExtendWith(MockitoExtension.class)
class UmbilicalCrossSectionServiceTest {

  @Spy
  private SpringValidatorAdapter groupValidator;

  private UmbilicalCrossSectionService crossSectionService;
  private UmbilicalCrossSectionForm form;
  private BindingResult bindingResult;
  private PwaApplicationDetail detail;

  @BeforeEach
  void setUp() {
    crossSectionService = new UmbilicalCrossSectionService(groupValidator);
    form = new UmbilicalCrossSectionForm();
    bindingResult = new BeanPropertyBindingResult(form, "form");
    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  void validate_noFiles() {
    var result = crossSectionService.validate(form, bindingResult, ValidationType.FULL, detail);
    assertThat(result.hasErrors()).isFalse();
  }

  @Test
  void validate_oneFile() {
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    var result = crossSectionService.validate(form, bindingResult, ValidationType.FULL, detail);
    assertThat(result.hasErrors()).isFalse();
  }

  @Test
  void validate_twoFiles() {
    form.setUploadedFileWithDescriptionForms(List.of(
        new UploadFileWithDescriptionForm("1", "desc", Instant.now()),
        new UploadFileWithDescriptionForm("2", "desc 2", Instant.now())
    ));
    var result = crossSectionService.validate(form, bindingResult, ValidationType.FULL, detail);
    assertThat(result.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .containsExactly(
            "uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.EXCEEDED_MAXIMUM_FILE_UPLOAD_COUNT.getCode());
  }

  @Test
  void validate_fileDescriptionOverMaxCharLength_invalid() {
    FileUploadTestUtil.addUploadFileWithDescriptionOverMaxCharsToForm(form);
    crossSectionService.validate(form, bindingResult, ValidationType.FULL, detail);

    var fieldErrors = ValidatorTestUtils.extractErrors(bindingResult);
    assertThat(fieldErrors).contains(
        entry(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath(),
            Set.of(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath() + FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED.getCode()))
    );
  }

  @Test
  void validate_emptyFileDescription_invalid() {
    FileUploadTestUtil.addUploadFileWithoutDescriptionToForm(form);
    crossSectionService.validate(form, bindingResult, ValidationType.FULL, detail);

    var fieldErrors = ValidatorTestUtils.extractErrors(bindingResult);
    assertThat(fieldErrors).contains(
        entry(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath(),
            Set.of(FileUploadTestUtil.getFirstUploadedFileDescriptionFieldPath() + FieldValidationErrorCodes.REQUIRED.getCode()))
    );
  }

  @Test
  void canUploadDocuments_allowed() {
    Set.of(PwaApplicationType.INITIAL, PwaApplicationType.CAT_1_VARIATION)
        .forEach(pwaApplicationType -> {
          detail.getPwaApplication().setApplicationType(pwaApplicationType);
          assertThat(crossSectionService.canUploadDocuments(detail)).isTrue();
        });
  }

  @Test
  void canUploadDocuments_notAllowed() {
    var typeSet = EnumSet.allOf(PwaApplicationType.class);
    typeSet.removeAll(Set.of(PwaApplicationType.INITIAL, PwaApplicationType.CAT_1_VARIATION));
    typeSet.forEach(pwaApplicationType -> {
      detail.getPwaApplication().setApplicationType(pwaApplicationType);
      assertThat(crossSectionService.canUploadDocuments(detail)).isFalse();
    });
  }
}