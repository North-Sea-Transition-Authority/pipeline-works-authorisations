package uk.co.ogauthority.pwa.service.mailmerge;

import java.util.Collection;
import java.util.Map;
import uk.co.ogauthority.pwa.model.entity.mailmerge.MailMergeField;
import uk.co.ogauthority.pwa.service.documents.DocumentSource;

/**
 * Allows a different mail merge field resolver to be used for each type of DocumentSource so that object-specific logic
 * can be defined.
 */
public interface DocumentSourceMailMergeResolver {

  /**
   * Returns true if the resolver being questioned supports the passed document source, false otherwise.
   */
  boolean supportsDocumentSource(DocumentSource documentSource);

  /**
   * Taking a document source and some merge fields to resolve, return a map of field mnem name to resolved value.
   */
  Map<String, String> resolveMergeFields(DocumentSource documentSource,
                                         Collection<MailMergeField> mailMergeFields);

}
