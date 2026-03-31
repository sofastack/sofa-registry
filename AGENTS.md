# AGENTS.md - AI Agent Development Guide

> This file provides context for AI coding assistants (Claude Code, GitHub Copilot, Cursor, Cline, Aider, etc.)

## ⚠️ Critical Rules (Read First)

```
1. DO NOT edit generated files in target/
2. DO NOT delete backward-compatible code without discussion
3. DO NOT add new dependencies without review
4. DO NOT swallow exceptions silently
5. DO NOT create threads directly - use thread pools
6. DO NOT hardcode sensitive data
7. DO NOT skip tests with @Ignore without explanation
```

## Project Overview

**SOFARegistry** is a production-grade service registry for microservices, developed by Ant Group. It features:
- AP architecture with millisecond-level push notifications
- Multi-tier architecture for horizontal scalability
- Support for massive connections and large-scale datasets

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 8+ |
| Build | Maven 3.2.5+ |
| Framework | Spring Boot 2.3.x |
| RPC | SOFABolt |
| Storage | JRaft, JDBC (H2/MySQL) |
| Serialization | Protobuf 3.17, Hessian |
| HTTP | Jersey 2.26, Jetty |

## Module Structure

```
sofa-registry/
├── client/                    # Client SDK
│   ├── api/                   # Client API interfaces
│   ├── impl/                  # Client implementation
│   ├── log/                   # Client logging
│   └── all/                   # Aggregated client artifact
├── server/
│   ├── common/
│   │   ├── model/             # Data models & Protobuf definitions
│   │   └── util/              # Common utilities
│   ├── remoting/
│   │   ├── api/               # Remoting abstractions
│   │   ├── bolt/              # SOFABolt implementation
│   │   └── http/              # HTTP/REST implementation
│   ├── server/
│   │   ├── meta/              # Meta server (cluster coordination)
│   │   ├── data/              # Data server (data storage)
│   │   ├── session/           # Session server (client connections)
│   │   ├── shared/            # Shared server components
│   │   └── integration/       # Integration entry point
│   ├── store/
│   │   ├── api/               # Storage abstractions
│   │   ├── jraft/             # JRaft-based storage
│   │   └── jdbc/              # JDBC-based storage
│   └── distribution/          # Distribution packaging
├── core/                      # Core shared code
└── test/                      # Integration tests
```

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      Clients                             │
└─────────────────────┬───────────────────────────────────┘
                      │ SOFABolt
┌─────────────────────▼───────────────────────────────────┐
│                 Session Server                           │
│  (Client connections, Pub/Sub routing)                   │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                  Data Server                             │
│  (Data storage, Slot-based sharding)                     │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                  Meta Server                             │
│  (Cluster coordination, Leader election)                 │
└─────────────────────────────────────────────────────────┘
```

## Development Commands

```bash
# Build (skip tests)
mvn clean install -DskipTests

# Run unit tests
mvn test

# Run integration tests
mvn verify -Pintegration-test

# Run single test class
mvn test -Dtest=ClassName

# Generate protobuf
make proto

