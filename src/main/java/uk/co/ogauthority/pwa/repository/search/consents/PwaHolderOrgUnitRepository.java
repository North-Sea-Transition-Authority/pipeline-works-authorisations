package uk.co.ogauthority.pwa.repository.search.consents;


import java.util.Set;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnit;

public interface PwaHolderOrgUnitRepository extends CrudRepository<PwaHolderOrgUnit, Integer> {

  Set<PwaHolderOrgUnit> findAllByPwaId(int pwaId);

  Set<PwaHolderOrgUnit> findAllByPwaIdIn(Set<Integer> pwaIds);

}