package uk.co.ogauthority.pwa.features.application.tasks.crossings.cable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;

@RunWith(MockitoJUnitRunner.class)
public class PadCableCrossingServiceTest {

  @Mock
  private PadCableCrossingRepository padCableCrossingRepository;

  @Mock
  private CableCrossingFileService cableCrossingFileService;

  @Mock
  private EntityCopyingService entityCopyingService;

  @Mock
  private PadFileService padFileService;

  private PadCableCrossingService padCableCrossingService;
  private PwaApplicationDetail pwaApplicationDetail;
  private PadCableCrossing padCableCrossing;

  @Before
  public void setUp() {
    padCableCrossingService = new PadCableCrossingService(
        padCableCrossingRepository,
        cableCrossingFileService,
        entityCopyingService,
        padFileService
    );
    pwaApplicationDetail = new PwaApplicationDetail();
    padCableCrossing = new PadCableCrossing();
    padCableCrossing.setCableName("cableName");
    padCableCrossing.setCableOwner("cableOwner");
    padCableCrossing.setLocation("locationDetails");
  }

  @Test
  public void getCableCrossing() {
    when(padCableCrossingRepository.findByPwaApplicationDetailAndId(pwaApplicationDetail, 1))
        .thenReturn(Optional.of(padCableCrossing));
    var result = padCableCrossingService.getCableCrossing(pwaApplicationDetail, 1);
    assertThat(result).isEqualTo(padCableCrossing);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getCableCrossing_NotFound() {
    when(padCableCrossingRepository.findByPwaApplicationDetailAndId(pwaApplicationDetail, 1))
        .thenReturn(Optional.empty());
    padCableCrossingService.getCableCrossing(pwaApplicationDetail, 1);
  }

  @Test
  public void getCableCrossingViews() {
    when(padCableCrossingRepository.findAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padCableCrossing));
    var result = padCableCrossingService.getCableCrossingViews(pwaApplicationDetail);
    assertThat(result).extracting(CableCrossingView::getCableName, CableCrossingView::getOwner, CableCrossingView::getLocation)
        .containsExactly(Tuple.tuple("cableName", "cableOwner", "locationDetails"));
  }

  @Test
  public void setCrossingInformationFromForm() {
    var crossing = new PadCableCrossing();
    var form = new AddCableCrossingForm();
    form.setCableName("Name");
    form.setCableOwner("Owner");
    form.setLocation("Location");
    padCableCrossingService.setCrossingInformationFromForm(crossing, form);
    assertThat(crossing.getCableName()).isEqualTo(form.getCableName());
    assertThat(crossing.getCableOwner()).isEqualTo(form.getCableOwner());
    assertThat(crossing.getLocation()).isEqualTo(form.getLocation());
  }

  @Test
  public void createCableCrossing() {

    when(padCableCrossingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var form = new AddCableCrossingForm();
    form.setCableName("Name");
    form.setCableOwner("Owner");
    form.setLocation("Location");
    var crossing = padCableCrossingService.createCableCrossing(pwaApplicationDetail, form);
    assertThat(crossing.getCableName()).isEqualTo(form.getCableName());
    assertThat(crossing.getCableOwner()).isEqualTo(form.getCableOwner());
    assertThat(crossing.getLocation()).isEqualTo(form.getLocation());
    assertThat(crossing.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
  }

  @Test
  public void updateCableCrossing() {
    when(padCableCrossingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(padCableCrossingRepository.findByPwaApplicationDetailAndId(pwaApplicationDetail, 1))
        .thenReturn(Optional.of(padCableCrossing));

    var form = new AddCableCrossingForm();
    form.setCableName("Name");
    form.setCableOwner("Owner");
    form.setLocation("Location");
    padCableCrossingService.updateCableCrossing(pwaApplicationDetail, 1, form);
    assertThat(padCableCrossing.getCableName()).isEqualTo(form.getCableName());
    assertThat(padCableCrossing.getCableOwner()).isEqualTo(form.getCableOwner());
    assertThat(padCableCrossing.getLocation()).isEqualTo(form.getLocation());
  }

  @Test
  public void removeCableCrossing() {
    when(padCableCrossingRepository.findByPwaApplicationDetailAndId(pwaApplicationDetail, 1))
        .thenReturn(Optional.of(padCableCrossing));
    padCableCrossingService.removeCableCrossing(pwaApplicationDetail, 1);
    verify(padCableCrossingRepository, times(1)).delete(padCableCrossing);
  }

  @Test
  public void mapCrossingToForm() {
    var form = new AddCableCrossingForm();
    padCableCrossingService.mapCrossingToForm(padCableCrossing, form);
    assertThat(form.getCableName()).isEqualTo(padCableCrossing.getCableName());
    assertThat(form.getCableOwner()).isEqualTo(padCableCrossing.getCableOwner());
    assertThat(form.getLocation()).isEqualTo(padCableCrossing.getLocation());
  }
}