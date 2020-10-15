package uk.co.ogauthority.pwa.service.applicationsummariser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.rendering.TemplateRenderingService;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationSummaryViewServiceTest {

  @Mock
  private ApplicationSummaryService applicationSummaryService;

  @Mock
  private TemplateRenderingService templateRenderingService;

  private ApplicationSummaryViewService applicationSummaryViewService;

  private PwaApplicationDetail detail;

  @Before
  public void setUp() {
    applicationSummaryViewService = new ApplicationSummaryViewService(applicationSummaryService, templateRenderingService);
    detail = new PwaApplicationDetail();
  }

  @Test
  public void getApplicationSummaryView_usingDetail() {

    when(applicationSummaryService.summarise(detail)).thenReturn(List.of(
        new ApplicationSectionSummary("test", List.of(SidebarSectionLink.createAnchorLink("text", "#")), Map.of("test", "1")),
        new ApplicationSectionSummary("test2", List.of(SidebarSectionLink.createAnchorLink("text2", "#")), Map.of("test", "2"))
    ));

    when(templateRenderingService.render(any(), any(), anyBoolean())).thenReturn("FAKE");

    var appSummaryView = applicationSummaryViewService.getApplicationSummaryView(detail);

    assertThat(appSummaryView.getSummaryHtml()).isEqualTo("FAKEFAKE");
    assertThat(appSummaryView.getSidebarSectionLinks())
        .extracting(SidebarSectionLink::getDisplayText)
        .containsExactly("text", "text2");


  }

}
