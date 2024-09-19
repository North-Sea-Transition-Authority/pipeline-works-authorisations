package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsBlock;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class PadCrossedBlockService {

  private final PadCrossedBlockRepository padCrossedBlockRepository;

  @Autowired
  public PadCrossedBlockService(PadCrossedBlockRepository padCrossedBlockRepository) {
    this.padCrossedBlockRepository = padCrossedBlockRepository;
  }

  @Transactional
  public void save(PadCrossedBlock padCrossedBlock) {
    padCrossedBlockRepository.save(padCrossedBlock);
  }

  public List<PadCrossedBlock> getBlocksByDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padCrossedBlockRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)
        ;
  }

  public PadCrossedBlock createFromPearsBlock(PearsBlock pearsBlock) {
    var block = new PadCrossedBlock();
    block.setBlockNumber(pearsBlock.getBlockNumber());
    block.setBlockReference(pearsBlock.getBlockReference());
    block.setLicence(pearsBlock.getPearsLicence());
    block.setQuadrantNumber(pearsBlock.getQuadrantNumber());
    block.setSuffix(pearsBlock.getSuffix());
    block.setLocation(pearsBlock.getBlockLocation());
    block.setCreatedInstant(Instant.now());
    return block;
  }
}
