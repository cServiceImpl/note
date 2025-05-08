### 问题1：

一行未指定线程池的 CompletableFuture 代码，在高并发下触发默认线程池资源耗尽，导致任务队列无限堆积，最终内存溢出（OOM）

问题复现：

```java
public class OrderSystemCrash {

    // 模拟高并发场景
    public static void main(String[] args) {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            processPayment();
        }
        // 阻塞主线程观察结果
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
        }
    }

    // 模拟订单服务接口：支付完成后发送通知
    public static void processPayment() {
        // 致命点：使用默认线程池 ForkJoinPool.commonPool()
        CompletableFuture.runAsync(() -> {
            // 1. 查询订单（模拟耗时操作）
            queryOrder();
            // 2. 支付（模拟阻塞IO）
            pay();
            // 3. 发送通知（模拟网络请求）
            sendNotification();
        });
    }

    // 模拟数据库查询（耗时100ms）
    private static void queryOrder() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
    }

    // 模拟支付接口（耗时500ms）
    private static void pay() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
    }

    // 模拟通知服务（耗时200ms）
    private static void sendNotification() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
        }
    }
```

当我们运用 CompletableFuture 执行异步任务时，比如调用 `CompletableFuture.runAsync(Runnable runnable)` 或者 `CompletableFuture.supplyAsync(Supplier<U> supplier)` 这类未明确指定线程池的方法，CompletableFuture 会自动采用默认线程池来处理这些异步任务。

而这个默认线程池，正是 `ForkJoinPool.commonPool()`

**ForkJoinPool.commonPool() 的致命陷阱**

**1、全局共享：资源竞争的 “修罗场”**

`ForkJoinPool.commonPool()` 是 JVM 全局共享的线程池，所有未指定线程池的 CompletableFuture 任务和并行流（parallelStream()）都会共享它。

这就像早高峰的地铁，所有人都挤在同一节车厢，资源争夺不可避免。

**2、无界队列：内存溢出的 “导火索”**

`ForkJoinPool.commonPool()` 使用无界队列，理论上能存储大量任务，但实际受内存限制。

大量任务到来时，队列会不断消耗内存，一旦超过系统承受能力，会触发OutOfMemoryError，服务直接宕机。

**修复方案：**

- **线程池隔离**：创建独立线程池，避免占用公共线程池资源，确保其他业务不受影响。
- **可控队列**：设有限容量的有界队列，配好拒绝策略，队列满时触发，防止任务堆积导致内存溢出。
- **异常处理**：为异步任务配置异常处理器，捕获记录日志，快速定位问题，提升系统可观测性和稳定性。

```java
作者：程序员徐述
链接：https://www.zhihu.com/question/599662485/answer/114557416686
来源：知乎
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。

public class OrderSystemFix {
    // 1. 自定义线程池（核心参数：核心线程数=50，队列容量=1000，拒绝策略=降级）
    private static final ExecutorService orderPool = new ThreadPoolExecutor(
            50, 50, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1000), // 有界队列
            new ThreadPoolExecutor.AbortPolicy() { // 自定义拒绝策略
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    // 记录日志 + 降级处理
                    System.err.println("任务被拒绝，触发降级");
                    // 异步重试或写入死信队列
                }
            }
    );

    // 2. 修复后的订单服务
    public static void processPayment() {
        CompletableFuture.runAsync(() -> {
            try {
                queryOrder();
                pay();
                sendNotification();
            } catch (Exception e) {
                // 3. 异常捕获 + 降级
                System.err.println("支付流程异常：" + e.getMessage());
            }
        }, orderPool); // 关键：显式指定线程池
    }

    // 其他代码同上...
}
```

### 问题2：

批量异步执行

**业务逻辑：** 我们的账号管理中心，需要对账号进行批量修改。修改完成之后需要将账号同步到各子系统。

**实现方式：** 因为同步逻辑有现成的方法，只是单个同步，现在因为要批量操作，因此调用同步逻辑就是开启新的线程，然后循环调用原来的单个同步方法。

```java
//问题代码
@Transactional(rollbackFor = Exception.class)
@Override
public Respoonse<String> updateAccountRoles(BatchUpdateParam param) {
    //校验参数，
    if(!paramCheck(param)){
        return Response.fail("网络繁忙！");
    }
    // 更新账号信息。
    List updateUserList = userService.updateRoles(param);
    if (updateUserList.size() > 0) {
        // 异步通知其他系统更新对应账号信息。
        //❌事务还没提交，以及已经开始异步执行了，异步方法里面可能查询还是更新前的数据。
        userSyncUtil.asyncUserList(updateUserList);
    }
    return Respoonse.ok("批量更新成功。");
}

//事务还没提交，以及已经开始异步执行了，异步方法里面可能查询还是更新前的数据
```

解决方法1

