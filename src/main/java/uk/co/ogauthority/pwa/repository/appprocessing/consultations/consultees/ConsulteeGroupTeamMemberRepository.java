package uk.co.ogauthority.pwa.repository.appprocessing.consultations.consultees;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;

public interface ConsulteeGroupTeamMemberRepository extends CrudRepository<ConsulteeGroupTeamMember, Integer> {

  @EntityGraph(attributePaths = "consulteeGroup")
  List<ConsulteeGroupTeamMember> findAllByPerson(Person person);

}
