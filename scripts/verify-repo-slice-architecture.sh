#!/bin/bash
# Repo Slice Architecture Verification Script
# This script verifies that the repo slice maintains hexagonal architecture principles

set -e

echo "=== Repo Slice Architecture Verification ==="
echo ""

ERRORS=0

# 1. Application → API imports
echo "1. Checking Application → API imports..."
if git grep -q "com\.optimetric\.github\.api" src/main/java/com/optimetric/github/application 2>/dev/null; then
    echo "❌ FAIL: Application layer imports API layer"
    git grep "com\.optimetric\.github\.api" src/main/java/com/optimetric/github/application
    ERRORS=$((ERRORS + 1))
else
    echo "✅ PASS: No API imports in application layer"
fi

echo ""

# 2. Application → Infrastructure imports
echo "2. Checking Application → Infrastructure imports..."
if git grep -q "com\.optimetric\.github\.infrastructure" src/main/java/com/optimetric/github/application 2>/dev/null; then
    echo "❌ FAIL: Application layer imports Infrastructure layer"
    git grep "com\.optimetric\.github\.infrastructure" src/main/java/com/optimetric/github/application
    ERRORS=$((ERRORS + 1))
else
    echo "✅ PASS: No Infrastructure imports in application layer"
fi

echo ""

# 3. Application → Persistence Entity imports
echo "3. Checking Application → Persistence Entity imports..."
if git grep -q "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/application 2>/dev/null; then
    echo "❌ FAIL: Application layer imports Persistence entities"
    git grep "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/application
    ERRORS=$((ERRORS + 1))
else
    echo "✅ PASS: No Persistence entity imports in application layer"
fi

echo ""

# 4. Application → JPA Repository imports
echo "4. Checking Application → JPA Repository imports..."
if git grep -q "RepositoryRepository" src/main/java/com/optimetric/github/application 2>/dev/null; then
    echo "❌ FAIL: Application layer imports JPA Repository"
    git grep "RepositoryRepository" src/main/java/com/optimetric/github/application
    ERRORS=$((ERRORS + 1))
else
    echo "✅ PASS: No JPA Repository imports in application layer"
fi

echo ""

# 5. API → Persistence Entity imports
echo "5. Checking API → Persistence Entity imports..."
if git grep -q "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/api 2>/dev/null; then
    echo "❌ FAIL: API layer imports Persistence entities"
    git grep "infrastructure\.persistence\.entity" src/main/java/com/optimetric/github/api
    ERRORS=$((ERRORS + 1))
else
    echo "✅ PASS: No Persistence entity imports in API layer"
fi

echo ""

# 6. RepositoryEntity in Application layer
echo "6. Checking RepositoryEntity usage in Application layer..."
if git grep -q "RepositoryEntity" src/main/java/com/optimetric/github/application 2>/dev/null; then
    echo "❌ FAIL: Application layer uses RepositoryEntity"
    git grep "RepositoryEntity" src/main/java/com/optimetric/github/application
    ERRORS=$((ERRORS + 1))
else
    echo "✅ PASS: No RepositoryEntity usage in application layer"
fi

echo ""

# 7. Infrastructure → Use Case usage (warning only, adapters can use use cases)
echo "7. Checking Infrastructure → Use Case usage..."
USE_CASE_MATCHES=$(git grep -l "usecase\|UseCase" src/main/java/com/optimetric/github/infrastructure 2>/dev/null | wc -l | tr -d ' ')
if [ "$USE_CASE_MATCHES" -gt 0 ]; then
    echo "⚠️  WARNING: Infrastructure layer uses UseCase ($USE_CASE_MATCHES files)"
    echo "   This is acceptable if adapters only call use cases, not orchestrate business logic"
    git grep "usecase\|UseCase" src/main/java/com/optimetric/github/infrastructure | head -5
else
    echo "✅ PASS: No UseCase usage in Infrastructure layer"
fi

echo ""

# 8. Port structure check
echo "8. Checking Outbound Port structure..."
if [ -d "src/main/java/com/optimetric/github/application/port/out" ]; then
    PORT_COUNT=$(find src/main/java/com/optimetric/github/application/port/out -name "*.java" 2>/dev/null | wc -l | tr -d ' ')
    if [ "$PORT_COUNT" -ge 3 ]; then
        echo "✅ PASS: Outbound ports found in correct location ($PORT_COUNT ports)"
        find src/main/java/com/optimetric/github/application/port/out -name "*.java" | sed 's/.*\//   - /'
    else
        echo "⚠️  WARNING: Expected at least 3 outbound ports, found $PORT_COUNT"
    fi
else
    echo "❌ FAIL: Outbound port directory not found"
    ERRORS=$((ERRORS + 1))
fi

echo ""

# 9. Use Case structure check
echo "9. Checking Use Case structure..."
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

# 10. Controller → Use Case check
echo "10. Checking Controller → Use Case usage..."
if git grep -q "UseCase" src/main/java/com/optimetric/github/api/RepoController.java 2>/dev/null; then
    echo "✅ PASS: Controller uses Use Cases"
else
    echo "❌ FAIL: Controller does not use Use Cases"
    ERRORS=$((ERRORS + 1))
fi

echo ""

# Summary
echo "=== Summary ==="
if [ $ERRORS -eq 0 ]; then
    echo "✅ All critical checks passed!"
    exit 0
else
    echo "❌ $ERRORS critical error(s) found. Please fix before merging."
    exit 1
fi
