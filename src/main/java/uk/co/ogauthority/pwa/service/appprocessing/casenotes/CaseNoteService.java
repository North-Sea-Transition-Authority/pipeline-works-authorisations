package uk.co.ogauthority.pwa.service.appprocessing.casenotes;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes.CaseNote;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.repository.appprocessing.casenotes.CaseNoteRepository;
import uk.co.ogauthority.pwa.service.appprocessing.casehistory.CaseHistoryItemService;
import uk.co.ogauthority.pwa.service.appprocessing.casehistory.CaseHistoryItemViewFactory;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;

@Service
public class CaseNoteService implements AppProcessingService, CaseHistoryItemService {

  private final CaseNoteRepository caseNoteRepository;
  private final Clock clock;

  @Autowired
  public CaseNoteService(CaseNoteRepository caseNoteRepository,
                         @Qualifier("utcClock") Clock clock) {
    this.caseNoteRepository = caseNoteRepository;
    this.clock = clock;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.ADD_CASE_NOTE);
  }

  @Override
  public Optional<TaskStatus> getTaskStatus(PwaAppProcessingContext processingContext) {
    return Optional.empty();
  }

  public void createCaseNote(PwaApplication pwaApplication,
                             String noteText,
                             Person creatingPerson) {

    var caseNote = new CaseNote(pwaApplication, creatingPerson.getId(), Instant.now(clock), noteText);

    caseNoteRepository.save(caseNote);

  }

  @Override
  public List<CaseHistoryItemView> getCaseHistoryItemViews(PwaApplication pwaApplication) {
    return caseNoteRepository.getAllByPwaApplication(pwaApplication).stream()
        .map(CaseHistoryItemViewFactory::create)
        .collect(Collectors.toList());
  }
}
