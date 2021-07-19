package uk.co.ogauthority.pwa.service.testharness;


import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationCreationService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.FieldGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.PadHuooGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.pipelinegenerator.PipelineGeneratorService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


@RunWith(MockitoJUnitRunner.class)
public class GenerateApplicationServiceTest {


  @Mock
  private PwaApplicationCreationService pwaApplicationCreationService;

  @Mock
  private TestHarnessUserRetrievalService testHarnessUserRetrievalService;

  @Mock
  private FieldGeneratorService fieldGeneratorService;

  @Mock
  private PadHuooGeneratorService padHuooGeneratorService;

  @Mock
  private PipelineGeneratorService pipelineGeneratorService;

  private GenerateApplicationService generateApplicationService;

  @Before
  public void setup(){
    generateApplicationService = new GenerateApplicationService(
        pwaApplicationCreationService, testHarnessUserRetrievalService, fieldGeneratorService, padHuooGeneratorService, pipelineGeneratorService);
  }

  @Test
  public void generateInitialPwaApplication_verifyServiceInteractions()  {

    var applicantPersonId = 100;
    var pipelineQuantity = 5;
    var user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of());

    when(testHarnessUserRetrievalService.getWebUserAccount(applicantPersonId)).thenReturn(user);

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
    when(pwaApplicationCreationService.createInitialPwaApplication(user)).thenReturn(pwaApplicationDetail);

    generateApplicationService.generateInitialPwaApplication(pipelineQuantity, applicantPersonId);

    verify(fieldGeneratorService).generatePadFields(pwaApplicationDetail);
    verify(padHuooGeneratorService).generatePadOrgRoles(user, pwaApplicationDetail);
    verify(pipelineGeneratorService).generatePadPipelinesAndIdents(pwaApplicationDetail, pipelineQuantity);

  }


  
  
  
}
