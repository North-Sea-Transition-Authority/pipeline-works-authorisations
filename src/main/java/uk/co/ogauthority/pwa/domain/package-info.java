
/**
 * This module is containing objects and services for core business logic concepts.
 *
 * <p>This module contains code that is going to be key to implementing many application use cases.</p>
 *
 * <p>Functionality that exists to support a single screen or single use case does not belong here.</p>
 *
 * <p>Nothing included in this package should refer to anything outside (in an upper level).
 * That means no(or very very limited) Spring framework code, Hibernate specific code, or simply application use case specific code.</p>
 *
 * <p>This package exists to support a "vertical slice" architecture (with the goal to move to a "package by component"
 * style) where certain domain critical logic has been seperated out due its general purpose applicability.</p>
 */
package uk.co.ogauthority.pwa.domain;