package uk.co.ogauthority.pwa.service.documents.instances;

import java.time.Clock;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.documents.DocumentInstanceException;
import uk.co.ogauthority.pwa.model.documents.SectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.templates.DocumentTemplateDto;
import uk.co.ogauthority.pwa.model.documents.templates.TemplateSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.model.form.documents.ClauseForm;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseVersionDtoRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseVersionRepository;
import uk.co.ogauthority.pwa.service.documents.DocumentClauseService;
import uk.co.ogauthority.pwa.service.documents.DocumentViewService;
import uk.co.ogauthority.pwa.service.documents.SectionClauseCreator;

@Service
public class DocumentInstanceService {

  private final DocumentInstanceRepository documentInstanceRepository;
  private final DocumentInstanceSectionClauseRepository instanceSectionClauseRepository;
  private final DocumentInstanceSectionClauseVersionRepository instanceSectionClauseVersionRepository;
  private final SectionClauseCreator sectionClauseCreator;
  private final Clock clock;
  private final DocumentInstanceSectionClauseVersionDtoRepository sectionClauseVersionDtoRepository;
  private final DocumentViewService documentViewService;
  private final DocumentClauseService documentClauseService;

  @Autowired
  public DocumentInstanceService(DocumentInstanceRepository documentInstanceRepository,
                                 DocumentInstanceSectionClauseRepository instanceSectionClauseRepository,
                                 DocumentInstanceSectionClauseVersionRepository instanceSectionClauseVersionRepository,
                                 SectionClauseCreator sectionClauseCreator,
                                 @Qualifier("utcClock") Clock clock,
                                 DocumentInstanceSectionClauseVersionDtoRepository sectionClauseVersionDtoRepository,
                                 DocumentViewService documentViewService,
                                 DocumentClauseService documentClauseService) {
    this.documentInstanceRepository = documentInstanceRepository;
    this.instanceSectionClauseRepository = instanceSectionClauseRepository;
    this.instanceSectionClauseVersionRepository = instanceSectionClauseVersionRepository;
    this.sectionClauseCreator = sectionClauseCreator;
    this.clock = clock;
    this.sectionClauseVersionDtoRepository = sectionClauseVersionDtoRepository;
    this.documentViewService = documentViewService;
    this.documentClauseService = documentClauseService;
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

    documentTemplateDto.getSections().forEach(section ->

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

        })

    );

    // set parent clauses on instance versions as required
    templateClauseVersionToInstanceClauseVersionMap
        .forEach((templateClauseVersion, instanceClauseVersion) ->
            setParentIfNeeded(templateClauseVersion, instanceClauseVersion, templateClauseIdToNewInstanceClauseMap));

    documentInstanceRepository.save(instance);
    instanceSectionClauseRepository.saveAll(templateClauseIdToNewInstanceClauseMap.values());
    instanceSectionClauseVersionRepository.saveAll(templateClauseVersionToInstanceClauseVersionMap.values());

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
        .findAllByDocumentInstanceSectionClauseIn(clauses);

    instanceSectionClauseVersionRepository.deleteAll(instanceClauseVersions);
    instanceSectionClauseRepository.deleteAll(clauses);

  }

  public Optional<DocumentInstance> getDocumentInstance(PwaApplication application,
                                                        DocumentTemplateMnem templateMnem) {
    return documentInstanceRepository.findByPwaApplicationAndDocumentTemplate_Mnem(application, templateMnem);
  }

  public DocumentView getDocumentView(DocumentInstance instance) {

    var clauseVersionDtos = sectionClauseVersionDtoRepository.findAllByDiId(instance.getId())
        .stream()
        .map(SectionClauseVersionDto.class::cast)
        .collect(Collectors.toList());

    return documentViewService.createDocumentView(
        PwaDocumentType.INSTANCE,
        instance.getPwaApplication(),
        clauseVersionDtos);

  }

  public DocumentView getDocumentView(DocumentInstance instance,
                                      DocumentSection documentSection) {

    var clauseVersionDtos = sectionClauseVersionDtoRepository.findAllByDiId_AndSectionNameEquals(instance.getId(), documentSection.name())
        .stream()
        .map(SectionClauseVersionDto.class::cast)
        .collect(Collectors.toList());

    return documentViewService.createDocumentView(
        PwaDocumentType.INSTANCE,
        instance.getPwaApplication(),
        clauseVersionDtos);

  }

  public DocumentInstanceSectionClauseVersion getInstanceClauseVersionByClauseIdOrThrow(Integer clauseId) {
    return instanceSectionClauseVersionRepository
        .findByDocumentInstanceSectionClause_IdAndTipFlagIsTrue(clauseId)
        .orElseThrow(() -> new DocumentInstanceException(String.format("Couldn't find instance clause with ID: [%s]", clauseId)));
  }

  /**
   * Add a new clause on the same level as but one position after the passed-in clause.
   */
  @Transactional
  public void addClauseAfter(DocumentInstanceSectionClauseVersion versionToAddAfter, ClauseForm form, Person creatingPerson) {

    var newVersion = (DocumentInstanceSectionClauseVersion) documentClauseService
        .addClauseAfter(PwaDocumentType.INSTANCE, versionToAddAfter, form, creatingPerson);

    newVersion.getDocumentInstanceSectionClause()
        .setDocumentInstance(versionToAddAfter.getDocumentInstanceSectionClause().getDocumentInstance());

    instanceSectionClauseRepository.save(newVersion.getDocumentInstanceSectionClause());
    instanceSectionClauseVersionRepository.save(newVersion);

  }

  /**
   * Add a new clause on the same level as but one position before the passed-in clause.
   * The passed-in clause and any clauses following that (on the same level) are re-ordered.
   */
  @Transactional
  public void addClauseBefore(DocumentInstanceSectionClauseVersion versionToAddBefore, ClauseForm form, Person creatingPerson) {

    var docInstance = versionToAddBefore.getDocumentInstanceSectionClause().getDocumentInstance();
    var parentClause = versionToAddBefore.getParentDocumentInstanceSectionClause();

    var clauseVersionsToUpdate = documentClauseService.addClauseBefore(
        PwaDocumentType.INSTANCE,
        versionToAddBefore,
        form,
        creatingPerson,
        () -> instanceSectionClauseVersionRepository
            .findByDocumentInstanceSectionClause_DocumentInstanceAndParentDocumentInstanceSectionClause(docInstance, parentClause)
            .stream()
            .map(SectionClauseVersion.class::cast)
    );

    var castVersionList = clauseVersionsToUpdate.stream()
        .map(DocumentInstanceSectionClauseVersion.class::cast)
        .collect(Collectors.toList());

    var newClause = castVersionList.stream()
        .map(DocumentInstanceSectionClauseVersion::getDocumentInstanceSectionClause)
        .filter(c -> c.getDocumentInstance() == null)
        .findFirst()
        .orElseThrow(() -> new DocumentInstanceException(
            String.format("Expected new clause to have a null document instance, all clauses have document instances." +
                    "Adding before doc instance clause version with id: [%s]", versionToAddBefore.getId())));

    newClause.setDocumentInstance(docInstance);

    instanceSectionClauseRepository.save(newClause);

    instanceSectionClauseVersionRepository.saveAll(castVersionList);

  }

  /**
   * Add a child clause for the passed-in clause.
   */
  @Transactional
  public void addSubClause(DocumentInstanceSectionClauseVersion versionToAddSubFor, ClauseForm form, Person creatingPerson) {

    var newVersion = (DocumentInstanceSectionClauseVersion) documentClauseService
        .addSubClause(PwaDocumentType.INSTANCE, versionToAddSubFor, form, creatingPerson);

    newVersion.getDocumentInstanceSectionClause()
        .setDocumentInstance(versionToAddSubFor.getDocumentInstanceSectionClause().getDocumentInstance());

    instanceSectionClauseRepository.save(newVersion.getDocumentInstanceSectionClause());
    instanceSectionClauseVersionRepository.save(newVersion);

  }

  @Transactional
  public void editClause(DocumentInstanceSectionClauseVersion clauseBeingEdited, ClauseForm form, Person editingPerson) {

    var updatedVersions = documentClauseService
        .editClause(PwaDocumentType.INSTANCE, clauseBeingEdited, form, editingPerson)
        .stream()
        .map(DocumentInstanceSectionClauseVersion.class::cast)
        .collect(Collectors.toList());

    instanceSectionClauseVersionRepository.saveAll(updatedVersions);

  }

  @Transactional
  public void removeClause(Integer clauseId, Person removingPerson) {

    var parentClause = getInstanceClauseVersionByClauseIdOrThrow(clauseId);

    var docInstance = parentClause.getDocumentInstanceSectionClause().getDocumentInstance();

    var deletedClauseVersions = documentClauseService.removeClause(
        parentClause,
        removingPerson,
        clause -> getChildClausesOfParent(docInstance, clause),
        clauses -> getChildClausesOfParents(docInstance, clauses))
        .stream()
        .map(DocumentInstanceSectionClauseVersion.class::cast)
        .collect(Collectors.toList());

    instanceSectionClauseVersionRepository.saveAll(deletedClauseVersions);

  }

  private List<SectionClauseVersion> getChildClausesOfParent(DocumentInstance documentInstance,
                                                             SectionClause clause) {

    var castClause = (DocumentInstanceSectionClause) clause;

    return instanceSectionClauseVersionRepository
        .findByDocumentInstanceSectionClause_DocumentInstanceAndParentDocumentInstanceSectionClause(documentInstance, castClause)
        .stream()
        .map(SectionClauseVersion.class::cast)
        .collect(Collectors.toList());

  }

  private List<SectionClauseVersion> getChildClausesOfParents(DocumentInstance documentInstance,
                                                              Collection<SectionClause> clauses) {

    var castClauses = clauses.stream()
        .map(DocumentInstanceSectionClause.class::cast)
        .collect(Collectors.toList());

    return instanceSectionClauseVersionRepository
        .findByDocumentInstanceSectionClause_DocumentInstanceAndParentDocumentInstanceSectionClauseIn(
            documentInstance, castClauses).stream()
        .map(SectionClauseVersion.class::cast)
        .collect(Collectors.toList());

  }

}
