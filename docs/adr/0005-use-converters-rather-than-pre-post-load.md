# Use JPA Attribute Converters over Hibernate PrePersist/PostLoad hooks

* Deciders: cwood, mheyes
* Date: 17/03/2020

## Context and Problem Statement

Some of our database tables use CSV strings to store lists of basic enum data (for example, team roles). This reduces the
overhead of having a separate linked table to store these. 

We need to be able to access these in a Java collection, without having to manually process the CSV string before use.  

## Considered Options

1. Use Hibernate PrePersist/PostLoad hooks
2. Use JPA Attribute Converters

## Decision Outcome

Chosen option: 2

While Hibernate PrePersist/PostLoad hooks achieve the desired result, using them requires the addition of a Transient 
collection field on the entity and additional methods on it to process either the csv field or the collection field before
persist/load.

JPA Attribute Converters are designed specifically for this purpose and only require the addition of an annotation on the 
field that should be converted. The pre/post-processing of the values is separated into its own class and can still be 
adequately tested. Using these results in cleaner entity classes.