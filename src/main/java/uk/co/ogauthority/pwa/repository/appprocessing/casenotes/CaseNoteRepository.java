package uk.co.ogauthority.pwa.repository.appprocessing.casenotes;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes.CaseNote;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

@Repository
public interface CaseNoteRepository extends CrudRepository<CaseNote, Integer> {

  List<CaseNote> getAllByPwaApplication(PwaApplication pwaApplication);

}
