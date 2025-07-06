# State Machine Implementation Guidelines

## Overview

This document establishes **mandatory standards** for State Machine usage throughout the Syndicated Loan Management System. These guidelines ensure **consistent**, **reliable**, and **maintainable** state management across all bounded contexts.

## Core Principles

### **1. State Machine Authority**
**🔴 MANDATORY**: All state transitions MUST go through the appropriate State Machine

```java
// ✅ CORRECT: Use State Machine for validation
boolean result = stateMachine.sendEvent(event);
if (result) {
    entity.setStatus(newStatus);
    repository.save(entity);
}

// ❌ FORBIDDEN: Direct status modification
entity.setStatus(newStatus);  // NEVER DO THIS
repository.save(entity);
```

### **2. Fail-Fast Principle**
**🔴 MANDATORY**: State Machine failures MUST halt business processes

```java
// ✅ CORRECT: Proper error handling
try {
    boolean result = stateMachine.sendEvent(event);
    if (!result) {
        throw new BusinessRuleViolationException("State transition rejected");
    }
} catch (Exception e) {
    logger.error("State transition failed", e);
    throw new BusinessRuleViolationException("State management error", e);
}

// ❌ FORBIDDEN: Silent failure handling
try {
    stateMachine.sendEvent(event);
} catch (Exception e) {
    System.err.println("Failed"); // Silent ignore
    return false;
}
```

### **3. Complete Event Modeling**
**🔴 MANDATORY**: All business state changes MUST have corresponding events

```java
// ✅ CORRECT: Complete event coverage
public enum FacilityEvent {
    DRAWDOWN_EXECUTED,  // Business event
    REVERT_TO_DRAFT     // Reversal event
}

// ❌ INCOMPLETE: Missing reversal events forces bypasses
public enum FacilityEvent {
    DRAWDOWN_EXECUTED   // Only forward transitions
}
```

## Implementation Standards

### **Standard Service Pattern**

#### **Base State Service Template**
```java
public abstract class BaseStateService<S, E> {
    
    private static final Logger logger = LoggerFactory.getLogger(BaseStateService.class);
    
    /**
     * Standard State Machine execution pattern
     * 
     * @param stateMachine The State Machine instance
     * @param event The event to trigger
     * @param entityType Entity type for logging
     * @param entityId Entity ID for context
     * @return true if transition successful
     * @throws BusinessRuleViolationException if transition fails
     */
    protected boolean executeStateTransition(
            StateMachine<S, E> stateMachine,
            E event,
            String entityType,
            Long entityId) {
        
        try {
            // Standard State Machine preparation
            stateMachine.getStateMachineAccessor().doWithAllRegions(access -> {
                access.resetStateMachine(null);
            });
            
            // Set entity context
            stateMachine.getExtendedState().getVariables().put("entityId", entityId);
            
            // Execute transition
            boolean result = stateMachine.sendEvent(event);
            
            // Standard logging
            if (result) {
                logger.info("State transition successful: entity={}, id={}, event={}", 
                    entityType, entityId, event);
            } else {
                logger.warn("State transition rejected: entity={}, id={}, event={}", 
                    entityType, entityId, event);
                throw new BusinessRuleViolationException(
                    String.format("State transition rejected for %s %d: %s", 
                        entityType, entityId, event));
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("State transition failed: entity={}, id={}, event={}", 
                entityType, entityId, event, e);
            
            throw new BusinessRuleViolationException(
                String.format("%s state transition failed", entityType), e);
        }
    }
}
```

#### **Service Implementation Example**
```java
@Service
@Transactional
public class FacilityService extends BaseStateService<FacilityState, FacilityEvent> {
    
    private final StateMachine<FacilityState, FacilityEvent> facilityStateMachine;
    private final FacilityRepository facilityRepository;
    
    /**
     * Fix facility status (DRAFT → FIXED)
     * Standard pattern for state transitions
     */
    public Facility fixFacility(Long facilityId) {
        Facility facility = getFacilityById(facilityId);
        
        // Validate current state
        if (facility.getStatus() != FacilityState.DRAFT) {
            throw new BusinessRuleViolationException(
                "Only DRAFT facilities can be fixed");
        }
        
        // Execute State Machine transition
        boolean result = executeStateTransition(
            facilityStateMachine, 
            FacilityEvent.DRAWDOWN_EXECUTED, 
            "Facility", 
            facilityId);
        
        // Update entity state
        facility.setStatus(FacilityState.FIXED);
        facility = facilityRepository.save(facility);
        
        // Trigger cross-context coordination
        entityStateService.onFacilityFixed(facility);
        
        return facility;
    }
}
```

