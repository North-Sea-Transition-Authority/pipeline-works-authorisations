package uk.co.ogauthority.pwa;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.fivium.energyportal.starter.accounts.EnergyPortalServiceAccessService;
import uk.co.fivium.energyportalmessagequeue.sns.SnsService;
import uk.co.fivium.energyportalmessagequeue.sqs.SqsService;

@ActiveProfiles("integration-test")
public abstract class AbstractIntegrationTest {

    @MockitoBean
    private EnergyPortalServiceAccessService energyPortalServiceAccessService;

    @MockitoBean
    private SqsService sqsService;

    @MockitoBean
    private SnsService snsService;
}
