# Use enum parameters over booleans when defining method parameters, return types etc

* Deciders: cwood, mheyes
* Date: 18-06-2020

## Context and Problem Statement

When defining a method that takes one or more boolean parameters or returns a boolean, we need a readable way to reference 
these.

## Considered Options

* use enum parameters
* use booleans

## Decision Outcome

Chosen option: "use enum parameters", because although it results in more files in the codebase, the readability benefits 
and the possibility to differentiate between as many states as is required (rather than writing code restricted by two states)
outweigh the negative.

This isn't to say that booleans don't have their place, if your method is asking a question to which there is only a Yes/No answer,
e.g. "isMyThingComplete", there would be no need to define an enum to encapsulate the result.

However, if you're using a boolean as a flag in your method, e.g. if true do x, if not do y, using an enum parameter would be much more readable:

    addNewThing(false)

    addNewThing(EmailMethod.DONT_EMAIL)

This readability is increased when there are a number of flags required by your method. 

## Pros and Cons of the Options

### use enum parameters

* Good, because more readable/understandable, especially when encountering code for the first time
* Good, because any number of states can be defined
* Bad, because results in more files in the codebase that require organising

### use booleans

* Good, because doesn't require additional code files
* Bad, because restricted to two states (true or false)
* Bad, because not very readable, sometimes extra thinking required to understand the purpose of the boolean