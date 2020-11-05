package uk.co.ogauthority.pwa.repository.licence;


import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.BlockLocation;
import uk.co.ogauthority.pwa.model.entity.licence.PearsBlock;

@Repository
public interface PearsBlockRepository extends CrudRepository<PearsBlock, String> {

  @EntityGraph(attributePaths = { "pearsLicence" })
  Optional<PearsBlock> findByCompositeKeyAndBlockLocation(String compositeKey, BlockLocation blockLocation);

}