# Build Docker image
make image_build
```

## Key Entry Points

| Component | Main Class |
|-----------|------------|
| All-in-one | `com.alipay.sofa.registry.server.integration.RegistryApplication` |
| Meta Server | `com.alipay.sofa.registry.server.meta.MetaApplication` |
| Data Server | `com.alipay.sofa.registry.server.data.DataApplication` |
| Session Server | `com.alipay.sofa.registry.server.session.SessionApplication` |

## Code Conventions

### Naming
- Classes: `PascalCase`
- Methods/Variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Packages: `com.alipay.sofa.registry.*`

### Style Rules (from ruleset.xml)
- Follow Alibaba Java Coding Guidelines (P3C)
- Use braces for all control structures
- Avoid manual thread creation (use thread pools)
- Handle exceptions properly, don't swallow them

### Logging
- Use `com.alipay.sofa.registry.log.Logger` (not SLF4J directly)
- Critical errors: `CRITICAL_LOGGER.safeError(...)`
- Use parameterized logging: `LOGGER.info("msg: {}", value)`

### Testing
- Unit tests: `*Test.java` in `src/test/java`
- Integration tests: `test/` module, extend `BaseIntegrationTest`
- Use JUnit 4 with `@Test` annotations

## Common Tasks

### Adding a new API endpoint
1. Define handler in `server/server/{meta|data|session}/src/.../resource/`
2. Register in corresponding `*Configuration.java`
3. Add tests in same module's test directory

### Modifying data models
1. Edit `.proto` files in `server/common/model/src/main/resources/proto/`
2. Run `make proto` to regenerate Java classes
3. Update corresponding Java wrappers if needed

### Adding client functionality
1. Define interface in `client/api/`
2. Implement in `client/impl/`
3. Add integration tests in `test/` module

## Important Patterns

### Bootstrap Pattern
Each server has a `*ServerBootstrap` class that initializes components:
- `MetaServerBootstrap`
- `DataServerBootstrap`
- `SessionServerBootstrap`

### Configuration Pattern
Spring configurations in `*ServerConfiguration.java` classes define beans and wiring.

### Handler Pattern
Request handlers implement specific interfaces and are registered with remoting layer.

## Prohibited Actions (IMPORTANT)

### Code Modification Rules

| ❌ Don't | ✅ Do Instead |
|----------|---------------|
| Edit generated protobuf files in `target/` | Modify `.proto` source files and run `make proto` |
| Delete backward-compatible code without discussion | Mark as `@Deprecated` first, remove in future versions |
| Add `@SuppressWarnings` to hide real issues | Fix the underlying problem |
| Catch and swallow exceptions silently | Log and handle exceptions properly |
| Create threads directly with `new Thread()` | Use thread pools from `ExecutorManager` |

### Configuration Rules

| ❌ Don't | ✅ Do Instead |
|----------|---------------|
| Hardcode IP addresses or ports | Use configuration properties |
| Add sensitive data (passwords, keys) to code | Use environment variables or config files |
| Modify `application.properties` defaults without review | Document the change and its impact |
| Change default timeout values arbitrarily | Benchmark and validate changes |

### Testing Rules

| ❌ Don't | ✅ Do Instead |
|----------|---------------|
| Skip tests with `@Ignore` without explanation | Add comment explaining why and create issue to fix |
| Add `Thread.sleep()` in tests for timing | Use `Awaitility` or proper synchronization |
| Write tests that depend on execution order | Make each test independent |
| Mock internal classes excessively | Test through public APIs |

### Architecture Rules

| ❌ Don't | ✅ Do Instead |
|----------|---------------|
| Add cross-module dependencies without review | Discuss architectural changes first |
| Bypass the remoting layer for direct network calls | Use provided remoting abstractions |
| Store state in static variables | Use proper dependency injection |
| Add new third-party dependencies without review | Evaluate impact and alternatives |

### Security Rules

| ❌ Don't | ✅ Do Instead |
|----------|---------------|
| Log sensitive information (tokens, credentials) | Mask or omit sensitive data in logs |
| Disable SSL/TLS verification | Fix certificate issues properly |
| Use deprecated crypto algorithms | Use modern, approved algorithms |
| Trust user input without validation | Validate at system boundaries |

### Files to Avoid Modifying

- Generated protobuf files in `target/`
- IDE-specific files (`.idea/`, `.vscode/`)

## Quick Reference

| Action | Command/Location |
|--------|------------------|
| Find a class | `server/server/{module}/src/main/java/com/alipay/sofa/registry/server/{module}/` |
| Configuration | `src/main/resources/application.properties` |
| SQL scripts | `create_table.sql`, `server/server/integration/src/main/resources/sql/` |
| Docker setup | `docker/compose/` |

---

*Last updated: 2026-03-31*
