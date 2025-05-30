package uk.co.ogauthority.pwa.features.application.creation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.service.masterpwas.ConsentedMasterPwaService;
import uk.co.ogauthority.pwa.service.masterpwas.NonConsentedPwaService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.Role;

@ExtendWith(MockitoExtension.class)
class PickedPwaRetrievalServiceTest {

  private static final int MASTER_PWA_ID = 1;

  @Mock
  private ConsentedMasterPwaService consentedMasterPwaService;

  @Mock
  private NonConsentedPwaService nonConsentedPwaService;

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;


  private WebUserAccount webUserAccount = new WebUserAccount(1);

  private PickedPwaRetrievalService pickedPwaRetrievalService;

  @BeforeEach
  void setup() {
    pickedPwaRetrievalService = new PickedPwaRetrievalService(
        consentedMasterPwaService,
        nonConsentedPwaService,
        pwaHolderTeamService);

  }

  @Test
  void getPickablePwaOptions_UserNotInOrganisation() {

    when(pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasAnyOrgRole(webUserAccount, Set.of(Role.APPLICATION_CREATOR)))
        .thenReturn(Collections.emptyList());

    var options = pickedPwaRetrievalService.getPickablePwaOptions(webUserAccount, PwaResourceType.PETROLEUM);
    assertThat(options.getConsentedPickablePwas()).isEmpty();
    assertThat(options.getNonconsentedPickablePwas()).isEmpty();
  }

  @Test
  void getPickablePwasWhereAuthorised_whenNoPickablePwasExist() {
    var options = pickedPwaRetrievalService.getPickablePwaOptions(webUserAccount, PwaResourceType.PETROLEUM);
    assertThat(options.getConsentedPickablePwas()).isEmpty();
    assertThat(options.getNonconsentedPickablePwas()).isEmpty();
    verify(consentedMasterPwaService, times(1)).getMasterPwaDetailsWhereAnyPortalOrgUnitsHolder(any());
  }

  @Test
  void getPickedConsentedPwa_whenUnknownPwaSource() {
    assertThrows(IllegalStateException.class, () -> {
      var masterPwa = pickedPwaRetrievalService.getPickedConsentedPwa(MASTER_PWA_ID, webUserAccount);
    });
  }

  @Test
  void getPickablePwaOptions_filterByResourceType_Petroleum() {
    var orgUnit = new PortalOrganisationUnit();

    when(pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasAnyOrgRole(webUserAccount, Set.of(Role.APPLICATION_CREATOR)))
        .thenReturn(List.of(orgUnit));
    when(consentedMasterPwaService.getMasterPwaDetailsWhereAnyPortalOrgUnitsHolder(List.of(orgUnit)))
        .thenReturn(getConsentedPwas());
    when(nonConsentedPwaService.getNonConsentedMasterPwaDetailByHolderOrgUnits(List.of(orgUnit)))
        .thenReturn(getUnConsentedPwas());

    var options = pickedPwaRetrievalService.getPickablePwaOptions(webUserAccount, PwaResourceType.PETROLEUM);
    assertThat(options.getConsentedPickablePwas())
        .hasSize(1);
    assertThat(options.getConsentedPickablePwas().values())
        .contains("CONSENTED/PETROLEUM/1");

    assertThat(options.getNonconsentedPickablePwas())
        .hasSize(1);
    assertThat(options.getNonconsentedPickablePwas().values())
        .contains("UNCONSENTED/PETROLEUM/1");
  }

  @Test
  void getPickablePwaOptions_filterByResourceType_Hydrogen() {
    var orgUnit = new PortalOrganisationUnit();

    when(pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasAnyOrgRole(webUserAccount, Set.of(Role.APPLICATION_CREATOR)))
        .thenReturn(List.of(orgUnit));
    when(consentedMasterPwaService.getMasterPwaDetailsWhereAnyPortalOrgUnitsHolder(List.of(orgUnit)))
        .thenReturn(getConsentedPwas());
    when(nonConsentedPwaService.getNonConsentedMasterPwaDetailByHolderOrgUnits(List.of(orgUnit)))
        .thenReturn(getUnConsentedPwas());

    var options = pickedPwaRetrievalService.getPickablePwaOptions(webUserAccount, PwaResourceType.HYDROGEN);
    assertThat(options.getConsentedPickablePwas())
        .hasSize(1);
    assertThat(options.getConsentedPickablePwas().values())
        .contains("CONSENTED/HYDROGEN/2");

    assertThat(options.getNonconsentedPickablePwas())
        .hasSize(1);
    assertThat(options.getNonconsentedPickablePwas().values())
        .contains("UNCONSENTED/HYDROGEN/2");
  }

  private List<MasterPwaDetail> getConsentedPwas() {
    var masterPwa = new MasterPwa();
    masterPwa.setId(1000);

    var master1 = new MasterPwaDetail();
    master1.setMasterPwa(masterPwa);
    master1.setReference("CONSENTED/PETROLEUM/1");
    master1.setResourceType(PwaResourceType.PETROLEUM);

    var master2 = new MasterPwaDetail();
    master2.setMasterPwa(masterPwa);
    master2.setReference("CONSENTED/HYDROGEN/2");
    master2.setResourceType(PwaResourceType.HYDROGEN);

    return List.of(master1, master2);
  }

  private List<MasterPwaDetail> getUnConsentedPwas() {
    var masterPwa = new MasterPwa();
    masterPwa.setId(1000);

    var master1 = new MasterPwaDetail();
    master1.setMasterPwa(masterPwa);
    master1.setReference("UNCONSENTED/PETROLEUM/1");
    master1.setResourceType(PwaResourceType.PETROLEUM);

    var master2 = new MasterPwaDetail();
    master2.setMasterPwa(masterPwa);
    master2.setReference("UNCONSENTED/HYDROGEN/2");
    master2.setResourceType(PwaResourceType.HYDROGEN);

    return List.of(master1, master2);
  }
}
