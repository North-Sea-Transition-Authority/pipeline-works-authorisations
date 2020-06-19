# Don't rely on PrePersist or PreUpdate annotations if using transient fields

* Deciders: cwood, jbarnett
* Date: 18-06-2020

## Context and Problem Statement

When using a Transient field on an entity in order to better encapsulate some of the entity's fields (e.g. using a coordinate
object to access the individual coordinate degrees, minutes, seconds etc), we need a way to update the underlying fields 
when the Transient object is updated. 

## Considered Options

* use PrePersist/PreUpdate annotations on the update methods
* modify the setters on the Transient object to update the underlying fields

## Decision Outcome

Chosen option: "modify the setters..", because PrePersist/PreUpdate do not fire if the only object that has changed 
is the Transient object. This means that if a user was to go in and, as in the coordinate example mentioned above, change
only the coordinates, if only the Transient coordinate object is updated, the PrePersist/PreUpdate trigger does not fire
and the underlying fields are not updated by JPA/Hibernate. This means that the user's change is not saved. This is
because these annotations appear to only recognise changes to the entity's fields that are linked to the database. 

Modifying the setters for the Transient object ensures that whenever the Transient object is updated, its underlying 
entity fields are also updated and will be saved by JPA when requested.