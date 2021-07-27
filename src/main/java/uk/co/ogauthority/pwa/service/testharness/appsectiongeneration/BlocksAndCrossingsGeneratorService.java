package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.CrossedBlockOwner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCrossedBlock;
import uk.co.ogauthority.pwa.repository.licence.PadCrossedBlockRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.service.licence.PearsBlockService;

@Service
@Profile("development")
public class BlocksAndCrossingsGeneratorService {

  private final PearsBlockService pearsBlockService;
  private final PadCrossedBlockRepository padCrossedBlockRepository;
  private final PwaApplicationDetailRepository pwaApplicationDetailRepository;


  @Autowired
  public BlocksAndCrossingsGeneratorService(PearsBlockService pearsBlockService,
                                            PadCrossedBlockRepository padCrossedBlockRepository,
                                            PwaApplicationDetailRepository pwaApplicationDetailRepository) {
    this.pearsBlockService = pearsBlockService;
    this.padCrossedBlockRepository = padCrossedBlockRepository;
    this.pwaApplicationDetailRepository = pwaApplicationDetailRepository;
  }


  public void generateBlocksAndCrossings(PwaApplicationDetail pwaApplicationDetail) {

    var crossedBlock = new PadCrossedBlock();
    setCrossedBlockData(pwaApplicationDetail, crossedBlock);
    padCrossedBlockRepository.save(crossedBlock);

    setCrossingTypes(pwaApplicationDetail);
    pwaApplicationDetailRepository.save(pwaApplicationDetail);
  }


  private void setCrossedBlockData(PwaApplicationDetail pwaApplicationDetail, PadCrossedBlock crossedBlock) {

    var blockRef = "10/1a101a300";
    var pearsBlock = pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKeyOrError(blockRef);

    crossedBlock.setPwaApplicationDetail(pwaApplicationDetail);
    crossedBlock.setQuadrantNumber(pearsBlock.getQuadrantNumber());
    crossedBlock.setBlockNumber(pearsBlock.getBlockNumber());
    crossedBlock.setSuffix(pearsBlock.getSuffix());
    crossedBlock.setBlockReference(pearsBlock.getBlockReference());
    crossedBlock.setLicence(pearsBlock.getPearsLicence());
    crossedBlock.setLocation(pearsBlock.getBlockLocation());
    crossedBlock.setCreatedInstant(Instant.now());
    crossedBlock.setBlockOwner(CrossedBlockOwner.HOLDER);
  }

  private void setCrossingTypes(PwaApplicationDetail detail) {
    detail.setCablesCrossed(false);
    detail.setPipelinesCrossed(false);
    detail.setMedianLineCrossed(false);
  }



}
