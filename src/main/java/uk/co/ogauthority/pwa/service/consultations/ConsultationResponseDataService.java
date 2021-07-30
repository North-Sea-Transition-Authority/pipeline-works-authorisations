package uk.co.ogauthority.pwa.service.consultations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseData;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseDataForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseDataRepository;

@Service
public class ConsultationResponseDataService {

  private final ConsultationResponseDataRepository consultationResponseDataRepository;

  @Autowired
  public ConsultationResponseDataService(ConsultationResponseDataRepository consultationResponseDataRepository) {
    this.consultationResponseDataRepository = consultationResponseDataRepository;
  }

  public List<ConsultationResponseData> createAndSaveResponseData(ConsultationResponse consultationResponse,
                                                                  ConsultationResponseForm form) {

    var dataToSave = new ArrayList<ConsultationResponseData>();

    form.getResponseDataForms().forEach((optionGroup, dataForm) -> {

      var data = new ConsultationResponseData(consultationResponse);

      data.setResponseGroup(optionGroup);
      data.setResponseType(dataForm.getConsultationResponseOption());

      var response1 = optionGroup.getResponseOptionNumber(1);
      var response2 = optionGroup.getResponseOptionNumber(2);
      var response3 = optionGroup.getResponseOptionNumber(3);

      setResponseTextIfMatches(dataForm, data, response1, dataForm.getOption1Description());
      setResponseTextIfMatches(dataForm, data, response2, dataForm.getOption2Description());
      setResponseTextIfMatches(dataForm, data, response3, dataForm.getOption3Description());

      dataToSave.add(data);

    });

    return IterableUtils.toList(consultationResponseDataRepository.saveAll(dataToSave));

  }

  private void setResponseTextIfMatches(ConsultationResponseDataForm dataForm,
                                        ConsultationResponseData data,
                                        Optional<ConsultationResponseOption> responseOptional,
                                        String description) {
    responseOptional.ifPresent(response -> {
      if (dataForm.getConsultationResponseOption().equals(response)) {
        data.setResponseText(description);
      }
    });
  }

  public List<ConsultationResponseData> findAllByConsultationResponseIn(Collection<ConsultationResponse> responses) {
    return consultationResponseDataRepository.findAllByConsultationResponseIn(responses);
  }

  public List<ConsultationResponseData> findAllByConsultationResponse(ConsultationResponse response) {
    return consultationResponseDataRepository.findAllByConsultationResponse(response);
  }

}