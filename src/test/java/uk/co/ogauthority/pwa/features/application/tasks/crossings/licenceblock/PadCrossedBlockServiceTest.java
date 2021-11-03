package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.BlockLocation;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsBlock;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsLicence;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@RunWith(MockitoJUnitRunner.class)
public class PadCrossedBlockServiceTest {

  @Mock
  private PadCrossedBlockRepository padCrossedBlockRepository;

  private PadCrossedBlockService padCrossedBlockService;

  @Before
  public void setUp() {
    padCrossedBlockService = new PadCrossedBlockService(padCrossedBlockRepository);
  }

  @Test
  public void save() {
    var padBlock = new PadCrossedBlock();
    padCrossedBlockService.save(padBlock);
    verify(padCrossedBlockRepository, times(1)).save(padBlock);
  }

  @Test
  public void getBlocksByDetail() {
    var padBlock = new PadCrossedBlock();
    var blocks = List.of(padBlock);
    var detail = new PwaApplicationDetail();
    when(padCrossedBlockRepository.getAllByPwaApplicationDetail(detail)).thenReturn(blocks);
    var result = padCrossedBlockService.getBlocksByDetail(detail);
    assertThat(result).isEqualTo(blocks);
  }

  @Test
  public void createFromPearsBlock() {
    var licence = new PearsLicence();
    var pearsBlock = new PearsBlock("Key", licence, "Ref", "BlockNo", "QuadNo", "Suffix",
        BlockLocation.OFFSHORE);
    var result = padCrossedBlockService.createFromPearsBlock(pearsBlock);
    assertThat(result.getBlockNumber()).isEqualTo(pearsBlock.getBlockNumber());
    assertThat(result.getBlockReference()).isEqualTo(pearsBlock.getBlockReference());
    assertThat(result.getLicence()).isEqualTo(pearsBlock.getPearsLicence());
    assertThat(result.getQuadrantNumber()).isEqualTo(pearsBlock.getQuadrantNumber());
    assertThat(result.getSuffix()).isEqualTo(pearsBlock.getSuffix());
    assertThat(result.getLocation()).isEqualTo(pearsBlock.getBlockLocation());
    assertThat(result.getCreatedInstant()).isNotNull();
  }
}