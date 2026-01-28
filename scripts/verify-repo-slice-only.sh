#!/bin/bash
# Repo Slice Only Architecture Verification Script
# This script verifies ONLY the repo slice maintains hexagonal architecture principles
# Other slices (Language, PullRequest, Commit, etc.) are excluded from this check

set -e

echo "=== Repo Slice Only Architecture Verification ==="
echo ""

ERRORS=0

# Repo slice specific files
REPO_APPLICATION_FILES="src/main/java/com/optimetric/github/application/usecase/*.java src/main/java/com/optimetric/github/application/port/out/*.java src/main/java/com/optimetric/github/application/dto/Repository*.java"
REPO_API_FILES="src/main/java/com/optimetric/github/api/RepoController.java src/main/java/com/optimetric/github/api/request/Repo*.java src/main/java/com/optimetric/github/api/request/GetReposRequest.java src/main/java/com/optimetric/github/api/response/RepoDto.java"

# 1. Application → API imports (Repo slice only)
echo "1. Checking Repo Application → API imports..."
REPO_APP_API_IMPORTS=$(git grep -l "com\.optimetric\.github\.api" $REPO_APPLICATION_FILES 2>/dev/null | wc -l | tr -d ' ')
if [ "$REPO_APP_API_IMPORTS" -gt 0 ]; then
    echo "❌ FAIL: Repo application layer imports API layer"
    git grep "com\.optimetric\.github\.api" $REPO_APPLICATION_FILES
    ERRORS=$((ERRORS + 1))
else
    echo "✅ PASS: No API imports in repo application layer"
fi

echo ""

# 2. Application → Infrastructure imports (Repo slice only)
echo "2. Checking Repo Application → Infrastructure imports..."
REPO_APP_INFRA_IMPORTS=$(git grep -l "com\.optimetric\.github\.infrastructure" $REPO_APPLICATION_FILES 2>/dev/null | wc -l | tr -d ' ')
if [ "$REPO_APP_INFRA_IMPORTS" -gt 0 ]; then
    echo "❌ FAIL: Repo application layer imports Infrastructure layer"
    git grep "com\.optimetric\.github\.infrastructure" $REPO_APPLICATION_FILES
    ERRORS=$((ERRORS + 1))
else
    echo "✅ PASS: No Infrastructure imports in repo application layer"
fi

echo ""

# 3. Application → Persistence Entity imports (Repo slice only)
echo "3. Checking Repo Application → Persistence Entity imports..."
REPO_APP_ENTITY_IMPORTS=$(git grep -l "infrastructure\.persistence\.entity" $REPO_APPLICATION_FILES 2>/dev/null | wc -l | tr -d ' ')
if [ "$REPO_APP_ENTITY_IMPORTS" -gt 0 ]; then
    echo "❌ FAIL: Repo application layer imports Persistence entities"
    git grep "infrastructure\.persistence\.entity" $REPO_APPLICATION_FILES
    ERRORS=$((ERRORS + 1))
else
    echo "✅ PASS: No Persistence entity imports in repo application layer"
fi

echo ""

# 4. Application → JPA Repository imports (Repo slice only)
echo "4. Checking Repo Application → JPA Repository imports..."
REPO_APP_REPO_IMPORTS=$(git grep -l "RepositoryRepository" $REPO_APPLICATION_FILES 2>/dev/null | wc -l | tr -d ' ')
if [ "$REPO_APP_REPO_IMPORTS" -gt 0 ]; then
    echo "❌ FAIL: Repo application layer imports JPA Repository"
    git grep "RepositoryRepository" $REPO_APPLICATION_FILES
    ERRORS=$((ERRORS + 1))
else
    echo "✅ PASS: No JPA Repository imports in repo application layer"
fi

echo ""

# 5. API → Persistence Entity imports (Repo slice only)
echo "5. Checking Repo API → Persistence Entity imports..."
REPO_API_ENTITY_IMPORTS=$(git grep -l "infrastructure\.persistence\.entity" $REPO_API_FILES 2>/dev/null | wc -l | tr -d ' ')
if [ "$REPO_API_ENTITY_IMPORTS" -gt 0 ]; then
    echo "❌ FAIL: Repo API layer imports Persistence entities"
    git grep "infrastructure\.persistence\.entity" $REPO_API_FILES
    ERRORS=$((ERRORS + 1))