### **State Machine Configuration Standards**

#### **Required Configuration Elements**
```java
@Configuration
@EnableStateMachine
public class FacilityStateMachineConfig extends StateMachineConfigurerAdapter<FacilityState, FacilityEvent> {
    
    @Override
    public void configure(StateMachineStateConfigurer<FacilityState, FacilityEvent> states) 
            throws Exception {
        states
            .withStates()
            .initial(FacilityState.DRAFT)  // ✅ REQUIRED: Initial state
            .states(EnumSet.allOf(FacilityState.class));  // ✅ REQUIRED: All states
    }
    
    @Override
    public void configure(StateMachineTransitionConfigurer<FacilityState, FacilityEvent> transitions) 
            throws Exception {
        transitions
            .withExternal()
                .source(FacilityState.DRAFT)
                .target(FacilityState.FIXED)
                .event(FacilityEvent.DRAWDOWN_EXECUTED)
                .guard(drawdownOnlyFromDraftGuard())  // ✅ REQUIRED: Guard conditions
                .action(facilityFixedAction())        // 🟡 RECOMMENDED: Actions
            .and()
            .withExternal()
                .source(FacilityState.FIXED)
                .target(FacilityState.DRAFT)
                .event(FacilityEvent.REVERT_TO_DRAFT)
                .guard(revertToDraftGuard())
                .action(facilityRevertedAction());
    }
    
    /**
     * ✅ REQUIRED: Guard conditions for all transitions
     */
    private Guard<FacilityState, FacilityEvent> drawdownOnlyFromDraftGuard() {
        return context -> {
            FacilityState currentState = context.getStateMachine().getState().getId();
            return FacilityState.DRAFT.equals(currentState);
        };
    }
    
    /**
     * 🟡 RECOMMENDED: Action implementations for automation
     */
    private Action<FacilityState, FacilityEvent> facilityFixedAction() {
        return context -> {
            Long facilityId = (Long) context.getExtendedState().getVariables().get("entityId");
            
            // Automatic cross-context coordination
            entityStateService.onFacilityFixed(facilityId);
            
            // Automatic audit logging
            auditService.recordFacilityFixed(facilityId);
        };
    }
}
```

### **Error Handling Standards**

#### **Exception Hierarchy**
```java
// Base exception for all state management errors
public class StateManagementException extends RuntimeException {
    public StateManagementException(String message) { super(message); }
    public StateManagementException(String message, Throwable cause) { super(message, cause); }
}

// Business rule violations
public class BusinessRuleViolationException extends StateManagementException {
    public BusinessRuleViolationException(String message) { super(message); }
    public BusinessRuleViolationException(String message, Throwable cause) { super(message, cause); }
}

// State Machine technical failures
public class StateMachineExecutionException extends StateManagementException {
    public StateMachineExecutionException(String message, Throwable cause) { super(message, cause); }
}
```

#### **Logging Standards**
```java
// ✅ REQUIRED: Structured logging for state transitions
logger.info("State transition executed: entity={}, id={}, from={}, to={}, event={}, duration={}ms",
    entityType, entityId, fromState, toState, event, duration);

// ✅ REQUIRED: Warning for rejected transitions
logger.warn("State transition rejected: entity={}, id={}, event={}, currentState={}, reason={}",
    entityType, entityId, event, currentState, reason);

// ✅ REQUIRED: Error logging for failures
logger.error("State transition failed: entity={}, id={}, event={}, currentState={}",
    entityType, entityId, event, currentState, exception);
```

## Entity-Specific Guidelines

### **Facility State Management**

#### **States and Events**
```java
public enum FacilityState {
    DRAFT,      // Modifiable state
    FIXED       // Locked state after drawdown
}

public enum FacilityEvent {
    DRAWDOWN_EXECUTED,  // Lock facility
    REVERT_TO_DRAFT     // Unlock facility (with business rules)
}
```

#### **Business Rules**
- **DRAFT → FIXED**: Only when drawdown is executed
- **FIXED → DRAFT**: Only when all drawdowns are deleted
- **Cross-Context**: Triggers Syndicate, Borrower, Investor state changes

