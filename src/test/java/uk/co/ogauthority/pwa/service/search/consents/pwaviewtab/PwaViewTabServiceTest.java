package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.testutil.PwaViewTabTestUtil;
import uk.co.ogauthority.pwa.service.search.consents.tabcontentviews.PwaPipelineView;
import uk.co.ogauthority.pwa.service.search.consents.testutil.PwaContextTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaViewTabServiceTest {

  @Mock
  private PipelineDetailService pipelineDetailService;

  private PwaViewTabService pwaViewTabService;

  private PwaContext pwaContext;


  private final String PIPELINE_REF_ID1 = "PLU001";
  private final String PIPELINE_REF_ID2 = "PL002";



  @Before
  public void setUp() throws Exception {

    pwaViewTabService = new PwaViewTabService(pipelineDetailService);

    pwaContext = PwaContextTestUtil.createPwaContext();

  }


  @Test
  public void getTabContentModelMap_pipelinesTab_modelMapContainsPipelineViews_orderedByPipelineNumber() {

    var unOrderedPipelineOverviews = List.of(
        PwaViewTabTestUtil.createPipelineOverview(PIPELINE_REF_ID2), PwaViewTabTestUtil.createPipelineOverview(PIPELINE_REF_ID1));
    when(pipelineDetailService.getAllPipelineOverviewsForMasterPwa(pwaContext.getMasterPwa())).thenReturn(unOrderedPipelineOverviews);

    var modelMap = pwaViewTabService.getTabContentModelMap(pwaContext, PwaViewTab.PIPELINES);
    var actualPwaPipelineViews = (List<PwaPipelineView>) modelMap.get("pwaPipelineViews");
    assertThat(actualPwaPipelineViews).containsExactly(
        new PwaPipelineView(unOrderedPipelineOverviews.get(1)),
        new PwaPipelineView(unOrderedPipelineOverviews.get(0)));

  }


  @Test
  public void getTabContentModelMap_getPipelineNumberOnlyFromReference_refPrependedWithPLChars_charsRemoved() {
    var pwaPipelineView = new PwaPipelineView(PwaViewTabTestUtil.createPipelineOverview(PIPELINE_REF_ID2));
    assertThat(pwaPipelineView.getPipelineNumberOnlyFromReference()).isEqualTo("002");
  }

  @Test
  public void getTabContentModelMap_getPipelineNumberOnlyFromReference_refPrependedWithPLUChars_charsRemoved() {
    var pwaPipelineView = new PwaPipelineView(PwaViewTabTestUtil.createPipelineOverview(PIPELINE_REF_ID1));
    assertThat(pwaPipelineView.getPipelineNumberOnlyFromReference()).isEqualTo("001");
  }

  @Test
  public void getTabContentModelMap_getPipelineNumberOnlyFromReference_refPrependedWithWhitespace_whitespaceRemoved() {
    var pwaPipelineView = new PwaPipelineView(PwaViewTabTestUtil.createPipelineOverview("  001"));
    assertThat(pwaPipelineView.getPipelineNumberOnlyFromReference()).isEqualTo("001");
  }





}