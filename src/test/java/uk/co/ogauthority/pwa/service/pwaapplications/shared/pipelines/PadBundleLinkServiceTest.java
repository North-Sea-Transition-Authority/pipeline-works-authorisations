package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
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
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadBundleLinkRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadBundleLinkServiceTest {

  @Mock
  private PadBundleLinkRepository padBundleLinkRepository;

  @Mock
  private PadPipelineService padPipelineService;

  private PadBundleLinkService padBundleLinkService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    padBundleLinkService = new PadBundleLinkService(padBundleLinkRepository, padPipelineService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  public void createBundleLinks() {

    var bundle = new PadBundle();
    bundle.setPwaApplicationDetail(pwaApplicationDetail);

    var form = new BundleForm();
    form.setPadPipelineIds(Set.of(1, 2));

    var pipeline = new PadPipeline();
    var pipeline2 = new PadPipeline();

    when(padPipelineService.getByIdList(pwaApplicationDetail, List.copyOf(form.getPadPipelineIds())))
        .thenReturn(List.of(pipeline, pipeline2));

    var listCapture = ArgumentCaptor.forClass(List.class);
    padBundleLinkService.createBundleLinks(bundle, form);

    verify(padBundleLinkRepository, times(1)).saveAll(listCapture.capture());

    assertThat((List<PadBundleLink>) listCapture.getValue()).extracting(PadBundleLink::getBundle, PadBundleLink::getPipeline)
        .containsExactly(
            tuple(bundle, pipeline),
            tuple(bundle, pipeline2)
        );

  }

  @Test
  public void getAllLinksForDetail_serviceInteraction() {
    padBundleLinkService.getAllLinksForDetail(pwaApplicationDetail);
    verify(padBundleLinkRepository, times(1))
        .getAllByBundle_PwaApplicationDetail(pwaApplicationDetail);
  }

  @Test
  public void removeBundleLinks_repositoryInteraction() {
    var bundle = new PadBundle();
    var link = new PadBundleLink();
    when(padBundleLinkRepository.getAllByBundle(bundle)).thenReturn(List.of(link));
    padBundleLinkService.removeBundleLinks(bundle);
    verify(padBundleLinkRepository, times(1)).deleteAll(List.of(link));
  }
}