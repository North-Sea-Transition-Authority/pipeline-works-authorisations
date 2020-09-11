package uk.co.ogauthority.pwa.service.entitycopier;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>This service is designed to make copying simple ORM Entities easy and attempts to recreate a
 * SQL style INSERT INTO SELECT.</p>
 *
 * <p>When to use this service:<ul>
 * <li> When only a small (rule of thumb less than 3) number of attributes need to be manually set after copying</li>
 * <li>When the entity contains simple data with no "downward" entity links. Assumption made when creating class
 * is that bi-directional relationship are not modelled on entities, only in the "upwards" direction.</li></ul></p>
 *
 * <p>When not to use this service:
 * -> When a significant number of fields need to be changed or ignored as part of the copying process,
 * a copy constructor is likely the better way to go as it gives more control and avoids the need to detach entities
 * from the persistence context.</p>
 *
 * <p>Risks associated with this class:<ul>
 *   <li>If an entity we are duplicating already exists in the persistence context, its possible that changes such as the
 *   parent re-targeting will propagate outside the scope of the service methods. This will become a problem if the detached
 *   entity is reattached to the persistence context and changes are persisted.</li>
 * </ul></p>
 */
@Service
public class EntityCopyingService {

  private final EntityManager entityManager;

  @Autowired
  public EntityCopyingService(EntityManager entityManager) {
    this.entityManager = entityManager;
  }


  /**
   * Duplicate single entity, retarget the parent, and return new entity.
   *
   * @param getEntityToCopy supplies the entity to duplicate
   * @param parentEntity the entity to be set as the parent of the duplicated entity
   * @param duplicationEntityClass the class of the entity to duplicate
   * @param <I> This is the class which represents the ID of T
   * @param <P> this is the class which is the PARENT of T
   * @param <T> this is the class of the entity to duplicate
   *
   * @return newly created ChildEntity attached to the persistence context
   */
  public <I, P, T extends ChildEntity<I, P>> T duplicateEntityAndSetParent(Supplier<T> getEntityToCopy,
                                                                                       P parentEntity,
                                                                                       Class<T> duplicationEntityClass) {
    var newEntityId = duplicateEntityAndSetParentAndReturnId(getEntityToCopy, parentEntity, duplicationEntityClass);

    return entityManager.find(duplicationEntityClass, newEntityId);
  }

  /**
   * Duplicate collection of entities, retarget each entity's parent, and return Set of Ids associated with created entities.
   * The Calling code is left to decide what do with the ids, as the objects themselves may not be required for further processing.
   *
   * @param getEntitiesToCopy supplies a collection of entity objects which require duplicating
   * @param parentEntity the entity to be set as the parent of each entity to be duplicated
   * @param duplicationEntityClass the class of the entity to duplicate
   * @param <I> This is the class which represents the ID of T
   * @param <P> this is the class which is the PARENT of T
   * @param <T> this is the class of the entity to duplicate
   *
   * @return the set of newly created ChildEntity Ids;
   */
  public <I, P, T extends ChildEntity<I, P>> Set<I> duplicateEntitiesAndSetParent(Supplier<Collection<T>> getEntitiesToCopy,
                                                                                  P parentEntity,
                                                                                  Class<T> duplicationEntityClass) {
    var entitiesToCopy = getEntitiesToCopy.get();
    var newEntityIds = new HashSet<I>();

    entitiesToCopy.forEach(entityToDuplicate -> newEntityIds.add(duplicateEntityAndSetParentAndReturnId(
        () -> entityToDuplicate,
        parentEntity,
        duplicationEntityClass
    )));

    return newEntityIds;


  }



  private <I, P, T extends ChildEntity<I, P>> I duplicateEntityAndSetParentAndReturnId(Supplier<T> getEntityToCopy,
                                                                                       P parentEntity,
                                                                                       Class<T> duplicationEntityClass) {
    var entityToDuplicate = getEntityToCopy.get();
    // we dont want updates to this specific object to be persisted, so detach original entity from the persistence context.
    entityManager.detach(entityToDuplicate);
    // clear the id. Is this needed?
    entityToDuplicate.clearId();
    entityToDuplicate.setParent(parentEntity);
    entityManager.persist(entityToDuplicate);
    // force persistence context to provide a new object reference for the new entity when object is requested
    entityManager.detach(entityToDuplicate);
    // return the ID of the new new entity
    return entityToDuplicate.getId();
  }


}

