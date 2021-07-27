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
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.BlocksAndCrossingsGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.DesignOpConditionsGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.EnvAndDecomGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.FastTrackGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.FieldGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.FluidCompositionGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.GeneralTechDetailsGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.LocationDetailsGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.OtherPropertiesGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.PadHuooGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.PartnerLettersGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.PipelineHuooGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.PipelineSchematicsGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.ProjectInformationGeneratorService;
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
  private ProjectInformationGeneratorService projectInformationGeneratorService;
  @Mock
  private FastTrackGeneratorService fastTrackGeneratorService;
  @Mock
  private EnvAndDecomGeneratorService envAndDecomGeneratorService;
  @Mock
  private PadHuooGeneratorService padHuooGeneratorService;
  @Mock
  private PartnerLettersGeneratorService partnerLettersGeneratorService;
  @Mock
  private LocationDetailsGeneratorService locationDetailsGeneratorService;
  @Mock
  private BlocksAndCrossingsGeneratorService blocksAndCrossingsGeneratorService;
  @Mock
  private GeneralTechDetailsGeneratorService generalTechDetailsGeneratorService;
  @Mock
  private FluidCompositionGeneratorService fluidCompositionGeneratorService;
  @Mock
  private OtherPropertiesGeneratorService otherPropertiesGeneratorService;
  @Mock
  private DesignOpConditionsGeneratorService designOpConditionsGeneratorService;
  @Mock
  private PipelineGeneratorService pipelineGeneratorService;
  @Mock
  private PipelineSchematicsGeneratorService pipelineSchematicsGeneratorService;
  @Mock
  private PipelineHuooGeneratorService pipelineHuooGeneratorService;

  private GenerateApplicationService generateApplicationService;

  @Before
  public void setup(){
    generateApplicationService = new GenerateApplicationService(
        pwaApplicationCreationService, testHarnessUserRetrievalService, fieldGeneratorService,
        projectInformationGeneratorService, fastTrackGeneratorService, envAndDecomGeneratorService, padHuooGeneratorService,
        partnerLettersGeneratorService, locationDetailsGeneratorService, blocksAndCrossingsGeneratorService, generalTechDetailsGeneratorService,
        fluidCompositionGeneratorService, otherPropertiesGeneratorService, designOpConditionsGeneratorService, pipelineGeneratorService,
        pipelineSchematicsGeneratorService, pipelineHuooGeneratorService);
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
    verify(projectInformationGeneratorService).generateProjectInformation(user, pwaApplicationDetail);
    verify(fastTrackGeneratorService).generateFastTrack(pwaApplicationDetail);
    verify(envAndDecomGeneratorService).generateEnvAndDecom(pwaApplicationDetail);
    verify(padHuooGeneratorService).generatePadOrgRoles(user, pwaApplicationDetail);
    verify(partnerLettersGeneratorService).generatePartnerLetters(user, pwaApplicationDetail);
    verify(locationDetailsGeneratorService).generateLocationDetails(pwaApplicationDetail);
    verify(blocksAndCrossingsGeneratorService).generateBlocksAndCrossings(pwaApplicationDetail);
    verify(generalTechDetailsGeneratorService).generateGeneralTechDetails(pwaApplicationDetail);
    verify(fluidCompositionGeneratorService).generateFluidComposition(pwaApplicationDetail);
    verify(otherPropertiesGeneratorService).generateOtherProperties(pwaApplicationDetail);
    verify(designOpConditionsGeneratorService).generateDesignOpConditions(pwaApplicationDetail);
    verify(pipelineGeneratorService).generatePadPipelinesAndIdents(pwaApplicationDetail, pipelineQuantity);
    verify(pipelineSchematicsGeneratorService).generatePipelineSchematics(user, pwaApplicationDetail);
    verify(pipelineHuooGeneratorService).generatePipelineHuoos(pwaApplicationDetail);
  }


  
  
  
}
