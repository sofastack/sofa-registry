# Session Auto-Batching Duration Documentation

## 1. Overview

SOFARegistry's Session server supports an auto-batching mechanism to improve push efficiency. This document explains how the auto-batching works, how to configure it, and how to tune it based on your workload.

## 2. Auto-Batching Mechanism

### 2.1 How It Works

The auto-batching mechanism in the Session server is designed to:

1. **Reduce push frequency**: By batching multiple push requests together, it reduces the number of network round-trips and improves throughput.

2. **Optimize resource usage**: It helps reduce CPU and network overhead by processing multiple push tasks in a single batch.

3. **Balance latency and throughput**: The mechanism allows you to tune the batching duration to balance between push latency and system throughput.

### 2.2 Key Components

- **PushEfficiencyImproveConfig**: The main configuration class for push efficiency improvements, including batching settings.
- **AutoPushEfficiencyConfig**: The configuration class specifically for auto-tuning batching duration.
- **AutoPushEfficiencyRegulator**: The component that automatically adjusts batching duration based on system load and push frequency.

### 2.3 Relationship Between Batching Duration and Push Latency

- **Shorter batching duration**: Results in lower push latency but higher system overhead due to more frequent push operations.
- **Longer batching duration**: Results in higher push latency but lower system overhead due to fewer push operations.

## 3. Configuration Parameters

### 3.1 Basic Batching Parameters

| Parameter | Description | Default Value | Valid Range |
|-----------|-------------|---------------|-------------|
| `changeDebouncingMillis` | Delay time for processing change tasks, to avoid frequent pushes caused by continuous data changes | 1000ms | > 0 |
| `changeDebouncingMaxMillis` | Maximum delay time for change tasks, to prevent starvation | 3000ms | > 0 |
| `pushTaskDebouncingMillis` | Delay time for processing push tasks, to merge similar push tasks | 500ms | > 0 |
| `changeTaskWaitingMillis` | Interval time for asynchronous processing of change tasks | 100ms | > 0 |
| `largeChangeTaskWaitingMillis` | Interval time for asynchronous processing of large change tasks | 1000ms | > 0 |
| `pushTaskWaitingMillis` | Interval time for asynchronous processing of push tasks | 200ms | > 0 |
| `regWorkWaitingMillis` | Loop wait time for Sub request BufferWorker | 200ms | > 0 |

### 3.2 Auto-Tuning Parameters

| Parameter | Description | Default Value | Valid Range |
|-----------|-------------|---------------|-------------|
| `enableAutoPushEfficiency` | Whether to enable auto-tuning of push efficiency | false | true/false |
| `enableDebouncingTime` | Whether to enable auto-tuning of batching duration | false | true/false |
| `debouncingTimeMax` | Maximum batching duration | 1000ms | > 0 |
| `debouncingTimeMin` | Minimum batching duration | 100ms | > 0 |
| `debouncingTimeStep` | Step size for adjusting batching duration | 100ms | > 0 |
| `enableMaxDebouncingTime` | Whether to enable auto-tuning of maximum batching duration | false | true/false |
| `maxDebouncingTimeMax` | Maximum value for maximum batching duration | 3000ms | > 0 |
| `maxDebouncingTimeMin` | Minimum value for maximum batching duration | 1000ms | > 0 |
| `maxDebouncingTimeStep` | Step size for adjusting maximum batching duration | 200ms | > 0 |
| `windowNum` | Number of time windows for calculating push frequency | 6 | > 0 |
| `windowTimeMillis` | Time window size for calculating push frequency | 10000ms | > 0 |
| `pushCountThreshold` | Threshold for push count to trigger auto-tuning | 170000 | > 0 |

### 3.3 Traffic Control Parameters

| Parameter | Description | Default Value | Valid Range |
|-----------|-------------|---------------|-------------|
| `enableTrafficOperateLimitSwitch` | Whether to enable traffic control | false | true/false |
| `loadThreshold` | System load threshold for triggering traffic control | 6.0 | > 0 |

## 4. How to Configure

### 4.1 Configuration File

You can configure the batching parameters in the `application.properties` file of the Session server:

