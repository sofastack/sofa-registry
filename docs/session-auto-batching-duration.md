# Session Auto-Batching Duration

## Overview

SOFARegistry's Session server supports an **auto-batching mechanism** to improve push efficiency. When data changes occur frequently, instead of pushing each change individually to clients, the Session server can batch multiple changes together and push them as a single notification. This reduces network overhead and improves overall system throughput.

The **auto-batching duration** (debouncing time) controls how long the Session server waits to accumulate changes before sending a push notification. This document explains the configuration parameters, tuning strategies, and trade-offs involved.

## How Auto-Batching Works

When publisher data changes, the Session server does not immediately push the change to subscribers. Instead, it uses a **debouncing** strategy:

1. The server waits for a configurable duration (`debouncingTime`) to collect multiple data changes.
2. After the wait period, changes are batched and pushed together.
3. A **maximum debouncing time** (`maxDebouncingTime`) caps the wait period to prevent excessive latency.

This mechanism is similar to "debounce" in UI event handling - it trades a small amount of latency for significantly reduced push frequency when changes come in bursts.

### Auto-Adaptive Adjustment

Beyond static configuration, SOFARegistry supports **automatic adjustment** of batching duration based on real-time system load:

- The `AutoPushEfficiencyRegulator` monitors push frequency across sliding time windows.
- When push count exceeds a threshold AND system load is high, it gradually increases the batching duration to reduce pressure.
- When push count drops below the threshold, it gradually decreases the batching duration to improve responsiveness.

## Configuration Parameters

### Basic Push Efficiency Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `enableAutoPushEfficiency` | `false` | Enable the auto push efficiency regulation system |
| `windowNum` | `6` | Number of sliding windows for push frequency statistics |
| `windowTimeMillis` | `10000` (10s) | Duration of each sliding window in milliseconds |
| `pushCountThreshold` | `170000` | Push count threshold across all windows to trigger adjustment |

### Auto-Batching Duration Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `enableDebouncingTime` | `false` | Enable automatic adjustment of batching duration |
| `debouncingTimeMax` | `1000` (1s) | Maximum value for batching duration (milliseconds) |
| `debouncingTimeMin` | `100` (100ms) | Minimum value for batching duration (milliseconds) |
| `debouncingTimeStep` | `100` (100ms) | Step size for increasing/decreasing batching duration |
| `enableMaxDebouncingTime` | `false` | Enable automatic adjustment of maximum batching duration |
| `maxDebouncingTimeMax` | `3000` (3s) | Maximum value for the upper bound of batching duration (milliseconds) |
| `maxDebouncingTimeMin` | `1000` (1s) | Minimum value for the upper bound of batching duration (milliseconds) |
| `maxDebouncingTimeStep` | `200` (200ms) | Step size for adjusting the maximum batching duration |

### Traffic Control Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `enableTrafficOperateLimitSwitch` | `false` | Enable automatic traffic control switch based on system load |
| `loadThreshold` | `6` | System load average threshold for triggering traffic control |

## Tuning Recommendations

### Scenario 1: High Throughput (Large-scale Data Changes)

When your system has many publishers with frequent data changes:

```properties
# Enable auto push efficiency
enableAutoPushEfficiency=true

# Enable auto-batching adjustment
enableDebouncingTime=true
enableMaxDebouncingTime=true

# Increase batching range to handle high throughput
debouncingTimeMin=200
debouncingTimeMax=2000
debouncingTimeStep=200

maxDebouncingTimeMin=2000
maxDebouncingTimeMax=5000
maxDebouncingTimeStep=500

# Enable traffic control for system protection
enableTrafficOperateLimitSwitch=true
loadThreshold=4
```

### Scenario 2: Low Latency (Real-time Sensitive)

When your system requires minimal push latency:

```properties
# Enable auto push efficiency
enableAutoPushEfficiency=true

# Enable auto-batching but with tight bounds
enableDebouncingTime=true
debouncingTimeMin=50
debouncingTimeMax=500
debouncingTimeStep=50

# Disable max debouncing time auto-adjustment for predictability
enableMaxDebouncingTime=false
```

### Scenario 3: Balanced (Default with Mild Tuning)

For general-purpose usage with moderate traffic:

```properties
enableAutoPushEfficiency=true
enableDebouncingTime=true
debouncingTimeMin=100
debouncingTimeMax=1000
debouncingTimeStep=100
enableMaxDebouncingTime=true
maxDebouncingTimeMin=1000
maxDebouncingTimeMax=3000
maxDebouncingTimeStep=200
```

## Trade-offs

| Aspect | Shorter Batching | Longer Batching |
|--------|-----------------|-----------------|
| **Latency** | Lower - clients receive updates faster | Higher - clients wait longer for updates |
| **Throughput** | Lower - more individual push operations | Higher - more changes per push |
| **Network** | More network packets | Fewer, larger packets |
| **Server Load** | Higher CPU and connection pressure | Lower per-change overhead |
| **Client Experience** | More responsive | May see batched updates |

## Monitoring and Verification

The auto-batching regulator logs its adjustments. You can monitor the following in the `AUTO-PUSH-EFFICIENCY-REGULATOR` log:

```
[ID: 1][Increment] debouncingTime: 300 maxDebouncingTime: 1200
[ID: 1][Decrement] debouncingTime: 200 maxDebouncingTime: 1000
```

- **Increment** means the system detected high push frequency and increased batching duration.
- **Decrement** means push frequency dropped and the system reduced batching duration.

## Internal Architecture

The key components involved in auto-batching are:

- **`AutoPushEfficiencyConfig`** - Configuration holder for all batching parameters
- **`AutoPushEfficiencyRegulator`** - Background daemon that monitors push frequency and adjusts batching duration using sliding windows
- **`PushEfficiencyConfigUpdater`** - Applies configuration changes to the running system
- **`ChangeProcessor`** - Uses the configured `debouncingTime` and `maxDebouncingTime` to schedule push tasks

The regulator uses `windowNum` sliding windows, each lasting `windowTimeMillis`. After a warmup period of `windowNum * windowTimeMillis`, it begins evaluating push frequency. If total pushes across all windows exceed `pushCountThreshold`, the regulator incrementally increases batching duration; otherwise, it gradually decreases it back toward the minimum.
