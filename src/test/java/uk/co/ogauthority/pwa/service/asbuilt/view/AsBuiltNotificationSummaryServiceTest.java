package uk.co.ogauthority.pwa.service.asbuilt.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.controller.appsummary.ApplicationSummaryController;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentTestUtil;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltNotificationSummaryServiceTest {

  private AsBuiltNotificationSummaryService asBuiltNotificationSummaryService;

  @Mock
  private PwaHolderService pwaHolderService;

  private final Person person = PersonTestUtil.createDefaultPerson();
  private final PwaApplicationDetail applicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 5);
  private final PwaConsent pwaConsent = PwaConsentTestUtil
      .createPwaConsentWithCompleteApplicationDetail(40, "CONSENT_REF", Instant.now(), applicationDetail);
  private final AsBuiltNotificationGroup asBuiltNotificationGroup = new AsBuiltNotificationGroup(pwaConsent, "APP_REF", Instant.now());
  private final AsBuiltNotificationGroupDetail asBuiltNotificationGroupDetail = new AsBuiltNotificationGroupDetail(asBuiltNotificationGroup, LocalDate
      .now(), person.getId(), Instant.now());
  private final PortalOrganisationGroup portalOrganisationGroup = PortalOrganisationTestUtils.generateOrganisationGroup(50, "Org Group", "OG");

  @Before
  public void setup() {
    asBuiltNotificationSummaryService = new AsBuiltNotificationSummaryService(pwaHolderService);

    when(pwaHolderService.getPwaHolders(applicationDetail.getMasterPwa())).thenReturn(Set.of(portalOrganisationGroup));
  }

  @Test
  public void getAsBuiltNotificationGroupSummaryView_expectedView() {
    var asBuiltSummaryView = asBuiltNotificationSummaryService
        .getAsBuiltNotificationGroupSummaryView(asBuiltNotificationGroupDetail);

    assertThat(asBuiltSummaryView.getConsentReference()).isEqualTo(pwaConsent.getReference());
    assertThat(asBuiltSummaryView.getAppReference()).isEqualTo(asBuiltNotificationGroup.getReference());
    assertThat(asBuiltSummaryView.getAsBuiltDeadline()).isEqualTo(DateUtils.formatDate(asBuiltNotificationGroupDetail.getDeadlineDate()));
    assertThat(asBuiltSummaryView.getHolder()).isEqualTo(portalOrganisationGroup.getName());
    assertThat(asBuiltSummaryView.getApplicationTypeDisplay()).isEqualTo(applicationDetail.getPwaApplication().getApplicationType().getDisplayName());
    assertThat(asBuiltSummaryView.getAccessLink()).isEqualTo(ReverseRouter.route(on(ApplicationSummaryController.class)
        .renderSummary(applicationDetail.getPwaApplication().getId(), applicationDetail.getPwaApplication().getApplicationType(),
            null, null, null, null)));
  }

}
