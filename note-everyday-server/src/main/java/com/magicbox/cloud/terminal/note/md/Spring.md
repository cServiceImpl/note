

### spring

mapstruct效率比BeanUtils.copyProperties ()高

```java
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
</dependency>
    
//用法 名称相同直接映射，不同用Mapping注解标准；支持嵌套映射和多数据源映射，如下
    @Mapper(componentModel = "spring")
public interface VolcPodPowerConverter {
    @Mapping(source = "teacher.wifi.name",target = "nameVo")
    @Mapping(source = "student.age",target = "ageVo")
    Student toStudentVO(String student,Teahcher teacher);
}
    
    @Autowired
    private VolcPodPowerConverter volcPodPowerConverter;
//支持不同数据类型转化，可以指定转化格式
	@Mapping(source = "student.age",target = "ageVo",numberFormat="#.00")
	@Mapping(source = "birth",target = "birth",dataFormat="yyyy-MM-dd")
//支持枚举映射，默认情况下，源枚举中的每个常量都映射到目标枚举类型中具有相同名称的常量。如果需要，可以使用@ValueMapping注解将源枚举中的常量映射到具有其他名称的常量
@Mapper
public interface EnumMapper {
	@Mapping(source = "sexEnum",target = "sex2Enum
	StudentVO toStudentVO(Student student);

	@ValueMapping(source = "SECRECY",target = "SECRECY2"),
	@ValueMapping(source = "MAN",target = "MAN2"") ،
	@ValueMapping(source = "WOMAN",target = "WOMAN2" 
	Sex2Enum toSex2Enum(SexEnum sexEnum);
}
// 对象工厂
public class StudentFactory {
	public StudentVO createStudentVo(){
		return StudentVO.getInstance()
    }
}
@Mapper(uses = {StudentFactory.class})
public interface StudentMapper {
	StudentMapper INSTANCE = Mappers.getMapper(StudNentMapper.class);//mapstruct自动生成，这句可省略
	StudentVO toStudentVO(Student student);
}
//使用
 public static void main(String[] args) {
	Student student = new Student( name:"张三", age:12);
	StudentVO studentVO = StudentMapper.INSTANCE.toStudentvO(student)
	System.out.println(studentVO);
 }
```

1、starter-parent换了settings拉不下来时，直接install到本地

2、yml中需要注入比较多的属性，可以用配置类，就不用写一堆@Value注入了，但是配置类中的属性要与yml中属性名一致

```java
@Data
@Configuration
//@PropertySource(value = "classpath:application.yml", encoding = "UTF-8")
@ConfigurationProperties("sms-message")
public class SmsConfig {

    /*
        endpoint
     */
    private String endpoint;

    /*
        signName
     */
    private String signName;}
```

```yml
sms-message:

  endpoint: "dysmsapi.aliyuncs.com"
  signName: "哈哈哈哈"
  loginTemplateCode: "SMS_294045572"
  logoutTemplateCode: "SMS_294045572"
```

3、@Valid 用于在控制器方法参数上标注需要验证的数据。它与Spring的验证功能集成，用于验证传递给控制器方法的参数是否符合指定的验证规则

```java
BasePageParam
    @ApiModelProperty("排序列表")
    @Valid
    private List<OrderByBO> orderByList = new LinkedList<>();
OrderByBO
    @ApiModelProperty("排序值:asc升序,desc降序")
    @Pattern(regexp = "^(asc|desc)", message = "请输入正确的orderValue")
    private String orderValue;
```

入参校验时，在对应入参属性加相应注解，controller的入参前标注@Valid

入参：

```java
@Data
@ApiModel("添加用户信息参数")
public class SysUserAddParam extends BaseEntity implements Serializable {

    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 邮箱
     */
    @ApiModelProperty("邮箱")
    @NotBlank(message = "邮箱不能为空")
    @Email
    private String email;

    /**
     * 手机号
     */
    @ApiModelProperty("手机号")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3|4|5|7|8][0-9]{9}$")
    private String phoneNum;
    
@Min (数值)验证 Number 和 String 对象是否大等于指定的值

@Max (数值)验证 Number 和 String 对象是否小等于指定的值

@Size(min=, max=) 验证对象（Array,Collection,Map,String）长度是否在给定的范围之内

@Length(min=, max=) 验证字符串长度是否在给定的范围之内

    max和min是对你填的“数字”是否大于或小于指定值，这个“数字”可以是number或者string类型。长度限制用length。

```

