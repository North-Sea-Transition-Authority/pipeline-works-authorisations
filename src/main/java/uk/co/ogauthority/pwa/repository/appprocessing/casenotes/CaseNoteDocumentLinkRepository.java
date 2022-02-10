package uk.co.ogauthority.pwa.repository.appprocessing.casenotes;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes.CaseNote;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes.CaseNoteDocumentLink;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;

@Repository
public interface CaseNoteDocumentLinkRepository extends CrudRepository<CaseNoteDocumentLink, Integer> {

  Optional<CaseNoteDocumentLink> findByCaseNote_PwaApplicationAndAppFile(PwaApplication application,
                                                                         AppFile appFile);

  @EntityGraph(attributePaths = {"caseNote", "appFile"})
  List<CaseNoteDocumentLink> findAllByCaseNoteIn(List<CaseNote> caseNotes);
}
