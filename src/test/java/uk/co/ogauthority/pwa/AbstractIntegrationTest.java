package uk.co.ogauthority.pwa;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.fivium.energyportal.accounts.starter.EnergyPortalServiceAccessService;

@ActiveProfiles("integration-test")
public abstract class AbstractIntegrationTest {

    @MockitoBean
    private EnergyPortalServiceAccessService energyPortalServiceAccessService;
}