controller：

```java
    @PostMapping("/add")
    public Response<?> add(@RequestBody @Valid @ApiParam("新增用户参数") SysUserAddParam sysUserAddParam) {
        userManageService.add(sysUserAddParam);
        return ResponseHelper.createSuccessResponse();
    }
```

嵌套对象，对象的对象中失效解决办法，需要在外层对象的属性上加@Valid

```java
@Data
public class TestParam {
   @NotBlank
   private String className;

   @Valid
   private List<UserDTO> users;
}
@Data
public class UserDTO {
    @NotBlank
    private String name;

}
```

```
@Validated：提供分组功能，可以在参数验证时，根据不同的分组采用不同的验证机制；用在方法入参上无法单独提供嵌套验证功能。不能用在成员属性（字段）上，也无法提示框架进行嵌套验证。能配合嵌套验证注解@Valid进行嵌套验证。

@Valid：用在方法入参上无法单独提供嵌套验证功能。能够用在成员属性（字段）上，提示验证框架进行嵌套验证。能配合嵌套验证注解@Valid进行嵌套验证。
```

4、@RefreshScope动态更新

读值的时候 不用加 在nacos修改后服务不需要重启就能读到

读连接时候 需要重启服务，加这个注解也没用

5、mybatis plus和mybatis打印sql

```yml
logging:
  level:
    com.magicbox.cloud.terminal.dao: debug  //mybatis plus的mapper所在包
mybatis:
  # 控制台打印sql日志
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

6、用feign时，给对应的ScheduleEngineFeign类加

```Java
@FeignClient(value = "mb-schedule-engine-server")
```

application启动类加

```Java
@EnableFeignClients
```

这样自动注入时候就不报错了

7、启动类的

```Java
@EnableTransactionManagement
```

用于启用基于注解的事务管理。通过使用这个注解，你可以开启Spring对事务的自动管理功能，使得你可以在方法上使用 `@Transactional` 注解来声明事务。

9、返回json如果空值，结构体里该字段就不返回

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
 private String userAgreementUrl;
```

```go
type AllocFreePodResult struct {
    Got          bool       `json:"got"`                    // 是否获取到了空闲实例 true:是 false:否
    PodId        string     `json:"podId,omitempty"`        // podId 获取成功才有值
    ChannelTopic string     `json:"channelTopic,omitempty"` // pod订阅的topic 获取成功才有值
    ProductId    string     `json:"productId,omitempty"`
    TokenData    *TokenData `json:"tokenData,omitempty"` // token信息 获取成功才有值
    PoolData     *PoolData  `json:"poolData,omitempty"`  // 排队信息 没有资源才有值
}
```

10.假设有两份 `List` 数据：`userList` 和 `userMemoList`。需要遍历 `userList`，根据每个用户的 `userId`，从 `userMemoList` 中查找并取出对应 `userId` 的 `content` 值进行后续处理。

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

11、AOP通知类型及执行顺序

前置通知→环绕通知→正常返回通知/异常返回通知→返回通知



### Mybatis

1、mybatisplus常用，列表去除重复字段，返回结果组map

```java
QueryWrapper<DrivingVehicleSeriesType> queryWrapper = new QueryWrapper<>();
queryWrapper.select("code,min(name) as name").last("group by code");
List<DrivingVehicleSeriesType> list = drivingVehicleSeriesTypeMapper.selectList(queryWrapper);
Map<String,String> seriesTypeMap = list.stream().collect(Collectors.toMap(DrivingVehicleSeriesType::getCode,DrivingVehicleSeriesType::getName));
```

筛选

