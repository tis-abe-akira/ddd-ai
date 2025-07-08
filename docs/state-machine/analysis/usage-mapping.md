# State Machine Usage Mapping

## Overview

This document provides a comprehensive mapping of State Machine usage throughout the application, identifying where the State Machine API is properly used versus where direct status modifications occur.

## Usage Categories

- 🟢 **Proper Usage**: State Machine API with proper event handling
- 🟡 **Mixed Usage**: State Machine validation + direct assignment
- 🔴 **Bypass**: Direct status modification without State Machine
- ❓ **Unknown**: Requires investigation

## Entity State Management Mapping

### **Facility State Management**

#### State Definitions
```java
// common/statemachine/facility/FacilityState.java
DRAFT,      // 作成直後（変更可能）
FIXED       // Drawdown実行後（変更不可・確定済み）
```

#### Events Defined
```java
// common/statemachine/facility/FacilityEvent.java
DRAWDOWN_EXECUTED  // Drawdown実行
```

#### Usage Analysis

| Location | Method | Usage Type | Code Pattern | Issue |
|----------|---------|------------|--------------|-------|
| `FacilityService.java:224-242` | `fixFacility()` | 🟡 Mixed | State Machine validation + direct assignment | Acceptable pattern |
| `FacilityService.java:278-280` | `revertToDraft()` | 🔴 Bypass | Direct status modification | **Critical Issue** |
| `EntityStateService.java:112-113` | `transitionSyndicateToActive()` | 🟡 Mixed | Post-validation assignment | Acceptable |

**Critical Finding**: Missing `REVERT_TO_DRAFT` event forces bypass implementation

#### Code Examples

**Proper Pattern**:
```java
// FacilityService.java:232-241
boolean result = stateMachine.sendEvent(FacilityEvent.DRAWDOWN_EXECUTED);
if (!result) {
    throw new BusinessRuleViolationException("状態遷移が失敗しました。");
}
facility.setStatus(FacilityState.FIXED);
facilityRepository.save(facility);
```

**Bypass Pattern** (❌ Problem):
```java
// FacilityService.java:278-280
// Note: 既存のFacilityEvent列挙型にREVERT_TO_DRAFTがない場合は追加が必要
// 一時的に直接状態を変更
facility.setStatus(FacilityState.DRAFT);  // DIRECT BYPASS!
facilityRepository.save(facility);
```

### **Loan State Management**

#### State Definitions
```java
// common/statemachine/loan/LoanState.java
DRAFT,      // 作成直後状態（ドローダウン直後）
ACTIVE,     // 返済中状態（正常返済中）
OVERDUE,    // 遅延状態（返済遅延中）
COMPLETED   // 完済状態（返済完了）
```

#### Events Defined
```java
// common/statemachine/loan/LoanEvent.java
FIRST_PAYMENT,      // 初回返済実行
PAYMENT_OVERDUE,    // 支払い遅延
OVERDUE_RESOLVED,   // 遅延解消
FINAL_PAYMENT,      // 最終返済実行
PAYMENT_CANCELLED   // 支払い取り消し
```

#### Usage Analysis

| Location | Method | Usage Type | Code Pattern | Issue |
|----------|---------|------------|--------------|-------|
| `PaymentService.java:208-212` | `updateLoanStateForFirstPayment()` | 🟡 Mixed | State Machine + direct assignment | Standard pattern |
| `PaymentService.java:224-242` | `executeLoanStateTransition()` | 🟢 Proper | Full State Machine usage | Good implementation |
| `PaymentService.java:237-241` | Error handling | 🟡 Problematic | Silent failure handling | **Reliability Issue** |

**Notable Pattern**: State Machine used for validation, entity updated separately

#### Code Examples

**Mixed Pattern** (Standard but could be improved):
```java
// PaymentService.java:208-212
if (executeLoanStateTransition(loan, LoanEvent.FIRST_PAYMENT)) {
    loan.setStatus(LoanState.ACTIVE);
    loanRepository.save(loan);
}
```

**Problematic Error Handling**:
```java
// PaymentService.java:237-241
catch (Exception e) {
    System.err.println("Loan state transition failed for ID: " + loan.getId());
    return false; // Silent failure - business process continues
}
```

### **Party State Management**

#### State Definitions
```java
// Borrower States
ACTIVE,      // 通常状態
RESTRICTED   // Facility参加後制限

// Investor States  
ACTIVE,      // 通常状態
RESTRICTED   // Facility参加後制限
```

#### Events Defined
```java
// Borrower Events
FACILITY_PARTICIPATION  // Facility参加

// Investor Events
FACILITY_PARTICIPATION  // Facility参加
```

#### Usage Analysis

| Location | Method | Usage Type | Code Pattern | Issue |
|----------|---------|------------|--------------|-------|
| `EntityStateService.java:135-139` | `transitionBorrowerToRestricted()` | 🟡 Mixed | State Machine + assignment | Standard pattern |
| `EntityStateService.java:157-161` | `transitionInvestorToRestricted()` | 🟡 Mixed | State Machine + assignment | Standard pattern |
| `PartyService.java` | ❓ Unknown | ❓ Needs investigation | May have direct modifications | **Investigation needed** |

### **Syndicate State Management**

#### State Definitions
```java
// common/statemachine/syndicate/SyndicateState.java
DRAFT,   // 作成直後
ACTIVE   // 確定済み
```

