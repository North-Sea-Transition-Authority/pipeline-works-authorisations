package uk.co.ogauthority.pwa.repository.masterpwas.contacts;

import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;

@Repository
public class PwaContactDtoRepositoryImpl implements PwaContactDtoRepository {
  private final EntityManager entityManager;

  @Autowired
  public PwaContactDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<PwaContactDto> findAllAsDtoByPerson(Person person) {
    return entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.repository.masterpwas.contacts.PwaContactDto(" +
            "  pc.pwaApplication.id" +
            ", pc.person.id" +
            ", pc.roles " +
            ") " +
            "FROM uk.co.ogauthority.pwa.model.entity.masterpwas.contacts.PwaContact pc " +
            "WHERE pc.person = :person ",
        PwaContactDto.class
    )
        .setParameter("person", person)
        .getResultList();
  }
}