```properties
# Basic batching parameters
session.server.data.change.debouncing.millis=1000
session.server.data.change.max.debouncing.millis=3000
session.server.push.data.task.debouncing.millis=500

# Auto-tuning parameters
session.server.push.efficiency.auto.enable=false
session.server.push.efficiency.debouncing.time.enable=false
session.server.push.efficiency.debouncing.time.max=1000
session.server.push.efficiency.debouncing.time.min=100
session.server.push.efficiency.debouncing.time.step=100
session.server.push.efficiency.max.debouncing.time.enable=false
session.server.push.efficiency.max.debouncing.time.max=3000
session.server.push.efficiency.max.debouncing.time.min=1000
session.server.push.efficiency.max.debouncing.time.step=200
```

### 4.2 Dynamic Configuration

SOFARegistry also supports dynamic configuration through the meta server. You can update the push efficiency configuration without restarting the Session server.

## 5. Tuning Recommendations

### 5.1 High Throughput Scenario

For scenarios with high push frequency and large data volume:

1. **Increase batching duration**: Set a longer `pushTaskDebouncingMillis` (e.g., 1000ms) to reduce the number of push operations.
2. **Enable auto-tuning**: Set `enableAutoPushEfficiency` and `enableDebouncingTime` to `true` to allow the system to automatically adjust batching duration based on load.
3. **Adjust thresholds**: Increase `pushCountThreshold` if you have a high-volume system.

### 5.2 Low Latency Scenario

For scenarios that require low push latency:

1. **Decrease batching duration**: Set a shorter `pushTaskDebouncingMillis` (e.g., 100ms) to minimize push delay.
2. **Keep auto-tuning disabled**: Manual configuration gives you more control over latency.
3. **Optimize other parameters**: Ensure `changeTaskWaitingMillis` and `pushTaskWaitingMillis` are set to reasonable values to avoid processing delays.

### 5.3 Best Practices

1. **Start with defaults**: Begin with the default values and monitor system performance.
2. **Gradual adjustment**: Make small changes to parameters and observe the impact.
3. **Monitor metrics**: Track push latency, throughput, and system load to find the optimal configuration.
4. **Consider workload patterns**: Adjust parameters based on your specific workload characteristics.
5. **Test in staging**: Always test configuration changes in a staging environment before applying them to production.

## 6. Typical Configurations

### 6.1 Default Configuration

```properties
# Basic batching
changeDebouncingMillis=1000
changeDebouncingMaxMillis=3000
pushTaskDebouncingMillis=500

# Auto-tuning (disabled by default)
enableAutoPushEfficiency=false
enableDebouncingTime=false
enableMaxDebouncingTime=false
```

### 6.2 High Throughput Configuration

```properties
# Basic batching
changeDebouncingMillis=1500
changeDebouncingMaxMillis=4000
pushTaskDebouncingMillis=1000

# Auto-tuning (enabled)
enableAutoPushEfficiency=true
enableDebouncingTime=true
debouncingTimeMax=1500
debouncingTimeMin=200
debouncingTimeStep=100
enableMaxDebouncingTime=true
maxDebouncingTimeMax=5000
maxDebouncingTimeMin=1500
maxDebouncingTimeStep=200
```

### 6.3 Low Latency Configuration

```properties
# Basic batching
changeDebouncingMillis=300
changeDebouncingMaxMillis=1000
pushTaskDebouncingMillis=100

# Auto-tuning (disabled)
enableAutoPushEfficiency=false
enableDebouncingTime=false
enableMaxDebouncingTime=false
```

## 7. Monitoring and Troubleshooting

### 7.1 Key Metrics to Monitor

- **Push latency**: Average time from data change to push completion
- **Push throughput**: Number of push operations per second
- **System load**: CPU and memory usage
- **Push task queue length**: Number of pending push tasks

### 7.2 Troubleshooting Tips

1. **High push latency**: Check if batching duration is set too high. Consider reducing `pushTaskDebouncingMillis`.

2. **Low throughput**: Check if batching duration is set too low. Consider increasing `pushTaskDebouncingMillis` or enabling auto-tuning.

3. **System overload**: If system load is high, consider enabling traffic control with `enableTrafficOperateLimitSwitch` and adjusting `loadThreshold`.

4. **Push task starvation**: If push tasks are not being processed in a timely manner, check if `changeDebouncingMaxMillis` is set appropriately.

## 8. Conclusion

The Session auto-batching mechanism is a powerful feature that can significantly improve push efficiency in SOFARegistry. By understanding how it works and properly configuring it based on your specific workload, you can achieve the right balance between push latency and system throughput.

Remember to monitor system performance and adjust configuration parameters as needed to optimize the auto-batching mechanism for your use case.