```java
LambdaQueryWrapper<SysOperationLogEntity> wrapper = new LambdaQueryWrapper<>();
wrapper.ge(SysOperationLogEntity::getCreateTime, param.getStartTime())
        .le(SysOperationLogEntity::getCreateTime, param.getEndTime());
if(StringUtils.isNotBlank(param.getKeyword())){
    wrapper.and(i -> i.like(SysOperationLogEntity::getPage, param.getKeyword())
                .or()
                .like(SysOperationLogEntity::getEmail, param.getKeyword())
                .or()
                .like(SysOperationLogEntity::getOperator, param.getKeyword()));
}
```

2、mybatisplus分页

```java
@Mapper
public interface ErrCodeMsgMapper extends BaseMapper<ErrCodeMsgEntity> {
}
```

```java
/*实现类*/
@Override
public PageResult<ErrCodeMsgDTO> getErrCodeMsgPage(ErrCodeMsgQueryParameter parameter) {
    Page<ErrCodeMsgEntity> page = new Page<>(parameter.getPageNum(), parameter.getPageSize());
    LambdaQueryWrapper<ErrCodeMsgEntity> queryWrapper = new LambdaQueryWrapper<>();

    queryWrapper.eq(ErrCodeMsgEntity::getIsDeleted, 0);
    if (!StringUtils.isEmpty(parameter.getContent())) {
        queryWrapper.and(q -> q.like(ErrCodeMsgEntity::getMsg, parameter.getContent())
                .or().like(ErrCodeMsgEntity::getCode, parameter.getContent()));
    }
    if (parameter.getStyles() != null) {
        List<Integer> styleList = Arrays.stream(parameter.getStyles().split(",")).map(Integer::parseInt).collect(Collectors.toList());
        queryWrapper.in(ErrCodeMsgEntity::getStyle, styleList);
    }

    Page<ErrCodeMsgEntity> result = errCodeMsgMapper.selectPage(page, queryWrapper);

    // transfer entity to dto
    List<ErrCodeMsgDTO> dtoList = result.getRecords().stream().map(entity -> {
        ErrCodeMsgDTO dto = new ErrCodeMsgDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }).collect(Collectors.toList());


    PageResult<ErrCodeMsgDTO> pageResult = new PageResult<>();
    pageResult.setTotalNum(result.getTotal());
    pageResult.setPageNo(result.getCurrent());
    pageResult.setPageSize(result.getSize());
    pageResult.setData(dtoList);

    return pageResult;
}
```

```
/*分页实体类*/
@Data
public class PageResult<T> {
    /**
     * 数据
     */
    private List<T> data;
    /**
     * 页码
     */
    private long pageNo;
    /**
     * 页面大小
     */
    private long pageSize;
    /**
     * 总条数
     */
    private long totalNum;
}
```

3、mybatis的dao接口（mapper）接口，可以重载多个方法，但是多个接口对应的映射必须只有一个，否则启动会报错（**不推荐使用重载**）

需要满足：仅有一个无参和有参；多个有参时，参数数量必须一致

4、mybatis plus的selectById和deleteById会自动过滤逻辑删除字段

```java
    /**
     * 是否删除 0.未删除 1.已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
```

5、批量插入

在 MyBatis-Plus 中，saveBatch 方法是用于**批量保存数据**的方法。它能够在单次操作中将多条数据同时插入数据库，从而提高插入效率，减少数据库连接次数，提升性能。

```
    boolean saveBatch(Collection<T> entityList);
    boolean saveBatch(Collection<T> entityList, int batchSize);
```

- entityList：要插入的实体类集合。可以是任何实现了 Collection 接口的集合类型，如 List、Set 等。
- batchSize（可选）：指定每次批量插入的大小。默认情况下，MyBatis-Plus 会一次性插入所有数据。如果设置了 batchSize，则会按指定大小分批插入，避免一次性插入大量数据时出现性能问题或内存溢出。

**默认实现的局限性**

