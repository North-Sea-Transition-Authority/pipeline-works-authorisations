package uk.co.ogauthority.pwa.service.asbuilt.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.summary.controller.ApplicationSummaryController;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentTestUtil;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@ExtendWith(MockitoExtension.class)
class AsBuiltNotificationSummaryServiceTest {

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

  @BeforeEach
  void setup() {
    asBuiltNotificationSummaryService = new AsBuiltNotificationSummaryService(pwaHolderService);

    when(pwaHolderService.getPwaHolderOrgGroups(applicationDetail.getMasterPwa())).thenReturn(Set.of(portalOrganisationGroup));
  }

  @Test
  void getAsBuiltNotificationGroupSummaryView_expectedView() {
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
