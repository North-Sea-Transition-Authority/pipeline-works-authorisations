package uk.co.ogauthority.pwa.validators.consultations;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class RequestConsultationValidatorTest {

  @Mock
  private ConsultationRequestService consultationRequestService;
  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  private ConsultationRequestValidator validator;

  @Before
  public void setUp() {
    validator = new ConsultationRequestValidator();
  }



  @Test
  public void validate_form_empty() {
    var form = new ConsultationRequestForm();
    form.setDaysToRespond(null);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, consultationRequestService, consulteeGroupTeamService);
    assertThat(errorsMap).containsOnly(
        entry("consulteeGroupSelection", Set.of("consulteeGroupSelection.required")),
        entry("daysToRespond", Set.of("daysToRespond.required"))
    );
  }

  @Test
  public void validate_form_valid() {
    var form = new ConsultationRequestForm();
    form.getConsulteeGroupSelection().put("1", "true");
    form.setDaysToRespond(5);

    var consulteeGroupDetail = new ConsulteeGroupDetail();
    consulteeGroupDetail.setId(1);

    when(consulteeGroupTeamService.getConsulteeGroupDetailById(1)).thenReturn(consulteeGroupDetail);
    when(consultationRequestService.isConsultationRequestOpen(consulteeGroupDetail)).thenReturn(false);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, consultationRequestService, consulteeGroupTeamService);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  public void validate_consultationAlreadyOpen_invalid() {
    var form = new ConsultationRequestForm();
    form.getConsulteeGroupSelection().put("1", "true");
    form.setDaysToRespond(5);

    var consulteeGroupDetail = new ConsulteeGroupDetail();
    consulteeGroupDetail.setId(1);

    when(consulteeGroupTeamService.getConsulteeGroupDetailById(1)).thenReturn(consulteeGroupDetail);
    when(consultationRequestService.isConsultationRequestOpen(consulteeGroupDetail)).thenReturn(true);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, consultationRequestService, consulteeGroupTeamService);
    assertThat(errorsMap).contains(
        entry("consulteeGroupSelection", Set.of("consulteeGroupSelection.invalid"))
    );
  }

  @Test
  public void validate_onlyOtherGroupIsSelected_valid() {
    var form = new ConsultationRequestForm();
    form.setOtherGroupSelected(true);
    form.setOtherGroupLogin("myLogin");
    form.setDaysToRespond(5);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, consultationRequestService, consulteeGroupTeamService);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  public void validate_otherGroupSelected_noLoginInfoProvided_invalid() {
    var form = new ConsultationRequestForm();
    form.setOtherGroupSelected(true);
    form.setDaysToRespond(5);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, consultationRequestService, consulteeGroupTeamService);
    assertThat(errorsMap).contains(
        entry("otherGroupLogin", Set.of("otherGroupLogin.required"))
    );
  }

  @Test
  public void validate_daysToRespondZero_invalid() {
    var form = new ConsultationRequestForm();
    form.getConsulteeGroupSelection().put("1", "true");
    form.setDaysToRespond(0);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, consultationRequestService, consulteeGroupTeamService);
    assertThat(errorsMap).contains(
        entry("daysToRespond", Set.of("daysToRespond.invalid"))
    );
  }






}