#### Events Defined
```java
// common/statemachine/syndicate/SyndicateEvent.java
FACILITY_CREATED  // Facility組成
```

#### Usage Analysis

| Location | Method | Usage Type | Code Pattern | Issue |
|----------|---------|------------|--------------|-------|
| `EntityStateService.java:107-117` | `transitionSyndicateToActive()` | 🟡 Mixed | State Machine + assignment | Standard pattern |

### **Transaction State Management**

#### Investigation Required
- TransactionService.java exists but State Machine usage unclear
- Transaction entity has status field but integration unknown
- **Priority**: High - needs immediate investigation

## State Machine API Usage Patterns

### **Pattern 1: Validation + Assignment (Most Common)**
```java
// Standard pattern used throughout the application
if (stateMachine.sendEvent(event)) {
    entity.setStatus(newStatus);
    repository.save(entity);
}
```

**Pros**: 
- State Machine validates transitions
- Clear separation of concerns

**Cons**: 
- Dual responsibility for state management
- Temptation to bypass validation

### **Pattern 2: Cross-Context Coordination (EntityStateService)**
```java
// Centralized cross-bounded-context state management
public void onFacilityCreated(Facility facility) {
    transitionSyndicateToActive(syndicate);
    transitionBorrowerToRestricted(borrowerId);
    transitionInvestorToRestricted(investorId);
}
```

**Pros**: 
- Centralized coordination logic
- Proper event-driven design

**Cons**: 
- Still relies on Pattern 1 internally

### **Pattern 3: Direct Bypass (Anti-Pattern)**
```java
// Direct status modification - AVOID
entity.setStatus(newStatus);
repository.save(entity);
```

**Problems**: 
- No validation
- Bypasses business rules
- Breaks State Machine consistency

## State Machine Configuration Quality

### **Configuration Completeness**

| Entity | States Defined | Events Defined | Guards Implemented | Actions Implemented | Completeness Score |
|--------|---------------|----------------|-------------------|-------------------|-------------------|
| Facility | ✅ Complete | ⚠️ Missing REVERT_TO_DRAFT | ✅ Yes | ❌ No | 75% |
| Loan | ✅ Complete | ✅ Comprehensive | ✅ Yes | ❌ No | 85% |
| Borrower | ✅ Complete | ✅ Minimal but sufficient | ❌ Unknown | ❌ Unknown | 60% |
| Investor | ✅ Complete | ✅ Minimal but sufficient | ❌ Unknown | ❌ Unknown | 60% |
| Syndicate | ✅ Complete | ✅ Minimal but sufficient | ❌ Unknown | ❌ Unknown | 60% |

### **Missing Configurations**

1. **Facility**: REVERT_TO_DRAFT event needed
2. **All Entities**: Action implementations could improve automation
3. **Party Entities**: Guard implementations unknown
4. **Transaction**: Complete State Machine integration unclear

## Service Layer State Management Compliance

### **Compliance Scoring**

| Service | State Machine Import | Event Usage | Direct Status Modifications | Compliance Score |
|---------|---------------------|-------------|---------------------------|------------------|
| FacilityService | ✅ Yes | 🟡 Partial | 🔴 1 bypass | 60% |
| PaymentService | ✅ Yes | 🟡 Partial | 🟡 Mixed pattern | 70% |
| DrawdownService | ❓ Unknown | ❓ Unknown | ❓ Unknown | TBD |
| PartyService | ❓ Unknown | ❓ Unknown | ❓ Unknown | TBD |
| SyndicateService | ❓ Unknown | ❓ Unknown | ❓ Unknown | TBD |
| TransactionService | ❓ Unknown | ❓ Unknown | ❓ Unknown | TBD |

## Anti-Pattern Detection

### **Detected Anti-Patterns**

1. **Direct Status Assignment Bypass**
   - Location: `FacilityService.java:279`
   - Risk: High
   - Impact: Bypasses business rules

2. **Silent State Machine Failure**
   - Location: `PaymentService.java:237-241`
   - Risk: Medium
   - Impact: Reduced reliability

3. **Missing Event Definitions**
   - Context: Facility revert operations
   - Risk: Medium
   - Impact: Forces workarounds

### **Potential Anti-Patterns (Investigation Needed)**

1. **Unverified Services**: PartyService, DrawdownService, SyndicateService
2. **Transaction State Management**: Complete integration unknown
3. **Bulk Operations**: May bypass State Machine for performance

## State Machine Integration Quality Assessment

### **Strong Points**
- Comprehensive State Machine configurations exist
- Cross-bounded-context coordination implemented
- Guard conditions properly implemented where used

### **Weak Points**
- Inconsistent usage patterns across services
- Missing event definitions force bypasses
- Silent failure handling reduces reliability
- No enforcement mechanisms prevent bypasses

### **Critical Gaps**
- REVERT_TO_DRAFT event missing
- Transaction State Machine integration unclear
- Service-level compliance varies significantly
- No runtime validation against direct status changes

## Next Steps for Complete Mapping

1. **Investigate remaining services**: DrawdownService, PartyService, SyndicateService
2. **Complete Transaction State Machine analysis**
3. **Audit all `setStatus` calls** for bypass patterns
4. **Create comprehensive refactoring plan**
5. **Establish enforcement mechanisms**