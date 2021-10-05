package uk.co.ogauthority.pwa.model.view;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;
import uk.co.ogauthority.pwa.service.search.consents.tabcontentviews.PwaPipelineView;

@RunWith(MockitoJUnitRunner.class)
public class PwaPipelineViewTest {


  @Test
  public void compareTo_comparisonExcludesAnyLetterPrefix_pipelineNumberNumericValuesCompared() {
    var pwaPipelineView1 = new PwaPipelineView(PipelineDetailTestUtil.createPipelineOverview("  PLU4353"));
    var pwaPipelineView2 = new PwaPipelineView(PipelineDetailTestUtil.createPipelineOverview("PL638   "));
    assertThat(pwaPipelineView1.compareTo(pwaPipelineView2)).isGreaterThan(0);
  }

  @Test
  public void compareTo_comparisonExcludesAnyLetterSuffix_pipelineNumberNumericValuesCompared() {
    var pwaPipelineView1 = new PwaPipelineView(PipelineDetailTestUtil.createPipelineOverview("4353RTD"));
    var pwaPipelineView2 = new PwaPipelineView(PipelineDetailTestUtil.createPipelineOverview("PL638NL"));
    assertThat(pwaPipelineView1.compareTo(pwaPipelineView2)).isGreaterThan(0);
  }

  @Test
  public void compareTo_comparisonIncludesDot_pipelineNumberNumericValuesCompared() {
    var pwaPipelineView1 = new PwaPipelineView(PipelineDetailTestUtil.createPipelineOverview("PL4353.65"));
    var pwaPipelineView2 = new PwaPipelineView(PipelineDetailTestUtil.createPipelineOverview("PL638.23"));
    assertThat(pwaPipelineView1.compareTo(pwaPipelineView2)).isGreaterThan(0);
  }

  @Test
  public void compareTo_numericValuesAreEqual_pipelineNumberNumberExcludingPrefixCompared() {
    var pwaPipelineView1 = new PwaPipelineView(PipelineDetailTestUtil.createPipelineOverview("PL1905(J)21/24-TU"));
    var pwaPipelineView2 = new PwaPipelineView(PipelineDetailTestUtil.createPipelineOverview("PL1905(J)21/24-T1"));
    assertThat(pwaPipelineView1.compareTo(pwaPipelineView2)).isGreaterThan(0);
  }

  @Test
  public void compareTo_pipelineNumericValueNotFound_defaultStringComparisonPerformed() {
    var pwaPipelineView1 = new PwaPipelineView(PipelineDetailTestUtil.createPipelineOverview("PLURTS"));
    var pwaPipelineView2 = new PwaPipelineView(PipelineDetailTestUtil.createPipelineOverview("PLNL"));
    assertThat(pwaPipelineView1.compareTo(pwaPipelineView2)).isGreaterThan(0);
  }


}