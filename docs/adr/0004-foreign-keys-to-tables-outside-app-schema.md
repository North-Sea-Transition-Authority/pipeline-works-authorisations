# Foreign keys to tables outside app schema

* Deciders: mheyes, cayres, jbarnett, cwood
* Date: 05-03-2020

## Context and Problem Statement

Creating tables that reference primary key columns of tables outside of application schemas.

Should we create a foreign key to that table?

## Decision Drivers

* We'd like data integrity.
* We'd like to de-couple parts of the system from EDU.
* Experience with foreign keys shows that various locks can happen if integrity is enforced, which will affect our application.

## Considered Options

* Create foreign keys.
* Don't create foreign keys.
* Create and reference and materialized view.

## Decision Outcome

Chosen option: Don't use foreign keys on non-PWA tables.

### Create foreign keys

* Good, because foreign keys allows us to have full referential integrity
* Good, because Oracle can use foreign keys to improve query performance.
* Bad, because if a table is locked, it'll halt until able to assert that the foreign key is valid.
* Bad, because IRS has had problems relating to referencing tables outside of the application.

### Don't create foreign keys

* Good, because there aren't any slow-downs if a table is locked.
* Bad, because we can't enforce that the data is consistent with EDU data.

### Create and reference and materialized view

* Good, because it allows us to circumvent the foreign key slow-downs
* Good, because we can enforce that the data being referenced is up-to-date with data on EDU.
* Bad, because we can't have a "live" view of EDU data.
* Bad, because we have to schedule an update for the materialized view.
