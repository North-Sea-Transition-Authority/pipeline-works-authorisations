# Migrating files from Oracle to S3 for the conversion to the File Upload Library

## Context and Problem Statement
PWA has been converted from handling file management itself to using the File Upload Spring Starter (FUSS).
PWA stores all of its files in an Oracle database. FUSS stores all of its files in S3.
This means that all of the files currently stored in Oracle need to be migrated over to S3.

For most of the files, we can add the required metadata and move them to S3. 
However, a few file upload areas require additional links to other entities stored in the Oracle database.
The files that will need additional consideration are listed [here](../system-overviews/0004-legacy-file-upload-links.md)

## Chosen Approach
The technical approach that has been chosen to complete the migration is to implement a set of scripts and use the S3 migration tool.
This has been done in the past by other services (IRS for example) which have been converted to use FUSS.

The set of scripts include: 
- Creating the migration entities
- Collecting all of the file data 
- Mapping the file metadata once it has been uploaded to S3

For each document type the files need to be inserted into the `promotemgr.s3_file_migration` table with the relevant metadata.

Once the files have been put into `promotemgr.s3_file_migration` the s3 migrator script needs to be run to move the file data to S3.

Once the files have been moved to S3, the files need to be processed into `file_upload_library_uploaded_files` for each document type.

When the file metadata is mapped to the files, the document types that have links to the legacy entities will need to 
set the previously stored file id in the associated AppFile/PadFile to the new file id assigned in `file_upload_library_uploaded_files`.

Additional clean-up will also be required to remove the file data from the document types that do not require any legacy systems.