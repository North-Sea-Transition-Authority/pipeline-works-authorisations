package uk.co.ogauthority.pwa.service.licence;

import java.time.Instant;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.licence.PearsBlock;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCrossedBlock;
import uk.co.ogauthority.pwa.repository.licence.PadCrossedBlockRepository;

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
