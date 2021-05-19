package uk.co.ogauthority.pwa.repository.documents.templates;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.documents.templates.DocumentTemplateSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;

@Repository
public interface DocumentTemplateSectionClauseVersionDtoRepository extends JpaRepository<DocumentTemplateSectionClauseVersionDto, Integer> {

  List<DocumentTemplateSectionClauseVersionDto> findAllByDocumentTemplateMnemAndSectionIn(DocumentTemplateMnem documentTemplateMnem,
                                                                                          Set<DocumentSection> sections);

}
