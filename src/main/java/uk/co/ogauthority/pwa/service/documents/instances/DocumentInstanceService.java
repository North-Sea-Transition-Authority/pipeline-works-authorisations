package uk.co.ogauthority.pwa.service.documents.instances;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.documents.DocumentTemplateDto;
import uk.co.ogauthority.pwa.model.documents.TemplateSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseVersionRepository;
import uk.co.ogauthority.pwa.service.documents.SectionClauseCreator;

@Service
public class DocumentInstanceService {

  private final DocumentInstanceRepository documentInstanceRepository;
  private final DocumentInstanceSectionClauseRepository instanceSectionClauseRepository;
  private final DocumentInstanceSectionClauseVersionRepository instanceSectionClauseVersionRepository;
  private final SectionClauseCreator sectionClauseCreator;
  private final Clock clock;

  @Autowired
  public DocumentInstanceService(DocumentInstanceRepository documentInstanceRepository,
                                 DocumentInstanceSectionClauseRepository instanceSectionClauseRepository,
                                 DocumentInstanceSectionClauseVersionRepository instanceSectionClauseVersionRepository,
                                 SectionClauseCreator sectionClauseCreator,
                                 @Qualifier("utcClock") Clock clock) {
    this.documentInstanceRepository = documentInstanceRepository;
    this.instanceSectionClauseRepository = instanceSectionClauseRepository;
    this.instanceSectionClauseVersionRepository = instanceSectionClauseVersionRepository;
    this.sectionClauseCreator = sectionClauseCreator;
    this.clock = clock;
  }

  /**
   * Create a document instance based on a document template.
   * @param pwaApplication document is being created for
   * @param documentTemplateDto object containing document template information
   * @param creatingPerson person creating document
   */
  public void createFromDocumentDto(PwaApplication pwaApplication,
                                    DocumentTemplateDto documentTemplateDto,
                                    Person creatingPerson) {

    // find instance if present already, otherwise create it
    var instance = getDocumentInstance(pwaApplication, documentTemplateDto.getDocumentTemplate().getMnem())
        .orElseGet(() -> new DocumentInstance(documentTemplateDto.getDocumentTemplate(), pwaApplication, clock.instant()));

    var templateClauseVersionToInstanceClauseVersionMap =
        new HashMap<TemplateSectionClauseVersionDto, DocumentInstanceSectionClauseVersion>();

    var templateClauseIdToNewInstanceClauseMap = new HashMap<Integer, DocumentInstanceSectionClause>();

    documentTemplateDto.getSections().forEach(section -> {

      // for each current clause version within a section
      section.getClauses().forEach(templateClauseVersion -> {

        var createdFromTemplateClause = templateClauseVersion.getTemplateClauseRecord();

        // create a corresponding instance clause linked to the template clause it was created from
        var clause = new DocumentInstanceSectionClause();
        clause.setDocumentInstance(instance);
        clause.setDocumentTemplateSectionClause(createdFromTemplateClause);

        // store the new instance clause against the id of its associated template clause
        templateClauseIdToNewInstanceClauseMap.put(createdFromTemplateClause.getId(), clause);

        // create a new instance clause version from the template clause version
        DocumentInstanceSectionClauseVersion newClauseVersion = sectionClauseCreator
            .createInstanceClauseVersionFromTemplate(templateClauseVersion, creatingPerson);

        // link the newly created instance clause to its version
        newClauseVersion.setDocumentInstanceSectionClause(clause);

        // store the creating template clause version and the new instance clause based on it for later lookup
        templateClauseVersionToInstanceClauseVersionMap.put(templateClauseVersion, newClauseVersion);

      });

    });

    // set parent clauses as required, returning to list before saving
    List<DocumentInstanceSectionClauseVersion> modifiedInstanceClauseVersions = templateClauseVersionToInstanceClauseVersionMap
        .entrySet()
        .stream()
        .peek(entry -> setParentIfNeeded(entry.getKey(), entry.getValue(), templateClauseIdToNewInstanceClauseMap))
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());

    documentInstanceRepository.save(instance);
    instanceSectionClauseRepository.saveAll(templateClauseIdToNewInstanceClauseMap.values());
    instanceSectionClauseVersionRepository.saveAll(modifiedInstanceClauseVersions);

  }

  /**
   * If the template clause has a parent, set the parent on the newly created instance clause appropriately by looking
   * up the instance parent using the template parent.
   * @param templateClauseVersion that created the instance clause
   * @param newInstanceClauseVersion instance clause created from template clause
   * @param templateClauseIdToNewInstanceClauseMap map of template clause ids to the instance clauses created from them
   */
  private void setParentIfNeeded(TemplateSectionClauseVersionDto templateClauseVersion,
                                 DocumentInstanceSectionClauseVersion newInstanceClauseVersion,
                                 Map<Integer, DocumentInstanceSectionClause> templateClauseIdToNewInstanceClauseMap) {

    if (templateClauseVersion.getParentTemplateClause() != null) {

      // lookup the parent template clause and get the instance clause that was created from that
      var parentTemplateClause = templateClauseVersion.getParentTemplateClause();

      var associatedNewInstanceClause = templateClauseIdToNewInstanceClauseMap
          .get(parentTemplateClause.getId());

      // set the correct instance clause as the parent of the instance clause being processed
      newInstanceClauseVersion.setParentDocumentInstanceSectionClause(associatedNewInstanceClause);

    }

  }

  /**
   * Remove all clauses linked to a document instance for an application, to allow for reloading.
   * @param pwaApplication that document is being cleared for
   * @param templateMnem to identify which document to clear
   */
  public void clearClauses(PwaApplication pwaApplication,
                           DocumentTemplateMnem templateMnem) {

    var instance = getDocumentInstance(pwaApplication, templateMnem)
        .orElseThrow(() -> new EntityNotFoundException(String.format(
            "Couldn't find doc instance for pwa app with id [%s] and doc template mnem [%s]",
            pwaApplication.getId(),
            templateMnem.name())));

    var clauses = instanceSectionClauseRepository.findAllByDocumentInstance(instance);

    var instanceClauseVersions = instanceSectionClauseVersionRepository
        .findAllByDocumentInstanceSectionClauseInAndTipFlagIsTrue(clauses);

    instanceSectionClauseVersionRepository.deleteAll(instanceClauseVersions);
    instanceSectionClauseRepository.deleteAll(clauses);

  }

  public Optional<DocumentInstance> getDocumentInstance(PwaApplication application,
                                                        DocumentTemplateMnem templateMnem) {
    return documentInstanceRepository.findByPwaApplicationAndDocumentTemplate_Mnem(application, templateMnem);
  }

}
