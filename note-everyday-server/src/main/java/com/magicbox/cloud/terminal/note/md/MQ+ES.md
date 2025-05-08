
### MQ

| 特性         | Kafka                  | RocketMQ                           | RabbitMQ               |
| ------------ | ---------------------- | ---------------------------------- | ---------------------- |
| **吞吐量**   | 超高（百万级 TPS）     | 高（比 RabbitMQ 高，但低于 Kafka） | 适中（万级 TPS）       |
| **消息模型** | 发布/订阅（Pub/Sub）   | 类似 Kafka 的订阅模式              | AMQP（交换机 + 队列）  |
| **消息顺序** | 分区内顺序（部分有序） | 严格顺序消息                       | 需要额外配置           |
| **持久化**   | 日志存储，默认持久化   | 可配置持久化                       | 默认持久化             |
| **事务消息** | 不支持                 | 支持                               | 支持                   |
| **消息延迟** | 毫秒级                 | 毫秒级                             | 低（推模式）           |
| **扩展性**   | 易扩展（基于分区扩展） | 良好（多主多从）                   | 一般（单节点性能有限） |
| **适用场景** | 大数据、流式计算       | 金融、电商、事务型消息             | 金融、支付、任务调度   |

**Kafka**：适用于**高吞吐、流式计算、日志处理**，如大数据场景。

**RocketMQ**：适用于**事务消息、电商、金融**等高可靠性场景。



### Elasticsearch

elk:**ElasticSearch、Logstash(数据采集工具) 、Kibana(数据可视化和分析平台)**。

| **RDBS**            | **ES**                                         |
| ------------------- | ---------------------------------------------- |
| 数据库（database）  | 索引（index）                                  |
| 表（table）         | 类型（type）（ES6.0之后被废弃，es7中完全删除） |
| 表结构（schema）    | 映射（mapping）                                |
| 行（row）           | 文档（document）                               |
| 列（column）        | 字段（field）                                  |
| 索引                | 反向索引                                       |
| SQL                 | 查询DSL                                        |
| SELECT * FROM table | GET http://.....                               |
| UPDATE table SET    | PUT http://.....                               |
| DELETE              | DELETE http://......                           |



```bash
curl '36.134.140.149:30920/_cat/indices?v'  // 查看所有index
curl -XDELETE 'http://36.134.140.149:30920/mb-dev-saas-vehicle-log2024-07-05' // 删除索引
```

使用 SpEL 在 Spring Data Elasticsearch 中提供动态索引名称

```java
//1、使用应用程序配置的值
index.prefix=test //假如application.properties文件中有以下条目
@Document(indexName = "#{@environment.getProperty('index.prefix')}-log")
//使用的索引名称更改为test-log
    
//2、使用某些类的静态方法提供的值
@Document(indexName = "log-#{T(java.time.LocalDate).now().toString()}")
//这将提供索引名称log-2024-07-05
    
//3、使用 Spring bean 提供的值
@Component
public class LogIndexNameProvider {
    @Value("${log.baseIndex}")
    private String baseIndex;
    public String timeSuffix() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return baseIndex + LocalDate.now().format(formatter);
    }
}
@Document(indexName = "#{@logIndexNameProvider.timeSuffix()}")
//索引名称为：log.baseIndex2024-07-05
```