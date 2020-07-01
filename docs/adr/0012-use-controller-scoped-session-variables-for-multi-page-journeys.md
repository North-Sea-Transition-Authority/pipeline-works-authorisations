# Use controller scoped session variables for multi-page user journeys

* Deciders: mheyes
* Date: 29-06-2020

## Context and Problem Statement

An application form has interconnected data which is persisted in several foreign keyed tables. In order to make a change to the top level data, and have that change reflected correctly across all usages often requires the user to complete a multi-page journey defining the result of the change.

What is the most appropriate way to implement a multi-page journey such that:
* A journey can be left half completed without leaving underlying data in an invalid state.
* Back and forward browser buttons can be used to step through the journey as the user would expect.
* At any stage of the journey the user can jump to an arbitrary point to make changes.
* There is minimal boiler plate code

## Considered Options

* Flash scope redirect attributes
* Controller scoped session variables
* Cookies containing journey data
* Persisting journey data in database (Posting simple forms)
* URL query params

## Decision Outcome

Chosen option: Controller scoped session variables

Spring provides out of the box support for controller scoped session variables. This means that we would get out of the box
* no overhead for protecting in-progress or completed journeys as session data is cleared on logout and session data is only available to the logged in user
* no need to create database entities their Java mappings or repos.
* can construct journeys of arbitrary complexity in bespoke pojos (that implement serializable correctly)
* data set from one end point and is accessible from any other within the same controller, this will avoid manual data transfer from one place to another
* Users able to use browser back button and refresh page functionality

Cons:
* Data serialisation means that its possible there will be breaking changes which cause app errors for users who maintain a session over a application upgrade.
-> mitigation: create patches removing session data when breaking changes are made.
* Some code overhead will be required to make sure that a user leaving a journey incomplete can restart journeys without getting data pre-populated that doesnt make any sense.
-> mitigation: create a standard "journey key" pattern and general case interface to implement in each journey object which handles journey start/end/resumption.
* Not a feature where devs have a lot of experience meaning there will be some overhead on devs encountering the code for the first time when creating/changing journeys.
-> mitigation: Standardise implementation around 1 backing journey object and one journey controller to limit complexity and keep code contained in single location.  

## Pros and Cons of the Options not chosen

### Flash scope redirect attributes
* Bad: cannot refresh the page without losing journey progress (Blocker)
* Bad: lots of manual processing of journey data on every endpoint in journey
* Good: simple to get started

### Cookies containing journey data
* Neutral: Appear to shares pretty much all the pros and cons associated with using session variables regarding data serialisation, journey resets and navigation.
* Bad: storing anything on the clients means the clients can do weird things we dont expect
* Bad: unknown if cookies can nicely support complex data which might be required.
* Bad: unfamiliar pattern: potentially many surprises/limitations 

### Persisting journey data in database (Posting simple forms)
* Good: very familiar pattern for all devs - few implementation surprises likely
* Bad: introduces permanent storage of transient data. Storage of old and incomplete journeys need to be tidied up.
* Bad: need to handle returning to old journeys where underlying data might be out of date
* Bad: Introduces new concerns around securing journey by application and user
* Neutral: Same complexities as other approaches where journeys involving groups requires some kind of group key that will need to be persisted. Underlying changes to data can mean groups keys no longer make sense. This should be dealt with nicely.   

### URL query params
* Good: simple controller markup
* Good: no variable serialisation and deserialisation(*all journey data needs to simple enough for url mapping)
* Good: browser refresh would work out of the box
* Bad: every element of the journey become bookmarkable and would be expected to work/ handle being abused.
* Bad: will very likely not extend well as use cases become complex and change over time
* Bad: for long journeys, every endpoint will need to suport all journey data in the url which could get messy.
