package uk.co.ogauthority.pwa.domain.pwa.application.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.repository.PwaApplicationRepository;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PwaApplicationServiceTest {

  @Mock
  private PwaApplicationRepository pwaApplicationRepository;

  private PwaApplicationService pwaApplicationService;

  @Captor
  private ArgumentCaptor<PwaApplication> pwaApplicationArgumentCaptor;

  private PwaApplication pwaApplication;

  @BeforeEach
  void setUp() {
    pwaApplicationService = new PwaApplicationService(
        pwaApplicationRepository
    );

    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL).getPwaApplication();

  }

  @Test
  void getApplicationFromId_verifyServiceInteraction() {
    when(pwaApplicationRepository.findById(any())).thenReturn(Optional.of(pwaApplication));
    pwaApplicationService.getApplicationFromId(1);
    verify(pwaApplicationRepository, times(1)).findById(1);
  }


  @Test
  void getApplicationFromId_noApplicationFound() {
    when(pwaApplicationRepository.findById(any())).thenReturn(Optional.of(pwaApplication));
    when(pwaApplicationRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(PwaEntityNotFoundException.class, () ->
      pwaApplicationService.getApplicationFromId(1));

  }

  @Test
  void getAllApplicationsForMasterPwa() {
    when(pwaApplicationRepository.findAllByMasterPwa(pwaApplication.getMasterPwa())).thenReturn(List.of(pwaApplication));
    assertThat(pwaApplicationService.getAllApplicationsForMasterPwa(pwaApplication.getMasterPwa())).isEqualTo(List.of(pwaApplication));
  }

  @Test
  void updateApplicantOrganisationUnitId() {

    var orgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "Umbrella");

    pwaApplicationService.updateApplicantOrganisationUnitId(pwaApplication, orgUnit);

    verify(pwaApplicationRepository, times(1)).save(pwaApplicationArgumentCaptor.capture());

    assertThat(pwaApplicationArgumentCaptor.getValue().getApplicantOrganisationUnitId()).isEqualTo(OrganisationUnitId.from(orgUnit));

  }

}