- **不支持多条 SQL 合并**：在默认情况下，即便使用 saveBatch，也有可能是逐条发送 SQL 语句。这会导致生成的 SQL 更冗长、性能较低，尤其是在数据量较大时，执行效率会明显下降，无法充分利用数据库批量插入的特性。
- **性能提升有限**：默认实现并未针对批量插入进行特殊优化。例如，它可能无法充分利用 JDBC 的批量操作特性，导致性能不如手动实现的批量插入逻辑。对于大批量插入，性能可能不理想。
- **主键生成方式局限性**：如果实体类中主键是由数据库自动生成（如自增主键），默认实现会多次与数据库交互获取主键值。这会增加额外的数据库开销。尤其是当数据量较大时，主键生成的额外查询操作会显著降低性能。
- **外键关系处理复杂**：需要在插入数据后获取主键 ID，这导致无法在批量插入时建立关联关系，使得外键关系处理变得复杂。
- **缺乏灵活性**：默认实现只能进行简单的插入操作，不能处理条件性插入（如：插入前判断是否已存在相同记录）或插入冲突处理（如主键冲突时自动更新数据）。对需要动态逻辑的场景不适用。

**rewriteBatchedStatements=true 的作用**

JDBC 批处理机制是一种优化数据库操作性能的技术，允许将多条 SQL 语句作为一个批次发送到数据库服务器执行，从而减少客户端与数据库之间的交互次数，显著提高性能。通常用于 批量插入、批量更新 和 批量删除 等场景

**启用批处理重写**：启用批处理重写功能后，驱动能够将多条同类型的 SQL 语句进行合并，进而发送给数据库执行。

**减少网络交互**：一次发送多条 SQL，可有效降低网络延迟，减少网络交互次数。

**提高执行效率**：当所有数据都通过一条 SQL 插入时，MySQL 只需要解析一次 SQL，降低了解析和执行的开销。

**减少内存消耗**：虽然批量操作时将数据合并到一条 SQL 中，理论上会增加内存使用（因为需要构建更大的 SQL 字符串），但相比多次单条插入的网络延迟和处理开销，整体的资源消耗和执行效率是更优的

未开启参数时的批处理 SQL：

```sql
INSERT INTO question (exam_id, content) VALUES (?, ?);
INSERT INTO question (exam_id, content) VALUES (?, ?);
INSERT INTO question (exam_id, content) VALUES (?, ?);
```

开启参数后的批处理 SQL：

```sql
INSERT INTO question (exam_id, content) VALUES (?, ?), (?, ?), (?, ?);
```

**配置 rewriteBatchedStatements=true**

 **修改数据库连接配置**

实现这个配置的方式很简单，只需要在我们现有的数据库连接后面直接加上就好。例如：jdbc:mysql://localhost:3306/db_name?`rewriteBatchedStatements=true`

6、

| 对比项             | `BaseMapper`                                          | `IService`                                                   |
| ------------------ | ----------------------------------------------------- | ------------------------------------------------------------ |
| 所属层次           | DAO 层（数据访问层）                                  | Service 层（业务逻辑层）                                     |
| 主要用途           | 与数据库直接交互，封装 SQL 操作                       | 提供更高级的业务方法封装（调用 BaseMapper）                  |
| 是否为接口默认实现 | 是接口，MyBatis-Plus 会自动为其生成实现类             | 是接口，需手动继承 `ServiceImpl` 实现类                      |
| 常用方法           | `insert`、`deleteById`、`selectById`、`updateById` 等 | `save`、`removeById`、`getById`、`updateById`、`saveOrUpdate` 等 |
| 是否支持事务控制   | 不支持（通常由 Service 层控制）                       | 支持（在 Service 层可加事务注解）                            |
| 是否建议直接使用   | 是，适合简单 CRUD 操作                                | 是，适合封装业务逻辑，屏蔽 DAO 层细节                        |
| 自定义扩展         | 通常扩展 XML Mapper 或使用注解                        | 通常扩展自定义 Service 方法                                  |



### 分布式锁

