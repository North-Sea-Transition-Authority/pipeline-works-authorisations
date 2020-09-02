package uk.co.ogauthority.pwa.service.appprocessing.applicationupdate;

import com.google.common.annotations.VisibleForTesting;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.applicationupdates.ApplicationUpdateRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.notify.emailproperties.ApplicationUpdateRequestEmailProps;
import uk.co.ogauthority.pwa.repository.appprocessing.applicationupdates.ApplicationUpdateRequestRepository;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;

@Service
public class ApplicationUpdateRequestService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationUpdateRequestService.class);

  private final ApplicationUpdateRequestRepository applicationUpdateRequestRepository;
  private final Clock clock;
  private final NotifyService notifyService;
  private final PwaContactService pwaContactService;

  @Autowired
  public ApplicationUpdateRequestService(ApplicationUpdateRequestRepository applicationUpdateRequestRepository,
                                         @Qualifier("utcClock") Clock clock,
                                         NotifyService notifyService,
                                         PwaContactService pwaContactService) {
    this.applicationUpdateRequestRepository = applicationUpdateRequestRepository;
    this.clock = clock;
    this.notifyService = notifyService;
    this.pwaContactService = pwaContactService;
  }


  @Transactional
  public void submitApplicationUpdateRequest(PwaApplicationDetail pwaApplicationDetail,
                                             Person requestingPerson,
                                             String requestReason) {
    createApplicationUpdateRequest(pwaApplicationDetail, requestingPerson, requestReason);
    sendApplicationUpdateRequestedEmail(pwaApplicationDetail, requestingPerson);

  }


  @VisibleForTesting
  void createApplicationUpdateRequest(PwaApplicationDetail pwaApplicationDetail,
                                      Person requestingPerson,
                                      String requestReason) {
    var updateRequest = ApplicationUpdateRequest.createRequest(
        pwaApplicationDetail,
        requestingPerson,
        clock,
        requestReason);

    applicationUpdateRequestRepository.save(updateRequest);

  }

  @VisibleForTesting
  void sendApplicationUpdateRequestedEmail(PwaApplicationDetail pwaApplicationDetail, Person requestingPerson) {
    var recipients = pwaContactService.getPeopleInRoleForPwaApplication(
        pwaApplicationDetail.getPwaApplication(),
        PwaContactRole.PREPARER
    );

    if (!recipients.isEmpty()) {
      recipients.forEach(person ->
          notifyService.sendEmail(
              new ApplicationUpdateRequestEmailProps(
                  person.getFullName(),
                  pwaApplicationDetail.getPwaApplicationRef(),
                  requestingPerson.getFullName()
              ),
              person.getEmailAddress()
          )
      );

    } else {
      LOGGER.error(
          "Tried to send application update request email, but no recipients found. pwaApplication.id:" +
              pwaApplicationDetail.getMasterPwaApplicationId()
      );
    }

  }


  // TODO PWA-161 implements submission of app updates and therefore should determine how update requests go from opened to closed.
  public boolean applicationDetailHasOpenUpdateRequest(PwaApplicationDetail pwaApplicationDetail) {
    return applicationUpdateRequestRepository.existsByPwaApplicationDetail(pwaApplicationDetail);
  }
}
