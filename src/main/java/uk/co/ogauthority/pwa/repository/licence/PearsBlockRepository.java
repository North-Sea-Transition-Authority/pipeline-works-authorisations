package uk.co.ogauthority.pwa.repository.licence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.licence.PearsBlock;

@Repository
public interface PearsBlockRepository extends CrudRepository<PearsBlock, Integer> {

}
