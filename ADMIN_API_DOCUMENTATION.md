# SOFARegistry Admin API Documentation

## 1. Overview

SOFARegistry 提供了丰富的 Admin API 接口，用于管理和监控服务注册中心的运行状态。本文档整理了所有可用的 Admin API 端点，包括请求方法、参数说明、响应格式和使用示例。

## 2. API Classification

SOFARegistry 的 Admin API 按照服务器类型分为以下几类：

- **Meta Server API**: 管理元数据服务器相关功能
- **Data Server API**: 管理数据服务器相关功能
- **Session Server API**: 管理会话服务器相关功能

## 3. Meta Server API

### 3.1 Meta Center API

#### 3.1.1 接口列表

| API Path | Method | Description |
|----------|--------|-------------|
| `/metaCenter/interfaceAppsIndex/renew` | PUT | 刷新接口应用索引 |
| `/metaCenter/interfaceAppsIndex/clean` | PUT | 清理接口应用索引 |
| `/metaCenter/appRevisionCleaner/switch` | PUT | 启用/禁用应用版本清理器 |
| `/metaCenter/interfaceAppsCleaner/switch` | PUT | 启用/禁用接口应用清理器 |
| `/metaCenter/appRevision/writeSwitch` | PUT | 设置应用版本写入开关 |

#### 3.1.2 请求/响应示例

##### 3.1.2.1 刷新接口应用索引

**请求：**
```bash
curl -X PUT http://localhost:9615/metaCenter/interfaceAppsIndex/renew
```

**响应：**
```json
{
  "success": true,
  "message": null
}
```

##### 3.1.2.2 清理接口应用索引

**请求：**
```bash
curl -X PUT http://localhost:9615/metaCenter/interfaceAppsIndex/clean
```

**响应：**
```json
{
  "success": true,
  "message": null
}
```

##### 3.1.2.3 启用/禁用应用版本清理器

**请求：**
```bash
curl -X PUT -d "enabled=true" http://localhost:9615/metaCenter/appRevisionCleaner/switch
```

**响应：**
```json
{
  "success": true,
  "message": null
}
```

##### 3.1.2.4 启用/禁用接口应用清理器

**请求：**
```bash
curl -X PUT -d "enabled=true" http://localhost:9615/metaCenter/interfaceAppsCleaner/switch
```

**响应：**
```json
{
  "success": true,
  "message": null
}
```

##### 3.1.2.5 设置应用版本写入开关

**请求：**
```bash
curl -X PUT -H "Content-Type: application/json" -d '{"enabled": true}' http://localhost:9615/metaCenter/appRevision/writeSwitch
```

**响应：**
```json
{
  "success": true,
  "message": null
}
```

## 4. Data Server API

### 4.1 Datum API

#### 4.1.1 接口列表

| API Path | Method | Description |
|----------|--------|-------------|
| `/datum/api/get` | POST | 获取指定数据信息 |
| `/datum/api/summary/query` | GET | 查询数据摘要信息 |
| `/datum/api/delete` | POST | 删除指定数据信息 |
| `/datum/api/pub/add` | POST | 添加发布者数据 |
| `/datum/api/pub/delete` | POST | 删除发布者数据 |
| `/datum/api/getDatumVersions` | POST | 获取数据版本信息 |
| `/datum/api/getRemoteDatumVersions` | POST | 获取远程数据版本信息 |
| `/datum/api/getDatumSizes` | GET | 获取数据大小信息 |
| `/datum/api/getDatumVersion` | POST | 获取指定数据版本信息 |

#### 4.1.2 请求/响应示例

##### 4.1.2.1 获取指定数据信息

**请求：**
```bash
curl -i -d '{"dataInfoId":"testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP"}' -H "Content-Type: application/json" -X POST http://localhost:9622/datum/api/get
```

**响应：**
```json
{
  "success": true,
  "message": "datum=Datum{dataInfoId='testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP', version=1, pubMap={...}}",
  "data": null
}
```

##### 4.1.2.2 查询数据摘要信息

**请求：**
```bash
curl -X GET http://localhost:9622/datum/api/summary/query?dataCenter=dc1
```

**响应：**
```json
{
  "testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP": 1
}
```

##### 4.1.2.3 删除指定数据信息

**请求：**
```bash
curl -i -d '{"dataInfoId":"testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP"}' -H "Content-Type: application/json" -X POST http://localhost:9622/datum/api/delete
```

**响应：**
```json
{
  "success": true,
  "message": "datum=null, publishers=null",
  "data": null
}
```

##### 4.1.2.4 添加发布者数据

