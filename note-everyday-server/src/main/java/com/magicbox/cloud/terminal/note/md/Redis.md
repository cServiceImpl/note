
### Redis

StringRedisTemplate pom依赖

```java
@Configuration
public class RedisConfig {
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```bash
SCAN 0 MATCH activationCode:bind:* COUNT 100    //查找所有的activationCode:bind:前缀的key
返回游标0是代表扫描完成，如果返回游标304则表示没扫完，需要从304游标继续扫描

redis-cli -h 192.168.0.29  //连接redis
select 9                   //选择redis指定库
```



```shell
慢查询日志
在 redis.conf 文件中，我们可以使用 slowlog-log-slower-than 参数设置耗时命令的阈值，并使用 slowlog-max-len 参数设置耗时命令的最大记录条数
获取慢查询日志的使用 SLOWLOG GET
```



### Redis Functions

允许你将 Lua 脚本以“函数”的形式注册到 Redis 中，然后通过函数名来调用，**避免每次都传送和解析脚本内容**，实现了：

- **更高效**：注册一次，调用时只传函数名；
- **更安全**：只执行已注册的脚本；
- **更易维护**：支持模块化组织函数。

Redis Functions 是服务端注册的，需要持久化脚本状态；

如果 Redis 实例重启，脚本不会自动恢复，建议使用 RDB 或 AOF 保存

```lua
//脚本注册代码
FUNCTION LOAD LUA "
redis.register_function{
  function_name = 'decrease_stock',
  callback = function(keys, args)
    local stock_key = keys[1]
    local quantity = tonumber(args[1])

    local current_stock = tonumber(redis.call('GET', stock_key))
    if not current_stock then
      return {err = 'Stock key does not exist'}
    end

    if current_stock < quantity then
      return {err = 'Insufficient stock'}
    end

    local new_stock = redis.call('DECRBY', stock_key, quantity)
    return new_stock
  end
}
"
// 使用
FCALL decrease_stock 1 product:123:stock 5
```



### Redis问题

1、引入缓存后，需要考虑缓存和数据库一致性问题，可选的方案有：

「更新数据库 + 更新缓存」、**「更新数据库 + 删除缓存」**

2、更新数据库 + 更新缓存方案，在「并发」场景下无法保证缓存和数据一致性，且存在「缓存资源浪费」和「机器性能浪费」的情况发生

3、在更新数据库 + 删除缓存的方案中，**「先删除缓存，再更新数据库」**在「并发」场景下依旧有数据不一致问题，解决方案是**「延迟双删」**，凭借经验发送「延迟消息」到队列中，延迟删除缓存但这个延迟时间很难评估，所以**推荐用「先更新数据库，再删除缓存」**的方案

4、在「先更新数据库，再删除缓存」方案下，为了保证两步都成功执行，需配合「**消息队列」或「订阅MySQL的binlog变更日志」**的方案来做，本质是通过「重试」的方式保证数据一致性


### 本地缓存（Caffeine等）
减少Redis的直接请求压力，减少不必要的缓存穿透

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

##### 部署redis

```bash
docker pull redis

# 配置redis.conf
主要配置的如下：
 bind 127.0.0.1 #注释掉这部分，使redis可以外部访问
 daemonize no#用守护线程的方式启动
 requirepass 你的密码#给redis设置密码
 appendonly yes#redis持久化　　默认是no
 tcp-keepalive 300 #防止出现远程主机强迫关闭了一个现有的连接的错误 默认是300
 
# 创建本地与docker映射的目录，即本地存放的位置
sudo mkdir /data/redis
sudo mkdir /data/redis/data
sudo cp -p redis.conf /data/redis/

# 启动docker redis
docker run -p 6379:6379 --name redis 
-v /data/redis/redis.conf:/etc/redis/redis.conf  
-v /data/redis/data:/data 
-d redis redis-server /etc/redis/redis.conf --appendonly yes

docker run -p 6379:6379 --name redis
docker run: 这是 Docker 的命令，用于从指定的镜像启动一个新的容器实例。
-p 6379:6379: 这是一个端口映射参数。它将宿主机的 6379 端口映射到容器的 6379 端口。这样，任何访问宿主机 6379 端口的流量都会被转发到容器的 6379 端口。
--name redis: 为新启动的容器指定一个名称，这里是 redis。这样，你就可以通过名称（而不是容器ID）来引用或管理这个容器了。

-v /data/redis/redis.conf:/etc/redis/redis.conf
-v 或 --volume 参数用于挂载卷。这里，它将宿主机的 /data/redis/redis.conf 文件或目录挂载到容器的 /etc/redis/redis.conf 路径。
这意味着容器内的 Redis 服务将使用宿主机的 /data/redis/redis.conf 作为其配置文件。这是一个很好的做法，因为它允许你更改配置并立即在容器中看到效果，而不需要重新构建镜像。

-d redis redis-server /etc/redis/redis.conf --appendonly yes
-d: 以“分离”模式（即后台模式）运行容器。这意味着容器将在后台启动，并立即返回命令提示符，而不是等待容器退出。
redis: 这是要从中启动容器的镜像名称。
redis-server /etc/redis/redis.conf --appendonly yes: 这是要在容器内执行的命令。它启动 Redis 服务器，并使用前面挂载的配置文件（/etc/redis/redis.conf）。--appendonly yes 是一个 Redis 命令行参数，它告诉 Redis 使用 AOF（Append Only File）持久化模式。在 AOF 模式下，Redis 将每个写命令（如 SET、LPUSH 等）追加到一个日志文件中，并在服务器重启时重新执行这些命令来恢复数据。
```


