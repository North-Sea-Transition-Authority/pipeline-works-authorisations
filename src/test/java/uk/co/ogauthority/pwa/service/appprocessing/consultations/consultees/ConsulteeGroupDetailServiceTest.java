package uk.co.ogauthority.pwa.underTest.appprocessing.consultations.consultees;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.fds.searchselector.SearchSelectorResults;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.repository.appprocessing.consultations.consultees.ConsulteeGroupDetailRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;

@ExtendWith(MockitoExtension.class)
class ConsulteeGroupDetailServiceTestTest {
  @Mock
  private ConsulteeGroupDetailRepository groupDetailRepository;

  @InjectMocks
  private ConsulteeGroupDetailService underTest;

  @Test
  void searchConsulteeGroups_returnsSortedResultsMappedCorrectly() {
    ConsulteeGroupDetail detail1 = mock(ConsulteeGroupDetail.class);
    when(detail1.getName()).thenReturn("alpha"); // lower case
    when(detail1.getConsulteeGroupId()).thenReturn(2);

    ConsulteeGroupDetail detail2 = mock(ConsulteeGroupDetail.class);
    when(detail2.getName()).thenReturn("Bravo");
    when(detail2.getConsulteeGroupId()).thenReturn(1);

    ConsulteeGroupDetail detail3 = mock(ConsulteeGroupDetail.class);
    when(detail3.getName()).thenReturn("Charlie");
    when(detail3.getConsulteeGroupId()).thenReturn(3);

    List<ConsulteeGroupDetail> details = Arrays.asList(detail2, detail3, detail1); // unsorted input

    when(groupDetailRepository.findAllByTipFlagIsTrueAndNameContainsIgnoreCase("test"))
        .thenReturn(details);

    List<SearchSelectorResults.Result> results = underTest.searchConsulteeGroups("test");

    assertThat(results)
        .extracting(SearchSelectorResults.Result::id, SearchSelectorResults.Result::text)
        .containsExactly(
            tuple("2", "alpha"),
            tuple("1", "Bravo"),
            tuple("3", "Charlie")
        );
  }
}