**请求：**
```bash
curl -i -d '{"dataInfoId":"testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP", "publisherDataBox":"1234", "publisherCell":"TestZone2", "publisherConnectId":"127.0.0.1:80"}' -H "Content-Type: application/json" -X POST http://localhost:9622/datum/api/pub/add
```

**响应：**
```json
{
  "success": true,
  "message": "datum=Datum{dataInfoId='testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP', version=2, pubMap={...}}",
  "data": null
}
```

##### 4.1.2.5 删除发布者数据

**请求：**
```bash
curl -i -d '{"dataInfoId":"testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP", "publisherRegisterId":"98de0e41-2a4d-44d7-b1f7-c520660657e8"}' -H "Content-Type: application/json" -X POST http://localhost:9622/datum/api/pub/delete
```

**响应：**
```json
{
  "success": true,
  "message": "datum=Datum{dataInfoId='testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP', version=3, pubMap={...}}",
  "data": null
}
```

##### 4.1.2.6 获取数据版本信息

**请求：**
```bash
curl -d '{"dataCenter":"registry-cloud-test-b"}' -H "Content-Type: application/json" http://localhost:9622/datum/api/getDatumVersions
```

**响应：**
```json
{
  "testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP": 3
}
```

##### 4.1.2.7 获取远程数据版本信息

**请求：**
```bash
curl -d '{"dataCenter":"registry-cloud-test-b"}' -H "Content-Type: application/json" http://localhost:9622/datum/api/getRemoteDatumVersions
```

**响应：**
```
UnsupportedOperationException
```

##### 4.1.2.8 获取数据大小信息

**请求：**
```bash
curl -H "Content-Type: application/json" http://localhost:9622/datum/api/getDatumSizes
```

**响应：**
```json
{
  "dc1": 10
}
```

##### 4.1.2.9 获取指定数据版本信息

**请求：**
```bash
curl -d '{"dataCenter":"registry-cloud-test-b", "dataInfoId":"testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP"}' -H "Content-Type: application/json" http://localhost:9622/datum/api/getDatumVersion
```

**响应：**
```
dataCenter:registry-cloud-test-b, dataInfoId:testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP, dataServer:null, version:3
```

## 5. Session Server API

### 5.1 Session Open API

#### 5.1.1 接口列表

| API Path | Method | Description |
|----------|--------|-------------|
| `/api/servers/query.json` | GET | 获取会话服务器列表（JSON 格式） |
| `/api/servers/query` | GET | 获取会话服务器列表（文本格式） |
| `/api/servers/queryWithWeight` | GET | 获取带权重的会话服务器列表 |
| `/api/servers/connectionNum` | GET | 获取当前会话连接数 |
| `/api/servers/alive` | GET | 检查服务器是否存活 |
| `/api/servers/dataCenter` | GET | 获取当前数据中心的服务器列表 |
| `/api/servers/slot` | GET | 获取指定数据信息的槽位信息 |

#### 5.1.2 请求/响应示例

##### 5.1.2.1 获取会话服务器列表（JSON 格式）

**请求：**
```bash
curl -X GET http://localhost:9603/api/servers/query.json?zone=ZONE1
```

**响应：**
```json
[
  "192.168.1.1:9603",
  "192.168.1.2:9603"
]
```

##### 5.1.2.2 获取会话服务器列表（文本格式）

**请求：**
```bash
curl -X GET http://localhost:9603/api/servers/query?zone=ZONE1
```

**响应：**
```
192.168.1.1:9603;192.168.1.2:9603
```

##### 5.1.2.3 获取带权重的会话服务器列表

**请求：**
```bash
curl -X GET http://localhost:9603/api/servers/queryWithWeight?zone=ZONE1
```

**响应：**
```
192.168.1.1:9603?weight=100;192.168.1.2:9603?weight=200
```

##### 5.1.2.4 获取当前会话连接数

**请求：**
```bash
curl -X GET http://localhost:9603/api/servers/connectionNum
```

**响应：**
```
100
```

##### 5.1.2.5 检查服务器是否存活

**请求：**
```bash
curl -X GET http://localhost:9603/api/servers/alive
```

**响应：**
```
OK
```

##### 5.1.2.6 获取当前数据中心的服务器列表

**请求：**
```bash
curl -X GET http://localhost:9603/api/servers/dataCenter
```

**响应：**
```json
[
  "192.168.1.1:9603",
  "192.168.1.2:9603"
]
```

##### 5.1.2.7 获取指定数据信息的槽位信息