```java
//将异步代码 注册到事务提交后的回调中
@Transactional(rollbackFor = Exception.class)
@Override
public Respoonse<String> updateAccountRoles(BatchUpdateParam param) {
    //校验参数，
    if(!paramCheck(param)){
        return Response.fail("网络繁忙！");
    }
    // 更新账号信息。
    List updateUserList = userService.updateRoles(param);
    if (updateUserList.size() > 0) {
        // 异步通知其他系统更新对应账号信息。
        // 👍事务提交后执行
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                userSyncUtil.asyncUserList(updateUserList);
            }
        });

        
    }
    return Respoonse.ok("批量更新成功。");
}

```

解决方法2

```java
// 控制事务范围
//❌删除外层事务
//@Transactional(rollbackFor = Exception.class)
@Override
public Respoonse<String> updateAccountRoles(BatchUpdateParam param) {
    //校验参数，
    if(!paramCheck(param)){
        return Response.fail("网络繁忙！");
    }
    // 更新账号信息。👍 updateRoles 加事务注解就行了（或者用编程式事务）
    List updateUserList = userService.updateRoles(param);
    if (updateUserList.size() > 0) {
        // 异步通知其他系统更新对应账号信息。
        userSyncUtil.asyncUserList(updateUserList);
    }
    return Respoonse.ok("批量更新成功。");
}

```

### 问题3：

多级缓存：**Redis 设计多级缓存**

多级缓存的设计主要是为了提高系统的访问速度、降低数据库压力，并尽可能减少缓存击穿、缓存雪崩等问题。一般可以设计 **三层缓存架构**：

**1. 多级缓存架构**

**(1) L1 缓存（本地缓存）**

- **特点**：存储在应用服务器内存中，访问速度最快。
- **方案**：使用 **Guava Cache、Caffeine、Ehcache** 等 JVM 内存缓存。
- **适用场景**：热点数据、高并发读取场景。

**(2) L2 缓存（分布式缓存）**

- **特点**：存储在 Redis/Memcached 中，访问速度快，但比本地缓存慢。
- **方案**：**Redis** 作为主要分布式缓存方案。
- **适用场景**：大规模数据缓存，适用于跨实例的访问。

**(3) L3 缓存（持久化存储）**

- **特点**：数据库（MySQL、MongoDB 等），访问速度最慢，但数据持久化。
- **适用场景**：最终数据存储层，在缓存失效时提供数据。

**2. Redis 多级缓存架构的设计**

可以基于 **冷热数据分层** 来设计 Redis 多级缓存：

1. **热点缓存**（高频访问数据，TTL 短）：
   - 放入 Redis **Cluster** 的 **内存缓存（RAM）**，比如 **Redis 主从模式或集群模式**。
   - 适用于秒杀、热门文章等高频访问数据。
2. **冷数据缓存**（访问较少，TTL 长）：
   - 使用 **Redis + RocksDB/Aerospike** 存储冷数据，或基于 **SSD + Redis** 作为二级缓存。
   - 适用于不常访问但需要快速查找的数据。
3. **降级策略**：
   - L1/L2 缓存失效后，优先从 L3 缓存加载，减少数据库压力。

```java
@Slf4j
@Service
public class CacheService {

    private static final Duration REDIS_TTL = Duration.ofMinutes(30);
    private static final Duration NULL_TTL = Duration.ofMinutes(5);
    private static final String NULL_PLACEHOLDER = "__NULL__";
    private static final Duration LOCK_WAIT = Duration.ofSeconds(2);
    private static final Duration LOCK_LEASE = Duration.ofSeconds(5);

    private final Cache<String, String> localCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private RedissonClient redissonClient;

    public String getData(String key) {
        // 1. 本地缓存
        String value = localCache.getIfPresent(key);
        if (value != null) {
            return resolvePlaceholder(value);
        }

        try {
            // 2. Redis 缓存
            value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                localCache.put(key, value);
                return resolvePlaceholder(value);
            }

            // 3. 加锁防止缓存击穿
            RLock lock = redissonClient.getLock("cache:lock:" + key);
            if (lock.tryLock(LOCK_WAIT.getSeconds(), LOCK_LEASE.getSeconds(), TimeUnit.SECONDS)) {
                try {
                    // double check redis
                    value = redisTemplate.opsForValue().get(key);
                    if (value != null) {
                        localCache.put(key, value);
                        return resolvePlaceholder(value);
                    }

                    // 查询数据库
                    value = databaseService.queryFromDB(key);
                    if (value != null) {
                        redisTemplate.opsForValue().set(key, value, REDIS_TTL);
                        localCache.put(key, value);
                    } else {
                        redisTemplate.opsForValue().set(key, NULL_PLACEHOLDER, NULL_TTL);
                        localCache.put(key, NULL_PLACEHOLDER);
                    }
                    return value;
                } finally {
                    lock.unlock();
                }
            } else {
                // 未获取到锁，短暂等待后再尝试读取缓存
                Thread.sleep(100); // 或退避重试策略
                return redisTemplate.opsForValue().get(key);
            }

        } catch (Exception e) {
            log.error("getData error", e);
            return null;
        }
    }

    public void evict(String key) {
        redisTemplate.delete(key);
        localCache.invalidate(key);
    }

    private String resolvePlaceholder(String value) {
        return NULL_PLACEHOLDER.equals(value) ? null : value;
    }
}

```
