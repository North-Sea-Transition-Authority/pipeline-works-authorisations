package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequest;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestItem;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.PwaAppChargeRequestDetailRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.PwaAppChargeRequestItemRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.PwaAppChargeRequestRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationChargeRequestServiceTest {

  @Mock
  private PwaAppChargeRequestRepository pwaAppChargeRequestRepository;

  @Mock
  private PwaAppChargeRequestDetailRepository pwaAppChargeRequestDetailRepository;

  @Mock
  private PwaAppChargeRequestItemRepository pwaAppChargeRequestItemRepository;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private ApplicationChargeRequestService applicationChargeRequestService;

  private Person requestPerson;
  private Person caseOfficerPerson;

  private PwaApplication pwaApplication;

  @Before
  public void setUp() throws Exception {

    requestPerson = PersonTestUtil.createPersonFrom(new PersonId(10));
    caseOfficerPerson = PersonTestUtil.createPersonFrom(new PersonId(20));

    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL)
        .getPwaApplication();

    applicationChargeRequestService = new ApplicationChargeRequestService(
        pwaAppChargeRequestRepository,
        pwaAppChargeRequestDetailRepository,
        pwaAppChargeRequestItemRepository,
        clock
    );

    when(pwaAppChargeRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(pwaAppChargeRequestDetailRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

  }

  @Test
  public void createPwaAppChargeRequest_withValidSpec_createsExpectedData() {

    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.OPEN)
        .setChargeSummary("CHARGE_SUMMARY")
        .setTotalPennies(100)
        .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
        .addChargeItem("CHARGE_1", 25)
        .addChargeItem("CHARGE_2", 75);

    applicationChargeRequestService.createPwaAppChargeRequest(requestPerson, spec);

    ArgumentCaptor<List<PwaAppChargeRequestItem>> chargeItemCaptor = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<PwaAppChargeRequest> chargeRequestCaptor = ArgumentCaptor.forClass(PwaAppChargeRequest.class);
    ArgumentCaptor<PwaAppChargeRequestDetail> chargeRequestDetailCaptor = ArgumentCaptor.forClass(PwaAppChargeRequestDetail.class);

    InOrder inOrder = Mockito.inOrder(
        pwaAppChargeRequestRepository,
        pwaAppChargeRequestDetailRepository,
        pwaAppChargeRequestItemRepository);

    inOrder.verify(pwaAppChargeRequestRepository, times(1)).save(chargeRequestCaptor.capture());
    inOrder.verify(pwaAppChargeRequestDetailRepository, times(1)).save(chargeRequestDetailCaptor.capture());
    inOrder.verify(pwaAppChargeRequestItemRepository, times(1)).saveAll(chargeItemCaptor.capture());
    inOrder.verifyNoMoreInteractions();

    assertThat(chargeRequestCaptor.getValue()).satisfies(pwaAppChargeRequest -> {
      assertThat(pwaAppChargeRequest.getPwaApplication()).isEqualTo(pwaApplication);
      assertThat(pwaAppChargeRequest.getRequestedByTimestamp()).isEqualTo(clock.instant());
      assertThat(pwaAppChargeRequest.getRequestedByPersonId()).isEqualTo(requestPerson.getId());
    });

    assertThat(chargeRequestDetailCaptor.getValue()).satisfies(pwaAppChargeRequestDetail -> {
      assertThat(pwaAppChargeRequestDetail.getPwaAppChargeRequest()).isEqualTo(chargeRequestCaptor.getValue());
      assertThat(pwaAppChargeRequestDetail.getStartedTimestamp()).isEqualTo(clock.instant());
      assertThat(pwaAppChargeRequestDetail.getStartedByPersonId()).isEqualTo(requestPerson.getId());
      assertThat(pwaAppChargeRequestDetail.getEndedTimestamp()).isNull();
      assertThat(pwaAppChargeRequestDetail.getEndedByPersonId()).isNull();
      assertThat(pwaAppChargeRequestDetail.getTipFlag()).isTrue();
      assertThat(pwaAppChargeRequestDetail.getTotalPennies()).isEqualTo(spec.getTotalPennies());
      assertThat(pwaAppChargeRequestDetail.getChargeSummary()).isEqualTo(spec.getChargeSummary());
      assertThat(pwaAppChargeRequestDetail.getAutoCaseOfficerPersonId()).isEqualTo(spec.getOnPaymentCompleteCaseOfficerPersonId());
      assertThat(pwaAppChargeRequestDetail.getStatus()).isEqualTo(spec.getPwaAppChargeRequestStatus());
      assertThat(pwaAppChargeRequestDetail.getChargeWaivedReason()).isNull();
    });


    assertThat(chargeItemCaptor.getValue())
        .isNotEmpty()
        .anySatisfy(pwaAppChargeRequestItem -> {
          assertThat(pwaAppChargeRequestItem.getPwaAppChargeRequest()).isEqualTo(chargeRequestCaptor.getValue());
          assertThat(pwaAppChargeRequestItem.getPennyAmount()).isEqualTo(25);
          assertThat(pwaAppChargeRequestItem.getDescription()).isEqualTo("CHARGE_1");
        })
        .anySatisfy(pwaAppChargeRequestItem -> {
          assertThat(pwaAppChargeRequestItem.getPwaAppChargeRequest()).isEqualTo(chargeRequestCaptor.getValue());
          assertThat(pwaAppChargeRequestItem.getPennyAmount()).isEqualTo(75);
          assertThat(pwaAppChargeRequestItem.getDescription()).isEqualTo("CHARGE_2");
        });

  }


  @Test
  public void createPwaAppChargeRequest_withValidSpec_andWaivedStatus_setsExpectedData() {

    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.WAIVED)
        .setChargeWaivedReason("WAIVED_REASON")
        .setChargeSummary("CHARGE_SUMMARY")
        .setTotalPennies(0)
        .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
        .addChargeItem("CHARGE_1", 25)
        .addChargeItem("CHARGE_2", 75);

    applicationChargeRequestService.createPwaAppChargeRequest(requestPerson, spec);

    ArgumentCaptor<PwaAppChargeRequest> chargeRequestCaptor = ArgumentCaptor.forClass(PwaAppChargeRequest.class);
    ArgumentCaptor<PwaAppChargeRequestDetail> chargeRequestDetailCaptor = ArgumentCaptor.forClass(
        PwaAppChargeRequestDetail.class);

    verify(pwaAppChargeRequestDetailRepository, times(1)).save(chargeRequestDetailCaptor.capture());

    assertThat(chargeRequestDetailCaptor.getValue()).satisfies(pwaAppChargeRequestDetail -> {

      assertThat(pwaAppChargeRequestDetail.getTotalPennies()).isEqualTo(spec.getTotalPennies());
      assertThat(pwaAppChargeRequestDetail.getStatus()).isEqualTo(spec.getPwaAppChargeRequestStatus());
      assertThat(pwaAppChargeRequestDetail.getChargeWaivedReason()).isEqualTo(spec.getChargeWaivedReason());
    });
  }

  @Test(expected = UnsupportedOperationException.class)
  public void createPwaAppChargeRequest_whenNotWaived_andWaivedReasonProvided() {

    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.OPEN)
        .setChargeWaivedReason("WAIVED_REASON")
        .setChargeSummary("CHARGE_SUMMARY")
        .setTotalPennies(100)
        .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
        .addChargeItem("CHARGE_1", 25);

    applicationChargeRequestService.createPwaAppChargeRequest(requestPerson, spec);

  }

  @Test(expected = UnsupportedOperationException.class)
  public void createPwaAppChargeRequest_whenWaived_andWaivedNotReasonProvided() {

    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.WAIVED)
        .setChargeSummary("CHARGE_SUMMARY")
        .setTotalPennies(100)
        .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
        .addChargeItem("CHARGE_1", 25);

    applicationChargeRequestService.createPwaAppChargeRequest(requestPerson, spec);

  }

  @Test(expected = UnsupportedOperationException.class)
  public void createPwaAppChargeRequest_whenNoChargeItems() {

    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.WAIVED)
        .setChargeSummary("CHARGE_SUMMARY")
        .setTotalPennies(100)
        .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId());

    applicationChargeRequestService.createPwaAppChargeRequest(requestPerson, spec);

  }

  @Test(expected = UnsupportedOperationException.class)
  public void createPwaAppChargeRequest_whenChargeItems_negativePennyAmount() {

    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.WAIVED)
        .setChargeSummary("CHARGE_SUMMARY")
        .setTotalPennies(100)
        .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
        .addChargeItem("CHARGE_1", -1);

    applicationChargeRequestService.createPwaAppChargeRequest(requestPerson, spec);

  }

  @Test(expected = UnsupportedOperationException.class)
  public void createPwaAppChargeRequest_whenMissingChargeSummary() {

    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.WAIVED)
        .setTotalPennies(100)
        .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
        .addChargeItem("CHARGE_1", 100);

    applicationChargeRequestService.createPwaAppChargeRequest(requestPerson, spec);

  }

  @Test(expected = UnsupportedOperationException.class)
  public void createPwaAppChargeRequest_whenMissingTotalPennies() {

    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.WAIVED)
        .setChargeSummary("CHARGE_SUMMARY")
        .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
        .addChargeItem("CHARGE_1", 100);

    applicationChargeRequestService.createPwaAppChargeRequest(requestPerson, spec);

  }

  @Test(expected = UnsupportedOperationException.class)
  public void createPwaAppChargeRequest_whenNegativeTotalPennies() {

    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.WAIVED)
        .setChargeSummary("CHARGE_SUMMARY")
        .setTotalPennies(-10)
        .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
        .addChargeItem("CHARGE_1", 100);

    applicationChargeRequestService.createPwaAppChargeRequest(requestPerson, spec);

  }
}