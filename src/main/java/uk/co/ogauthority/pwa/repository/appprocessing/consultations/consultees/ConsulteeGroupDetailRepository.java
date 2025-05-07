package uk.co.ogauthority.pwa.repository.appprocessing.consultations.consultees;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;

@Repository
public interface ConsulteeGroupDetailRepository extends CrudRepository<ConsulteeGroupDetail, Integer> {

  @EntityGraph(attributePaths = "consulteeGroup")
  List<ConsulteeGroupDetail> findAllByEndTimestampIsNull();

  @EntityGraph(attributePaths = "consulteeGroup")
  List<ConsulteeGroupDetail> findAllByConsulteeGroupInAndEndTimestampIsNull(Iterable<ConsulteeGroup> groups);

  List<ConsulteeGroupDetail> findAllByTipFlagIsTrue();

  Optional<ConsulteeGroupDetail> findByConsulteeGroupAndTipFlagIsTrue(ConsulteeGroup consulteeGroup);

  List<ConsulteeGroupDetail> findAllByConsulteeGroupInAndTipFlagIsTrue(Iterable<ConsulteeGroup> groups);

  List<ConsulteeGroupDetail> findAllByTipFlagIsTrueAndNameContainsIgnoreCase(String partialName);

}
