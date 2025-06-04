#!/bin/bash
# Update scenario test script
# 事前に: test_scenario.sh を実行してデータが作成されていること
# サーバーが http://localhost:8080 で起動していること
# jqコマンドが必要です（brew install jq などでインストール）

set -e

API_URL="http://localhost:8080/api/v1"

echo "=== シンジケートローン管理システム Update テストシナリオ ==="
echo ""

echo "--- 既存データの取得 ---"

# Company一覧から最初のCompanyを取得
COMPANY_DATA=$(curl -s "$API_URL/parties/companies" | jq -r '.content[0]')
COMPANY_ID=$(echo "$COMPANY_DATA" | jq -r '.id')
COMPANY_VERSION=$(echo "$COMPANY_DATA" | jq -r '.version')
echo "Company ID: $COMPANY_ID, Version: $COMPANY_VERSION"

# Borrower一覧から最初のBorrowerを取得
BORROWER_DATA=$(curl -s "$API_URL/parties/borrowers" | jq -r '.content[0]')
BORROWER_ID=$(echo "$BORROWER_DATA" | jq -r '.id')
BORROWER_VERSION=$(echo "$BORROWER_DATA" | jq -r '.version')
echo "Borrower ID: $BORROWER_ID, Version: $BORROWER_VERSION"

# Investor一覧から最初のInvestorを取得
INVESTOR_DATA=$(curl -s "$API_URL/parties/investors" | jq -r '.content[0]')
INVESTOR_ID=$(echo "$INVESTOR_DATA" | jq -r '.id')
INVESTOR_VERSION=$(echo "$INVESTOR_DATA" | jq -r '.version')
INVESTOR_TYPE=$(echo "$INVESTOR_DATA" | jq -r '.investorType')
echo "Investor ID: $INVESTOR_ID, Version: $INVESTOR_VERSION, Type: $INVESTOR_TYPE"

# Syndicate一覧から最初のSyndicateを取得
SYNDICATE_DATA=$(curl -s "$API_URL/syndicates" | jq -r '.content[0]')
SYNDICATE_ID=$(echo "$SYNDICATE_DATA" | jq -r '.id')
SYNDICATE_VERSION=$(echo "$SYNDICATE_DATA" | jq -r '.version')
echo "Syndicate ID: $SYNDICATE_ID, Version: $SYNDICATE_VERSION"

# Facility一覧から最初のFacilityを取得
FACILITY_DATA=$(curl -s "$API_URL/facilities" | jq -r '.content[0]')
FACILITY_ID=$(echo "$FACILITY_DATA" | jq -r '.id')
FACILITY_VERSION=$(echo "$FACILITY_DATA" | jq -r '.version')
echo "Facility ID: $FACILITY_ID, Version: $FACILITY_VERSION"

echo ""
echo "========================================"
echo "=== 1. Company Update テスト ==="
echo "========================================"

echo "--- 1-1. 正常なCompany更新 ---"
# Company更新
COMPANY_UPDATE_SUCCESS=$(curl -s -X PUT "$API_URL/parties/companies/$COMPANY_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Updated Test Company",
    "registrationNumber": "REG123-UPDATED",
    "industry": "FINANCE",
    "address": "Tokyo Updated",
    "country": "JAPAN",
    "version": '$COMPANY_VERSION'
  }' \
  -w "%{http_code}")

