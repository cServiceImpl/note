
### Java

```
            if("A09".equals(drivingBehaviorResponse.getVehicleSeries())){
                for(DrivingCalculateRuleBO bo : returnFinalSafeList){
                    if ("分神".equals(bo.getFieldName())||"打电话".equals(bo.getFieldName())||"抽烟".equals(bo.getFieldName())){
                        returnFinalSafeList.remove(bo);
                    }
                }
            }
```

1、上述代码错误，java.util.ConcurrentModificationException，当一个线程在遍历或修改一个集合的同时，另一个线程尝试修改该集合时，就可能会抛出此异常。这通常发生在迭代集合的过程中尝试直接修改它（例如，添加或删除元素）。遍历过程同时修改用迭代器！改完：

```java
if ("A09".equals(drivingBehaviorResponse.getVehicleSeries())) {
    Iterator<DrivingCalculateRuleBO> iterator = returnFinalSafeList.iterator();
    while (iterator.hasNext()) {
        DrivingCalculateRuleBO bo = iterator.next();
        if ("分神".equals(bo.getFieldName()) || "打电话".equals(bo.getFieldName()) || "抽烟".equals(bo.getFieldName())) {
            iterator.remove();
        }
    }
}

（2）
Iterator<DrivingCalculateRule> iterator = list.iterator();
            while (iterator.hasNext()) {
                DrivingCalculateRule rule = iterator.next();
                if (!keys.contains(rule.getDataField())) {
                    iterator.remove();
                }
            }
```

或者可以用Java 8 的 Stream API

```
list.removeIf(rule -> !keys.contains(rule.getDataField()));
```

2、获取前i天日期

```java
// 将日期和时间设置到日历对象中
Calendar c = Calendar.getInstance();
Date end = new Date(condition.getEndTime());
c.setTime(end);
c.add(Calendar.DATE, -i);
```

5、流操作

除了forEach操作会改变原集合的数据，其他的操作均不会改变原集合

```java
//filter：过滤，就是过滤器，符合条件的通过，不符合条件的过滤掉
// 筛选出成绩不为空的学生人数
count = list.stream().filter(p -> null != p.getScore()).count();
```

```java
//map：映射，他将原集合映射成为新的集合
// 取出所有学生的成绩
List<Double> scoreList = list.stream().map(p -> p.getScore()).collect(Collectors.toList());
// 将学生姓名集合串成字符串，用逗号分隔
String nameString = list.stream().map(p -> p.getName()).collect(Collectors.joining(","));
```

```java
//sorted：排序，可以根据指定的字段进行排序
// 按学生成绩逆序排序 正序则不需要加.reversed()
filterList = list.stream().filter(p -> null != p.getScore()).sorted(Comparator.comparing(UserPo::getScore).reversed()).collect(Collectors.toList());
```

```java
// 学生成绩太差了，及格率太低，给每个学生加10分，放个水
filterList.stream().forEach(p -> p.setScore(p.getScore() + 10));
```

```java
// 按成绩进行归集
Map<Double, List<UserPo>> groupByScoreMap = list.stream().filter(p -> null != p.getScore()).collect(Collectors.groupingBy(UserPo::getScore));
// 返回
listList<Double> scoreList = list.stream().map(p -> p.getScore()).collect(Collectors.toList());
// 返回string用逗号分隔
String nameString = list.stream().map(p -> p.getName()).collect(Collectors.joining(","));
```

```java
//statistics：统计，可以统计中位数，平均值，最大最小值
DoubleSummaryStatistics statistics = filterList.stream().mapToDouble(p -> p.getScore()).summaryStatistics();
System.out.println("列表中最大的数 : " + statistics.getMax());
System.out.println("列表中最小的数 : " + statistics.getMin());
System.out.println("所有数之和 : " + statistics.getSum());
System.out.println("平均数 : " + statistics.getAverage());
```

```java
List<Integer> res = Arrays.stream(parameter.getApkStatus().split(",")).filter(StringUtils::isNotBlank).map(Integer::parseInt).collect(Collectors.toList());
```

```java
// 使用stream过滤出符合条件的Map
List<Map<String, Object>> filteredMaps = maps.stream()
        .filter(map -> (Double) map.get("value") > 0)
        .collect(Collectors.toList());
```

6、数的处理

```
Math.ceil 向上取整
Math.floor 向下取整
String.format("%.2f", result) 保留两位小数
```

7、浮点数之间的等值判断，不能使用==或equals使用方法

