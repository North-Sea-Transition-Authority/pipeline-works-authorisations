package uk.co.ogauthority.pwa.service.licence;

import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.licence.PadCrossedBlock;
import uk.co.ogauthority.pwa.model.entity.licence.PearsBlock;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.licence.PadCrossedBlockRepository;

@Service
public class PadCrossedBlockService {

  private final PadCrossedBlockRepository padCrossedBlockRepository;

  @Autowired
  public PadCrossedBlockService(PadCrossedBlockRepository padCrossedBlockRepository) {
    this.padCrossedBlockRepository = padCrossedBlockRepository;
  }

  public void save(PadCrossedBlock padCrossedBlock) {
    padCrossedBlockRepository.save(padCrossedBlock);
  }

  public List<PadCrossedBlock> getBlocksByDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padCrossedBlockRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
  }

  public PadCrossedBlock createFromPearsBlock(PearsBlock pearsBlock) {
    var block = new PadCrossedBlock();
    block.setBlockNumber(pearsBlock.getBlockNumber());
    block.setBlockReference(pearsBlock.getBlockReference());
    block.setLicence(pearsBlock.getPearsLicence());
    block.setQuadrantNumber(pearsBlock.getQuadrantNumber());
    block.setSuffix(pearsBlock.getSuffix());
    block.setLicenceStatus(pearsBlock.getLicenceStatus());
    block.setLocation(pearsBlock.getLocation());
    block.setStartTimestamp(Instant.now());
    padCrossedBlockRepository.save(block);
    return block;
  }
}
