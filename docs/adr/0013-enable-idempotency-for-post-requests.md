# Enable post requests to be idempotent to prevent duplicate submissions

## Context and Problem Statement

In some parts of the application (eg. in the 'add pipeline' page) we don't have any effective measure in place that prevents users
from spamming the submit button and create duplicate entities.

We need to find a robust solution that allows for back-end validation to check if the user is making the same request multiple times.

## Pros and Cons of the Options

### Use idempotent key to check for duplicate requests. Generate and pass token to model (fds can attach it to form object similarly to csrf tokens), then validate key on post against database

* Good, because it's robust as it enforces database validation
* Bad, because we need to think about latency between requests as we store/query idempotent keys from the database
* Bad, because requires extra conversion back and forth between form objects and entities

### Same method as above, but explore possibility of using embedded Redis db instead of oracle

* Good, because of possible better performance
* Good, because we could leverage Redis db to implement caching in different areas of app
* Bad, because might be high effort for potentially low gain

### Use fds preventDoubleCick prop that's embedded in buttons (not working properly as of time of writing)

* Good, because very low effort
* Bad, because it probably relies on JS and clients may have it disabled
* Bad, because it's pure front-end validation, thus not robust