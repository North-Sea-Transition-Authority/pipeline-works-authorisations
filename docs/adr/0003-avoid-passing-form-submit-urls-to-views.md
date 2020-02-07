# Avoid passing form submit urls to views

* Deciders: jbarnett, cwood, mheyes
* Date: 07-02-2020

## Context and Problem Statement

Typically we pass reverse routed form submit url into all views with a form, which is used as the form `action` attribute.

Does this actually provide any benefit? Should we continue to do this?

## Considered Options

1. Always pass submit urls
2. Don't bother passing submit urls, unless required due to a unusual route.

## Decision Outcome

Chosen option: 2

As described in our [coding standards](https://confluence.fivium.co.uk/display/JAVA/Spring+Boot#SpringBoot-Controllers), controller GET and corresponding POST handlers should always have the same route.

This provides consistency and allows re-entry into the correct place if the POST required an authentication redirect.

By not passing in a separate form submit url, we can enforce this rule as by default a `form` with no `action` attribute will post to the current url.
It also means one less model attribute to add and one less reverse route to resolve.

In the rare case that the submit url should be different to the current url, this should be done with the reverse routed submit url, but it should not be the default behaviour.