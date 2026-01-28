#!/bin/bash

# Language Idempotency Test Script
# Bu script, aynı repo için languages/init endpoint'ini 3 kez çağırır
# ve sonuçları gösterir

# Kullanım:
#   ./scripts/test_language_idempotency.sh <JWT_TOKEN> <REPO_FULL_NAME>
#
# Örnek:
#   ./scripts/test_language_idempotency.sh "eyJhbGc..." "facebook/react"

if [ "$#" -ne 2 ]; then
    echo "Kullanım: $0 <JWT_TOKEN> <REPO_FULL_NAME>"
    echo "Örnek: $0 \"eyJhbGc...\" \"facebook/react\""
    exit 1
fi

TOKEN=$1
REPO_FULL_NAME=$2
BASE_URL="http://localhost:8080"

echo "=========================================="
echo "Language Idempotency Test"
echo "=========================================="
echo "Repo: $REPO_FULL_NAME"
echo ""

# 1. İlk çağrı
echo "1️⃣  İlk çağrı yapılıyor..."
RESPONSE1=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/github/languages/init" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"repoFullName\": \"$REPO_FULL_NAME\"}")

HTTP_CODE1=$(echo "$RESPONSE1" | grep "HTTP_CODE" | cut -d: -f2)
BODY1=$(echo "$RESPONSE1" | sed '/HTTP_CODE/d')

if [ "$HTTP_CODE1" == "200" ]; then
    echo "✅ Başarılı (HTTP $HTTP_CODE1)"
    echo "Response: $(echo "$BODY1" | jq -r '.languages | keys | length') language bulundu"
else
    echo "❌ Hata (HTTP $HTTP_CODE1)"
    echo "$BODY1"
    exit 1
fi

echo ""
sleep 2

# 2. İkinci çağrı
echo "2️⃣  İkinci çağrı yapılıyor (aynı repo)..."
RESPONSE2=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/github/languages/init" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"repoFullName\": \"$REPO_FULL_NAME\"}")

HTTP_CODE2=$(echo "$RESPONSE2" | grep "HTTP_CODE" | cut -d: -f2)
BODY2=$(echo "$RESPONSE2" | sed '/HTTP_CODE/d')

if [ "$HTTP_CODE2" == "200" ]; then
    echo "✅ Başarılı (HTTP $HTTP_CODE2)"
    echo "Response: $(echo "$BODY2" | jq -r '.languages | keys | length') language bulundu"
else
    echo "❌ Hata (HTTP $HTTP_CODE2)"
    echo "$BODY2"
    exit 1
fi

echo ""
sleep 2

# 3. Üçüncü çağrı
echo "3️⃣  Üçüncü çağrı yapılıyor (aynı repo)..."
RESPONSE3=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/api/github/languages/init" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"repoFullName\": \"$REPO_FULL_NAME\"}")

HTTP_CODE3=$(echo "$RESPONSE3" | grep "HTTP_CODE" | cut -d: -f2)
BODY3=$(echo "$RESPONSE3" | sed '/HTTP_CODE/d')

if [ "$HTTP_CODE3" == "200" ]; then
    echo "✅ Başarılı (HTTP $HTTP_CODE3)"
    echo "Response: $(echo "$BODY3" | jq -r '.languages | keys | length') language bulundu"
else
    echo "❌ Hata (HTTP $HTTP_CODE3)"
    echo "$BODY3"
    exit 1
fi

echo ""
echo "=========================================="
echo "✅ Tüm çağrılar başarılı!"
echo ""
echo "📝 Şimdi DB'de kontrol et:"
echo "   SELECT repository_id, language_name, COUNT(*) AS cnt"
echo "   FROM github_repository_languages"
echo "   WHERE repository_id = (SELECT id FROM repositories WHERE full_name = '$REPO_FULL_NAME')"
echo "   GROUP BY repository_id, language_name;"
echo ""
echo "   Her language için cnt=1 olmalı ✅"
echo "=========================================="
