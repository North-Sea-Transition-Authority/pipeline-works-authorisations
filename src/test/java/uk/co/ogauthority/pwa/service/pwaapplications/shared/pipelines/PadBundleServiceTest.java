package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundle;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundleLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.BundleForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadBundleRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadBundleServiceTest {

  @Mock
  private PadBundleRepository padBundleRepository;

  @Mock
  private PadBundleLinkService padBundleLinkService;

  @Mock
  private PadPipelineService padPipelineService;

  private PadBundleService padBundleService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    padBundleService = new PadBundleService(padBundleRepository, padBundleLinkService, padPipelineService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  public void createBundleAndLinks_serviceInteraction() {
    var form = new BundleForm();
    form.setBundleName("bundle");
    form.setPipelineIds(List.of(1));

    padBundleService.createBundleAndLinks(pwaApplicationDetail, form);

    var bundleCaptor = ArgumentCaptor.forClass(PadBundle.class);
    verify(padBundleRepository, times(1)).save(bundleCaptor.capture());
    assertThat(bundleCaptor.getValue()).extracting(PadBundle::getBundleName, PadBundle::getPwaApplicationDetail)
        .containsExactly("bundle", pwaApplicationDetail);

    verify(padBundleLinkService, times(1)).createBundleLinks(bundleCaptor.getValue(), form);
  }

  @Test
  public void canAddBundle_twoPipelines() {
    when(padPipelineService.getTotalPipelinesContainedInApplication(pwaApplicationDetail)).thenReturn(2L);
    assertThat(padBundleService.canAddBundle(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canAddBundle_notEnoughPipelines() {
    when(padPipelineService.getTotalPipelinesContainedInApplication(pwaApplicationDetail)).thenReturn(1L);
    assertThat(padBundleService.canAddBundle(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void getBundleViews() {
    var bundle = new PadBundle();
    bundle.setBundleName("name");

    var pipeline = new PadPipeline();
    pipeline.setPipelineRef("ref");

    var link = new PadBundleLink();
    link.setBundle(bundle);
    link.setPipeline(pipeline);

    var linkList = List.of(link);

    when(padBundleLinkService.getAllLinksForDetail(pwaApplicationDetail)).thenReturn(linkList);
    var results = padBundleService.getBundleViews(pwaApplicationDetail);
    assertThat(results).extracting(PadBundleView::getBundle, PadBundleView::getLinks)
        .containsExactlyInAnyOrder(tuple(bundle, linkList));
  }

  @Test
  public void getBundleSummaryViews() {
    var bundle = new PadBundle();
    bundle.setBundleName("name");

    var pipeline = new PadPipeline();
    pipeline.setPipelineRef("ref");

    var link = new PadBundleLink();
    link.setBundle(bundle);
    link.setPipeline(pipeline);

    when(padBundleLinkService.getAllLinksForDetail(pwaApplicationDetail)).thenReturn(List.of(link));
    var results = padBundleService.getBundleSummaryViews(pwaApplicationDetail);
    assertThat(results).extracting(PadBundleSummaryView::getBundleName, PadBundleSummaryView::getPipelineReferences)
        .containsExactlyInAnyOrder(tuple("name", List.of("ref")));
  }
}