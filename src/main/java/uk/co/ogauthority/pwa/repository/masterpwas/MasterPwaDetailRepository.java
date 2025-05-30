package uk.co.ogauthority.pwa.repository.masterpwas;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;

@Repository
public interface MasterPwaDetailRepository extends CrudRepository<MasterPwaDetail, Integer> {

  @EntityGraph(attributePaths = {"masterPwa"})
  List<MasterPwaDetail> findByEndInstantIsNullAndMasterPwaDetailStatus(MasterPwaDetailStatus masterPwaDetailStatus);

  @EntityGraph(attributePaths = {"masterPwa"})
  List<MasterPwaDetail> findByMasterPwaInAndEndInstantIsNull(Collection<MasterPwa> masterPwas);

  @EntityGraph(attributePaths = {"masterPwa"})
  Optional<MasterPwaDetail> findByMasterPwaAndEndInstantIsNull(MasterPwa masterPwa);

  @EntityGraph(attributePaths = {"masterPwa"})
  List<MasterPwaDetail> findAllByReferenceContainingIgnoreCaseAndMasterPwaDetailStatus(String filter,
                                                                                       MasterPwaDetailStatus masterPwaDetailStatus);

  @EntityGraph(attributePaths = {"masterPwa"})
  Optional<MasterPwaDetail> findByReferenceAndMasterPwaDetailStatus(String reference, MasterPwaDetailStatus masterPwaDetailStatus);

  List<MasterPwaDetail> findAllByMasterPwaInAndEndInstantIsNull(Collection<MasterPwa> masterPwas);
}
