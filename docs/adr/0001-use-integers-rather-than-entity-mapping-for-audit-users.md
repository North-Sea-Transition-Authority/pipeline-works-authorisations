# Map user ids stored for audit purposes only instead of their entity class 

* Deciders: cwood, jbarnett
* Date: 03-02-2020

## Context and Problem Statement

The user class used for auditing can be a performance burden when queried as this contains the list of user privileges which is slow to retrieve. 
When working with most entities, we don't need to see any of this user information, making the performance impact unnecessary. How can this be improved?

## Considered Options

* map user objects on entities to their entity class (e.g. AuthenticatedUserAccount)
* store only the id of a user object on an entity

## Decision Outcome

Chosen option: "store only the id...", because we can still access the user information if required, but in the majority of cases we can ignore it, speeding up the performance of queries.