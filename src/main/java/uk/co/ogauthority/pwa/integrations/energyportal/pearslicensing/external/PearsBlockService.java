package uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external;

import jakarta.persistence.EntityManager;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.internal.PearsBlockRepository;

@Service
public class PearsBlockService {

  private final PearsBlockRepository pearsBlockRepository;
  private final EntityManager entityManager;

  @Autowired
  public PearsBlockService(PearsBlockRepository pearsBlockRepository, EntityManager entityManager) {
    this.pearsBlockRepository = pearsBlockRepository;
    this.entityManager = entityManager;
  }


  public List<PickablePearsBlock> findOffshorePickablePearsBlocks(String searchReference, Pageable pageable) {

    // could use dto constructor in query if performance becomes a problem to avoid fetching Licence data at the same time
    // join fetch used to avoid n+1
    var query = entityManager.createQuery(
        "FROM PearsBlock pb " +
            "LEFT JOIN FETCH pb.pearsLicence " +
            "WHERE pb.blockReference LIKE :search || '%' " +
            "AND (pb.pearsLicence.licenceStatus = :licenceStatus OR pb.pearsLicence IS NULL) " +
            "AND pb.blockLocation = :location " +
            "ORDER BY pb.quadrantNumber , pb.blockNumber, pb.suffix " +
            "", PearsBlock.class)
        .setParameter("search", searchReference)
        .setParameter("location", BlockLocation.OFFSHORE)
        .setParameter("licenceStatus", LicenceStatus.EXTANT)
        .setFirstResult((int) pageable.getOffset())
        .setMaxResults(pageable.getPageSize());

    var results = query.getResultList();
    return results.stream()
        .sorted(Comparator.comparing(PearsBlock::getQuadrantNumber)
            .thenComparing(PearsBlock::getBlockNumber)
            .thenComparing((pb) -> pb.getSuffix() != null ? pb.getSuffix() : "")
            .thenComparing(PearsBlock::getCompositeKey))
        .map(PickablePearsBlock::new)
        .collect(Collectors.toList());
  }

  public Optional<PearsBlock> getExtantOrUnlicensedOffshorePearsBlockByCompositeKey(String compositeKey) {
    return pearsBlockRepository.findByCompositeKeyAndBlockLocation(compositeKey, BlockLocation.OFFSHORE)
        // find block by composite key and location then filter by licence status is extant of block is unlicensed
        .filter(pb -> (pb.isLicensed() && LicenceStatus.EXTANT.equals(pb.getPearsLicence().getLicenceStatus())
            || !pb.isLicensed()));
  }

  public PearsBlock getExtantOrUnlicensedOffshorePearsBlockByCompositeKeyOrError(String compositeKey) {
    return getExtantOrUnlicensedOffshorePearsBlockByCompositeKey(compositeKey)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Did not find Extant or unlicensed PearsBlock with compositeKey:" + compositeKey));
  }

}
