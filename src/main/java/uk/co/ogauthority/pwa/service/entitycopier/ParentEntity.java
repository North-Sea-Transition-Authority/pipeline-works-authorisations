package uk.co.ogauthority.pwa.service.entitycopier;

/**
 * Interface to identify entities which act as a parent for other entities.
 *
 */
public interface ParentEntity {

  /**
   * Returns the Id of the entity without exposing the specific class.
   */
  Object getIdAsParent();

}
