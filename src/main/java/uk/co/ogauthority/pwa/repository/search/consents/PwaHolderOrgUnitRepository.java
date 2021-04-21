package uk.co.ogauthority.pwa.repository.search.consents;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnit;

public interface PwaHolderOrgUnitRepository extends CrudRepository<PwaHolderOrgUnit, Integer> {

  List<PwaHolderOrgUnit> findAllByPwaId(int pwaId);

}