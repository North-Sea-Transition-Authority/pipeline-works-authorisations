package uk.co.ogauthority.pwa.service.appprocessing.options;

import java.time.Clock;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApplicationApproval;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApprovalDeadlineHistory;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApplicationApprovalRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApprovalDeadlineHistoryRepository;

@Service
class OptionsApprovalPersister {

  private final OptionsApplicationApprovalRepository optionsApplicationApprovalRepository;

  private final OptionsApprovalDeadlineHistoryRepository optionsApprovalDeadlineHistoryRepository;

  private final Clock clock;

  @Autowired
  public OptionsApprovalPersister(OptionsApplicationApprovalRepository optionsApplicationApprovalRepository,
                                  OptionsApprovalDeadlineHistoryRepository optionsApprovalDeadlineHistoryRepository,
                                  @Qualifier("utcClock") Clock clock) {
    this.optionsApplicationApprovalRepository = optionsApplicationApprovalRepository;
    this.optionsApprovalDeadlineHistoryRepository = optionsApprovalDeadlineHistoryRepository;
    this.clock = clock;
  }


  @Transactional
  public OptionsApprovalDeadlineHistory createInitialOptionsApproval(PwaApplication pwaApplication,
                                                                     Person createdBy,
                                                                     Instant deadlineDate) {
    var optionsApproval = OptionsApplicationApproval.from(
        createdBy.getId(),
        clock.instant(),
        pwaApplication
    );

    optionsApproval = optionsApplicationApprovalRepository.save(optionsApproval);

    var deadlineHistory = OptionsApprovalDeadlineHistory.createInitialTipFrom(optionsApproval,
        deadlineDate,
        null
    );

    return optionsApprovalDeadlineHistoryRepository.save(deadlineHistory);

  }

  private OptionsApprovalDeadlineHistory getTipDeadlineHistoryItemOrError(
      OptionsApplicationApproval optionsApplicationApproval) {
    return optionsApprovalDeadlineHistoryRepository
        .findByOptionsApplicationApprovalAndTipFlagIsTrue(optionsApplicationApproval)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Could not find tip deadline for optionsApplicationApproval.id:" + optionsApplicationApproval.getId())
        );
  }

  @Transactional
  public void endTipDeadlineHistoryItem(OptionsApplicationApproval optionsApplicationApproval) {
    var currentDeadline = getTipDeadlineHistoryItemOrError(optionsApplicationApproval);
    currentDeadline.setTipFlag(false);
    optionsApprovalDeadlineHistoryRepository.save(currentDeadline);
  }


  @Transactional
  public OptionsApprovalDeadlineHistory createTipDeadlineHistoryItem(
      OptionsApplicationApproval optionsApplicationApproval,
      Person createdBy,
      Instant deadlineDate,
      String note) {

    var newOptionsDeadline = OptionsApprovalDeadlineHistory.createTipFrom(
        optionsApplicationApproval,
        createdBy.getId(),
        clock,
        deadlineDate,
        note
    );

    return optionsApprovalDeadlineHistoryRepository.save(newOptionsDeadline);

  }

}