```java
正例：
(1) 指定一个误差范围，两个浮点数的差值在此范围之内，则认为是相等的。
float a = 1.0F - 0.9F;
float b = 0.9F - 0.8F;
float diff = 1e-6F;
if (Math.abs(a - b) < diff) {
 System.out.println("true");
}
(2) 使用 BigDecimal 来定义值，再进行浮点数的运算操作。
BigDecimal a = new BigDecimal("1.0");
BigDecimal b = new BigDecimal("0.9");
BigDecimal c = new BigDecimal("0.8");
BigDecimal x = a.subtract(b);
BigDecimal y = b.subtract(c);
if (x.compareTo(y) == 0) {
 System.out.println("true");
}
```

8、@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")*// 注解统一时间格式*

9、get请求传时间，配置时间格式 

@RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime

@JsonFormat和@DataTimeFormat的区别

注解@JsonFormat主要是后台到前台的时间格式的转换

注解@DataTimeFormat主要是前后到后台的时间格式的转换

10、idea的application启动参数  vm -options   控制台打印日志

```vm options
-Dasync.log.output=console
```

11、用BigDecimal防止丢精度

```java
/*
百分比保留0位小数 四舍五入
 */
String getDataPercentTwo(String value){
    BigDecimal bd = new BigDecimal(value);
    return bd.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).toString();
}
```

12、@EqualsAndHashCode(callSuper = true)

```java
public class Animal {  
    private String species;  
  
    public Animal(String species) {  
        this.species = species;  
    }  
  
    // Getter 和 Setter 方法（这里省略了）  
  
    @Override  
    public boolean equals(Object o) {  
        if (this == o) return true;  
        if (o == null || getClass() != o.getClass()) return false;  
        Animal animal = (Animal) o;  
        return Objects.equals(species, animal.species);  
    }  
  
    @Override  
    public int hashCode() {  
        return Objects.hash(species);  
    }  
}
```

```java
import lombok.EqualsAndHashCode;  
  
@EqualsAndHashCode(callSuper = true) // 这里我们使用了 @EqualsAndHashCode 注解，并设置了 callSuper 为 true  
public class Dog extends Animal {  
    private String name;  
  
    public Dog(String species, String name) {  
        super(species);  
        this.name = name;  
    }  
  
    // Getter 和 Setter 方法（这里省略了）  
  
    // 由于我们使用了 Lombok 的 @EqualsAndHashCode 注解，并且设置了 callSuper 为 true，  
    // Lombok 会自动生成一个包含对父类 Animal 的 equals 和 hashCode 方法调用的实现。  
    // 因此，我们不需要在这里重写 equals 和 hashCode 方法。  
}
```

```java
public class Main {  
    public static void main(String[] args) {  
        Dog dog1 = new Dog("Dog", "Buddy");  
        Dog dog2 = new Dog("Dog", "Buddy");  
        Dog dog3 = new Dog("Cat", "Buddy");  
  
        System.out.println(dog1.equals(dog2)); // 输出 true，因为 species 和 name 都相同  
        System.out.println(dog1.equals(dog3)); // 输出 false，因为 species 不同  
    }  
}
//@EqualsAndHashCode(callSuper = true)时 输出true true
```

12、返回两个localDataTime日期之间相差天数

```java
private LocalDateTime startTime;

private LocalDateTime endTime;

ChronoUnit.DAYS.between(param.getStartTime().toLocalDate(), param.getEndTime().toLocalDate()  //返回两个localDataTime日期之间相差天数
```

14、本地缓存（Caffeine等）减少Redis的直接请求压力，减少不必要的缓存穿透

```java
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
    <version>3.1.0</version>
</dependency>

public class ProductService {
    private static final Cache<String, Product> productCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)  // 设置缓存过期时间
            .maximumSize(1000)  // 设置缓存最大容量
            .build();

    @Autowired
    private RedisTemplate<String, Product> redisTemplate;

    public Product getProductById(String productId) {
        // 首先尝试从本地缓存中获取数据
        Product product = productCache.getIfPresent(productId);
        if (product == null) {
            // 如果本地缓存没有，再从Redis中读取
            product = redisTemplate.opsForValue().get("product:" + productId);
            if (product != null) {
                // 将Redis中的数据缓存到本地缓存中
                productCache.put(productId, product);
            }
        }
        return product;
    }
}
```



16、synchronized vs AQS 区别对比

CAS（Compare and Swap）机制是一种原子操作，用于在并发编程中保证线程安全。它的基本原理是：先比较某个变量的当前值是否等于期望值，如果相等则更新该变量为新值。这个操作在比较和更新时是不可中断的，因此可以避免多线程环境下的数据竞争问题。

CAS机制：Atomic修饰的类；比较内存位置当前值与预期值，如果相等，就将新值写入。如果不相等，表示有其他线程修改了值，CAS操作会失败，并重新进行比较和交换。

可能存在的问题：

