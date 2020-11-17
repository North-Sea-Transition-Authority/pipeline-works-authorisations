package uk.co.ogauthority.pwa.controller.masterpwas.contacts;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.AddPwaContactFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PwaContactController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PwaContactControllerTest extends PwaApplicationContextAbstractControllerTest {

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private TeamManagementService teamManagementService;

  @MockBean
  private AddPwaContactFormValidator addPwaContactFormValidator;

  @MockBean
  private PadOrganisationRoleService padOrganisationRoleService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), Set.of());

  private PwaApplicationDetail detail;

  @Before
  public void setUp() {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(pwaApplicationDetailService.withDraftTipDetail(any(), any(), any())).thenCallRealMethod();
    when(pwaApplicationDetailService.getTipDetailWithStatus(any(), any())).thenReturn(detail);

  }

  @Test
  public void renderContactsScreen_holderNamesNotDuplicated() throws Exception {

    var orgGroup = PortalOrganisationTestUtils.generateOrganisationGroup(1, "ORGGRP", "OG");

    // two org units, both with same group
    var orgUnit1 = PortalOrganisationTestUtils.generateOrganisationUnit(2, "OU1", orgGroup);
    var orgUnit2 = PortalOrganisationTestUtils.generateOrganisationUnit(3, "OU2", orgGroup);

    var role1 = new PadOrganisationRole();
    role1.setOrganisationUnit(orgUnit1);

    var role2 = new PadOrganisationRole();
    role2.setOrganisationUnit(orgUnit2);

    when(padOrganisationRoleService.getOrgRolesForDetailAndRole(detail, HuooRole.HOLDER)).thenReturn(List.of(role1, role2));

    mockMvc.perform(get(ReverseRouter.route(on(PwaContactController.class)
        .renderContactsScreen(PwaApplicationType.INITIAL, 1, null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("orgGroupHolders", Set.of("ORGGRP")));

  }

}
