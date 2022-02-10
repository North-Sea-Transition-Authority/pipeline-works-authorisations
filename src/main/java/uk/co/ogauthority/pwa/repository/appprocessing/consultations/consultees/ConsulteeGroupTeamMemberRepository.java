package uk.co.ogauthority.pwa.repository.appprocessing.consultations.consultees;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;

public interface ConsulteeGroupTeamMemberRepository extends CrudRepository<ConsulteeGroupTeamMember, Integer> {

  @EntityGraph(attributePaths = "consulteeGroup")
  Optional<ConsulteeGroupTeamMember> findByPerson(Person person);

  @EntityGraph(attributePaths = {"consulteeGroup", "person"})
  List<ConsulteeGroupTeamMember> findAllByConsulteeGroup(ConsulteeGroup consulteeGroup);

  @EntityGraph(attributePaths = {"consulteeGroup", "person"})
  Optional<ConsulteeGroupTeamMember> findByConsulteeGroupAndPerson(ConsulteeGroup consulteeGroup, Person person);

}