1、ABA问题：一个变量的值从A变为B再变回A，CAS操作可能会认为它是原本的A值，从而误认为是一个没有发生变化的状态，而实际的值可能已经发生了变化

2、高竞争下自旋问题：在一个高并发的情况下，如果多个线程都频繁尝试修改同一个共享变量，每次CAS操作都失败，线程将进行自旋，直到CAS成功。这种高频的自旋会导致CPU资源浪费，降低程序的整体性能

解决方式：

1、引入版本号或者时间戳；AtomicStampedReference类可以帮助解决ABA问题，它在值旁边维护了一个“版本号”

2、避免高竞争的共享资源；使用适当的退让策略（比如限时自旋）

| 特性       | synchronized（悲观锁）                   | AQS（AbstractQueuedSynchronizer）抽象队列同步器              |
| ---------- | ---------------------------------------- | ------------------------------------------------------------ |
| 实现       | JVM 内置，依赖于对象头和 Monitor 机制    | 基于 CAS （compare and swap乐观锁）+ CLH （多线程争用资源被阻塞时会进入此队列）队列 |
| 锁类型     | ① 偏向锁、② 轻量级锁、③ 重量级锁         | 支持独占（ReentrantLock）、共享（Semaphore）                 |
| 性能优化   | 锁升级（偏向锁 → 轻量级锁 → 重量级锁）   | 自旋 + 阻塞，支持多线程并发和锁降级                          |
| 阻塞机制   | 依赖操作系统的 `mutex`，阻塞线程切换慢   | 使用 `LockSupport.park()`，减少上下文切换                    |
| 使用场景   | 简单同步场景（代码块、方法同步）写多读少 | 复杂并发控制（显式锁、条件变量、读写锁等）读多写少           |
| 灵活性     | 只能实现互斥锁                           | 支持多种锁模型（独占、共享、可重入、可中断）                 |
| 性能       |                                          | 更快                                                         |
| 是否可中断 | 不支持线程中断                           | 支持线程中断、超时等待等高级操作                             |
| 是否可公平 | 非公平，锁竞争无序                       | 选择公平锁和非公平锁                                         |

17、JVM内存结构

**线程私有（栈相关）**

- **JVM 栈**：存局部变量 & 方法调用栈帧，可能 `StackOverflowError`。
- **本地方法栈**：存 JNI 方法信息，可能 `OutOfMemoryError`。
- **PC 计数器**：存字节码行号，线程切换时保证正确恢复。

**线程共享（堆 & 方法区）**

- **堆**：存对象，GC 处理，可能 `OutOfMemoryError: Java heap space`。
- **方法区**：存类信息，可能 `OutOfMemoryError: Metaspace`。

**直接内存**

- **存 NIO 堆外内存**，可能 `OutOfMemoryError: Direct buffer memory`。




18.假设有两份 `List` 数据：`userList` 和 `userMemoList`。需要遍历 `userList`，根据每个用户的 `userId`，从 `userMemoList` 中查找并取出对应 `userId` 的 `content` 值进行后续处理。

```java
// 暴力方法  
public static void nestedLoop(List<User> userTestList, List<UserMemo> userMemoTestList) {
        for (User user : userTestList) {
            Long userId = user.getUserId();
            for (UserMemo userMemo : userMemoTestList) {
                if (userId.equals(userMemo.getUserId())) {
                    String content = userMemo.getContent();
                    // System.out.println("模拟数据content 业务处理......" + content); // 避免打印影响测试结果
                }
            }
        }
    }
// 优化方法
public static void mapOptimizedLoop(List<User> userTestList, List<UserMemo> userMemoTestList) {
        Map<Long, String> contentMap = userMemoTestList.stream().collect(Collectors.toMap(UserMemo::getUserId, UserMemo::getContent));

        for (User user : userTestList) {
            Long userId = user.getUserId();
            String content = contentMap.get(userId);

            if (StringUtils.hasLength(content)) {
               // System.out.println("模拟数据content 业务处理......" + content); // 避免打印影响测试结果
            }
        }
    }
```


#### Java

