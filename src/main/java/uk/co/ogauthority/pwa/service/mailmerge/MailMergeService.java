package uk.co.ogauthority.pwa.service.mailmerge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.documents.view.SectionClauseVersionView;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.mailmerge.MailMergeField;
import uk.co.ogauthority.pwa.repository.mailmerge.MailMergeFieldRepository;
import uk.co.ogauthority.pwa.service.documents.DocumentSource;
import uk.co.ogauthority.pwa.service.markdown.MailMergeContainer;
import uk.co.ogauthority.pwa.service.markdown.MarkdownService;
import uk.co.ogauthority.pwa.service.markdown.automatic.AutomaticMergeField;

@Service
public class MailMergeService {

  private final List<DocumentSourceMailMergeResolver> mailMergeResolvers;
  private final MarkdownService markdownService;
  private final MailMergeFieldRepository mailMergeFieldRepository;

  private final Pattern automaticMergeFieldPattern = Pattern.compile("\\(\\([^\\v]+?\\)\\)");

  @Autowired
  public MailMergeService(List<DocumentSourceMailMergeResolver> mailMergeResolvers,
                          MarkdownService markdownService,
                          MailMergeFieldRepository mailMergeFieldRepository) {
    this.mailMergeResolvers = mailMergeResolvers;
    this.markdownService = markdownService;
    this.mailMergeFieldRepository = mailMergeFieldRepository;
  }

  public List<MailMergeField> getMailMergeFieldsForDocumentSource(DocumentSource documentSource) {
    var availableFieldMnems = getMailMergeResolver(documentSource).getAvailableMailMergeFields(documentSource);
    return mailMergeFieldRepository.findAllByMnemIn(availableFieldMnems);
  }

  public DocumentView mailMerge(DocumentView documentView, DocGenType docGenType) {

    var resolver = getMailMergeResolver(documentView.getDocumentSource());
    var resolvedMergeFieldNameToValueMap = new HashMap<>(resolver.resolveMergeFields(documentView.getDocumentSource()));

    var container = new MailMergeContainer();
    container.setMailMergeFields(resolvedMergeFieldNameToValueMap);

    if (docGenType == DocGenType.PREVIEW) {
      container.setAutomaticMailMergeDataHtmlAttributeMap(Map.of("class", "pwa-mail-merge__automatic--preview"));
      container.setManualMailMergeDataHtmlAttributeMap(Map.of("class", "pwa-mail-merge__manual--preview"));
    }

    documentView.getSections().forEach(section ->

        section.getClauses().forEach(clause -> mergeClauseText(clause, container))

    );

    return documentView;

  }

  private void mergeClauseText(SectionClauseVersionView clause,
                               MailMergeContainer container) {

    String html = markdownService.convertMarkdownToHtml(clause.getText(), container);

    clause.setText(html);

    clause.getChildClauses().forEach(child -> mergeClauseText(child, container));

  }

  private DocumentSourceMailMergeResolver getMailMergeResolver(DocumentSource documentSource) {

    return mailMergeResolvers.stream()
        .filter(r -> r.supportsDocumentSource(documentSource))
        .findFirst()
        .orElseThrow(() -> new RuntimeException(String.format(
            "Couldn't find mail merge resolver for document source class %s", documentSource.getSource().getClass().getName())));

  }

  public Set<String> validateMailMergeFields(DocumentSource docSource, String text) {

    Set<String> detectedMergeFields = detectMergeFields(text);

    var allowedMergeFieldMnems = getMailMergeFieldsForDocumentSource(docSource).stream()
        .map(MailMergeField::getMnem)
        .map(MailMergeFieldMnem::name)
        .collect(Collectors.toSet());

    return detectedMergeFields.stream()
        .filter(detectedField -> !allowedMergeFieldMnems.contains(detectedField))
        .collect(Collectors.toSet());

  }

  private Set<String> detectMergeFields(String text) {

    var detectedFields = new HashSet<String>();
    var matcher = automaticMergeFieldPattern.matcher(text);

    while (matcher.find()) {

      String detectedField = matcher.group()
          .replace(AutomaticMergeField.OPENING_DELIMITER, "")
          .replace(AutomaticMergeField.CLOSING_DELIMITER, "");

      detectedFields.add(detectedField);

    }

    return detectedFields;

  }

}