package uk.co.ogauthority.pwa.service.entitycopier;

/**
 * Interface to identify entities which have a single parent and allow manipulation of that relationship.
 *
 * @param <I> type used for child entity ID
 * @param <P> type of entity's Parent.
 */
public interface ChildEntity<I, P extends ParentEntity> {

  I getId();

  void clearId();

  void setParent(P parentEntity);

  P getParent();

}
