DECLARE
  l_mm_page_break VARCHAR2(20) := 'PAGE_BREAK';
  l_mm_page_break_count NUMBER;
BEGIN

  -- Check if PAGE_BREAK mail merge field already exists
  SELECT COUNT(*)
  INTO l_mm_page_break_count
  FROM ${datasource.user}.mail_merge_fields
  WHERE mnem = l_mm_page_break;

  IF l_mm_page_break_count = 0 THEN
    INSERT INTO ${datasource.user}.mail_merge_fields (mnem, type)
    VALUES (l_mm_page_break, 'AUTOMATIC');
  END IF;

END;
/