#### **Implementation Requirements**
```java
@Service
public class FacilityService extends BaseStateService<FacilityState, FacilityEvent> {
    
    // ✅ REQUIRED: Validation before state transition
    public void fixFacility(Long facilityId) {
        Facility facility = validateFacilityForFixing(facilityId);
        executeStateTransition(facilityStateMachine, FacilityEvent.DRAWDOWN_EXECUTED, "Facility", facilityId);
        updateFacilityStatus(facility, FacilityState.FIXED);
        triggerCrossContextCoordination(facility);
    }
    
    // ✅ REQUIRED: Business rule validation
    private Facility validateFacilityForFixing(Long facilityId) {
        Facility facility = getFacilityById(facilityId);
        if (facility.getStatus() != FacilityState.DRAFT) {
            throw new BusinessRuleViolationException("Only DRAFT facilities can be fixed");
        }
        return facility;
    }
}
```

### **Loan State Management**

#### **States and Events**
```java
public enum LoanState {
    DRAFT,      // Post-drawdown, pre-payment
    ACTIVE,     // Active repayment
    OVERDUE,    // Overdue payments
    COMPLETED   // Fully repaid
}

public enum LoanEvent {
    FIRST_PAYMENT,      // Activate loan
    PAYMENT_OVERDUE,    // Mark overdue
    OVERDUE_RESOLVED,   // Resolve overdue
    FINAL_PAYMENT,      // Complete loan
    PAYMENT_CANCELLED   // Reverse payment
}
```

#### **Business Rules**
- **DRAFT → ACTIVE**: First payment triggers activation
- **ACTIVE ↔ OVERDUE**: Based on payment timing
- **ACTIVE/OVERDUE → COMPLETED**: Final payment completion
- **Automated Detection**: Scheduled process for overdue detection

#### **Implementation Requirements**
```java
@Service
public class LoanService extends BaseStateService<LoanState, LoanEvent> {
    
    // ✅ REQUIRED: Automated state management
    @Scheduled(fixedRate = 3600000) // Every hour
    public void detectOverdueLoans() {
        List<Loan> activeLoans = loanRepository.findByStatus(LoanState.ACTIVE);
        LocalDate today = LocalDate.now();
        
        for (Loan loan : activeLoans) {
            if (hasOverduePayments(loan, today)) {
                executeStateTransition(loanStateMachine, LoanEvent.PAYMENT_OVERDUE, "Loan", loan.getId());
                updateLoanStatus(loan, LoanState.OVERDUE);
                notificationService.sendOverdueNotification(loan);
            }
        }
    }
}
```

### **Party State Management**

#### **Cross-Context Coordination**
```java
@Service
@Transactional
public class EntityStateService {
    
    // ✅ REQUIRED: Coordinated state management
    public void onFacilityCreated(Facility facility) {
        // Coordinate multiple entity state changes atomically
        transitionSyndicateToActive(facility.getSyndicateId());
        transitionBorrowerToRestricted(getBorrowerId(facility));
        transitionInvestorsToRestricted(getInvestorIds(facility));
        
        // Audit coordination
        auditService.recordCrossContextStateChange("FacilityCreated", facility.getId());
    }
}
```

## Development Workflow

### **Pre-Development Checklist**
- [ ] Identify all required state transitions for the feature
- [ ] Design events and guard conditions
- [ ] Plan cross-context coordination requirements
- [ ] Design error handling strategy

### **Implementation Checklist**
- [ ] Extend appropriate State Machine configuration
- [ ] Implement service using BaseStateService
- [ ] Add comprehensive guard conditions
- [ ] Implement cross-context coordination
- [ ] Add proper error handling and logging

### **Testing Checklist**
- [ ] Unit tests for all state transitions
- [ ] Unit tests for guard conditions
- [ ] Integration tests for cross-context coordination
- [ ] Error handling tests
- [ ] Performance tests for State Machine operations

### **Code Review Checklist**
- [ ] No direct status modifications (`entity.setStatus()` without State Machine)
- [ ] Proper error handling (no silent failures)
- [ ] Comprehensive logging
- [ ] Guard conditions cover all business rules
- [ ] Cross-context coordination implemented

## Performance Guidelines

### **State Machine Optimization**
```java
// ✅ RECOMMENDED: Optimize State Machine usage
@Service
public class OptimizedStateService {
    
    // Cache State Machine configurations
    private final Map<String, StateMachine<?, ?>> stateMachineCache = new ConcurrentHashMap<>();
    
    // Use pooling for high-frequency operations
    private final ObjectPool<StateMachine<FacilityState, FacilityEvent>> facilityStateMachinePool;
    
    public boolean executeOptimizedTransition(FacilityEvent event, Long facilityId) {
        StateMachine<FacilityState, FacilityEvent> sm = null;
        try {
            sm = facilityStateMachinePool.borrowObject();
            return executeWithPooledStateMachine(sm, event, facilityId);
        } catch (Exception e) {
            throw new StateMachineExecutionException("Pooled execution failed", e);
        } finally {
            if (sm != null) {
                facilityStateMachinePool.returnObject(sm);
            }
        }
    }
}
```

