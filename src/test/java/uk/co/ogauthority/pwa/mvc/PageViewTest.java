package uk.co.ogauthority.pwa.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class PageViewTest {

  private Page<String> page;
  private List<String> pageContent = Arrays.asList("a", "b", "c");
  private List<String> mappedPageContent = Arrays.asList("mapped_a", "mapped_b", "mapped_c");

  @BeforeEach
  void setUp() {
    page = mock(Page.class);
    when(page.stream()).then(a -> pageContent.stream());
    when(page.getNumber()).thenReturn(1);
    when(page.getTotalPages()).thenReturn(3);
    when(page.getPageable()).thenReturn(PageRequest.of(1, 20));
  }

  @Test
  void createWithDefaultPageParamName() {
    PageView withParamName = PageView.fromPage(page, "/endpoint?page=x");
    assertThat(withParamName.urlForPage(1)).isEqualTo("/endpoint?page=1");
    assertThat(withParamName.getCurrentPage()).isEqualTo(1);
    assertThat(withParamName.getTotalPages()).isEqualTo(3);
    assertThat(withParamName.getPageContent()).isEqualTo(pageContent);

    PageView noParamName = PageView.fromPage(page, "/endpoint");
    assertThat(noParamName.urlForPage(1)).isEqualTo("/endpoint?page=1");

    PageView wrongParamName = PageView.fromPage(page, "/endpoint?wrongPage=x");
    assertThat(wrongParamName.urlForPage(1)).isEqualTo("/endpoint?wrongPage=x&page=1");
  }

  @Test
  void createWithDefaultPageParamNameAndMappingFunction() {
    PageView withParamName = PageView.fromPage(page, "/endpoint?page=x", e -> "mapped_" + e);
    assertThat(withParamName.urlForPage(1)).isEqualTo("/endpoint?page=1");
    assertThat(withParamName.getCurrentPage()).isEqualTo(1);
    assertThat(withParamName.getTotalPages()).isEqualTo(3);
    assertThat(withParamName.getPageContent()).isEqualTo(mappedPageContent);
  }

  @Test
  void createWithPageParamName() {
    PageView withParamName = PageView.fromPage(page, "/endpoint?somePage=x", "somePage");
    assertThat(withParamName.urlForPage(1)).isEqualTo("/endpoint?somePage=1");
    assertThat(withParamName.getCurrentPage()).isEqualTo(1);
    assertThat(withParamName.getTotalPages()).isEqualTo(3);
    assertThat(withParamName.getPageContent()).isEqualTo(pageContent);

    PageView noParamName = PageView.fromPage(page, "/endpoint", "somePage");
    assertThat(noParamName.urlForPage(1)).isEqualTo("/endpoint?somePage=1");

    PageView wrongParamName = PageView.fromPage(page, "/endpoint?wrongPage=x", "somePage");
    assertThat(wrongParamName.urlForPage(1)).isEqualTo("/endpoint?wrongPage=x&somePage=1");
  }

  @Test
  void createWithPageParamNameAndMappingFunction() {
    PageView withParamName = PageView.fromPage(page, "/endpoint?somePage=x", "somePage", e -> "mapped_" + e);
    assertThat(withParamName.urlForPage(1)).isEqualTo("/endpoint?somePage=1");
    assertThat(withParamName.getCurrentPage()).isEqualTo(1);
    assertThat(withParamName.getTotalPages()).isEqualTo(3);
    assertThat(withParamName.getPageContent()).isEqualTo(mappedPageContent);
  }

  @Test
  void createWithPageUriFunction() {
    PageView view = PageView.fromPage(page, pageNum -> "/endpoint/" + pageNum);
    assertThat(view.urlForPage(1)).isEqualTo("/endpoint/1");
    assertThat(view.getCurrentPage()).isEqualTo(1);
    assertThat(view.getTotalPages()).isEqualTo(3);
    assertThat(view.getPageContent()).isEqualTo(pageContent);
  }

  @Test
  void createWithPageUriFunctionAndMappingFunction() {
    PageView view = PageView.fromPage(page, pageNum -> "/endpoint/" + pageNum, e -> "mapped_" + e);
    assertThat(view.urlForPage(1)).isEqualTo("/endpoint/1");
    assertThat(view.getCurrentPage()).isEqualTo(1);
    assertThat(view.getTotalPages()).isEqualTo(3);
    assertThat(view.getPageContent()).isEqualTo(mappedPageContent);
  }
}