**分布式锁**：在多个服务或进程间控制对共享资源的访问，以确保同一时刻只有一个实例能够访问或修改某个资源。分布式锁的核心目标是解决多个并发请求时，如何保证某个操作的原子性

**基于数据库的分布式锁**

**基于Redis的分布式锁**：通过Redis的`SETNX`命令进行锁的控制，简单且高效。适用于高并发的场景。

利用Redis的**SETNX**命令来确保锁的唯一性。`SETNX`是Redis的一个原子操作，当键不存在时设置键值并返回成功；如果键已经存在，则返回失败；设置锁时，除了设置键值外，通常还会设置一个过期时间，防止死锁的发生。

**基于ZooKeeper的分布式锁**：利用ZooKeeper的临时顺序节点实现，具有较高的一致性保障，适用于对一致性要求较高的场景。创建一个临时顺序节点，最小的节点会成为锁的持有者。

#### GO

```go
func (l *RedisDistributeLock) Lock(ctx context.Context, key string, expire time.Duration) (UnlockFunc, error) {
    key = lockKeyPrefix + key
    val := lockValPrefix + randomString(5)
    for {
        res, err := l.client.Do(ctx, "SET", key, val, "PX", expire.Milliseconds(), "NX").Result()
        if err != nil {
            if err == redis.Nil {
                time.Sleep(retryInterval)
                continue
            }
            return nil, err
        }
        if res != nil {
            return func() error {
                return l.unLock(context.Background(), key, val)
            }, nil
        }
    }
}
//这个方法在获取不到锁时会自动重试，直到成功获取为止。
//如果获取到锁，返回一个 UnlockFunc，这个函数可以在后续释放锁。
//锁的过期时间是由 expire 参数控制的。
//适用于需要阻塞等待锁的场景，它会自动重试，直到获取到锁。
```



```go
func (l *RedisDistributeLock) TryLock(ctx context.Context, key string, expire time.Duration) (bool, UnlockFunc, error) {
    key = lockKeyPrefix + key
    val := lockValPrefix + randomString(5)
    _, err := l.client.Do(ctx, "SET", key, val, "PX", expire.Milliseconds(), "NX").Result()
    if err != nil {
        if err == redis.Nil {
            return false, nil, nil
        }
        return false, nil, err
    }
    return true, func() error {
        return l.unLock(ctx, key, val)
    }, nil
}
//该方法尝试一次获取锁，如果获取不到锁（例如锁已被其他客户端持有），则返回 false。
//如果成功获取锁，返回 true 和一个 UnlockFunc，用于后续的锁释放。
//不会像 Lock 方法那样进行重试，因此适用于那些不需要等待锁的场景。
//适用于不希望一直等待的场景，它只会尝试一次，如果无法获取锁，则返回 false
```



