package uk.co.ogauthority.pwa.repository.docgen;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;

@Repository
public interface DocgenRunRepository extends CrudRepository<DocgenRun, Long> {

}
