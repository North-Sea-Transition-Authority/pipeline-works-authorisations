package uk.co.ogauthority.pwa.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;

public class PageViewTest {

  private Page<String> page;
  private List<String> pageContent = Arrays.asList("a", "b", "c");
  private List<String> mappedPageContent = Arrays.asList("mapped_a", "mapped_b", "mapped_c");

  @Before
  public void setUp() {
    page = mock(Page.class);
    when(page.stream()).then(a -> pageContent.stream());
    when(page.getNumber()).thenReturn(1);
    when(page.getTotalPages()).thenReturn(3);
  }

  @Test
  public void testCreateWithDefaultPageParamName() {
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
  public void testCreateWithDefaultPageParamNameAndMappingFunction() {
    PageView withParamName = PageView.fromPage(page, "/endpoint?page=x", e -> "mapped_" + e);
    assertThat(withParamName.urlForPage(1)).isEqualTo("/endpoint?page=1");
    assertThat(withParamName.getCurrentPage()).isEqualTo(1);
    assertThat(withParamName.getTotalPages()).isEqualTo(3);
    assertThat(withParamName.getPageContent()).isEqualTo(mappedPageContent);
  }

  @Test
  public void testCreateWithPageParamName() {
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
  public void testCreateWithPageParamNameAndMappingFunction() {
    PageView withParamName = PageView.fromPage(page, "/endpoint?somePage=x", "somePage", e -> "mapped_" + e);
    assertThat(withParamName.urlForPage(1)).isEqualTo("/endpoint?somePage=1");
    assertThat(withParamName.getCurrentPage()).isEqualTo(1);
    assertThat(withParamName.getTotalPages()).isEqualTo(3);
    assertThat(withParamName.getPageContent()).isEqualTo(mappedPageContent);
  }

  @Test
  public void testCreateWithPageUriFunction() {
    PageView view = PageView.fromPage(page, pageNum -> "/endpoint/" + pageNum);
    assertThat(view.urlForPage(1)).isEqualTo("/endpoint/1");
    assertThat(view.getCurrentPage()).isEqualTo(1);
    assertThat(view.getTotalPages()).isEqualTo(3);
    assertThat(view.getPageContent()).isEqualTo(pageContent);
  }

  @Test
  public void testCreateWithPageUriFunctionAndMappingFunction() {
    PageView view = PageView.fromPage(page, pageNum -> "/endpoint/" + pageNum, e -> "mapped_" + e);
    assertThat(view.urlForPage(1)).isEqualTo("/endpoint/1");
    assertThat(view.getCurrentPage()).isEqualTo(1);
    assertThat(view.getTotalPages()).isEqualTo(3);
    assertThat(view.getPageContent()).isEqualTo(mappedPageContent);
  }
}