HTTP_CODE=$(echo "$COMPANY_UPDATE_SUCCESS" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY=$(echo "$COMPANY_UPDATE_SUCCESS" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE" = "200" ]]; then
  echo "[OK] Company更新成功: HTTP $HTTP_CODE"
  NEW_COMPANY_VERSION=$(echo "$RESPONSE_BODY" | jq -r '.version')
  echo "更新後のVersion: $NEW_COMPANY_VERSION"
else
  echo "[NG] Company更新失敗: HTTP $HTTP_CODE"
  echo "レスポンス: $RESPONSE_BODY"
  exit 1
fi

echo ""
echo "--- 1-2. 楽観的排他制御エラーテスト（古いVersionを使用） ---"
COMPANY_UPDATE_ERROR=$(curl -s -X PUT "$API_URL/parties/companies/$COMPANY_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "This Should Fail",
    "registrationNumber": "REG123-FAIL",
    "industry": "IT",
    "address": "Tokyo Fail",
    "country": "JAPAN",
    "version": '$COMPANY_VERSION'
  }' \
  -w "%{http_code}")

HTTP_CODE_ERROR=$(echo "$COMPANY_UPDATE_ERROR" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY_ERROR=$(echo "$COMPANY_UPDATE_ERROR" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE_ERROR" = "409" ]]; then
  echo "[OK] 楽観的排他制御エラーが正常に発生: HTTP $HTTP_CODE_ERROR"
  echo "エラーメッセージ: $RESPONSE_BODY_ERROR"
else
  echo "[NG] 楽観的排他制御エラーが発生しませんでした: HTTP $HTTP_CODE_ERROR"
  echo "レスポンス: $RESPONSE_BODY_ERROR"
fi

echo ""
echo "--- 1-3. 正しいVersionでの再更新 ---"
COMPANY_UPDATE_RETRY=$(curl -s -X PUT "$API_URL/parties/companies/$COMPANY_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Final Updated Test Company",
    "registrationNumber": "REG123-FINAL",
    "industry": "FINANCE",
    "address": "Tokyo Final",
    "country": "JAPAN",
    "version": '$NEW_COMPANY_VERSION'
  }' \
  -w "%{http_code}")

HTTP_CODE_RETRY=$(echo "$COMPANY_UPDATE_RETRY" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY_RETRY=$(echo "$COMPANY_UPDATE_RETRY" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE_RETRY" = "200" ]]; then
  echo "[OK] 正しいVersionでの再更新成功: HTTP $HTTP_CODE_RETRY"
  FINAL_COMPANY_VERSION=$(echo "$RESPONSE_BODY_RETRY" | jq -r '.version')
  echo "最終Version: $FINAL_COMPANY_VERSION"
else
  echo "[NG] 正しいVersionでの再更新失敗: HTTP $HTTP_CODE_RETRY"
  echo "レスポンス: $RESPONSE_BODY_RETRY"
fi

echo ""
echo "========================================"
echo "=== 2. Borrower Update テスト ==="
echo "========================================"

echo "--- 2-1. 正常なBorrower更新 ---"
BORROWER_UPDATE_SUCCESS=$(curl -s -X PUT "$API_URL/parties/borrowers/$BORROWER_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Test Borrower",
    "email": "updated-borrower@example.com",
    "phoneNumber": "123-456-7891",
    "companyId": '$COMPANY_ID',
    "creditLimit": 12000000,
    "creditRating": "AAA",
    "version": '$BORROWER_VERSION'
  }' \
  -w "%{http_code}")

HTTP_CODE=$(echo "$BORROWER_UPDATE_SUCCESS" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY=$(echo "$BORROWER_UPDATE_SUCCESS" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE" = "200" ]]; then
  echo "[OK] Borrower更新成功: HTTP $HTTP_CODE"
  NEW_BORROWER_VERSION=$(echo "$RESPONSE_BODY" | jq -r '.version')
  echo "更新後のVersion: $NEW_BORROWER_VERSION"
else
  echo "[NG] Borrower更新失敗: HTTP $HTTP_CODE"
  echo "レスポンス: $RESPONSE_BODY"
  exit 1
fi

echo ""
echo "--- 2-2. 楽観的排他制御エラーテスト ---"
BORROWER_UPDATE_ERROR=$(curl -s -X PUT "$API_URL/parties/borrowers/$BORROWER_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "This Should Fail Borrower",
    "email": "fail-borrower@example.com",
    "phoneNumber": "123-456-7892",
    "companyId": '$COMPANY_ID',
    "creditLimit": 5000000,
    "creditRating": "BB",
    "version": '$BORROWER_VERSION'
  }' \
  -w "%{http_code}")

HTTP_CODE_ERROR=$(echo "$BORROWER_UPDATE_ERROR" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY_ERROR=$(echo "$BORROWER_UPDATE_ERROR" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE_ERROR" = "409" ]]; then
  echo "[OK] Borrower楽観的排他制御エラーが正常に発生: HTTP $HTTP_CODE_ERROR"
  echo "エラーメッセージ: $RESPONSE_BODY_ERROR"
else
  echo "[NG] Borrower楽観的排他制御エラーが発生しませんでした: HTTP $HTTP_CODE_ERROR"
  echo "レスポンス: $RESPONSE_BODY_ERROR"
fi

echo ""
echo "========================================"
echo "=== 3. Investor Update テスト ==="
echo "========================================"

echo "--- 3-1. 正常なInvestor更新 ---"
INVESTOR_UPDATE_SUCCESS=$(curl -s -X PUT "$API_URL/parties/investors/$INVESTOR_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Test Investor",
    "email": "updated-investor@example.com",
    "phoneNumber": "987-654-3211",
    "companyId": null,
    "investmentCapacity": 8000000,
    "investorType": "'$INVESTOR_TYPE'",
    "version": '$INVESTOR_VERSION'
  }' \
  -w "%{http_code}")

HTTP_CODE=$(echo "$INVESTOR_UPDATE_SUCCESS" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY=$(echo "$INVESTOR_UPDATE_SUCCESS" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE" = "200" ]]; then
  echo "[OK] Investor更新成功: HTTP $HTTP_CODE"
  NEW_INVESTOR_VERSION=$(echo "$RESPONSE_BODY" | jq -r '.version')
  echo "更新後のVersion: $NEW_INVESTOR_VERSION"
else
  echo "[NG] Investor更新失敗: HTTP $HTTP_CODE"
  echo "レスポンス: $RESPONSE_BODY"
  exit 1
fi

echo ""
echo "--- 3-2. 楽観的排他制御エラーテスト ---"
INVESTOR_UPDATE_ERROR=$(curl -s -X PUT "$API_URL/parties/investors/$INVESTOR_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "This Should Fail Investor",
    "email": "fail-investor@example.com",
    "phoneNumber": "987-654-3212",
    "companyId": null,
    "investmentCapacity": 3000000,
    "investorType": "'$INVESTOR_TYPE'",
    "version": '$INVESTOR_VERSION'
  }' \
  -w "%{http_code}")

HTTP_CODE_ERROR=$(echo "$INVESTOR_UPDATE_ERROR" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY_ERROR=$(echo "$INVESTOR_UPDATE_ERROR" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE_ERROR" = "409" ]]; then
  echo "[OK] Investor楽観的排他制御エラーが正常に発生: HTTP $HTTP_CODE_ERROR"
  echo "エラーメッセージ: $RESPONSE_BODY_ERROR"
else
  echo "[NG] Investor楽観的排他制御エラーが発生しませんでした: HTTP $HTTP_CODE_ERROR"
  echo "レスポンス: $RESPONSE_BODY_ERROR"
fi

echo ""
echo "========================================"
echo "=== 4. Syndicate Update テスト ==="
echo "========================================"

echo "--- 4-1. 正常なSyndicate更新 ---"
SYNDICATE_UPDATE_SUCCESS=$(curl -s -X PUT "$API_URL/syndicates/$SYNDICATE_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Test Syndicate",
    "leadBankId": '$INVESTOR_ID',
    "borrowerId": '$BORROWER_ID',
    "memberInvestorIds": ['$INVESTOR_ID'],
    "version": '$SYNDICATE_VERSION'
  }' \
  -w "%{http_code}")

HTTP_CODE=$(echo "$SYNDICATE_UPDATE_SUCCESS" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY=$(echo "$SYNDICATE_UPDATE_SUCCESS" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE" = "200" ]]; then
  echo "[OK] Syndicate更新成功: HTTP $HTTP_CODE"
  NEW_SYNDICATE_VERSION=$(echo "$RESPONSE_BODY" | jq -r '.version')
  echo "更新後のVersion: $NEW_SYNDICATE_VERSION"
else
  echo "[NG] Syndicate更新失敗: HTTP $HTTP_CODE"
  echo "レスポンス: $RESPONSE_BODY"
  exit 1
fi

echo ""
echo "--- 4-2. 楽観的排他制御エラーテスト ---"
SYNDICATE_UPDATE_ERROR=$(curl -s -X PUT "$API_URL/syndicates/$SYNDICATE_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "This Should Fail Syndicate",
    "leadBankId": '$INVESTOR_ID',
    "borrowerId": '$BORROWER_ID',
    "memberInvestorIds": ['$INVESTOR_ID'],
    "version": '$SYNDICATE_VERSION'
  }' \
  -w "%{http_code}")

HTTP_CODE_ERROR=$(echo "$SYNDICATE_UPDATE_ERROR" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY_ERROR=$(echo "$SYNDICATE_UPDATE_ERROR" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE_ERROR" = "409" ]]; then
  echo "[OK] Syndicate楽観的排他制御エラーが正常に発生: HTTP $HTTP_CODE_ERROR"
  echo "エラーメッセージ: $RESPONSE_BODY_ERROR"
else
  echo "[NG] Syndicate楽観的排他制御エラーが発生しませんでした: HTTP $HTTP_CODE_ERROR"
  echo "レスポンス: $RESPONSE_BODY_ERROR"
fi

echo ""
echo "========================================"
echo "=== 5. Facility Update テスト ==="
echo "========================================"

echo "--- 5-1. 正常なFacility更新 ---"
FACILITY_UPDATE_SUCCESS=$(curl -s -X PUT "$API_URL/facilities/$FACILITY_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "syndicateId": '$SYNDICATE_ID',
    "commitment": 6000000,
    "currency": "JPY",
    "startDate": "2025-02-01",
    "endDate": "2026-02-01",
    "interestTerms": "LIBOR + 2.5%",
    "sharePies": [
      {"investorId": '$INVESTOR_ID', "share": 1.0}
    ],
    "version": '$FACILITY_VERSION'
  }' \
  -w "%{http_code}")

HTTP_CODE=$(echo "$FACILITY_UPDATE_SUCCESS" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY=$(echo "$FACILITY_UPDATE_SUCCESS" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE" = "200" ]]; then
  echo "[OK] Facility更新成功: HTTP $HTTP_CODE"
  NEW_FACILITY_VERSION=$(echo "$RESPONSE_BODY" | jq -r '.version')
  echo "更新後のVersion: $NEW_FACILITY_VERSION"
else
  echo "[NG] Facility更新失敗: HTTP $HTTP_CODE"
  echo "レスポンス: $RESPONSE_BODY"
  exit 1
fi

echo ""
echo "--- 5-2. 楽観的排他制御エラーテスト ---"
FACILITY_UPDATE_ERROR=$(curl -s -X PUT "$API_URL/facilities/$FACILITY_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "syndicateId": '$SYNDICATE_ID',
    "commitment": 3000000,
    "currency": "EUR",
    "startDate": "2025-03-01",
    "endDate": "2026-03-01",
    "interestTerms": "EURIBor + 3%",
    "sharePies": [
      {"investorId": '$INVESTOR_ID', "share": 1.0}
    ],
    "version": '$FACILITY_VERSION'
  }' \
  -w "%{http_code}")

HTTP_CODE_ERROR=$(echo "$FACILITY_UPDATE_ERROR" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY_ERROR=$(echo "$FACILITY_UPDATE_ERROR" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE_ERROR" = "409" ]]; then
  echo "[OK] Facility楽観的排他制御エラーが正常に発生: HTTP $HTTP_CODE_ERROR"
  echo "エラーメッセージ: $RESPONSE_BODY_ERROR"
else
  echo "[NG] Facility楽観的排他制御エラーが発生しませんでした: HTTP $HTTP_CODE_ERROR"
  echo "レスポンス: $RESPONSE_BODY_ERROR"
fi

echo ""
echo "--- 5-3. 正しいVersionでの最終更新 ---"
FACILITY_UPDATE_FINAL=$(curl -s -X PUT "$API_URL/facilities/$FACILITY_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "syndicateId": '$SYNDICATE_ID',
    "commitment": 11000000,
    "currency": "USD",
    "startDate": "2025-06-01",
    "endDate": "2026-06-01",
    "interestTerms": "SOFR + 2.0%",
    "sharePies": [
      {"investorId": '$INVESTOR_ID', "share": 1.0}
    ],
    "version": '$NEW_FACILITY_VERSION'
  }' \
  -w "%{http_code}")

HTTP_CODE_FINAL=$(echo "$FACILITY_UPDATE_FINAL" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY_FINAL=$(echo "$FACILITY_UPDATE_FINAL" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE_FINAL" = "200" ]]; then
  echo "[OK] 正しいVersionでのFacility最終更新成功: HTTP $HTTP_CODE_FINAL"
  FINAL_FACILITY_VERSION=$(echo "$RESPONSE_BODY_FINAL" | jq -r '.version')
  echo "最終Version: $FINAL_FACILITY_VERSION"
else
  echo "[NG] 正しいVersionでのFacility最終更新失敗: HTTP $HTTP_CODE_FINAL"
  echo "レスポンス: $RESPONSE_BODY_FINAL"
fi

echo ""
echo "========================================"
echo "=== テスト結果サマリー ==="
echo "========================================"

echo "--- 更新されたデータの確認 ---"
echo "[Company]"
curl -s "$API_URL/parties/companies/$COMPANY_ID" | jq '{id, companyName, registrationNumber, industry, version}'

echo "[Borrower]"
curl -s "$API_URL/parties/borrowers/$BORROWER_ID" | jq '{id, name, email, creditLimit, creditRating, version}'

echo "[Investor]"  
curl -s "$API_URL/parties/investors/$INVESTOR_ID" | jq '{id, name, email, investmentCapacity, investorType, version}'

echo "[Syndicate]"
curl -s "$API_URL/syndicates/$SYNDICATE_ID" | jq '{id, name, leadBankId, borrowerId, memberInvestorIds, version}'

echo "[Facility]"
curl -s "$API_URL/facilities/$FACILITY_ID" | jq '{id, syndicateId, commitment, currency, interestTerms, version}'

echo ""
echo "✅ 楽観的排他制御を含むUpdate テストシナリオが正常に完了しました！"
echo ""
echo "📝 実行されたテスト:"
echo "  - Company, Borrower, Investor, Syndicate, Facility の正常更新"
echo "  - 各エンティティでの楽観的排他制御エラーテスト (HTTP 409)"
echo "  - 正しいバージョンでの再更新テスト"
echo ""
echo "--- 完了 ---"
