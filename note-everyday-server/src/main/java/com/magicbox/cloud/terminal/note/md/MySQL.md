
### Mysql

MySQL 执行一条 SQL 查询语句的过程是怎样的？

MySQL 执行 SQL 查询的过程可以分为多个步骤。首先，连接器负责建立连接并验证用户身份。接着，MySQL 会检查查询是否命中缓存（8.0后移除），若命中则直接返回结果；否则进入解析阶段，进行词法和语法分析，生成语法树。

然后，在执行阶段，MySQL 会经历预处理、优化和实际执行三个步骤。在预处理阶段，MySQL 会检查表和字段是否存在；在优化阶段，它会选择最优的查询计划，最后通过执行阶段从存储引擎读取数据并返回结果。

1、保留四位小数

```mysql
select MAX(target_fault_cd) name,ROUND(MAX(rela_coef),4) value from
```

2、查询字段长度大于1w的

```mysql
select * from sys_task where CHAR_LENGTH(task_message)>10000
```

3、MySQL存储引擎区别

|                  | MyISAM                         | InnoDB                                             |
| ---------------- | ------------------------------ | -------------------------------------------------- |
| 默认索引类型     | 非聚簇索引                     | 聚簇索引                                           |
| 数据和索引的存储 | 索引和数据分开存储             | 数据和主键索引存储在一起（数据即主键索引）         |
| 索引效率         | 查询效率稍低，尤其是范围查询   | 范围查询效率较高，因为数据按索引顺序存储           |
| 写入性能         | 插入和更新索引成本较低         | 更新和插入时可能需要调整数据存储位置，成本较高     |
| 存储空间         | 索引和数据分开，占用空间较大   | 数据即索引，占用空间较小                           |
| 事务             | 不支持                         | 支持                                               |
| 锁类型           | 表级锁，读写冲突时会锁住整个表 | 行级锁，支持更高并发，但在某些情况下可能退化为表锁 |

**使用 MyISAM 的场景**：

- 数据以读为主，写操作较少。
- 对事务一致性要求不高。
- 大量全文搜索需求（如日志系统、文档存储）。

**使用 InnoDB 的场景**：

- 数据以写操作为主，且需要高并发。
- 对数据一致性要求较高（如银行、订单系统）。
- 需要事务支持或外键约束。
- 需要频繁范围查询或排序操作



**数据库中存储过程、函数、视图的区别**

| 特性 / 类型      | **函数（Function）**                            | **存储过程（Stored Procedure）**               | **视图（View）**                         |
| ---------------- | ----------------------------------------------- | ---------------------------------------------- | ---------------------------------------- |
| 定义方式         | `CREATE FUNCTION`                               | `CREATE PROCEDURE`                             | `CREATE VIEW`                            |
| 返回值           | 必须有返回值（标量或表）                        | 可以有返回值，也可以没有                       | 没有，直接查询结果                       |
| 是否可以传参     | ✅ 支持参数                                      | ✅ 支持参数（IN/OUT/INOUT）                     | ❌ 不支持                                 |
| 调用方式         | 可在 SQL 中直接使用，如 `SELECT func(...)`      | 需使用 `CALL proc(...)` 或数据库提供的调用方式 | 可直接查询，如 `SELECT * FROM view_name` |
| 可用于 SQL 语句  | ✅（可在 `SELECT`、`WHERE`、`JOIN` 等中调用）    | ❌（不能嵌入在普通 SQL 中）                     | ✅（看起来就像普通表）                    |
| 主要用途         | 封装计算逻辑、数据转换、返回单值或表            | 实现复杂业务逻辑、批处理、事务控制等           | 抽象复杂查询逻辑、简化 SQL 使用          |
| 是否可嵌套调用   | ✅（函数中可调用函数）                           | ✅（过程间可以调用）                            | ❌ 不能嵌套视图定义                       |
| 支持事务         | 通常不支持事务控制                              | ✅ 可管理事务（BEGIN、COMMIT、ROLLBACK）        | ❌ 不支持事务控制                         |
| 修改数据         | ❌ 通常只读（部分数据库支持修改，如 PostgreSQL） | ✅ 可执行 INSERT、UPDATE、DELETE 等数据修改     | ❌ 只读                                   |
| 安全性与权限控制 | ✅ 支持细粒度控制                                | ✅ 支持细粒度控制                               | ✅ 支持细粒度控制                         |


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

