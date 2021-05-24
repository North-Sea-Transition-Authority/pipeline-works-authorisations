package uk.co.ogauthority.pwa.service.documents.clauses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.documents.ClauseForm;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.mailmerge.MailMergeService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ClauseFormValidatorTest {

  @Mock
  private MailMergeService mailMergeService;

  private ClauseFormValidator validator;

  private PwaApplicationDetail detail;

  @Before
  public void setUp() {

    validator = new ClauseFormValidator(mailMergeService);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

  }

  @Test
  public void validate_emptyForm() {

    var form = new ClauseForm();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail.getPwaApplication());

    assertThat(errors).containsOnly(entry("name", Set.of("name.required")));

  }

  @Test
  public void validate_noText() {

    var form = new ClauseForm();
    form.setName("name");

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail.getPwaApplication());

    assertThat(errors).isEmpty();

  }

  @Test
  public void validate_withText() {

    ClauseForm form = getClauseForm(null);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail.getPwaApplication());

    assertThat(errors).isEmpty();

  }

  @Test
  public void validate_noInvalidMergeFields_noManualMergeDelims_noErrors() {

    ClauseForm form = getClauseForm("text");

    when(mailMergeService.validateMailMergeFields(detail.getPwaApplication(), form.getText())).thenReturn(Set.of());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail.getPwaApplication());

    assertThat(errors).isEmpty();

  }

  @Test
  public void validate_invalidMergeFields_noManualMergeDelims_error() {

    ClauseForm form = getClauseForm("text");

    when(mailMergeService.validateMailMergeFields(detail.getPwaApplication(), form.getText()))
        .thenReturn(Set.of("INVALIDFIELD1", "INVALIDFIELD2"));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail.getPwaApplication());

    assertThat(errors).contains(entry("text", Set.of("text.invalid")));

  }

  @Test
  public void validate_noInvalidMergeFields_manualMergeDelimsPresent_manualMergeNotAllowed_error() {

    var form = getClauseForm(String.format("text %soptional thing here%s",
        MailMergeFieldType.MANUAL.getOpeningDelimiter(), MailMergeFieldType.MANUAL.getClosingDelimiter()));

    when(mailMergeService.validateMailMergeFields(detail.getPwaApplication(), form.getText())).thenReturn(Set.of());

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, detail.getPwaApplication());

    assertThat(errors).contains(entry("text", Set.of("text.invalid")));

  }

  @Test
  public void validate_noInvalidMergeFields_manualMergeDelimsPresent_manualMergeAllowed_ok() {

    var form = getClauseForm(String.format("text %soptional thing here%s",
        MailMergeFieldType.MANUAL.getOpeningDelimiter(), MailMergeFieldType.MANUAL.getClosingDelimiter()));

    var docSource = new TemplateDocumentSource(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT);

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, docSource);

    assertThat(errors).isEmpty();

  }

  private ClauseForm getClauseForm(String text) {

    var form = new ClauseForm();
    form.setName("name");

    Optional.ofNullable(text)
        .ifPresent(form::setText);

    return form;

  }

}