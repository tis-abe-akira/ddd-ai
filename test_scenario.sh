#!/bin/bash
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
  -d '{"name":"Test Borrower","email":"borrower@example.com","phoneNumber":"123-456-7890","companyId":"'$COMPANY_ID'","creditLimit":10000000,"creditRating":"AA"}' \
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
  -d '{"name":"Test Syndicate","leadBankId":'$INVESTOR_ID',"borrowerId":'$BORROWER_ID',"memberInvestorIds":['$INVESTOR_ID', '$INVESTOR_ID2', '$INVESTOR_ID3']}' \
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

echo "--- Facility組成テスト開始 ---"

echo "--- 1. Investor IDが存在しないエラーケース ---"
FACILITY_ERROR_1=$(curl -s -X POST "$API_URL/facilities" \
  -H "Content-Type: application/json" \
  -d '{
    "syndicateId": '$SYNDICATE_ID',
    "commitment": 5000000,
    "currency": "USD",
    "startDate": "2025-01-01",
    "endDate": "2026-01-01",
    "interestTerms": "LIBOR + 2%",
    "sharePies": [
      {"investorId": 9999, "share": 1.0}
    ]
  }' \
  -w "%{http_code}")

HTTP_CODE_1=$(echo "$FACILITY_ERROR_1" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY_1=$(echo "$FACILITY_ERROR_1" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE_1" = "400" ]]; then
  echo "[OK] 存在しないInvestor IDでエラーが発生: HTTP $HTTP_CODE_1"
  echo "エラーメッセージ: $RESPONSE_BODY_1"
else
  echo "[NG] 存在しないInvestor IDなのにエラーが発生しなかった: HTTP $HTTP_CODE_1"
  echo "レスポンス: $RESPONSE_BODY_1"
fi

echo ""
echo "--- 2. Commit額がBorrowerのCreditLimit超過でエラーケース ---"
FACILITY_ERROR_2=$(curl -s -X POST "$API_URL/facilities" \
  -H "Content-Type: application/json" \
  -d '{
    "syndicateId": '$SYNDICATE_ID',
    "commitment": 15000000,
    "currency": "USD",
    "startDate": "2025-01-01",
    "endDate": "2026-01-01",
    "interestTerms": "LIBOR + 2%",
    "sharePies": [
      {"investorId": '$INVESTOR_ID', "share": 0.4},
      {"investorId": '$INVESTOR_ID2', "share": 0.35},
      {"investorId": '$INVESTOR_ID3', "share": 0.25}
    ]
  }' \
  -w "%{http_code}")

HTTP_CODE_2=$(echo "$FACILITY_ERROR_2" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY_2=$(echo "$FACILITY_ERROR_2" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE_2" = "400" ]]; then
  echo "[OK] CreditLimit超過でエラーが発生: HTTP $HTTP_CODE_2"
  echo "エラーメッセージ: $RESPONSE_BODY_2"
else
  echo "[NG] CreditLimit超過なのにエラーが発生しなかった: HTTP $HTTP_CODE_2"
  echo "レスポンス: $RESPONSE_BODY_2"
fi

echo ""
echo "--- 3. SharePieが合計100%でなくエラーケース ---"
FACILITY_ERROR_3=$(curl -s -X POST "$API_URL/facilities" \
  -H "Content-Type: application/json" \
  -d '{
    "syndicateId": '$SYNDICATE_ID',
    "commitment": 5000000,
    "currency": "USD",
    "startDate": "2025-01-01",
    "endDate": "2026-01-01",
    "interestTerms": "LIBOR + 2%",
    "sharePies": [
      {"investorId": '$INVESTOR_ID', "share": 0.4},
      {"investorId": '$INVESTOR_ID2', "share": 0.35}
    ]
  }' \
  -w "%{http_code}")

HTTP_CODE_3=$(echo "$FACILITY_ERROR_3" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY_3=$(echo "$FACILITY_ERROR_3" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE_3" = "400" ]]; then
  echo "[OK] SharePie合計が100%でない場合にエラーが発生: HTTP $HTTP_CODE_3"
  echo "エラーメッセージ: $RESPONSE_BODY_3"
else
  echo "[NG] SharePie合計が100%でないのにエラーが発生しなかった: HTTP $HTTP_CODE_3"
  echo "レスポンス: $RESPONSE_BODY_3"
fi

echo ""
echo "--- 4. 正常なFacility組成ケース ---"
FACILITY_SUCCESS=$(curl -s -X POST "$API_URL/facilities" \
  -H "Content-Type: application/json" \
  -d '{
    "syndicateId": '$SYNDICATE_ID',
    "commitment": 5000000,
    "currency": "USD",
    "startDate": "2025-01-01",
    "endDate": "2026-01-01",
    "interestTerms": "LIBOR + 2%",
    "sharePies": [
      {"investorId": '$INVESTOR_ID', "share": 0.4},
      {"investorId": '$INVESTOR_ID2', "share": 0.35},
      {"investorId": '$INVESTOR_ID3', "share": 0.25}
    ]
  }' \
  -w "%{http_code}")

HTTP_CODE_4=$(echo "$FACILITY_SUCCESS" | grep -o '[0-9]\{3\}$')
RESPONSE_BODY_4=$(echo "$FACILITY_SUCCESS" | sed 's/[0-9]\{3\}$//')

if [[ "$HTTP_CODE_4" = "200" ]]; then
  echo "[OK] 正常なFacility組成が成功: HTTP $HTTP_CODE_4"
  
  # Facility IDを取得して基本的な検証
  FACILITY_ID=$(echo "$RESPONSE_BODY_4" | jq -r '.id')
  echo "作成されたFacility ID: $FACILITY_ID"
  
  # 作成されたFacilityの詳細を確認
  # 注意：GET /facilities/{id} エンドポイントが実装されている場合のみ動作
  echo "Facility作成レスポンス:"
  echo "$RESPONSE_BODY_4" | jq
  
  FACILITY_SYNDICATE_ID=$(echo "$RESPONSE_BODY_4" | jq -r '.syndicateId')
  FACILITY_COMMITMENT=$(echo "$RESPONSE_BODY_4" | jq -r '.commitment')
  SHARE_PIES_COUNT=$(echo "$RESPONSE_BODY_4" | jq -r '.sharePies | length')
  
  # 基本的な検証
  if [[ "$FACILITY_SYNDICATE_ID" = "$SYNDICATE_ID" ]]; then
    echo "[OK] SyndicateIDが正しく設定されています: $FACILITY_SYNDICATE_ID"
  else
    echo "[NG] SyndicateIDが想定外: $FACILITY_SYNDICATE_ID"
  fi
  
  if [[ "$FACILITY_COMMITMENT" = "5000000.00" ]] || [[ "$FACILITY_COMMITMENT" = "5000000" ]]; then
    echo "[OK] Commitmentが正しく設定されています: $FACILITY_COMMITMENT"
  else
    echo "[NG] Commitmentが想定外: $FACILITY_COMMITMENT"
  fi
  
  if [[ "$SHARE_PIES_COUNT" = "3" ]]; then
    echo "[OK] SharePieが3つ作成されています"
  else
    echo "[NG] SharePieの数が想定外: $SHARE_PIES_COUNT"
  fi
  
else
  echo "[NG] 正常なFacility組成が失敗: HTTP $HTTP_CODE_4"
  echo "エラーレスポンス: $RESPONSE_BODY_4"
fi

echo ""
echo "--- Facility組成テスト完了 ---"

# Facilityが正常に作成された場合のみ、以降のテストを実行
if [[ "$HTTP_CODE_4" = "200" ]]; then
  echo ""
  echo "--- Transaction履歴テスト ---"
  echo "Facility別Transaction履歴を取得:"
  curl -s "$API_URL/transactions/facility/$FACILITY_ID" | jq

  echo ""
  echo "Transaction統計を取得:"
  curl -s "$API_URL/transactions/facility/$FACILITY_ID/statistics" | jq

  echo ""
  echo "--- Fee Payment テスト開始 ---"
  
  echo "--- 1. 管理手数料支払い作成 ---"
  FEE_PAYMENT_1=$(curl -s -X POST "$API_URL/fees/payments" \
    -H "Content-Type: application/json" \
    -d '{
      "facilityId": '$FACILITY_ID',
      "borrowerId": '$BORROWER_ID',
      "feeType": "MANAGEMENT_FEE",
      "feeDate": "2025-01-31",
      "feeAmount": 25000.00,
      "calculationBase": 5000000.00,
      "feeRate": 0.5,
      "recipientType": "BANK",
      "recipientId": '$INVESTOR_ID',
      "currency": "USD",
      "description": "2025年1月分管理手数料"
    }' \
    -w "%{http_code}")

  HTTP_CODE_FEE1=$(echo "$FEE_PAYMENT_1" | grep -o '[0-9]\{3\}$')
  RESPONSE_BODY_FEE1=$(echo "$FEE_PAYMENT_1" | sed 's/[0-9]\{3\}$//')

  if [[ "$HTTP_CODE_FEE1" = "200" ]]; then
    echo "[OK] 管理手数料支払い作成成功: HTTP $HTTP_CODE_FEE1"
    FEE_PAYMENT_ID1=$(echo "$RESPONSE_BODY_FEE1" | jq -r '.id')
    echo "作成された手数料支払いID: $FEE_PAYMENT_ID1"
    echo "$RESPONSE_BODY_FEE1" | jq
  else
    echo "[NG] 管理手数料支払い作成失敗: HTTP $HTTP_CODE_FEE1"
    echo "エラーレスポンス: $RESPONSE_BODY_FEE1"
  fi

  echo ""
  echo "--- 2. コミットメント手数料支払い作成（投資家配分） ---"
  FEE_PAYMENT_2=$(curl -s -X POST "$API_URL/fees/payments" \
    -H "Content-Type: application/json" \
    -d '{
      "facilityId": '$FACILITY_ID',
      "borrowerId": '$BORROWER_ID',
      "feeType": "COMMITMENT_FEE",
      "feeDate": "2025-01-31",
      "feeAmount": 12500.00,
      "calculationBase": 5000000.00,
      "feeRate": 0.25,
      "recipientType": "INVESTOR",
      "recipientId": '$INVESTOR_ID',
      "currency": "USD",
      "description": "2025年1月分コミットメント手数料"
    }' \
    -w "%{http_code}")

  HTTP_CODE_FEE2=$(echo "$FEE_PAYMENT_2" | grep -o '[0-9]\{3\}$')
  RESPONSE_BODY_FEE2=$(echo "$FEE_PAYMENT_2" | sed 's/[0-9]\{3\}$//')

  if [[ "$HTTP_CODE_FEE2" = "200" ]]; then
    echo "[OK] コミットメント手数料支払い作成成功: HTTP $HTTP_CODE_FEE2"
    FEE_PAYMENT_ID2=$(echo "$RESPONSE_BODY_FEE2" | jq -r '.id')
    echo "作成された手数料支払いID: $FEE_PAYMENT_ID2"
    echo "投資家別配分確認:"
    echo "$RESPONSE_BODY_FEE2" | jq '.feeDistributions'
  else
    echo "[NG] コミットメント手数料支払い作成失敗: HTTP $HTTP_CODE_FEE2"
    echo "エラーレスポンス: $RESPONSE_BODY_FEE2"
  fi

  echo ""
  echo "--- 3. 手数料計算エラーケース ---"
  FEE_PAYMENT_ERROR=$(curl -s -X POST "$API_URL/fees/payments" \
    -H "Content-Type: application/json" \
    -d '{
      "facilityId": '$FACILITY_ID',
      "borrowerId": '$BORROWER_ID',
      "feeType": "ARRANGEMENT_FEE",
      "feeDate": "2025-01-01",
      "feeAmount": 30000.00,
      "calculationBase": 5000000.00,
      "feeRate": 0.5,
      "recipientType": "BANK",
      "recipientId": '$INVESTOR_ID',
      "currency": "USD",
      "description": "計算が合わない手数料"
    }' \
    -w "%{http_code}")

  HTTP_CODE_FEE_ERROR=$(echo "$FEE_PAYMENT_ERROR" | grep -o '[0-9]\{3\}$')
  RESPONSE_BODY_FEE_ERROR=$(echo "$FEE_PAYMENT_ERROR" | sed 's/[0-9]\{3\}$//')

  if [[ "$HTTP_CODE_FEE_ERROR" = "400" ]]; then
    echo "[OK] 手数料計算エラーが正しく検出: HTTP $HTTP_CODE_FEE_ERROR"
    echo "エラーメッセージ: $RESPONSE_BODY_FEE_ERROR"
  else
    echo "[NG] 手数料計算エラーが検出されなかった: HTTP $HTTP_CODE_FEE_ERROR"
    echo "レスポンス: $RESPONSE_BODY_FEE_ERROR"
  fi

  echo ""
  echo "--- 4. 受益者タイプエラーケース ---"
  FEE_PAYMENT_TYPE_ERROR=$(curl -s -X POST "$API_URL/fees/payments" \
    -H "Content-Type: application/json" \
    -d '{
      "facilityId": '$FACILITY_ID',
      "borrowerId": '$BORROWER_ID',
      "feeType": "MANAGEMENT_FEE",
      "feeDate": "2025-01-01",
      "feeAmount": 25000.00,
      "calculationBase": 5000000.00,
      "feeRate": 0.5,
      "recipientType": "INVESTOR",
      "recipientId": '$INVESTOR_ID',
      "currency": "USD",
      "description": "間違った受益者タイプ"
    }' \
    -w "%{http_code}")

  HTTP_CODE_TYPE_ERROR=$(echo "$FEE_PAYMENT_TYPE_ERROR" | grep -o '[0-9]\{3\}$')
  RESPONSE_BODY_TYPE_ERROR=$(echo "$FEE_PAYMENT_TYPE_ERROR" | sed 's/[0-9]\{3\}$//')

  if [[ "$HTTP_CODE_TYPE_ERROR" = "400" ]]; then
    echo "[OK] 受益者タイプエラーが正しく検出: HTTP $HTTP_CODE_TYPE_ERROR"
    echo "エラーメッセージ: $RESPONSE_BODY_TYPE_ERROR"
  else
    echo "[NG] 受益者タイプエラーが検出されなかった: HTTP $HTTP_CODE_TYPE_ERROR"
    echo "レスポンス: $RESPONSE_BODY_TYPE_ERROR"
  fi

  echo ""
  echo "--- Fee Payment 検索テスト ---"
  
  echo "Facility別手数料履歴:"
  curl -s "$API_URL/fees/payments/facility/$FACILITY_ID" | jq

  echo ""
  echo "手数料タイプ別検索（MANAGEMENT_FEE）:"
  curl -s "$API_URL/fees/payments/type/MANAGEMENT_FEE" | jq

  echo ""
  echo "手数料統計:"
  curl -s "$API_URL/fees/payments/facility/$FACILITY_ID/statistics" | jq

  echo ""
  echo "--- Transaction履歴更新確認 ---"
  echo "更新されたFacility別Transaction履歴（Fee Paymentを含む）:"
  curl -s "$API_URL/transactions/facility/$FACILITY_ID" | jq

  echo ""
  echo "更新されたTransaction統計:"
  curl -s "$API_URL/transactions/facility/$FACILITY_ID/statistics" | jq

  echo ""
  echo "--- Fee Payment テスト完了 ---"

  echo ""
  echo "--- Drawdown テスト開始 ---"
  
  echo "--- 1. 正常なDrawdown作成 ---"
  DRAWDOWN_RESPONSE=$(curl -s -X POST "$API_URL/loans/drawdowns" \
    -H "Content-Type: application/json" \
    -d '{
      "facilityId": '$FACILITY_ID',
      "borrowerId": '$BORROWER_ID',
      "amount": 2000000,
      "currency": "USD",
      "drawdownDate": "2025-02-01",
      "annualInterestRate": 0.05,
      "repaymentPeriodMonths": 12,
      "repaymentCycle": "MONTHLY",
      "repaymentMethod": "EQUAL_INSTALLMENT",
      "purpose": "Working capital"
    }' \
    -w "%{http_code}")

  HTTP_CODE_DRAWDOWN=$(echo "$DRAWDOWN_RESPONSE" | grep -o '[0-9]\{3\}$')
  RESPONSE_BODY_DRAWDOWN=$(echo "$DRAWDOWN_RESPONSE" | sed 's/[0-9]\{3\}$//')

  if [[ "$HTTP_CODE_DRAWDOWN" = "200" ]]; then
    echo "[OK] Drawdown作成成功: HTTP $HTTP_CODE_DRAWDOWN"
    DRAWDOWN_ID=$(echo "$RESPONSE_BODY_DRAWDOWN" | jq -r '.id')
    LOAN_ID=$(echo "$RESPONSE_BODY_DRAWDOWN" | jq -r '.loanId')
    echo "作成されたDrawdown ID: $DRAWDOWN_ID"
    echo "作成されたLoan ID: $LOAN_ID"
    echo "$RESPONSE_BODY_DRAWDOWN" | jq
  else
    echo "[NG] Drawdown作成失敗: HTTP $HTTP_CODE_DRAWDOWN"
    echo "エラーレスポンス: $RESPONSE_BODY_DRAWDOWN"
  fi

  echo ""
  echo "--- 2. 利用可能額超過エラーケース ---"
  DRAWDOWN_ERROR=$(curl -s -X POST "$API_URL/loans/drawdowns" \
    -H "Content-Type: application/json" \
    -d '{
      "facilityId": '$FACILITY_ID',
      "borrowerId": '$BORROWER_ID',
      "amount": 6000000,
      "currency": "USD",
      "drawdownDate": "2025-02-01",
      "annualInterestRate": 0.05,
      "repaymentPeriodMonths": 12,
      "repaymentCycle": "MONTHLY",
      "repaymentMethod": "EQUAL_INSTALLMENT",
      "purpose": "Working capital"
    }' \
    -w "%{http_code}")

  HTTP_CODE_DRAWDOWN_ERROR=$(echo "$DRAWDOWN_ERROR" | grep -o '[0-9]\{3\}$')
  RESPONSE_BODY_DRAWDOWN_ERROR=$(echo "$DRAWDOWN_ERROR" | sed 's/[0-9]\{3\}$//')

  if [[ "$HTTP_CODE_DRAWDOWN_ERROR" = "400" ]]; then
    echo "[OK] 利用可能額超過エラーが正しく検出: HTTP $HTTP_CODE_DRAWDOWN_ERROR"
    echo "エラーメッセージ: $RESPONSE_BODY_DRAWDOWN_ERROR"
  else
    echo "[NG] 利用可能額超過エラーが検出されなかった: HTTP $HTTP_CODE_DRAWDOWN_ERROR"
    echo "レスポンス: $RESPONSE_BODY_DRAWDOWN_ERROR"
  fi

  echo ""
  echo "--- Drawdown テスト完了 ---"

  # Drawdownが成功した場合のみPaymentテストを実行
  if [[ "$HTTP_CODE_DRAWDOWN" = "200" ]]; then
    echo ""
    echo "--- Payment テスト開始 ---"
    
    echo "--- 1. 正常な返済処理 ---"
    PAYMENT_RESPONSE=$(curl -s -X POST "$API_URL/loans/payments" \
      -H "Content-Type: application/json" \
      -d '{
        "loanId": '$LOAN_ID',
        "paymentDate": "2025-03-01",
        "principalAmount": 100000,
        "interestAmount": 8333.33,
        "currency": "USD"
      }' \
      -w "%{http_code}")

    HTTP_CODE_PAYMENT=$(echo "$PAYMENT_RESPONSE" | grep -o '[0-9]\{3\}$')
    RESPONSE_BODY_PAYMENT=$(echo "$PAYMENT_RESPONSE" | sed 's/[0-9]\{3\}$//')

    if [[ "$HTTP_CODE_PAYMENT" = "200" ]]; then
      echo "[OK] Payment作成成功: HTTP $HTTP_CODE_PAYMENT"
      PAYMENT_ID=$(echo "$RESPONSE_BODY_PAYMENT" | jq -r '.id')
      echo "作成されたPayment ID: $PAYMENT_ID"
      echo "投資家別配分確認:"
      echo "$RESPONSE_BODY_PAYMENT" | jq '.paymentDistributions'
    else
      echo "[NG] Payment作成失敗: HTTP $HTTP_CODE_PAYMENT"
      echo "エラーレスポンス: $RESPONSE_BODY_PAYMENT"
    fi

    echo ""
    echo "--- 2. 返済額不正エラーケース ---"
    PAYMENT_ERROR=$(curl -s -X POST "$API_URL/loans/payments" \
      -H "Content-Type: application/json" \
      -d '{
        "loanId": '$LOAN_ID',
        "paymentDate": "2025-03-01",
        "principalAmount": -1000,
        "interestAmount": -100,
        "currency": "USD"
      }' \
      -w "%{http_code}")

    HTTP_CODE_PAYMENT_ERROR=$(echo "$PAYMENT_ERROR" | grep -o '[0-9]\{3\}$')
    
    if [[ "$HTTP_CODE_PAYMENT_ERROR" = "400" ]]; then
      echo "[OK] 返済額不正エラーが正しく検出: HTTP $HTTP_CODE_PAYMENT_ERROR"
    else
      echo "[NG] 返済額不正エラーが検出されなかった: HTTP $HTTP_CODE_PAYMENT_ERROR"
    fi

    echo ""
    echo "--- Payment履歴確認 ---"
    echo "Loan別Payment履歴:"
    curl -s "$API_URL/loans/payments/loan/$LOAN_ID" | jq

    echo ""
    echo "--- Payment テスト完了 ---"

    echo ""
    echo "--- 最終的なTransaction統合確認 ---"
    echo "すべての取引を含むFacility別Transaction履歴:"
    curl -s "$API_URL/transactions/facility/$FACILITY_ID" | jq

    echo ""
    echo "最終的なTransaction統計:"
    curl -s "$API_URL/transactions/facility/$FACILITY_ID/statistics" | jq

    echo ""
    echo "取引タイプ別確認 - DRAWDOWN:"
    curl -s "$API_URL/transactions/type/DRAWDOWN" | jq

    echo ""
    echo "取引タイプ別確認 - PAYMENT:"
    curl -s "$API_URL/transactions/type/PAYMENT" | jq

    echo ""
    echo "取引タイプ別確認 - FEE_PAYMENT:"
    curl -s "$API_URL/transactions/type/FEE_PAYMENT" | jq

    echo ""
    echo "--- Transaction統合確認完了 ---"
  else
    echo "Drawdownが正常に作成されなかったため、Paymentテストをスキップします"
  fi

else
  echo "Facilityが正常に作成されなかったため、Drawdown/Payment/Fee Paymentテストをスキップします"
fi

echo ""
echo "--- 全テスト完了 ---"
