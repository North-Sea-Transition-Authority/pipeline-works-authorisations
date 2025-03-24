package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.umbilical;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

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
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementValidatorTestUtils;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

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
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));
    var result = crossSectionService.validate(form, bindingResult, ValidationType.FULL, detail);
    assertThat(result.hasErrors()).isFalse();
  }

  @Test
  void validate_twoFiles() {
    form.setUploadedFiles(List.of(
        FileManagementValidatorTestUtils.createUploadedFileForm(),
        FileManagementValidatorTestUtils.createUploadedFileForm())
    );
    var result = crossSectionService.validate(form, bindingResult, ValidationType.FULL, detail);
    assertThat(result.getAllErrors()).extracting(DefaultMessageSourceResolvable::getCode)
        .containsExactly(FileValidationUtils.ABOVE_LIMIT_ERROR_CODE);
  }

  @Test
  void validate_emptyFileDescription_invalid() {
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileFormWithoutDescription()));
    crossSectionService.validate(form, bindingResult, ValidationType.FULL, detail);

    var fieldErrors = ValidatorTestUtils.extractErrors(bindingResult);
    assertThat(fieldErrors).contains(
        entry(FileValidationUtils.FIELD_NAME + "[0].uploadedFileDescription",
            Set.of(FileValidationUtils.FIELD_NAME + "[0].uploadedFileDescription.required"))
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