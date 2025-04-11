# SOFARegistry

[![unit test](https://github.com/sofastack/sofa-registry/actions/workflows/unit-test.yml/badge.svg)](https://github.com/sofastack/sofa-registry/actions/workflows/unit-test.yml)
[![integration test](https://github.com/sofastack/sofa-registry/actions/workflows/integration-test.yml/badge.svg)](https://github.com/sofastack/sofa-registry/actions/workflows/integration-test.yml)
![license](https://img.shields.io/badge/license-Apache--2.0-green.svg)
[![codecov](https://codecov.io/gh/sofastack/sofa-registry/branch/master/graph/badge.svg?token=K6x7h4Uxkn)](https://codecov.io/gh/sofastack/sofa-registry)
![maven](https://img.shields.io/github/release/sofastack/sofa-registry.svg)

[中文版本](./README_ZH.md)

SOFARegistry is a production-level, low-latency, high-availability registry system for micro-services registration powered by Ant Group. SOFARegistry originated from Taobao's ConfigServer. Over the past decade, as Ant Group's business has grown, its registry architecture has evolved to its sixth generation. Currently, SOFARegistry not only fully supports Ant Group's internal services, but also serves numerous partners through Ant Financial Technology while maintaining compatibility with the open-source ecosystem. Adopting an AP architecture, SOFARegistry enables millisecond-level real-time push notifications. Additionally, its layered architecture supports unlimited horizontal scalability.

## Functionality 

- Supports service publishing and subscription
- Provides real-time notifications upon service change
- Offers enhanced service governance API endpoints
- Employs multi-tier architecture for traffic and data wise to handle massive connections and large-scale datasets
- Ensures high data availability through facility replication capabilities
- Enables millisecond-level notifications for service registration/deregistration via [SOFABolt](https://github.com/alipay/sofa-bolt) communication framework
- AP architecture guarantees availability under network partitions


## Requirement

- Compilation requires JDK 8+ and Maven 3.2.5+
- Runtime environment requires JDK 6+ (JDK 8+ for server-side deployment)
- **Recommended: JDK 8 (JDK 16 has not been tested and may have compatibility issues)**

## Docs

- [Quick Start](https://www.sofastack.tech/sofa-registry/docs/Server-QuickStart)
- [Developer Guide](https://www.sofastack.tech/sofa-registry/docs/JAVA-SDK)
- [Operations Manual](https://www.sofastack.tech/sofa-registry/docs/Deployment)
- [Release Notes](https://www.sofastack.tech/sofa-registry/docs/ReleaseNotes)
- [Roadmap](https://www.sofastack.tech/sofa-registry/docs/RoadMap)
- Source Code Analysis
  - [Publish-Subscribe Push](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-publish-subscription-push/)
  - [Registry Meta Leader Election](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-registry-meta/)
  - [SlotTable](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-slottable/)
  - [Data Inverted Index](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-data-inverted-index/)
  - [Data Table Monitoring](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyza-data-table-listening/)
  - [Non-destructive Operations](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-non-destructive-o-and-m/)
  - [Push Latency Tracing](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-push-delay-trace/)
  - [Push Switch Mechanism](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-push-switch/)
  - [Communication Data Compression](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-communication-data-compression/)

## Contribute

[How to Contribute to SOFARegistry](https://www.sofastack.tech/sofa-registry/docs/Contributing)


## Acknowledgements

SOFARegistry originally evolved from Alibaba's internal ConfigServer. We extend our gratitude to Bi Xuan(毕玄) for creating ConfigServer, which laid a solid foundation for SOFARegistry's development. The implementation references portions of code from Netflix's [Eureka](https://github.com/Netflix/eureka), and we are grateful to Netflix for open-sourcing this exceptional framework.


## License

SOFARegistry is licensed under the [Apache License 2.0](https://github.com/sofastack/sofa-registry/blob/master/LICENSE).
