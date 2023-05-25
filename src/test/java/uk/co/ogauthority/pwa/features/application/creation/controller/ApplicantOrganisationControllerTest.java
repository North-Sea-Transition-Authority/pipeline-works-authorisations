package uk.co.ogauthority.pwa.features.application.creation.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.creation.ApplicantOrganisationFormValidator;
import uk.co.ogauthority.pwa.features.application.creation.ApplicantOrganisationService;
import uk.co.ogauthority.pwa.features.application.creation.PwaApplicationCreationService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ApplicantOrganisationController.class)
@Import(PwaMvcTestConfiguration.class)
public class ApplicantOrganisationControllerTest extends AbstractControllerTest {

  private static final EnumSet<PwaApplicationType> RELEVANT_APP_TYPES = EnumSet.of(
      PwaApplicationType.CAT_1_VARIATION,
      PwaApplicationType.CAT_2_VARIATION,
      PwaApplicationType.HUOO_VARIATION,
      PwaApplicationType.DEPOSIT_CONSENT,
      PwaApplicationType.OPTIONS_VARIATION,
      PwaApplicationType.DECOMMISSIONING
  );
  private static final int MASTER_PWA_ID = 1;

  @MockBean
  private ApplicantOrganisationService applicantOrganisationService;

  @MockBean
  private PwaApplicationCreationService pwaApplicationCreationService;

  @MockBean
  private MasterPwaService masterPwaService;

  @SpyBean
  private ApplicantOrganisationFormValidator applicantOrganisationFormValidator;

  @MockBean
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @MockBean
  private PwaHolderTeamService pwaHolderTeamService;

  private final AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(123),
      Set.of(PwaUserPrivilege.PWA_APPLICATION_CREATE));

  private MasterPwa masterPwa;

  private final PortalOrganisationUnit applicantOrganisation = PortalOrganisationTestUtils.generateOrganisationUnit(1, "ACME");

  private PwaApplication pwaApplication;

  private PwaApplicationDetail pwaApplicationDetail;
  private MasterPwaDetail masterPwaDetail;

  @Before
  public void setup() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    masterPwa = pwaApplication.getMasterPwa();

    masterPwaDetail = new MasterPwaDetail();
    masterPwaDetail.setReference("MYREF");

    // fake create application service so we get an app of the requested type back
    when(pwaApplicationCreationService.createVariationPwaApplication(any(), any(), any(), any(), any())).thenAnswer(invocation -> {
          PwaApplicationType appType = Arrays.stream(invocation.getArguments())
              .filter(arg -> arg instanceof PwaApplicationType)
              .map(o -> (PwaApplicationType) o)
              .findFirst().orElse(null);
         pwaApplication.setApplicationType(appType);
         return pwaApplicationDetail;
        }
    );

    when(masterPwaService.getMasterPwaById(MASTER_PWA_ID)).thenReturn(masterPwa);
    when(masterPwaService.getCurrentDetailOrThrow(masterPwa)).thenReturn(masterPwaDetail);
    when(portalOrganisationsAccessor.getOrganisationUnitById(applicantOrganisation.getOuId())).thenReturn(Optional.of(applicantOrganisation));
    when(applicantOrganisationService.getPotentialApplicantOrganisations(any(), any())).thenReturn(Set.of(applicantOrganisation));

  }

  @Test
  public void renderSelectOrganisation_onlySupportedTypesGetOkStatus() throws Exception {

    for (PwaApplicationType appType : PwaApplicationType.values()) {
      ResultMatcher expectedStatus = RELEVANT_APP_TYPES.contains(appType) ? status().isOk() : status().isForbidden();
      try {
        mockMvc.perform(
            get(ReverseRouter.route(on(ApplicantOrganisationController.class)
                .renderSelectOrganisation(MASTER_PWA_ID, appType, null, null)
            )).with(authenticatedUserAndSession(user))
                .with(csrf()))
            .andExpect(expectedStatus);
      } catch (AssertionError e) {
        throw new AssertionError("Failed! appType:" + appType + " Message:" + e.getMessage(), e);
      }
    }

  }

  @Test
  public void renderSelectOrganisation_noAcceptableOrgs_denied() throws Exception {

    when(applicantOrganisationService.getPotentialApplicantOrganisations(any(), any())).thenReturn(Set.of());

    mockMvc.perform(
            get(ReverseRouter.route(on(ApplicantOrganisationController.class)
                .renderSelectOrganisation(MASTER_PWA_ID, PwaApplicationType.CAT_1_VARIATION, null, null)
            )).with(authenticatedUserAndSession(user))
                .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  public void selectOrganisation_urlAppTypeCheck() throws Exception {
    for (PwaApplicationType appType : PwaApplicationType.values()) {
      ResultMatcher expectedStatus = RELEVANT_APP_TYPES.contains(appType) ? status().is3xxRedirection() : status().isForbidden();
      try {
        mockMvc.perform(post(ReverseRouter.route(on(ApplicantOrganisationController.class)
            .selectOrganisation(MASTER_PWA_ID, appType, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .param("applicantOrganisationOuId", String.valueOf(applicantOrganisation.getOuId())))
            .andExpect(expectedStatus);
      } catch (AssertionError e) {
        throw new AssertionError("Failed! appType:" + appType + " Message:" + e.getMessage(), e);
      }
    }

  }

  @Test
  public void selectOrganisation_noAcceptableOrgs_denied() throws Exception {

    when(applicantOrganisationService.getPotentialApplicantOrganisations(any(), any())).thenReturn(Set.of());

    mockMvc.perform(post(ReverseRouter.route(on(ApplicantOrganisationController.class)
            .selectOrganisation(MASTER_PWA_ID, PwaApplicationType.CAT_1_VARIATION, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .param("applicantOrganisationOuId", String.valueOf(applicantOrganisation.getOuId())))
        .andExpect(status().isForbidden());

  }

  @Test
  public void selectOrganisation_success_appCreated() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ApplicantOrganisationController.class)
            .selectOrganisation(MASTER_PWA_ID, PwaApplicationType.CAT_1_VARIATION, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .param("applicantOrganisationOuId", String.valueOf(applicantOrganisation.getOuId())))
        .andExpect(status().is3xxRedirection());

    verify(pwaApplicationCreationService, times(1))
        .createVariationPwaApplication(masterPwa, PwaApplicationType.CAT_1_VARIATION, PwaResourceType.PETROLEUM, applicantOrganisation, user);

  }

  @Test
  public void selectOrganisation_fail_noAppCreated() throws Exception {

    ControllerTestUtils.mockSmartValidatorErrors(applicantOrganisationFormValidator, List.of("applicantOrganisationOuId"));

    mockMvc.perform(post(ReverseRouter.route(on(ApplicantOrganisationController.class)
            .selectOrganisation(MASTER_PWA_ID, PwaApplicationType.CAT_1_VARIATION, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .param("applicantOrganisationOuId", String.valueOf(applicantOrganisation.getOuId())))
        .andExpect(status().isOk());

    verifyNoInteractions(pwaApplicationCreationService);

  }

}
