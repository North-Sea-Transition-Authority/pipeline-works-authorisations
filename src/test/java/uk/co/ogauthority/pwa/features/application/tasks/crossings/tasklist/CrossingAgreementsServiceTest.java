package uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.PadCableCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.BlockCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PadPipelineCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.types.CrossingTypesService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class CrossingAgreementsServiceTest {
  @Mock
  private PadMedianLineAgreementService padMedianLineAgreementService;

  @Mock
  private BlockCrossingService blockCrossingService;

  @Mock
  private PadCableCrossingService padCableCrossingService;

  @Mock
  private PadPipelineCrossingService padPipelineCrossingService;

  @Mock
  private CrossingTypesService crossingTypesService;

  @Mock
  private CrossingAgreementsTaskListService crossingAgreementsTaskListService;


  private CrossingAgreementsService crossingAgreementsService;

  private PwaApplicationDetail pwaApplicationDetail;


  @Before
  public void setup() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    crossingAgreementsService = new CrossingAgreementsService(
        padMedianLineAgreementService,
        blockCrossingService,
        padCableCrossingService,
        padPipelineCrossingService,
        crossingTypesService,
        crossingAgreementsTaskListService
    );
  }

  @Test
  public void getValidationResult_depcon_blocksComplete() {
    var application = new PwaApplication(null, PwaApplicationType.DEPOSIT_CONSENT, null);
    pwaApplicationDetail.setPwaApplication(application);

    when(blockCrossingService.isComplete(pwaApplicationDetail)).thenReturn(true);
    var validationResult =  crossingAgreementsService.getValidationResult(pwaApplicationDetail);
    assertThat(validationResult.isCrossingAgreementsValid()).isTrue();
  }

  @Test
  public void getValidationResult_depcon_blocksIncomplete() {
    var application = new PwaApplication(null, PwaApplicationType.DEPOSIT_CONSENT, null);
    pwaApplicationDetail.setPwaApplication(application);

    when(blockCrossingService.isComplete(pwaApplicationDetail)).thenReturn(false);
    var validationResult =  crossingAgreementsService.getValidationResult(pwaApplicationDetail);
    assertThat(validationResult.isCrossingAgreementsValid()).isFalse();
  }

  @Test
  public void getValidationResult_appTypesExceptDepcon_sectionsCrossedButIncomplete() {
    when(crossingTypesService.isComplete(pwaApplicationDetail)).thenReturn(false);
    var validationResult =  crossingAgreementsService.getValidationResult(pwaApplicationDetail);
    assertThat(validationResult.isCrossingAgreementsValid()).isFalse();
  }

  @Test
  public void getValidationResult_appTypesExceptDepcon_sectionsCrossedAndComplete() {
    when(crossingTypesService.isComplete(pwaApplicationDetail)).thenReturn(true);
    when(blockCrossingService.isComplete(pwaApplicationDetail)).thenReturn(true);
    when(padMedianLineAgreementService.isComplete(pwaApplicationDetail)).thenReturn(true);
    when(padCableCrossingService.isComplete(pwaApplicationDetail)).thenReturn(true);
    when(padPipelineCrossingService.isComplete(pwaApplicationDetail)).thenReturn(true);

    var validationResult =  crossingAgreementsService.getValidationResult(pwaApplicationDetail);
    assertThat(validationResult.isCrossingAgreementsValid()).isTrue();
  }



  @Test
  public void copySectionInformation_serviceInteractions_allConditionalCrossingsTasksShown() {
    var newDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 99, 99);
    pwaApplicationDetail.setCablesCrossed(true);
    pwaApplicationDetail.setPipelinesCrossed(true);
    pwaApplicationDetail.setMedianLineCrossed(true);

    crossingAgreementsService.copySectionInformation(pwaApplicationDetail, newDetail);

    verify(blockCrossingService, times(1)).copySectionInformation(pwaApplicationDetail, newDetail);
    verify(padCableCrossingService, times(1)).copySectionInformation(pwaApplicationDetail, newDetail);
    verify(padPipelineCrossingService, times(1)).copySectionInformation(pwaApplicationDetail, newDetail);
    verify(padMedianLineAgreementService, times(1)).copySectionInformation(pwaApplicationDetail, newDetail);
    verifyNoMoreInteractions(
        padMedianLineAgreementService,
        blockCrossingService,
        padCableCrossingService,
        padPipelineCrossingService,
        crossingTypesService,
        crossingAgreementsTaskListService
    );
  }

  @Test
  public void copySectionInformation_serviceInteractions_allConditionalCrossingsTasksHidden() {
    var newDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 99, 99);

    crossingAgreementsService.copySectionInformation(pwaApplicationDetail, newDetail);

    verify(blockCrossingService, times(1)).copySectionInformation(pwaApplicationDetail, newDetail);
    verifyNoMoreInteractions(
        padMedianLineAgreementService,
        blockCrossingService,
        padCableCrossingService,
        padPipelineCrossingService,
        crossingTypesService,
        crossingAgreementsTaskListService
    );

  }
}