**请求：**
```bash
curl -X GET http://localhost:9603/api/servers/slot?dataInfoId=testDataId#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP
```

**响应：**
```json
{
  "slotId": 1,
  "leader": "192.168.1.1:9622",
  "followers": ["192.168.1.2:9622"],
  "epoch": 1
}
```

## 6. Common Parameters

| Parameter | Description | Required | Default Value |
|-----------|-------------|----------|---------------|
| `dataCenter` | 数据中心名称 | 否 | 本地数据中心 |
| `zone` | 区域名称 | 否 | 本地区域 |
| `dataInfoId` | 数据信息ID，格式为 `service#@#instance#@#group` | 是 | 无 |
| `enabled` | 启用/禁用标志 | 是（部分接口） | 无 |

## 7. Error Codes

| Error Code | Description |
|------------|-------------|
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用 |

## 8. Usage Scenarios

### 8.1 Meta Server API Use Cases

- **接口应用索引管理**：用于维护和清理接口与应用之间的映射关系，确保数据一致性。
- **应用版本管理**：控制应用版本的写入和清理，优化存储空间。

### 8.2 Data Server API Use Cases

- **数据查询与管理**：查询和管理注册中心中的数据信息，用于监控和调试。
- **发布者管理**：手动添加或删除发布者数据，用于测试和应急处理。
- **数据版本监控**：监控数据版本变化，确保数据一致性。

### 8.3 Session Server API Use Cases

- **服务器发现**：客户端通过这些接口获取可用的会话服务器列表。
- **连接监控**：监控会话服务器的连接数，用于容量规划。
- **健康检查**：检查服务器是否正常运行，用于运维监控。
- **槽位查询**：查询数据信息对应的槽位信息，用于调试和监控。

## 9. API Endpoints Summary

### 9.1 Meta Server Endpoints

| Module | API Path | Method | Description |
|--------|----------|--------|-------------|
| Meta Center | `/metaCenter/interfaceAppsIndex/renew` | PUT | 刷新接口应用索引 |
| Meta Center | `/metaCenter/interfaceAppsIndex/clean` | PUT | 清理接口应用索引 |
| Meta Center | `/metaCenter/appRevisionCleaner/switch` | PUT | 启用/禁用应用版本清理器 |
| Meta Center | `/metaCenter/interfaceAppsCleaner/switch` | PUT | 启用/禁用接口应用清理器 |
| Meta Center | `/metaCenter/appRevision/writeSwitch` | PUT | 设置应用版本写入开关 |

### 9.2 Data Server Endpoints

| Module | API Path | Method | Description |
|--------|----------|--------|-------------|
| Datum API | `/datum/api/get` | POST | 获取指定数据信息 |
| Datum API | `/datum/api/summary/query` | GET | 查询数据摘要信息 |
| Datum API | `/datum/api/delete` | POST | 删除指定数据信息 |
| Datum API | `/datum/api/pub/add` | POST | 添加发布者数据 |
| Datum API | `/datum/api/pub/delete` | POST | 删除发布者数据 |
| Datum API | `/datum/api/getDatumVersions` | POST | 获取数据版本信息 |
| Datum API | `/datum/api/getRemoteDatumVersions` | POST | 获取远程数据版本信息 |
| Datum API | `/datum/api/getDatumSizes` | GET | 获取数据大小信息 |
| Datum API | `/datum/api/getDatumVersion` | POST | 获取指定数据版本信息 |

### 9.3 Session Server Endpoints

| Module | API Path | Method | Description |
|--------|----------|--------|-------------|
| Session Open | `/api/servers/query.json` | GET | 获取会话服务器列表（JSON 格式） |
| Session Open | `/api/servers/query` | GET | 获取会话服务器列表（文本格式） |
| Session Open | `/api/servers/queryWithWeight` | GET | 获取带权重的会话服务器列表 |
| Session Open | `/api/servers/connectionNum` | GET | 获取当前会话连接数 |
| Session Open | `/api/servers/alive` | GET | 检查服务器是否存活 |
| Session Open | `/api/servers/dataCenter` | GET | 获取当前数据中心的服务器列表 |
| Session Open | `/api/servers/slot` | GET | 获取指定数据信息的槽位信息 |

## 10. Conclusion

SOFARegistry 提供了丰富的 Admin API 接口，用于管理和监控服务注册中心的运行状态。这些接口覆盖了元数据管理、数据管理、会话管理等多个方面，为运维人员和开发者提供了便捷的管理工具。

通过本文档，您可以了解 SOFARegistry 的 Admin API 功能和使用方法，从而更好地管理和监控服务注册中心。