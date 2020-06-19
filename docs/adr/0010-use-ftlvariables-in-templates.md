# Define ftlvariables in .ftl templates to assist devs and avoid IntelliJ warnings

* Deciders: cwood
* Date: 18-06-2020

## Context and Problem Statement

As .ftl templates are not compiled, issues such as accessing invalid model attributes in a template are only experienced at runtime.
IntelliJ tries to help with this by highlighting any attributes accessed in the templates that it cannot recognise or resolve.

We need a way of defining model attributes that a template is expecting in order to:
- avoid IntelliJ warnings
- provide better IntelliJ auto-completion in templates
- signal to devs what should be passed into each template to cut down on unexpected runtime errors and avoid the need
to check the controller to see what is being passed in

## Considered Options

* use ftlvariables
* increase scope of tests to cover checking existence of expected model attributes

## Decision Outcome

Chosen option: "use ftlvariables", because it has a higher value/effort ratio than covering the issue with tests. 

As most (if not all) template runtime issues are caught by devs testing their functionality or internal QA, the main benefit
of this decision is to make devs aware what a template is expecting/what is accessible in the template without having to 
check the controllers. If these were only defined in tests, this goal wouldn't be achieved.