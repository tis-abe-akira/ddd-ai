# Syndicated Loan Management System - Documentation

## Overview

This directory contains comprehensive documentation for the Syndicated Loan Management System backend architecture, with a focus on the recently implemented **Spring Events + StateMachine Coexistence Pattern**.

## 📁 Documentation Structure

### Core Architecture Documents

- **[architecture-overview.md](./architecture-overview.md)** - High-level system architecture and key components
- **[spring-events-statemachine-integration.md](./spring-events-statemachine-integration.md)** - Detailed explanation of the event-driven state management architecture
- **[cross-context-dependencies.md](./cross-context-dependencies.md)** - How cross-context dependencies are resolved through events
- **[bounded-context-detail.md](./bounded-context-detail.md)** - Internal structure of each bounded context with 3-layer architecture

### Architecture Evaluation

- **[architecture-evaluation/memo.md](./architecture-evaluation/memo.md)** - Historical evaluation, challenges identified, and solutions implemented

## 🏗️ Key Architectural Achievement

### Problem Solved
The monolithic **EntityStateService** (661 lines) that became unwieldy has been successfully refactored into a sophisticated **Spring Events + StateMachine** architecture.

### Solution Implemented
```mermaid
graph LR
    A[Business Service] -->|publishEvent| B[ApplicationEventPublisher]
    B -.->|@EventListener| C[Event Handler]
    C --> D[State Manager]
    D --> E[StateMachine Executor]
    E --> F[StateMachine]
```

### Key Benefits
- ✅ **Event-Centric Organization**: Split by "Events that occur" rather than "affected entity states"
- ✅ **Preserved StateMachine Logic**: All existing business rules and constraints maintained
- ✅ **Reduced Code Duplication**: Common infrastructure for state transitions
- ✅ **Improved Testability**: Each component can be tested in isolation
- ✅ **Better Maintainability**: Clear separation of responsibilities

## 🎯 Architecture Highlights

### 1. Event-Driven State Management
- **Domain Events**: FacilityCreatedEvent, DrawdownCreatedEvent, etc.
- **Event Handlers**: FacilityLifecycleEventHandler, DrawdownLifecycleEventHandler
- **State Managers**: Entity-specific state transition logic
- **Common Infrastructure**: StateMachineExecutor for unified execution

### 2. Cross-Context Coordination
Instead of direct repository dependencies, the system now uses events to coordinate state changes across bounded contexts:

```java
// Before: Direct dependency
if (syndicateRepository.existsByBorrowerId(id)) {
    throw new BusinessRuleViolationException("削除できません");
}

// After: Event-driven coordination
eventPublisher.publishEvent(new FacilityCreatedEvent(facility));
```

### 3. State Machine Integration
- **Facility Lifecycle**: DRAFT → FIXED (immutable after first drawdown)
- **Loan Lifecycle**: DRAFT → ACTIVE → OVERDUE → COMPLETED
- **Party Status**: ACTIVE → RESTRICTED (when participating in facilities)
- **Transaction Status**: PENDING → PROCESSING → COMPLETED/CANCELLED

## 📊 Implementation Results

### Test Coverage
- **190/190 tests passing** ✅
- **0 failures, 0 errors** ✅
- **Complete migration** from EntityStateService ✅

### Architecture Metrics
- **Code Reduction**: 661-line monolith → distributed specialized components
- **Responsibility Distribution**: Single Responsibility Principle enforced
- **Maintainability**: Event-centric organization improves readability

## 🔄 Evolution Path

This architecture provides a foundation for future enhancements:

1. **Domain Event Sourcing**: Current event infrastructure can evolve to full event sourcing
2. **CQRS Implementation**: Read/Write model separation
3. **Microservices Decomposition**: Event boundaries naturally become service boundaries
4. **Distributed Systems**: Event-driven architecture supports distributed processing

## 🎉 Success Factors

### DDD Principles Adherence
- **Bounded Context Boundaries**: Event-mediated loose coupling
- **Single Responsibility**: Each component has a focused purpose
- **Domain Events**: Business events represented in the domain model

### Practical Benefits
- **Data Consistency**: Strong consistency required for financial systems
- **Business Rules**: Complete preservation of existing business logic
- **Operational Safety**: Gradual migration ensuring system stability

## 📖 Reading Guide

1. **Start with** [architecture-overview.md](./architecture-overview.md) for the big picture
2. **Deep dive** into [spring-events-statemachine-integration.md](./spring-events-statemachine-integration.md) for implementation details
3. **Understand** cross-context relationships in [cross-context-dependencies.md](./cross-context-dependencies.md)
4. **Explore** internal structure in [bounded-context-detail.md](./bounded-context-detail.md)
5. **Review** the journey in [architecture-evaluation/memo.md](./architecture-evaluation/memo.md)

---

This documentation reflects the current state of the system after the successful implementation of the Spring Events + StateMachine coexistence pattern in July 2025.