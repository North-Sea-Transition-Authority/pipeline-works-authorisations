package uk.co.ogauthority.pwa.repository.docgen;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.documents.generation.DocgenRunSectionData;

@Repository
public interface DocgenRunSectionDataRepository extends CrudRepository<DocgenRunSectionData, Integer> {
}
