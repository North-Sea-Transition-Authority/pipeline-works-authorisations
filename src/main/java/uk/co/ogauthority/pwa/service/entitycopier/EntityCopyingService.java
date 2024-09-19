package uk.co.ogauthority.pwa.service.entitycopier;

import static java.util.stream.Collectors.toMap;

import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.EntityCopyingException;

/**
 * <p>This service is designed to make copying simple ORM Entities easy.</p>
 *
 * <p>When to use this service:<ul>
 * <li> When only a small (rule of thumb less than 3) number of attributes need to be manually set after copying</li>
 * <li>When the entity contains simple data with no "downward" entity links. Assumption made when creating class
 * is that bi-directional relationship are not modelled on entities, only in the "upwards" direction.</li></ul></p>
 *
 * <p>When not to use this service:
 * -> When a significant number of fields need to be changed or ignored as part of the copying process,
 * a copy constructor is likely the better way to go as it gives more control.</p>
 *
 * <p>Risks associated with this class:<ul>
 * <li>Reflection is used to copy field values. This might lead to shared object references between the two versions
 * if not careful. Risk here that a change to one object might impact the new duplicated entity.</li>
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
   * @param getEntityToCopy        supplies the entity to duplicate
   * @param parentEntity           the entity to be set as the parent of the duplicated entity
   * @param duplicationEntityClass the class of the entity to duplicate
   * @param <I>                    This is the class which represents the ID of T
   * @param <P>                    this is the class which is the PARENT of T
   * @param <T>                    this is the class of the entity to duplicate
   * @return newly created ChildEntity attached to the persistence context
   */
  public <I, P extends ParentEntity, T extends ChildEntity<I, P>> T duplicateEntityAndSetParent(
      Supplier<T> getEntityToCopy,
      P parentEntity,
      Class<T> duplicationEntityClass) {
    return duplicateEntityAndSetParentAndReturnInstance(getEntityToCopy, parentEntity, duplicationEntityClass);
  }

  /**
   * Duplicate collection of entities, retarget each entity's parent, and return Set of Ids associated with created entities.
   * The Calling code is left to decide what do with the ids, as the objects themselves may not be required for further processing.
   *
   * @param getEntitiesToCopy      supplies a collection of entity objects which require duplicating
   * @param parentEntity           the entity to be set as the parent of each entity to be duplicated
   * @param duplicationEntityClass the class of the entity to duplicate
   * @param <I>                    This is the class which represents the ID of T
   * @param <P>                    this is the class which is the PARENT of T
   * @param <T>                    this is the class of the entity to duplicate
   * @return a set of objects mapping the original entity id to the corresponding duplicated entity id.
   */
  public <I, P extends ParentEntity, T extends ChildEntity<I, P>> Set<CopiedEntityIdTuple<I, T>> duplicateEntitiesAndSetParent(
      Supplier<Collection<T>> getEntitiesToCopy,
      P parentEntity,
      Class<T> duplicationEntityClass) {
    var entitiesToCopy = getEntitiesToCopy.get();
    var copiedEntityIdTuples = new HashSet<CopiedEntityIdTuple<I, T>>();

    entitiesToCopy.forEach(entityToDuplicate -> {

      // create and persist the duplicate entity with the new parent
      var duplicatedEntity = duplicateEntityAndSetParentAndReturnInstance(
          () -> entityToDuplicate,
          parentEntity,
          duplicationEntityClass
      );

      // create a tuple to allow mapping from previous version entity to new version entity
      var copiedEntityIdTuple = new CopiedEntityIdTuple<>(
          duplicationEntityClass,
          entityToDuplicate.getId(),
          duplicatedEntity.getId()
      );

      copiedEntityIdTuples.add(copiedEntityIdTuple);
    });

    return copiedEntityIdTuples;

  }

  /**
   * Duplicate collection of entities, retarget each entity's parent, and return Set of Ids associated with created entities.
   * The Calling code is left to decide what do with the ids, as the objects themselves may not be required for further processing.
   *
   * @param getEntitiesToCopy          supplies a collection of entity objects which require duplicating
   * @param copiedParentEntityIdTuples a set of copiedEntityIdTuples to allow mapping of new children duplicates to duplicated parents
   * @param duplicationEntityClass     the class of the entity to duplicate
   * @param <I>                        This is the class which represents the ID of T
   * @param <P>                        this is the class which is the PARENT of T
   * @param <T>                        this is the class of the entity to duplicate
   * @return a set of objects mapping the original entity id to the corresponding duplicated entity id.
   */
  public <I,
      P extends ParentEntity,
      T extends ChildEntity<I, P>> Set<CopiedEntityIdTuple<I, T>>  duplicateEntitiesAndSetParentFromCopiedEntities(
      Supplier<Collection<T>> getEntitiesToCopy,
      Set<CopiedEntityIdTuple<I, P>> copiedParentEntityIdTuples,
      Class<T> duplicationEntityClass) {
    var entitiesToCopy = getEntitiesToCopy.get();
    var copiedEntityIdTuples = new HashSet<CopiedEntityIdTuple<I, T>>();

    Map<Object, P> mapOfOriginalParentIdToDuplicatedParentEntityReference = copiedParentEntityIdTuples.stream()
        .collect(toMap(
            CopiedEntityIdTuple::getOriginalEntityId,
            o -> entityManager.getReference(o.getEntityClass(), o.getDuplicateEntityId())
        ));

    entitiesToCopy.forEach(entityToDuplicate -> {
      // get a reference the entity to be set as the parent of the duplicate
      var duplicateParentEntity = mapOfOriginalParentIdToDuplicatedParentEntityReference.get(
          entityToDuplicate.getParent().getIdAsParent());

      // create and persist the duplicate entity with the new pareent set
      var duplicatedEntity = duplicateEntityAndSetParentAndReturnInstance(
          () -> entityToDuplicate,
          duplicateParentEntity,
          duplicationEntityClass
      );

      // create a tuple to allow mapping from previous version entity to new version entity
      var copiedEntityIdTuple = new CopiedEntityIdTuple<>(
          duplicationEntityClass,
          entityToDuplicate.getId(),
          duplicatedEntity.getId()
      );

      copiedEntityIdTuples.add(copiedEntityIdTuple);
    });

    return copiedEntityIdTuples;

  }


  private <I, P extends ParentEntity, T extends ChildEntity<I, P>> T duplicateEntityAndSetParentAndReturnInstance(
      Supplier<T> getEntityToCopy,
      P parentEntity,
      Class<T> duplicationClass
  ) {
    var entityToDuplicate = getEntityToCopy.get();

    try {
      T newInstance = duplicationClass.getConstructor().newInstance();

      for (Field field : FieldUtils.getAllFields(duplicationClass)) {
        if (!field.isSynthetic()) {
          var value = FieldUtils.readField(field, entityToDuplicate, true);

          FieldUtils.writeField(field, newInstance, value, true);

        }
      }
      newInstance.clearId();
      newInstance.setParent(parentEntity);
      entityManager.persist(newInstance);

      return newInstance;
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
      throw new EntityCopyingException(
          String.format(
              "Could not copy entity of class %s with id: %s ",
              duplicationClass.getSimpleName(),
              entityToDuplicate.getId().toString()
          ),
          e
      );
    }

  }


  /**
   * Convert CopiedEntityidTuple into a map from the original entity Id to a reference of the new Entity.
   *
   * @param copiedEntityIdTuples entity ids to convert into map
   * @param <I>                  type of entity Id
   * @param <T>                  type of entity
   */
  public <I, T> Map<I, T> createMapOfOriginalIdToNewEntityReference(
      Set<CopiedEntityIdTuple<I, T>> copiedEntityIdTuples) {
    return copiedEntityIdTuples.stream()
        .collect(toMap(
            CopiedEntityIdTuple::getOriginalEntityId,
            o -> entityManager.getReference(o.getEntityClass(), o.getDuplicateEntityId())
        ));
  }

}

