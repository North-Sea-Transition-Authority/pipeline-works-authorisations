package uk.co.ogauthority.pwa.service.appprocessing.options;

import java.time.Clock;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApplicationApproval;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApprovalDeadlineHistory;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
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


}