else
    echo "✅ PASS: No Persistence entity imports in repo API layer"
fi

echo ""

# 6. RepositoryEntity in Application layer (Repo slice only)
echo "6. Checking RepositoryEntity usage in Repo Application layer..."
REPO_ENTITY_USAGE=$(git grep -l "RepositoryEntity" $REPO_APPLICATION_FILES 2>/dev/null | wc -l | tr -d ' ')
if [ "$REPO_ENTITY_USAGE" -gt 0 ]; then
    echo "❌ FAIL: Repo application layer uses RepositoryEntity"
    git grep "RepositoryEntity" $REPO_APPLICATION_FILES
    ERRORS=$((ERRORS + 1))
else
    echo "✅ PASS: No RepositoryEntity usage in repo application layer"
fi

echo ""

# 7. Port structure check
echo "7. Checking Outbound Port structure..."
if [ -d "src/main/java/com/optimetric/github/application/port/out" ]; then
    PORT_COUNT=$(find src/main/java/com/optimetric/github/application/port/out -name "*Port.java" 2>/dev/null | wc -l | tr -d ' ')
    if [ "$PORT_COUNT" -ge 3 ]; then
        echo "✅ PASS: Outbound ports found in correct location ($PORT_COUNT ports)"
        find src/main/java/com/optimetric/github/application/port/out -name "*Port.java" | sed 's/.*\//   - /'
    else
        echo "⚠️  WARNING: Expected at least 3 outbound ports, found $PORT_COUNT"
    fi
else
    echo "❌ FAIL: Outbound port directory not found"
    ERRORS=$((ERRORS + 1))
fi

echo ""

# 8. Use Case structure check
echo "8. Checking Use Case structure..."
if [ -d "src/main/java/com/optimetric/github/application/usecase" ]; then
    USE_CASE_COUNT=$(find src/main/java/com/optimetric/github/application/usecase -name "*UseCase.java" 2>/dev/null | wc -l | tr -d ' ')
    if [ "$USE_CASE_COUNT" -ge 3 ]; then
        echo "✅ PASS: Use cases found in correct location ($USE_CASE_COUNT use cases)"
        find src/main/java/com/optimetric/github/application/usecase -name "*UseCase.java" | sed 's/.*\//   - /'
    else
        echo "⚠️  WARNING: Expected at least 3 use cases, found $USE_CASE_COUNT"
    fi
else
    echo "❌ FAIL: Use case directory not found"
    ERRORS=$((ERRORS + 1))
fi

echo ""

# 9. Controller → Use Case check
echo "9. Checking RepoController → Use Case usage..."
if git grep -q "UseCase" src/main/java/com/optimetric/github/api/RepoController.java 2>/dev/null; then
    echo "✅ PASS: RepoController uses Use Cases"
else
    echo "❌ FAIL: RepoController does not use Use Cases"
    ERRORS=$((ERRORS + 1))
fi

echo ""

# 10. Adapter structure check
echo "10. Checking Adapter structure..."
REPO_ADAPTERS=$(find src/main/java/com/optimetric/github/infrastructure -name "*Repository*Adapter.java" -o -name "*Repo*Adapter.java" 2>/dev/null | wc -l | tr -d ' ')
if [ "$REPO_ADAPTERS" -ge 2 ]; then
    echo "✅ PASS: Repository adapters found ($REPO_ADAPTERS adapters)"
    find src/main/java/com/optimetric/github/infrastructure -name "*Repository*Adapter.java" -o -name "*Repo*Adapter.java" | sed 's/.*\//   - /'
else
    echo "⚠️  WARNING: Expected at least 2 repository adapters, found $REPO_ADAPTERS"
fi

echo ""

# Summary
echo "=== Summary ==="
if [ $ERRORS -eq 0 ]; then
    echo "✅ All critical checks passed for Repo Slice!"
    exit 0
else
    echo "❌ $ERRORS critical error(s) found in Repo Slice. Please fix before merging."
    exit 1
fi
