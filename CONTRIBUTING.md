# Contributing to SOFARegistry

Thank you for your interest in contributing to SOFARegistry! This document provides guidelines and instructions for contributing.

## Table of Contents

- [Development Environment](#development-environment)
- [Building the Project](#building-the-project)
- [Running Tests](#running-tests)
- [Making Changes](#making-changes)
- [Submitting a Pull Request](#submitting-a-pull-request)
- [Code Style](#code-style)
- [Reporting Issues](#reporting-issues)

## Development Environment

### Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK | 8+ (JDK 8 recommended) |
| Maven | 3.2.5+ |
| Git | 2.x+ |
| Docker | Optional, for integration testing |

### Setup

```bash
# Clone the repository
git clone https://github.com/sofastack/sofa-registry.git
cd sofa-registry

# Verify your environment
java -version   # Should be 1.8.x
mvn -version    # Should be 3.2.5+

# Build the project
mvn clean install -DskipTests
```

### IDE Setup

**IntelliJ IDEA (Recommended)**
1. Import as Maven project
2. Enable annotation processing: `Settings > Build > Compiler > Annotation Processors`
3. Import code style: use Alibaba Java Coding Guidelines plugin

**VS Code**
1. Install "Extension Pack for Java"
2. Open the project folder
3. Wait for Maven import to complete

## Building the Project

```bash
# Full build (skip tests)
mvn clean install -DskipTests

# Build specific module
mvn clean install -DskipTests -pl server/server/meta -am

# Generate protobuf classes
make proto
```

## Running Tests

### Unit Tests

```bash
# Run all unit tests
mvn test

# Run specific test class
mvn test -Dtest=YourTestClass

# Run with debug output
mvn test -Dtest=YourTestClass -Dtest.logging.level=DEBUG
```

### Integration Tests

```bash
# Run integration tests (requires more time)
mvn verify -Pintegration-test

# Run specific integration test
mvn test -Dtest=PubSubTest -pl test
```

### Local Development Server

```bash
# Start all-in-one server for local development
cd server/server/integration
mvn spring-boot:run -Dspring.profiles.active=dev
```

## Making Changes

### Branch Naming

```
<type>/<short-description>

Examples:
  feature/add-health-check-api
  fix/session-timeout-issue
  docs/update-readme
  refactor/simplify-push-logic
```

| Type | Description |
|------|-------------|
| `feature` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `refactor` | Code refactoring |
| `test` | Adding or updating tests |
| `chore` | Build, CI, or tooling changes |

### Commit Message Format

```
[module] Short description (under 72 chars)

Optional longer description explaining the change.
- Why is this change necessary?
- How does it address the issue?

Fixes #123
```

**Examples:**
```
[session] Fix connection timeout handling

The previous implementation did not properly handle timeouts
when the meta server was unreachable.

Fixes #456
```

```
[client] Add retry mechanism for failed registrations
```

### Module Prefixes

| Prefix | Module |
|--------|--------|
| `[meta]` | server/server/meta |
| `[data]` | server/server/data |
| `[session]` | server/server/session |
| `[client]` | client/* |
| `[core]` | core |
| `[remoting]` | server/remoting/* |
| `[store]` | server/store/* |
| `[test]` | test |
| `[build]` | pom.xml, CI/CD |
| `[docs]` | Documentation |

## Submitting a Pull Request

### Before Submitting

1. **Sync with upstream**
   ```bash
   git fetch upstream
   git rebase upstream/master
   ```

2. **Run checks locally**
   ```bash
   # Code style check
   mvn pmd:check

   # Unit tests
   mvn test

   # Build verification
   mvn clean install -DskipTests
   ```

3. **Keep PRs focused**
   - One PR per feature/fix
   - Avoid mixing refactoring with feature changes

### PR Process

1. Push your branch to your fork
2. Create a Pull Request against `master` branch
3. Fill in the PR template completely
4. Wait for CI checks to pass
5. Address review feedback
6. Squash commits if requested

### PR Checklist

- [ ] Code compiles without errors
- [ ] All tests pass
- [ ] PMD check passes
- [ ] New code has appropriate test coverage
- [ ] Documentation updated if needed
- [ ] Commit messages follow the format
- [ ] PR description explains the change

## Code Style

### General Rules

- Follow [Alibaba Java Coding Guidelines](https://github.com/alibaba/p3c)
- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Use braces for all control structures

### Naming Conventions

```java
// Classes: PascalCase
public class SessionServerBootstrap { }

// Methods/Variables: camelCase
public void handleRequest() { }
private int connectionCount;

// Constants: UPPER_SNAKE_CASE
public static final int MAX_RETRY_COUNT = 3;

// Packages: lowercase
package com.alipay.sofa.registry.server.session;
```

### Logging

```java
// Use project logger, not SLF4J directly
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

private static final Logger LOGGER = LoggerFactory.getLogger(MyClass.class);

// Use parameterized logging
LOGGER.info("Processing request: {}, count: {}", requestId, count);

// For critical errors
CRITICAL_LOGGER.safeError("Fatal error: {}", message, exception);
```

### Exception Handling

```java
// Do: Handle exceptions meaningfully
try {
    doSomething();
} catch (SpecificException e) {
    LOGGER.error("Failed to do something: {}", e.getMessage(), e);
    throw new RegistryException("Operation failed", e);
}

// Don't: Swallow exceptions silently
try {
    doSomething();
} catch (Exception e) {
    // Bad: silent catch
}
```

## Reporting Issues

### Bug Reports

Use the [Bug Report template](https://github.com/sofastack/sofa-registry/issues/new?template=Bug_Report.md) and include:

- SOFARegistry version
- JDK version
- Operating system
- Steps to reproduce
- Expected vs actual behavior
- Relevant logs

### Feature Requests

Use the [Feature Request template](https://github.com/sofastack/sofa-registry/issues/new?template=feature_request.md) and include:

- Use case description
- Proposed solution
- Alternatives considered

## Getting Help

- GitHub Issues: For bugs and feature requests
- GitHub Discussions: For questions and discussions
- Documentation: https://www.sofastack.tech/sofa-registry/docs/

---

Thank you for contributing to SOFARegistry!
