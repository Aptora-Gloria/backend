-- Update github_sync_jobs job_type check constraint to include new job types
-- New job types: BRANCH_SYNC, COMMIT_SYNC, LANGUAGE_SYNC
-- 
-- Copy and paste this SQL into your PostgreSQL client (pgAdmin, DBeaver, etc.)

-- Drop all existing check constraints on job_type column
-- PostgreSQL may have created constraints with different names
DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    -- Find and drop all check constraints on job_type column
    FOR constraint_name IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'github_sync_jobs'::regclass
        AND contype = 'c'
        AND conname LIKE '%job_type%'
    LOOP
        EXECUTE 'ALTER TABLE github_sync_jobs DROP CONSTRAINT IF EXISTS ' || constraint_name;
    END LOOP;
END $$;

-- Add the new constraint with all job types
ALTER TABLE github_sync_jobs 
ADD CONSTRAINT github_sync_jobs_job_type_check 
CHECK (job_type IN ('PR_LIST', 'PR_DETAIL', 'PR_REVIEWS', 'PR_COMMITS', 'BRANCH_SYNC', 'COMMIT_SYNC', 'LANGUAGE_SYNC'));

-- Verify the constraint was added
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conrelid = 'github_sync_jobs'::regclass 
AND contype = 'c' 
AND conname LIKE '%job_type%';
