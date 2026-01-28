-- Verify that the constraint was successfully updated
-- Run this query in DBeaver to check if the constraint includes all job types

SELECT 
    conname AS constraint_name,
    pg_get_constraintdef(oid) AS constraint_definition
FROM pg_constraint
WHERE conrelid = 'github_sync_jobs'::regclass
  AND contype = 'c'
  AND conname LIKE '%job_type%';

-- Expected result should show:
-- constraint_name: github_sync_jobs_job_type_check
-- constraint_definition: CHECK (job_type = ANY (ARRAY['PR_LIST'::character varying, 'PR_DETAIL'::character varying, 'PR_REVIEWS'::character varying, 'PR_COMMITS'::character varying, 'BRANCH_SYNC'::character varying, 'COMMIT_SYNC'::character varying, 'LANGUAGE_SYNC'::character varying]))
-- 
-- Or similar, showing all 7 job types including BRANCH_SYNC, COMMIT_SYNC, LANGUAGE_SYNC
