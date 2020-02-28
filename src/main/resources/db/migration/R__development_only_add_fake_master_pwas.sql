DECLARE

  l_master_id NUMBER;
BEGIN

  INSERT INTO ${datasource.user}.pwas (holder_ou_id, created_timestamp) VALUES (
    50
   , SYSTIMESTAMP
  ) RETURNING id INTO l_master_id;


  INSERT INTO ${datasource.user}.pwa_details (
    pwa_id
  , pwa_status
  , reference
  , start_timestamp
  ) VALUES (
   l_master_id
 , 'CONSENTED'
 , 'PWA-Example-BP-1'
 , SYSTIMESTAMP
 ) ;

  INSERT INTO ${datasource.user}.pwas (holder_ou_id, created_timestamp) VALUES (
   50
 , SYSTIMESTAMP
 ) RETURNING id INTO l_master_id;

  INSERT INTO ${datasource.user}.pwa_details (
   pwa_id
 , pwa_status
 , reference
 , start_timestamp
  ) VALUES (
   l_master_id
 , 'CONSENTED'
 , 'PWA-Example-BP-2'
 , SYSTIMESTAMP
 ) ;

  INSERT INTO ${datasource.user}.pwas (holder_ou_id, created_timestamp) VALUES (
   12
 , SYSTIMESTAMP
 ) RETURNING id INTO l_master_id;

  INSERT INTO ${datasource.user}.pwa_details (
   pwa_id
 , pwa_status
 , reference
 , start_timestamp
  ) VALUES (
   l_master_id
 , 'CONSENTED'
 , 'PWA-Example-SHELL-1'
 , SYSTIMESTAMP
 );
END;

