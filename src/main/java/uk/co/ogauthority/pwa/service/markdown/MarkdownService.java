package uk.co.ogauthority.pwa.service.markdown;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

@Service
public class MarkdownService {

  public String convertMarkdownToHtml(String markdown, Map<String, String> mailMergeFieldValues) {

    List<Extension> extensions = Arrays.asList(TablesExtension.create(), MailMergeExtension.create(mailMergeFieldValues));

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