```go
func (l *RedisDistributeLock) unLock(ctx context.Context, key, val string) (err error) {
    storeVal, err := l.client.Get(ctx, key).Result()
    if err != nil {
       return err
    }
    if val != storeVal {
       return ErrLockValueNotMatch
    }
    _, err = l.client.Del(ctx, key).Result()
    return err
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


### 拦截器和过滤器

### **拦截器（Interceptor）**

#### 1. **定义**：

拦截器是 Spring Framework 提供的功能，用于在请求处理过程中的特定时机进行拦截，可以对请求和响应做修改、记录日志等操作。它是基于 Spring MVC 的拦截机制，只适用于 Spring MVC 的请求处理。

#### 2. **应用场景**：

- **日志记录**：对所有请求和响应进行日志记录，方便追踪请求的执行过程。
- **权限控制**：在请求到达目标控制器方法之前进行权限验证，防止未经授权的请求访问。
- **性能监控**：在请求处理之前和之后计算请求的处理时间。
- **统一的处理（例如，设置请求头、设置统一的语言等）**。
- **事务管理**：在请求前后对事务进行控制。
- Spring AOP就是基于拦截器实现的

#### 3. **作用时机**：

拦截器的作用时机是 **请求到达控制器方法之前** 和 **响应从控制器返回之后**。拦截器通常用于：

- **在控制器方法执行之前**：做一些全局的预处理，例如用户身份验证、日志记录等。
- **在控制器方法执行之后**：做一些后处理，例如日志记录、响应修改等。

#### 4. **特点**：

- 只能在 Spring MVC 请求中使用，基于 HandlerInterceptor 接口。
- 支持请求的 `preHandle`（请求到达控制器之前）、`postHandle`（控制器方法执行后，视图渲染前）、`afterCompletion`（请求处理完后，视图渲染之后）这三个钩子方法。
- 只能访问 Spring 的上下文，无法直接访问 `Servlet` API。

### **过滤器（Filter）**

#### 1. **定义**：

过滤器是基于 Servlet 规范的机制，它是在请求到达 Servlet 之前以及响应从 Servlet 返回之前对请求和响应进行处理的组件。Spring Boot 中通过实现 `javax.servlet.Filter` 接口来定义过滤器。

#### 2. **应用场景**：

- **日志记录**：用于请求的输入输出日志记录，如记录每个请求的请求头、请求体、响应状态等。
- **CORS 处理**：处理跨域请求，设置响应头等。
- **请求修改**：对请求进行统一处理（如请求参数的过滤或修改）。
- **权限控制**：类似于拦截器，但更多用于 Servlet 层级的处理，可以处理所有 HTTP 请求。
- **安全过滤**：如防止 CSRF、XSS 攻击，或者其他安全相关操作。

#### 3. **作用时机**：

过滤器的作用时机是 **在请求进入 Servlet 之前** 和 **响应从 Servlet 返回之前**。它们会在 Spring MVC 的控制器处理之前和之后执行。

- **请求进入 Servlet 前**：可以对请求做一些修改或检查（如修改请求的参数、设置编码、日志记录、检查权限等）。
- **响应从 Servlet 返回前**：对响应做一些修改（如添加统一的响应头，记录响应日志等）。

#### 4. **特点**：

- 过滤器基于 Java EE 规范，因此它不仅仅限于 Spring MVC 应用，任何基于 Servlet 的应用都能使用。

- 它可以处理所有 HTTP 请求，不论请求是否通过 Spring MVC 控制器进行处理。

- 过滤器可以访问 `Servlet` API，可以对请求和响应做底层的操作（如修改请求内容或响应内容）。

- 过滤器使用时机早于拦截器。

### 过滤器和拦截器的区别

  **主要区别：**

  1、拦截器是基于Java的反射机制的，而过滤器是基于函数回调。
  2、拦截器依赖于spring容器，过滤器依赖于servlet容器。
  3、拦截器只能对action请求起作用，而过滤器则可以对几乎所有的请求起作用。
  4、拦截器可以访问action上下文、值栈里的对象，而过滤器不能访问。
  5、在action的生命周期中，拦截器可以多次被调用，而过滤器只能在容器初始化时被调用一次
  6、拦截器可以获取IOC容器中的各个bean（基于FactoryBean接口 ），而过滤器就不行，在拦截器里注入一个service，可以调用业务逻辑。

![image-20241211111426686](C:\Users\cuixiao\AppData\Roaming\Typora\typora-user-images\image-20241211111426686.png)

### 事务

事务隔离级别描述的是纵向事务并发调用时的行为模式，

而事务传播机制描述的是横向事务传递时的行为模式

### 事务隔离级别

Sping 中的事务隔离级别有 5 种，它们分别是：

1. **DEFAULT**：**Spring 中默认的事务隔离级别**，以连接的数据库的事务隔离级别为准。
2. **READ_UNCOMMITTED**：读未提交，也叫未提交读，该隔离级别的事务可以看到其他事务中未提交的数据。该隔离级别因为可以读取到其他事务中未提交的数据，而未提交的数据可能会发生回滚，因此我们把该级别读取到的数据称之为脏数据，把这个问题称之为脏读。
3. **READ_COMMITTED**：读已提交，也叫提交读，该隔离级别的事务能读取到已经提交事务的数据，因此它不会有脏读问题。但由于在事务的执行中可以读取到其他事务提交的结果，所以在不同时间的相同 SQL 查询中，可能会得到不同的结果，这种现象叫做不可重复读。
4. **REPEATABLE_READ**：可重复读，它能确保同一事务多次查询的结果一致。但也会有新的问题，比如此级别的事务正在执行时，另一个事务成功的插入了某条数据，但因为它每次查询的结果都是一样的，所以会导致查询不到这条数据，自己重复插入时又失败（因为唯一约束的原因）。明明在事务中查询不到这条信息，但自己就是插入不进去，这就叫幻读 （Phantom Read）。
5. **SERIALIZABLE**：串行化，最高的事务隔离级别，它会强制事务排序，使之不会发生冲突，从而解决了脏读、不可重复读和幻读问题，但因为执行效率低，所以真正使用的场景并不多。

- 脏读：一个事务读取到了另一个事务修改的数据之后，后一个事务又进行了回滚操作，从而导致第一个事务读取的数据是错误的。
- 不可重复读：一个事务两次查询得到的结果不同，因为在两次查询中间，有另一个事务把数据修改了。
- 幻读：一个事务两次查询中得到的结果集不同，因为在两次查询中另一个事务有新增了一部分数据。

Spring 中，事务隔离级别可以通过 @Transactional(isolation = Isolation.DEFAULT) 来设置。

### 事务应用

假设有两个事务a,b，a操作订单表，b操作库存表，a保存订单后，调用b减少库存失败了，这时a也要回滚，事务要怎么设计？

##### 使用本地事务+事务消息（TCC模式）

TCC（Try-Confirm-Cancel）模式是分布式事务的一种实现方式，将每个操作拆分为三个步骤。

**步骤：**

1. **Try阶段：**
   - `a`事务创建订单，标记为“处理中”状态。
   - `b`事务冻结库存，标记为“处理中”状态。
2. **Confirm阶段：**
   - 如果所有操作成功，确认订单，减库存。
3. **Cancel阶段：**
   - 如果任何操作失败，取消订单，释放冻结的库存。

**优点：**

- 资源锁时间短。
- 更高效。

**缺点：**

- 实现较复杂，需开发每个操作的补偿逻辑。

**事件驱动的最终一致性（异步补偿事务）**

  通过事件队列，保证最终一致性。

  **步骤：**

  1. `a`事务保存订单后，提交本地事务。
  2. `a`发送一个“订单已创建”的事件到消息队列。
  3. `b`监听该事件，减少库存。如果失败，记录失败日志并进行重试。
  4. 如果重试多次仍失败，可以通过人工干预或补偿逻辑（如取消订单）。

  **优点：**

  - 解耦订单和库存的逻辑。
  - 高并发场景下性能更优。

  **缺点：**

  - 需要保证消息队列的可靠性。
  - 可能出现短时间内的不一致状态。

### 事务传播机制

Spring 事务传播机制是指，包含多个事务的方法在相互调用时，事务是如何在这些方法间传播的。

Spring 事务传播机制可使用 @Transactional(propagation=Propagation.REQUIRED) 来设置，Spring 事务传播机制的级别包含以下 7 种：

1. Propagation.REQUIRED：默认的事务传播级别，它表示如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务。

2. Propagation.SUPPORTS：如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。

3. Propagation.MANDATORY：（mandatory：强制性）如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常。

4. Propagation.REQUIRES_NEW：表示创建一个新的事务，如果当前存在事务，则把当前事务挂起。也就是说不管外部方法是否开启事务，Propagation.REQUIRES_NEW 修饰的内部方法会新开启自己的事务，且开启的事务相互独立，互不干扰。

5. Propagation.NOT_SUPPORTED：以非事务方式运行，如果当前存在事务，则把当前事务挂起。

6. Propagation.NEVER：以非事务方式运行，如果当前存在事务，则抛出异常。

7. Propagation.NESTED：如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于PROPAGATION_REQUIRED。

   ![image-20241227161021547](D:\Download\TyporaPic\image-20241227161021547.png)
