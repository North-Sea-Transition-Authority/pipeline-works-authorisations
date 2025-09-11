File upload library migration

1. create a sideways copy of the table in promotemgr and add a fresh table
2. run V01__push_files_to_promote.sql
3. run the s3 migration tool
4. run V02__pull_file_data_into_uploaded_files.sql
5. check the file ids in the file_id_map table
6. run V03__replace_old_file_ids.sql
7. check the migration is successful with V04__check_file_migration_success

Legacy consent document migration
1. enable the devtools configuration
2. upload the documents and migration csv file to the secure-migration bucket for the correct environment
3. hit the verification endpoint (devtool/migration/consent-documents/verify)
4. check the migration tables for any missed files/errors
5. hit the migration endpoint (devtool/migration/consent-documents/migrate)