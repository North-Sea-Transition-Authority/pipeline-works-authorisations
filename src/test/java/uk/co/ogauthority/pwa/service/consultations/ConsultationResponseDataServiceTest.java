package uk.co.ogauthority.pwa.service.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseData;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseDataForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseDataRepository;

@ExtendWith(MockitoExtension.class)
class ConsultationResponseDataServiceTest {

  @Mock
  private ConsultationResponseDataRepository consultationResponseDataRepository;

  private ConsultationResponseDataService consultationResponseDataService;

  @Captor
  private ArgumentCaptor<List<ConsultationResponseData>> responseDataListCaptor;

  private ConsultationResponse response;

  private ConsultationResponseDataForm dataForm;

  @BeforeEach
  void setUp() {

    consultationResponseDataService = new ConsultationResponseDataService(consultationResponseDataRepository);

    response = new ConsultationResponse();
    dataForm = new ConsultationResponseDataForm();

  }

  @Test
  void createAndSaveResponseData_content_descriptionProvided() {

    testSingleResponseOptionAndTextStoredCorrectly(
        ConsultationResponseOptionGroup.CONTENT,
        ConsultationResponseOption.CONFIRMED,
        dataForm::setOption1Description,
        dataForm::getOption1Description);

  }

  @Test
  void createAndSaveResponseData_rejected_commentsProvided() {

    testSingleResponseOptionAndTextStoredCorrectly(
        ConsultationResponseOptionGroup.CONTENT,
        ConsultationResponseOption.REJECTED,
        dataForm::setOption2Description,
        dataForm::getOption2Description);

  }

  @Test
  void createAndSaveResponseData_advice_adviceProvided() {

    testSingleResponseOptionAndTextStoredCorrectly(
        ConsultationResponseOptionGroup.ADVICE,
        ConsultationResponseOption.PROVIDE_ADVICE,
        dataForm::setOption1Description,
        dataForm::getOption1Description);

  }

  @Test
  void createAndSaveResponseData_noAdvice_commentsProvided() {

    testSingleResponseOptionAndTextStoredCorrectly(
        ConsultationResponseOptionGroup.ADVICE,
        ConsultationResponseOption.NO_ADVICE,
        dataForm::setOption2Description,
        dataForm::getOption2Description);

  }

  @Test
  void createAndSaveResponseData_eiaAgree_consentConditionsProvided() {

    testSingleResponseOptionAndTextStoredCorrectly(
        ConsultationResponseOptionGroup.EIA_REGS,
        ConsultationResponseOption.EIA_AGREE,
        dataForm::setOption1Description,
        dataForm::getOption1Description);

  }

  @Test
  void createAndSaveResponseData_eiaDisagree_reasonProvided() {

    testSingleResponseOptionAndTextStoredCorrectly(
        ConsultationResponseOptionGroup.EIA_REGS,
        ConsultationResponseOption.EIA_DISAGREE,
        dataForm::setOption2Description,
        dataForm::getOption2Description);

  }

  @Test
  void createAndSaveResponseData_eiaNotRequired_reasonProvided() {

    testSingleResponseOptionAndTextStoredCorrectly(
        ConsultationResponseOptionGroup.EIA_REGS,
        ConsultationResponseOption.EIA_NOT_RELEVANT,
        dataForm::setOption3Description,
        dataForm::getOption3Description);

  }

  @Test
  void createAndSaveResponseData_habitatsAgree_consentConditionsProvided() {

    testSingleResponseOptionAndTextStoredCorrectly(
        ConsultationResponseOptionGroup.HABITATS_REGS,
        ConsultationResponseOption.HABITATS_AGREE,
        dataForm::setOption1Description,
        dataForm::getOption1Description);

  }

