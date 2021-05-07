package uk.co.ogauthority.pwa.service.mailmerge;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldType;
import uk.co.ogauthority.pwa.model.entity.mailmerge.MailMergeField;
import uk.co.ogauthority.pwa.model.entity.mailmerge.MailMergeFieldDocSpec;
import uk.co.ogauthority.pwa.repository.mailmerge.MailMergeFieldDocSpecRepository;
import uk.co.ogauthority.pwa.service.documents.DocumentSource;
import uk.co.ogauthority.pwa.service.markdown.MarkdownService;

@Service
public class MailMergeService {

  private final MailMergeFieldDocSpecRepository mailMergeFieldDocSpecRepository;
  private final List<DocumentSourceMailMergeResolver> mailMergeResolvers;
  private final MarkdownService markdownService;

  @Autowired
  public MailMergeService(MailMergeFieldDocSpecRepository mailMergeFieldDocSpecRepository,
                          List<DocumentSourceMailMergeResolver> mailMergeResolvers,
                          MarkdownService markdownService) {
    this.mailMergeFieldDocSpecRepository = mailMergeFieldDocSpecRepository;
    this.mailMergeResolvers = mailMergeResolvers;
    this.markdownService = markdownService;
  }

  public List<MailMergeField> getMailMergeFieldsForDocumentSource(DocumentSource documentSource) {
    return getMailMergeFieldsByDocumentSpec(documentSource.getDocumentSpec());
  }

  private List<MailMergeField> getMailMergeFieldsByDocumentSpec(DocumentSpec documentSpec) {
    return mailMergeFieldDocSpecRepository
        .getAllByDocumentSpec(documentSpec)
        .stream()
        .map(MailMergeFieldDocSpec::getMailMergeField)
        .collect(Collectors.toList());
  }

  // todo PWA-1227 properly implement and test
  public DocumentView mailMerge(DocumentView documentView) {

    var mailMergeTypeToFieldMap = mailMergeFieldDocSpecRepository
        .getAllByDocumentSpec(documentView.getDocumentSource().getDocumentSpec())
        .stream()
        .map(MailMergeFieldDocSpec::getMailMergeField)
        .collect(Collectors.groupingBy(MailMergeField::getType));

    var map = new HashMap<String, String>();

    mailMergeResolvers.stream()
        .filter(r -> r.supportsDocumentSource(documentView.getDocumentSource()))
        .findFirst()
        .ifPresent(documentSourceMailMergeResolver -> map.putAll(
            documentSourceMailMergeResolver.resolveMergeFields(documentView.getDocumentSource(),
            mailMergeTypeToFieldMap.get(MailMergeFieldType.AUTOMATIC))));

    documentView.getSections().forEach(section ->

        section.getClauses().forEach(clause -> {

          String html = markdownService.convertMarkdownToHtml(clause.getText(), map);

          clause.setText(html);

        })

    );

    return documentView;

  }

}