```java
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

public class RedisDistributedLock {

    private Jedis jedis;
    private static final String LOCK_KEY_PREFIX = "lock:";
    private static final String LOCK_VAL_PREFIX = "lockVal:";
    private static final long RETRY_INTERVAL = 100; // 毫秒

    public RedisDistributedLock(Jedis jedis) {
        this.jedis = jedis;
    }

    // 获取锁的方法
    public UnlockFunc lock(String key, long expireMillis) {
        String lockKey = LOCK_KEY_PREFIX + key;
        String lockValue = LOCK_VAL_PREFIX + generateRandomString(5);

        while (true) {
            SetParams params = new SetParams();
            params.nx().px(expireMillis);  // NX: 只有键不存在时才设置，PX: 设置过期时间，单位毫秒

            String result = jedis.set(lockKey, lockValue, params);

            if ("OK".equals(result)) {
                // 成功获得锁，返回解锁的回调函数
                return () -> {
                    unlock(lockKey, lockValue); // 调用解锁操作
                    return null;
                };
            } else {
                try {
                    Thread.sleep(RETRY_INTERVAL); // 锁已被占用，等待重试
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 解锁方法
    public void unlock(String key, String value) {
        // 这里我们用 Lua 脚本确保锁只会被持有者释放
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        jedis.eval(script, 1, key, value); // 只有持有锁的客户端才能删除锁
    }

    // 解锁函数类型
    public interface UnlockFunc {
        void unlock() throws Exception;
    }
    
        // 尝试获取锁的方法
    public TryLockResult tryLock(String key, long expireMillis) {
        String lockKey = LOCK_KEY_PREFIX + key;
        String lockValue = LOCK_VAL_PREFIX + generateRandomString(5);

        SetParams params = new SetParams();
        params.nx().px(expireMillis);  // NX: 只有键不存在时才设置，PX: 设置过期时间，单位毫秒

        String result = jedis.set(lockKey, lockValue, params);

        if ("OK".equals(result)) {
            // 获取锁成功，返回解锁函数和成功标志
            return new TryLockResult(true, () -> {
                unlock(lockKey, lockValue); // 解锁
            });
        } else {
            // 获取锁失败
            return new TryLockResult(false, null);
        }
    }
}

```



```java
//异步非阻塞地运行一段不返回结果的代码
//默认线程池
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> { System.out.println("Task is running in: " + Thread.currentThread().getName());        });

//指定线程池
ExecutorService executor = Executors.newFixedThreadPool(2);
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
System.out.println("Task is running in: " + Thread.currentThread().getName());}, executor);
```



### 多线程的实现方式

1、继承Thread类重写run方法，使用start方法启动线程

2、实现 `Runnable` 接口重写run方法，将该类的实例作为参数传递给 `Thread` 对象，然后调用 `start()`

3、**实现 `Callable` 接口**

- 定义一个实现 `Callable` 接口的类，重写其 `call()` 方法，并返回结果。

- 使用 `FutureTask` 包装 `Callable` 对象，然后使用 `Thread` 执行

4、**线程池（Executor 框架）**

- 使用 `Executors` 工具类创建线程池，例如 `FixedThreadPool`、`CachedThreadPool` 等。
- 提交任务到线程池，线程池会负责管理线程

**`corePoolSize`**

- 核心线程数，即线程池中始终存活的线程数量。
- 核心线程即使空闲也不会被回收，除非设置了 `allowCoreThreadTimeOut`。

**`maximumPoolSize`**

- 最大线程数，表示线程池能够容纳的最大线程数。

**`keepAliveTime`**

- 空闲线程的存活时间。当线程池中的线程数大于 `corePoolSize` 时，空闲时间超过 `keepAliveTime` 的线程会被终止。

**`unit`**

- 时间单位，用于指定 `keepAliveTime` 的时间单位。例如：`TimeUnit.SECONDS`。

**`workQueue`**

- 任务队列，用于保存等待执行的任务。例如：
  - `ArrayBlockingQueue`：有界队列。
  - `LinkedBlockingQueue`：无界队列。
  - `SynchronousQueue`：不存储任务的队列，直接提交给线程执行。

**`threadFactory`**

- 线程工厂，用于创建新线程，通常用于自定义线程名称等。

**`handler`**

- 拒绝策略。当线程池中的任务队列已满且线程数达到 

  ```
  maximumPoolSize
  ```

   时，如何处理新任务。

  - `AbortPolicy`（默认）：抛出异常。

  - `CallerRunsPolicy`：由提交任务的线程执行。

  - `DiscardPolicy`：丢弃任务，无通知。

  - `DiscardOldestPolicy`：丢弃最旧的任务。

线程池的对比

| 类型                 | 线程数量         | 队列类型 | 适用场景                         |
| -------------------- | ---------------- | -------- | -------------------------------- |
| FixedThreadPool      | 固定数量         | 无界队列 | 稳定且长期的并发任务             |
| CachedThreadPool     | 动态调整         | 无界队列 | 短期大量任务，且响应时间要求较高 |
| SingleThreadExecutor | 单线程           | 无界队列 | 任务需要按顺序执行，避免并发问题 |
| ScheduledThreadPool  | 固定核心线程数   | 延迟队列 | 定时或周期性任务                 |
| WorkStealingPool     | CPU 核心数（默认 | 多队列   | 大量独立的小任务，分治计算场景   |