  @Test
  void createAndSaveResponseData_habitatsDisagree_reasonProvided() {

    testSingleResponseOptionAndTextStoredCorrectly(
        ConsultationResponseOptionGroup.HABITATS_REGS,
        ConsultationResponseOption.HABITATS_DISAGREE,
        dataForm::setOption2Description,
        dataForm::getOption2Description);

  }

  @Test
  void createAndSaveResponseData_habitatsNotRequired_reasonProvided() {

    testSingleResponseOptionAndTextStoredCorrectly(
        ConsultationResponseOptionGroup.HABITATS_REGS,
        ConsultationResponseOption.HABITATS_NOT_RELEVANT,
        dataForm::setOption3Description,
        dataForm::getOption3Description);

  }

  private void testSingleResponseOptionAndTextStoredCorrectly(ConsultationResponseOptionGroup responseGroup,
                                                              ConsultationResponseOption responseOption,
                                                              Consumer<String> formSetMethod,
                                                              Supplier<String> formGetMethod) {

    dataForm.setConsultationResponseOption(responseOption);
    formSetMethod.accept("comments");

    var form = new ConsultationResponseForm();
    form.setResponseDataForms(Map.of(responseGroup, dataForm));

    consultationResponseDataService.createAndSaveResponseData(response, form);

    verify(consultationResponseDataRepository, times(1))
        .saveAll(responseDataListCaptor.capture());

    assertThat(responseDataListCaptor.getValue())
        .hasOnlyOneElementSatisfying(responseData -> {

          assertThat(responseData.getResponseGroup()).isEqualTo(responseGroup);
          assertThat(responseData.getResponseType()).isEqualTo(dataForm.getConsultationResponseOption());
          assertThat(responseData.getResponseText()).isEqualTo(formGetMethod.get());

        });

  }

  @Test
  void createAndSaveResponseData_eiaAndHabitatsRegs_bothSaved() {

    var eiaForm = new ConsultationResponseDataForm();
    eiaForm.setConsultationResponseOption(ConsultationResponseOption.EIA_DISAGREE);
    eiaForm.setOption2Description("disagree");

    var habitatsForm = new ConsultationResponseDataForm();
    habitatsForm.setConsultationResponseOption(ConsultationResponseOption.HABITATS_AGREE);

    var form = new ConsultationResponseForm();
    form.setResponseDataForms(Map.of(
        ConsultationResponseOptionGroup.EIA_REGS, eiaForm,
        ConsultationResponseOptionGroup.HABITATS_REGS, habitatsForm));

    consultationResponseDataService.createAndSaveResponseData(response, form);

    verify(consultationResponseDataRepository, times(1))
        .saveAll(responseDataListCaptor.capture());

    assertThat(responseDataListCaptor.getValue()).hasSize(2);

    assertThat(responseDataListCaptor.getValue())
        .anySatisfy(responseData -> {

          assertThat(responseData.getResponseGroup()).isEqualTo(ConsultationResponseOptionGroup.EIA_REGS);
          assertThat(responseData.getResponseType()).isEqualTo(eiaForm.getConsultationResponseOption());
          assertThat(responseData.getResponseText()).isEqualTo(eiaForm.getOption2Description());

        })
        .anySatisfy(responseData -> {

          assertThat(responseData.getResponseGroup()).isEqualTo(ConsultationResponseOptionGroup.HABITATS_REGS);
          assertThat(responseData.getResponseType()).isEqualTo(habitatsForm.getConsultationResponseOption());
          assertThat(responseData.getResponseText()).isNull();

        });

  }

  @Test
  void findAllByConsultationResponseIn() {

    var responses = List.of(response);

    consultationResponseDataService.findAllByConsultationResponseIn(responses);

    verify(consultationResponseDataRepository, times(1)).findAllByConsultationResponseIn(responses);

  }

  @Test
  void findAllByConsultationResponse() {

    consultationResponseDataService.findAllByConsultationResponse(response);

    verify(consultationResponseDataRepository, times(1)).findAllByConsultationResponse(response);

  }

}
