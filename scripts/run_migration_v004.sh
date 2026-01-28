#!/bin/bash

# Script to run V004 migration manually
# Updates github_sync_jobs job_type constraint to include new job types

# Database connection details (from application.yml)
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="optimetric"
DB_USER="optimetric_user"
DB_PASSWORD="optimetric_pass"

# Migration file path
MIGRATION_FILE="src/main/resources/db/migration/V004__update_sync_jobs_job_type_constraint.sql"

echo "=========================================="
echo "Running V004 Migration"
echo "=========================================="
echo "Database: $DB_NAME"
echo "User: $DB_USER"
echo "Migration: $MIGRATION_FILE"
echo ""

# Check if migration file exists
if [ ! -f "$MIGRATION_FILE" ]; then
    echo "❌ Error: Migration file not found: $MIGRATION_FILE"
    exit 1
fi

# Run migration using psql with password from environment variable
export PGPASSWORD="$DB_PASSWORD"

echo "Executing migration..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$MIGRATION_FILE"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Migration completed successfully!"
    echo ""
    echo "The constraint has been updated to include:"
    echo "  - PR_LIST, PR_DETAIL, PR_REVIEWS, PR_COMMITS"
    echo "  - BRANCH_SYNC, COMMIT_SYNC, LANGUAGE_SYNC"
    echo ""
    echo "You can now restart your application and test the analyze endpoint."
else
    echo ""
    echo "❌ Migration failed. Please check the error message above."
    exit 1
fi

# Clear password from environment
unset PGPASSWORD
