# Store wuaId integers for audit purposes rather than fully mapped WebUserAccount entities

* Deciders: cwood, jbarnett, mheyes
* Date: 03-02-2020

## Context and Problem Statement

When entities are updated we want to record who performed that action for audit purposes.

How should this user be recorded?

## Considered Options
1. Store the userId as a simple integer field on the parent entity.
2. Have a ManyToOne association with a child WebUserAccount entity on the parent entity which is eagerly loaded.
3. Have a ManyToOne association with a child WebUserAccount entity on the parent entity which is lazily loaded.

## Decision Outcome

Chosen option 1, as this is the simplest and most performant way to record this information in a way which meets the requirement.

In the vast majority of cases we do not need the additional information the full WebUserAccount entity carries. We only care who did the modification, not what their name or title is as we don't display this data on screen.

In the event that we do want this information, we can always access it via a second fetch, or have a projected version of the entity with the association and use this when needed.

As discussed in [ADR-0002 - Avoid Lazy Loading](0002-avoid-lazy-loading.md) we do not want to use lazy loaded entities where possible for the reasons described in that ADR.

### Positive Consequences 

* Fetching of entities is faster, as the object graph is smaller.
* Entity definitions are simpler.
* There is no risk of trying to fetch lazily loaded members out of transaction.

### Negative Consequences

If we do want to get the name, email address etc. of the user who did the modification for front-end display purposes we can't just get it directly from the entity.

However in this case the options as described in the 'Decision Outcome' section can be used.