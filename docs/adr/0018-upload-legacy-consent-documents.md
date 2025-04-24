# Upload legacy consent documents to PWA

## Context and Problem Statement
Before the development of PWA, pipeline consent documents were stored as physical paper documents. 

Now that PWA handles pipeline consent documents, the NSTA wants digital copies of all the legacy consent documents uploaded 
and available to view in the app.

The NSTA will provide the digital copies of the consent documents, along with a spreadsheet containing the information required
to migrate each document to the app and link it to the correct PWA.

The document types we need to migrate to the app are:
- Initial Consent
- Variation Consent
- HUOO Consent

In some cases, the document does not yet have a record on PWA or the record is attached to the wrong PWA.
For these documents, a new document record will need to be added to the correct PWA before we can link the file to it.

The implementation also needs to be able to run more than once. It should be possible to process documents that have not yet been uploaded and linked to a PWA
whilst skipping documents that have already been processed, so that documents can be added in batches as they are ready.

## Potential Solution - Endpoint
The suggested implementation for uploading the files into PWA is to create an endpoint that can be toggled on/off via an environment variable.

First all the files ready to be processed will be uploaded to an S3 bucket separate to the main PWA bucket.

When the endpoint is hit, the files uploaded to the bucket will be compared to a csv of the spreadsheet provided by the NSTA
and each file and its status will be logged in a migration table. The status will state if the file from each row has been found 
in the migration bucket and if it has been successfully migrated. 
Keeping a record of the files that have been migrated means we can run the migration multiple times with different files without reprocessing files from a previous run. 
It also allows potential migration issues to be spotted and resolved.

Each file will have a unique name and can be mapped to the csv by the reference and document type.

Once the files have been checked in the verification step, they will be moved to the main bucket and the references to the files will be created in the app.

## Decision Outcome
We have chosen to implement the above solution.