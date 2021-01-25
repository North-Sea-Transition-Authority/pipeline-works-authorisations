package uk.co.ogauthority.pwa.repository.documents.instances;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.documents.instances.DocumentInstanceSectionClauseVersionDto;

@Repository
public interface DocumentInstanceSectionClauseVersionDtoRepository extends JpaRepository<DocumentInstanceSectionClauseVersionDto, Integer> {

  List<DocumentInstanceSectionClauseVersionDto> findAllByDiId(Integer diId);

  List<DocumentInstanceSectionClauseVersionDto> findAllByDiId_AndSectionNameEquals(Integer diId, String sectionName);

}
