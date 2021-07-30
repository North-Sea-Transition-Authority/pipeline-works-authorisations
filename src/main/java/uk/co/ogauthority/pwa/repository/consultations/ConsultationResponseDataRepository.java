package uk.co.ogauthority.pwa.repository.consultations;

import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseData;

@Repository
public interface ConsultationResponseDataRepository extends CrudRepository<ConsultationResponseData, Integer> {

  List<ConsultationResponseData> findAllByConsultationResponseIn(Collection<ConsultationResponse> responses);

  List<ConsultationResponseData> findAllByConsultationResponse(ConsultationResponse response);

}