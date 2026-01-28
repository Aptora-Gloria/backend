#!/bin/bash

# Test script for repository analyze endpoint
# Usage: ./scripts/test_analyze_endpoint.sh <JWT_TOKEN> <REPO_FULL_NAME>

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <JWT_TOKEN> <REPO_FULL_NAME>"
    echo "Example: $0 \"eyJhbGc...\" \"Aptora-Gloria/frontend\""
    exit 1
fi

TOKEN=$1
REPO_FULL_NAME=$2
BASE_URL="http://localhost:8080"

echo "=========================================="
echo "Testing Repository Analyze Endpoint"
echo "=========================================="
echo "Repo: $REPO_FULL_NAME"
echo ""

# First, check sync status
echo "1️⃣  Checking sync status..."
STATUS_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/github/repositories/sync/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"fullName\": \"$REPO_FULL_NAME\"}")

STATUS_HTTP_CODE=$(echo "$STATUS_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
STATUS_BODY=$(echo "$STATUS_RESPONSE" | sed '/HTTP_CODE/d')

if [ "$STATUS_HTTP_CODE" == "200" ]; then
    echo "✅ Status check successful (HTTP $STATUS_HTTP_CODE)"
    echo "Response: $STATUS_BODY" | jq .
else
    echo "❌ Status check failed (HTTP $STATUS_HTTP_CODE)"
    echo "$STATUS_BODY"
    exit 1
fi

echo ""
sleep 1

# Then, call analyze endpoint
echo "2️⃣  Calling analyze endpoint..."
ANALYZE_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/github/repositories/analyze" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"fullName\": \"$REPO_FULL_NAME\"}")

ANALYZE_HTTP_CODE=$(echo "$ANALYZE_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
ANALYZE_BODY=$(echo "$ANALYZE_RESPONSE" | sed '/HTTP_CODE/d')

if [ "$ANALYZE_HTTP_CODE" == "200" ]; then
    echo "✅ Analyze successful (HTTP $ANALYZE_HTTP_CODE)"
    echo "Response: $ANALYZE_BODY" | jq .
else
    echo "❌ Analyze failed (HTTP $ANALYZE_HTTP_CODE)"
    echo "$ANALYZE_BODY"
    echo ""
    echo "Error details:"
    echo "$ANALYZE_BODY" | jq . 2>/dev/null || echo "$ANALYZE_BODY"
    exit 1
fi

echo ""
echo "=========================================="
echo "✅ Test completed successfully!"
echo "=========================================="
