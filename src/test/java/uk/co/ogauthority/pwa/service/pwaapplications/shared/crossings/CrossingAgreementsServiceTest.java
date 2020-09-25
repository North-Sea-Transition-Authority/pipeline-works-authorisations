package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PadPipelineCrossingService;
import uk.co.ogauthority.pwa.service.tasklist.CrossingAgreementsTaskListService;
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