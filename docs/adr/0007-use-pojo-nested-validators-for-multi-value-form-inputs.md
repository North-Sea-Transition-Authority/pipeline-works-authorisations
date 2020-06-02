# Use POJO nested validators for multi-value form inputs

* Deciders: cwood, mheyes
* Date: 01-06-2020

## Context and Problem Statement

Html forms often have multiple inputs represent a single form data item, e.g dates with separate fields for 
days, months and years.
 
Naively mapping each as a separate member variable on the POJO representing the html form 
leads to copy paste of form fields, duplicated validation logic and potentially inconsistently validated forms.

## Considered Options
Using a three input date widget as an example:
1. Naively map day, month and year as direct member variables on the form POJO
2. Create a generic component, e.g ThreeFieldDateInput, and execute nested validator on the object


## Decision Outcome

Chosen option: 2

* Having a single pojo a generic validator which supports that pojo simplifies development of further forms with the same input requirements.
* We go from 3 member variables(ymmv) on the form to a single object.
* Validation of the form POJO can be as simple or complex as needed but common validation rules can be coded once and reused simply.
* Embraces Single responsibility principle by extracting common validation requirements into one place.




