package uk.co.ogauthority.pwa.service.appprocessing.processingcharges;

import static java.util.stream.Collectors.toList;

import java.time.Clock;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequest;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestItem;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.PwaAppChargeRequestDetailRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.PwaAppChargeRequestItemRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.PwaAppChargeRequestRepository;

/**
 * Creates and reports on charges levied for applications.
 */
@Service
public class ApplicationChargeRequestService {

  private final PwaAppChargeRequestRepository pwaAppChargeRequestRepository;
  private final PwaAppChargeRequestDetailRepository pwaAppChargeRequestDetailRepository;
  private final PwaAppChargeRequestItemRepository pwaAppChargeRequestItemRepository;
  private final Clock clock;

  @Autowired
  public ApplicationChargeRequestService(PwaAppChargeRequestRepository pwaAppChargeRequestRepository,
                                         PwaAppChargeRequestDetailRepository pwaAppChargeRequestDetailRepository,
                                         PwaAppChargeRequestItemRepository pwaAppChargeRequestItemRepository,
                                         @Qualifier("utcClock") Clock clock) {
    this.pwaAppChargeRequestRepository = pwaAppChargeRequestRepository;
    this.pwaAppChargeRequestDetailRepository = pwaAppChargeRequestDetailRepository;
    this.pwaAppChargeRequestItemRepository = pwaAppChargeRequestItemRepository;
    this.clock = clock;
  }

  @Transactional
  public void createPwaAppChargeRequest(Person requesterPerson,
                                        ApplicationChargeRequestSpecification applicationChargeRequestSpecification) {
    validateAppChargeSpec(applicationChargeRequestSpecification);

    var chargeRequest = createAndSavePwaAppChargeRequestFromSpec(requesterPerson,
        applicationChargeRequestSpecification);

    createAndSaveTipChargeRequestDetailFromSpec(chargeRequest, applicationChargeRequestSpecification);

    var chargeItems = applicationChargeRequestSpecification.getApplicationChargeItems().stream()
        .map(applicationChargeItem -> new PwaAppChargeRequestItem(
            chargeRequest,
            applicationChargeItem.getDescription(),
            applicationChargeItem.getPennyAmount()
        ))
        .collect(toList());
    pwaAppChargeRequestItemRepository.saveAll(chargeItems);

  }

  private void createAndSaveTipChargeRequestDetailFromSpec(PwaAppChargeRequest pwaAppChargeRequest,
                                                           ApplicationChargeRequestSpecification applicationChargeRequestSpecification) {
    var detail = new PwaAppChargeRequestDetail(pwaAppChargeRequest);
    detail.setTipFlag(true);
    detail.setChargeSummary(applicationChargeRequestSpecification.getChargeSummary());
    detail.setTotalPennies(applicationChargeRequestSpecification.getTotalPennies());
    detail.setStatus(applicationChargeRequestSpecification.getPwaAppChargeRequestStatus());
    detail.setChargeWaivedReason(applicationChargeRequestSpecification.getChargeWaivedReason());
    detail.setAutoCaseOfficerPersonId(applicationChargeRequestSpecification.getOnPaymentCompleteCaseOfficerPersonId());

    pwaAppChargeRequestDetailRepository.save(detail);
  }

  private PwaAppChargeRequest createAndSavePwaAppChargeRequestFromSpec(Person requesterPerson,
                                                                       ApplicationChargeRequestSpecification chargeRequestSpecification) {
    var chargeRequest = new PwaAppChargeRequest();
    chargeRequest.setPwaApplication(chargeRequestSpecification.getPwaApplication());
    chargeRequest.setRequestedByPersonId(requesterPerson.getId());
    chargeRequest.setRequestedByTimestamp(clock.instant());
    return pwaAppChargeRequestRepository.save(chargeRequest);
  }

  private void validateAppChargeSpec(ApplicationChargeRequestSpecification chargeRequestSpecification) {

    if (chargeRequestSpecification.getTotalPennies() == null || chargeRequestSpecification.getTotalPennies() < 0) {
      throw new UnsupportedOperationException("Cannot create charge request for value less than 0");
    }

    if (chargeRequestSpecification.getApplicationChargeItems()
        .stream()
        .anyMatch(applicationChargeItem -> applicationChargeItem.getPennyAmount() < 0)) {
      throw new UnsupportedOperationException("Cannot create charge request negative penny charge item");
    }

    if (StringUtils.isBlank(chargeRequestSpecification.getChargeSummary())) {
      throw new UnsupportedOperationException("Cannot create charge request with blank summary");
    }


    if (chargeRequestSpecification.getApplicationChargeItems().isEmpty()) {
      throw new UnsupportedOperationException("Cannot create charge request empty charge items");
    }

    if (PwaAppChargeRequestStatus.WAIVED.equals(chargeRequestSpecification.getPwaAppChargeRequestStatus())
        && StringUtils.isBlank(chargeRequestSpecification.getChargeWaivedReason())) {
      throw new UnsupportedOperationException("Cannot create WAIVED charge request with no reason provided");
    }

    if (!PwaAppChargeRequestStatus.WAIVED.equals(chargeRequestSpecification.getPwaAppChargeRequestStatus())
        && !StringUtils.isBlank(chargeRequestSpecification.getChargeWaivedReason())) {
      throw new UnsupportedOperationException("Cannot create non-waived charge request with waived reason provided");
    }

  }

}

