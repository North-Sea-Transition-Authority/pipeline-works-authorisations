package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineIdentDataRepository;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineIdentDataServiceTest {

  @Mock
  private PadPipelineIdentDataRepository repository;

  private PadPipelineIdentDataService identDataService;

  @Captor
  private ArgumentCaptor<PadPipelineIdentData> identDataCaptor;

  @Before
  public void setUp() {
    identDataService = new PadPipelineIdentDataService(repository);
  }

  @Test
  public void addIdentData() {

    var ident = new PadPipelineIdent();

    var form = new PipelineIdentDataForm();
    form.setInsulationCoatingType("ins");
    form.setComponentPartsDescription("comp");
    form.setProductsToBeConveyed("prod");
    form.setExternalDiameter(BigDecimal.valueOf(10));
    form.setInternalDiameter(BigDecimal.valueOf(11));
    form.setWallThickness(BigDecimal.valueOf(22.22));
    form.setMaop(BigDecimal.valueOf(500));

    identDataService.addIdentData(ident, form);

    verify(repository, times(1)).save(identDataCaptor.capture());

    var identData = identDataCaptor.getValue();

    assertThat(identData.getPadPipelineIdent()).isEqualTo(ident);
    assertThat(identData.getComponentPartsDescription()).isEqualTo(form.getComponentPartsDescription());
    assertThat(identData.getInsulationCoatingType()).isEqualTo(form.getInsulationCoatingType());
    assertThat(identData.getProductsToBeConveyed()).isEqualTo(form.getProductsToBeConveyed());
    assertThat(identData.getExternalDiameter()).isEqualTo(form.getExternalDiameter());
    assertThat(identData.getInternalDiameter()).isEqualTo(form.getInternalDiameter());
    assertThat(identData.getWallThickness()).isEqualTo(form.getWallThickness());
    assertThat(identData.getMaop()).isEqualTo(form.getMaop());

  }

}
