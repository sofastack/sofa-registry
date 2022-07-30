# SOFARegistry

[![unit test](https://github.com/sofastack/sofa-registry/actions/workflows/unit-test.yml/badge.svg)](https://github.com/sofastack/sofa-registry/actions/workflows/unit-test.yml)
[![integration test](https://github.com/sofastack/sofa-registry/actions/workflows/integration-test.yml/badge.svg)](https://github.com/sofastack/sofa-registry/actions/workflows/integration-test.yml)
![license](https://img.shields.io/badge/license-Apache--2.0-green.svg)
[![codecov](https://codecov.io/gh/sofastack/sofa-registry/branch/master/graph/badge.svg?token=K6x7h4Uxkn)](https://codecov.io/gh/sofastack/sofa-registry)
![maven](https://img.shields.io/github/release/sofastack/sofa-registry.svg)

SOFARegistry 是蚂蚁金服开源的一个生产级、高时效、高可用的服务注册中心。SOFARegistry 最早源自于淘宝的 ConfigServer，十年来，随着蚂蚁金服的业务发展，注册中心架构已经演进至第六代。目前 SOFARegistry 不仅全面服务于蚂蚁金服的自有业务，还随着蚂蚁金融科技服务众多合作伙伴，同时也兼容开源生态。SOFARegistry 采用 AP 架构，支持秒级时效性推送，同时采用分层架构支持无限水平扩展。

## 功能特性 

- 支持服务发布与服务订阅
- 支持服务变更时的主动推送
- 丰富的 REST 接口
- 采用分层架构及数据分片，支持海量连接及海量数据
- 支持多副本备份，保证数据高可用
- 基于 [SOFABolt](https://github.com/alipay/sofa-bolt) 通信框架，服务上下线秒级通知
- AP 架构，保证网络分区下的可用性


## 需要

编译需要 JDK 8 及以上、Maven 3.2.5 及以上。

运行需要 JDK 6 及以上，服务端运行需要 JDK 8及以上。

**推荐使用JDK 8，JDK 16尚未被测试，可能会有兼容性问题**

## 文档

- [快速开始](https://www.sofastack.tech/sofa-registry/docs/Server-QuickStart)
- [开发手册](https://www.sofastack.tech/sofa-registry/docs/JAVA-SDK)
- [运维手册](https://www.sofastack.tech/sofa-registry/docs/Deployment)
- [发布历史](https://www.sofastack.tech/sofa-registry/docs/ReleaseNotes)
- [发展路线](https://www.sofastack.tech/sofa-registry/docs/RoadMap)
- 源码解析
   - [发布订阅推送](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-publish-subscription-push/)
   - [registry meta 选主](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-registry-meta/)
   - [SlotTable](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-slottable/)
   - [数据倒排索引](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-data-inverted-index/)
   - [数据表监听](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyza-data-table-listening/)
   - [无损运维](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-non-destructive-o-and-m/)
   - [推送延迟 trace](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-push-delay-trace/)
   - [推送开关](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-push-switch/)
   - [通讯数据压缩](https://www.sofastack.tech/projects/sofa-registry/code-analyze/code-analyze-communication-data-compression/)
## 贡献

[如何参与 SOFARegistry 代码贡献](https://www.sofastack.tech/sofa-registry/docs/Contributing)


## 致谢

SOFARegistry 最早源于阿里内部的 ConfigServer，感谢毕玄创造了 ConfigServer，使 SOFARegistry 的发展有了良好的基础。同时，部分代码参考了 Netflix 的 [Eureka](https://github.com/Netflix/eureka)，感谢 Netflix 开源了如此优秀框架。

## 开源许可

SOFARegistry 基于 [Apache License 2.0](https://github.com/sofastack/sofa-registry/blob/master/LICENSE) 协议
