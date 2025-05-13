package uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees;

import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.fds.searchselector.SearchSelectorResults;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.repository.appprocessing.consultations.consultees.ConsulteeGroupDetailRepository;

@Service
public class ConsulteeGroupDetailService {

  private final ConsulteeGroupDetailRepository groupDetailRepository;

  @Autowired
  public ConsulteeGroupDetailService(ConsulteeGroupDetailRepository groupDetailRepository) {
    this.groupDetailRepository = groupDetailRepository;
  }

  public ConsulteeGroupDetail getConsulteeGroupDetailById(Integer entityID) {
    return groupDetailRepository.findById(entityID)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format("Couldn't find consultee group detail with ID: %s", entityID)));
  }

  public ConsulteeGroupDetail getConsulteeGroupDetailByGroupAndTipFlagIsTrue(ConsulteeGroup consulteeGroup) {
    return groupDetailRepository.findByConsulteeGroupAndTipFlagIsTrue(consulteeGroup)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find consultee group detail by consultee group ID: %s", consulteeGroup.getId() +
                " where tip flag is true")));
  }

  public ConsulteeGroupDetail getConsulteeGroupDetailByGroupIdAndTipFlagIsTrue(Integer consulteeGroupId) {
    return groupDetailRepository.findByConsulteeGroup_IdAndTipFlagIsTrue(consulteeGroupId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find consultee group detail by consultee group ID: %s", consulteeGroupId +
                " where tip flag is true")));
  }

  public List<ConsulteeGroupDetail> getAllConsulteeGroupDetailsByGroup(Iterable<ConsulteeGroup> consulteeGroups) {
    return groupDetailRepository.findAllByConsulteeGroupInAndTipFlagIsTrue(consulteeGroups);
  }

  public List<ConsulteeGroupDetail> getAllConsulteeGroupDetails() {
    return groupDetailRepository.findAllByTipFlagIsTrue();
  }

  public List<SearchSelectorResults.Result> searchConsulteeGroups(String searchTerm) {
    return groupDetailRepository.findAllByTipFlagIsTrueAndNameContainsIgnoreCase(searchTerm).stream()
        .sorted(Comparator.comparing(ConsulteeGroupDetail::getName, String.CASE_INSENSITIVE_ORDER))
        .map(consulteeGroupDetail ->
            new SearchSelectorResults.Result(consulteeGroupDetail.getConsulteeGroupId().toString(), consulteeGroupDetail.getName()))
        .toList();
  }
}