### **Performance Monitoring**
```java
// ✅ REQUIRED: Performance metrics for State Machine operations
@Component
public class StateMachineMetrics {
    
    private final Timer transitionTimer;
    private final Counter successCounter;
    private final Counter failureCounter;
    
    @EventListener
    public void onStateTransition(StateTransitionEvent event) {
        transitionTimer.record(event.getDuration(), TimeUnit.MILLISECONDS);
        
        if (event.isSuccessful()) {
            successCounter.increment();
        } else {
            failureCounter.increment();
        }
    }
}
```

## Security Considerations

### **Runtime Enforcement**
```java
// ✅ RECOMMENDED: Runtime validation against bypasses
@Aspect
@Component
public class StateManagementSecurityAspect {
    
    @Around("execution(* *.setStatus(..))")
    public Object validateStatusChange(ProceedingJoinPoint joinPoint) throws Throwable {
        // Validate that status changes are State Machine authorized
        Object target = joinPoint.getTarget();
        Object[] args = joinPoint.getArgs();
        
        if (!isStateMachineAuthorized(target, args)) {
            logger.error("SECURITY: Unauthorized direct status modification attempted: {}", 
                target.getClass().getSimpleName());
            throw new SecurityException("Direct status modifications are not allowed");
        }
        
        return joinPoint.proceed();
    }
}
```

### **Audit Requirements**
```java
// ✅ REQUIRED: Complete audit trail for state changes
@EventListener
public void auditStateChange(StateTransitionEvent event) {
    StateChangeAuditRecord audit = StateChangeAuditRecord.builder()
        .entityType(event.getEntityType())
        .entityId(event.getEntityId())
        .fromState(event.getFromState())
        .toState(event.getToState())
        .event(event.getEvent())
        .userId(SecurityContextHolder.getContext().getAuthentication().getName())
        .timestamp(Instant.now())
        .context(event.getContext())
        .build();
    
    auditRepository.save(audit);
}
```

## Common Anti-Patterns to Avoid

### **❌ Anti-Pattern 1: Direct Status Bypass**
```java
// NEVER DO THIS
facility.setStatus(FacilityState.FIXED);
repository.save(facility);
```

### **❌ Anti-Pattern 2: Silent Failure Handling**
```java
// NEVER DO THIS
try {
    stateMachine.sendEvent(event);
} catch (Exception e) {
    // Silent ignore
    return false;
}
```

### **❌ Anti-Pattern 3: Incomplete Event Modeling**
```java
// AVOID: Missing events force workarounds
if (needsRevert) {
    // Comment: "Temporary direct assignment"
    entity.setStatus(originalState);  // Bypass due to missing event
}
```

### **❌ Anti-Pattern 4: Mixed Validation**
```java
// AVOID: Inconsistent validation
if (someCondition) {
    stateMachine.sendEvent(event);  // Validated
} else {
    entity.setStatus(newStatus);    // Unvalidated
}
```

## Migration Guidelines

### **Legacy Code Migration**
1. **Identify** all direct status modifications
2. **Design** appropriate events and transitions
3. **Implement** State Machine configuration
4. **Replace** direct modifications with State Machine calls
5. **Test** thoroughly with all scenarios
6. **Deploy** with feature flags for gradual rollout

### **Backward Compatibility**
- Maintain existing API signatures during migration
- Use feature flags to control State Machine activation
- Provide fallback mechanisms during transition period
- Monitor performance impact during migration

## Conclusion

These guidelines ensure **consistent**, **reliable**, and **maintainable** state management throughout the application. **Strict adherence** to these standards is **mandatory** for all state-related development.

**Key Principles to Remember**:
1. **State Machine Authority**: All transitions go through State Machine
2. **Fail-Fast**: State failures halt business processes
3. **Complete Modeling**: Every business transition has an event
4. **Comprehensive Testing**: All scenarios must be tested
5. **Proper Monitoring**: All transitions must be observable

Following these guidelines will ensure the **integrity** and **reliability** of the state management system across all bounded contexts.