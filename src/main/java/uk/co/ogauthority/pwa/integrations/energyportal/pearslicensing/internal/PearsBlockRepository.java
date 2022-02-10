package uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.internal;


import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.BlockLocation;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsBlock;

@Repository
public interface PearsBlockRepository extends CrudRepository<PearsBlock, String> {

  @EntityGraph(attributePaths = { "pearsLicence" })
  Optional<PearsBlock> findByCompositeKeyAndBlockLocation(String compositeKey, BlockLocation blockLocation);

}
