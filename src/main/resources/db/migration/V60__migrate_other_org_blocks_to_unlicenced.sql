UPDATE ${datasource.user}.pad_blocks
SET block_owner = 'UNLICENSED'
WHERE block_owner = 'OTHER_ORGANISATION';

DELETE FROM ${datasource.user}.pad_block_crossing_owners
WHERE pad_block_crossing_id IN (
    SELECT pb.id
    FROM ${datasource.user}.pad_blocks pb
    WHERE pb.block_owner = 'UNLICENSED'
);