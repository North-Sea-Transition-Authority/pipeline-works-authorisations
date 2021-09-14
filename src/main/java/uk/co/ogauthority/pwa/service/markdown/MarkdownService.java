package uk.co.ogauthority.pwa.service.markdown;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.service.markdown.automatic.AutomaticMailMergeExtension;
import uk.co.ogauthority.pwa.service.markdown.manual.ManualMailMergeExtension;

@Service
public class MarkdownService {

  public String convertMarkdownToHtml(String markdown) {

    if (StringUtils.isBlank(markdown)) {
      return markdown;
    }

    List<Extension> extensions = List.of(TablesExtension.create());

    return convertMarkdownWithExtensions(markdown, extensions);

  }

  public String convertMarkdownToHtml(String markdown,
                                      MailMergeContainer mailMergeContainer) {

    if (StringUtils.isBlank(markdown)) {
      return markdown;
    }

    List<Extension> extensions = Arrays.asList(
        TablesExtension.create(),
        AutomaticMailMergeExtension.create(mailMergeContainer),
        ManualMailMergeExtension.create(mailMergeContainer));

    return convertMarkdownWithExtensions(markdown, extensions);

  }

  private String convertMarkdownWithExtensions(String markdown, List<Extension> extensions) {

    Parser parser = Parser.builder()
        .extensions(extensions)
        .build();

    Node document = parser.parse(markdown);

    HtmlRenderer renderer = HtmlRenderer.builder()
        .escapeHtml(true)
        .extensions(extensions)
        .attributeProviderFactory(context -> new ListStyleAttributeProvider(
            Arrays.asList("list-style", "list-style--alpha")
        ))
        .build();

    return renderer.render(document);

  }

}
