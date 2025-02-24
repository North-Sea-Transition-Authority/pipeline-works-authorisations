package uk.co.ogauthority.pwa.service.mailmerge;

import static uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem.DIGITAL_SIGNATURE;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.service.documents.DocumentSource;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;

@Service
public class SignaturePlaceholderMailMergeResolver implements DocumentSourceMailMergeResolver {

  private final Set<Class<? extends DocumentSource>> supportedClasses = Set.of(PwaApplication.class, TemplateDocumentSource.class);
  private final List<MailMergeFieldMnem> mmFields = List.of(DIGITAL_SIGNATURE);

  @Override
  public boolean supportsDocumentSource(DocumentSource documentSource) {
    return supportedClasses.contains(documentSource.getClass());
  }

  @Override
  public List<MailMergeFieldMnem> getAvailableMailMergeFields(DocumentSource documentSource) {
    return mmFields;
  }

  @Override
  public Map<String, String> resolveMergeFields(DocumentSource documentSource) {
    return mmFields.stream()
        .collect(Collectors.toMap(Enum::name, mailMergeFieldMnem -> String.format("((%s))", mailMergeFieldMnem.name())));
  }
}
