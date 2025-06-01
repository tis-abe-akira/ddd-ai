#!/bin/zsh
# 事前に: サーバーが http://localhost:8080 で起動していること
# jqコマンドが必要です（brew install jq などでインストール）

set -e

API_URL="http://localhost:8080/api/v1"
echo "--- Company作成 ---"
COMPANY_ID=$(curl -s -X POST "$API_URL/parties/companies" \
  -H "Content-Type: application/json" \
  -d '{"companyName":"Test Company","registrationNumber":"REG123","industry":"IT","address":"Tokyo","country":"JAPAN"}' \
  | jq -r '.id')
echo "Company ID: $COMPANY_ID"

echo "--- Borrower作成 ---"
BORROWER_ID=$(curl -s -X POST "$API_URL/parties/borrowers" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Borrower","email":"borrower@example.com","phoneNumber":"123-456-7890","companyId":"'$COMPANY_ID'","creditLimit":1000000,"creditRating":"AA"}' \
  | jq -r '.id')
echo "Borrower ID: $BORROWER_ID"

echo "--- Investor作成 ---"
INVESTOR_ID=$(curl -s -X POST "$API_URL/parties/investors" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Investor","email":"investor@example.com","phoneNumber":"987-654-3210","companyId":null,"investmentCapacity":5000000,"investorType":"LEAD_BANK"}' \
  | jq -r '.id')
echo "Investor ID: $INVESTOR_ID"

echo "--- Investor2作成 ---"
INVESTOR_ID2=$(curl -s -X POST "$API_URL/parties/investors" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Investor2","email":"investor2@example.com","phoneNumber":"987-654-3212","companyId":null,"investmentCapacity":6000000,"investorType":"BANK"}' \
  | jq -r '.id')
echo "Investor ID 2: $INVESTOR_ID2"

echo "--- Investor3作成 ---"
INVESTOR_ID3=$(curl -s -X POST "$API_URL/parties/investors" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Investor3","email":"investor3@example.com","phoneNumber":"987-654-3213","companyId":null,"investmentCapacity":7000000,"investorType":"BANK"}' \
  | jq -r '.id')
echo "Investor ID 3: $INVESTOR_ID3"

echo "--- Company/Borrower/Investor 一覧取得 ---"
echo "[Company]"
curl -s "$API_URL/parties/companies" | jq

echo "[Borrower]"
curl -s "$API_URL/parties/borrowers" | jq

echo "[Investor]"
curl -s "$API_URL/parties/investors" | jq

echo "--- Syndicate作成 ---"
SYNDICATE_ID=$(curl -s -X POST "$API_URL/syndicates" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Syndicate","leadBankId":'$INVESTOR_ID',"memberInvestorIds":['$INVESTOR_ID', '$INVESTOR_ID2', '$INVESTOR_ID3']}' \
  | jq -r '.id')
echo "Syndicate ID: $SYNDICATE_ID"

echo "--- Syndicate 一覧取得 ---"
curl -s "$API_URL/syndicates" | jq

echo "--- 検証: Syndicateのリードバンクとメンバー ---"
SYNDICATE_JSON=$(curl -s "$API_URL/syndicates/$SYNDICATE_ID")
LEAD_BANK_ID=$(echo "$SYNDICATE_JSON" | jq -r '.leadBankId')
MEMBER_IDS=$(echo "$SYNDICATE_JSON" | jq -r '.memberInvestorIds | join(",")')

if [[ "$LEAD_BANK_ID" = "$INVESTOR_ID" ]]; then
  echo "[OK] リードバンクIDは正しく $LEAD_BANK_ID です"
else
  echo "[NG] リードバンクIDが想定外: $LEAD_BANK_ID"
  exit 1
fi

EXPECTED_MEMBER_IDS="$INVESTOR_ID,$INVESTOR_ID2,$INVESTOR_ID3"
if [[ "$MEMBER_IDS" = "$EXPECTED_MEMBER_IDS" ]]; then
  echo "[OK] メンバー投資家IDリストも正しい: $MEMBER_IDS"
else
  echo "[NG] メンバー投資家IDリストが想定外: $MEMBER_IDS"
  exit 1
fi

echo "--- 完了 ---"
