# Flash message guidelines

* Deciders: cwood, mheyes
* Date: 18-06-2020

## Context and Problem Statement

We need a consistent way to inform users of completion of actions and non-blocking errors (e.g. someone has already
done the thing you were trying to do).

## Decision Outcome

Using flash message categories appropriately will help users to understand the message we are trying to get across.

* success for positive user actions (e.g. review completed, consultation response sent)
* info for completion of negative/passive user actions (e.g. removed HUOO from application)
* error for errors (e.g. couldn't complete review because it has already been completed)