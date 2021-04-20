## <center>秒杀系统的实现记录</center>

1. 首先新建秒杀表的相关信息：

![img](file:///C:/Users/CHIZHO~1/AppData/Local/Temp/msohtmlclip1/01/clip_image002.png)

 

记录一下商品的信息

![img](file:///C:/Users/CHIZHO~1/AppData/Local/Temp/msohtmlclip1/01/clip_image004.png)

 

这里要记录一下当天要秒杀的商品信息:

![img](file:///C:/Users/CHIZHO~1/AppData/Local/Temp/msohtmlclip1/01/clip_image006.png)

 

记录一下用户秒杀的订单表：

![img](file:///C:/Users/CHIZHO~1/AppData/Local/Temp/msohtmlclip1/01/clip_image008.png)

 

```bash
# 配置Redis Cluster，结构为官方的结构，有三个master节点
# 其实后来又加了三个slave节点，对应的ip:port可以看ymal文件，为了模拟简单的主从结构
Node1：
ip: 172.17.0.2

Node2:
Ip: 172.17.0.3

Node3:
Ip: 172.17.0.4
```

 

进入redis节点1配置cluster信息以及插槽的分配：

![img](file:///C:/Users/CHIZHO~1/AppData/Local/Temp/msohtmlclip1/01/clip_image010.png)

所有的插槽全部分配完毕，现在测试将数据放入redis中

![img](file:///C:/Users/CHIZHO~1/AppData/Local/Temp/msohtmlclip1/01/clip_image012.png)

接下来要配置简单的主从结构（这里为了简单起见，我没有配置哨兵结构）

 

Slave1----->Node1

![img](file:///C:/Users/CHIZHO~1/AppData/Local/Temp/msohtmlclip1/01/clip_image014.png)

Slave2---->Node2

![img](file:///C:/Users/CHIZHO~1/AppData/Local/Temp/msohtmlclip1/01/clip_image016.png)

Slave3---->Node3

![img](file:///C:/Users/CHIZHO~1/AppData/Local/Temp/msohtmlclip1/01/clip_image018.png)

 

 

可以看到经过测试，从主机可以得到node1节点的数据

![img](file:///C:/Users/CHIZHO~1/AppData/Local/Temp/msohtmlclip1/01/clip_image020.png)

 

Redis Cluster的主从配置也配好了，下面配置springboot的ymal

```yaml
server:
  port: 443
  ssl:
    key-store: classpath:www.yulinet.asia.jks
    key-store-password: szkvk37s2y7qw6
    key-store-type: JKS
    key-alias: www.yulinet.asia

# 对Redis进行配置,Jedis部分直接使用默认配置
# 注意要配置redis cluster的nodes
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://172.21.16.6:3306/wechartshop
    type: com.alibaba.druid.pool.DruidDataSource
    username: root
    password: 123456
  redis:
    cluster:
      nodes:
        - 172.17.0.2:6379
        - 172.17.0.3:6379
        - 172.17.0.4:6379
        - 172.17.0.5:6379
        - 172.17.0.6:6379
        - 172.17.0.7:6379
      max-redirects: 3
    lettuce:
      cluster:
        refresh:
          adaptive: true
          period: 15s
      pool:
        max-active: 8
        max-wait: -1
        max-idle: 8
        min-idle: 0
  cache:
    type: redis
    redis:
      cache-null-values: true

# 配置mybatis
mybatis:
  mapper-locations: classpath:*mapper.xml
  configuration:
    map-underscore-to-camel-case: true
```



 

 

然后配置Redis的序列化配置方案：

```java

// 配置redisTemplete和stringRedisTemplete的序列化操作

@Configurable
public class RedisConfig {

    // 配置redis序列化和反序列化
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    // 配置redisManager节点,让getRedisSerializer和setRedisSerializer的性为保持一致
    @Bean
    public RedisCacheManager redisCacheManager(RedisTemplate redisTemplate) {
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisTemplate.getConnectionFactory());
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisTemplate.getValueSerializer()));
        return new RedisCacheManager(redisCacheWriter, redisCacheConfiguration);
    }
}
```



 

配置mybatis的xml，注意这个xml一定要放在resource目录下，否则它不会跟着一起被build，这样把jar包发送到服务器上的时候，会找不到mapper的配置文件

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.example.seckill.mappers.SeckillMapper">
    <!-- 数据库中的字段和实体类中的字段的结果映射 -->
    <resultMap id="allColumns" type="com.example.seckill.dao.SeckillInfo">
        <id column="id" property="id" />
        <result column="deid" property="deid" />
        <result column="sec_price" property="secPrice" />
        <result column="stock_num" property="stockNum" />
        <result column="start_time" property="startTime" />
        <result column="end_time" property="endTime" />
    </resultMap>

    <!-- 插入操作 -->
    <!-- 这个使用trim标签去除多余的逗号，同时将左右括号省略 -->
    <insert id="saveSeckillInfo" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO seckill
        <trim prefix="(" suffix=")" suffixOverrides=",">
            <if test="pojo.deid != null">
                deid,
            </if>
            <if test="pojo.secPrice != null">
                sec_price,
            </if>
            <if test="pojo.stockNum != null">
                stock_num,
            </if>
            start_time,
            end_time
        </trim>
        VALUES
        <trim prefix="(" suffix=")" suffixOverrides=",">
            <if test="pojo.deid != null and pojo.deid != ''">#{pojo.deid},</if>
            <if test="pojo.secPrice != null">#{pojo.secPrice},</if>
            <if test="pojo.stockNum != null">#{pojo.stockNum},</if>
            now(),
            now()
        </trim>
    </insert>

    <!-- 删除操作 -->
    <delete id="removeSeckillInfoById">
        DELETE FROM seckill
        WHERE id=#{id}
    </delete>

    <!-- 更新操作 -->
    <update id="updateSeckillInfo">
        UPDATE seckill
        <set>
            <if test="pojo.deid != null">deid=#{pojo.deid},</if>
            <if test="secPrice != null">sec_price=#{pojo.secPrice},</if>
            <if test="pojo.stockNum != null">stock_num=#{pojo.stockNum},</if>
            start_time=now(),
            end_time=now()
        </set>
        WHERE id=#{pojo.id}
    </update>

    <!-- 全体信息查询操作 -->
    <select id="findAllSeckillInfo" resultMap="allColumns">
        SELECT * FROM seckill
    </select>

    <!-- 根据id查找某个定义的值 -->
    <select id="findSeckillInfoById"  resultMap="allColumns">
        SELECT * FROM seckill
        WHERE id=#{id}
    </select>

</mapper>
```



 

 

 

配置service层以及缓存的具体实现

```java

/**
 * Service层主要定义具体的缓存操作提供给Controller调用
 */

@Service
public class SeckillServiceImpl implements SeckillService {

    @Resource
    private SeckillMapper seckillMapper;

    /**
     * 这里要将物品存入缓存当中，之前要将缓存全部清除
     * @param seckillInfo
     * @return
     */
    @Override
    @Caching(evict = {@CacheEvict(cacheNames = "goodsList", allEntries = true)},
             put = {@CachePut(cacheNames = "good", key = "#seckillInfo.id")})
    public int saveSeckillInfo(SeckillInfo seckillInfo) {
        return seckillMapper.saveSeckillInfo(seckillInfo);
    }

    @Override
    @Caching(evict = {@CacheEvict(cacheNames = "goodsList", allEntries = true),
                      @CacheEvict(cacheNames = "goods", key = "#id")})
    public int removeSeckillInfoById(Long id) {
        return seckillMapper.removeSeckillInfoById(id);
    }

    /**
     * 这里定义缓存中的每一项存储的类型
     * @param id
     * @return
     */
    @Override
    @Cacheable(value = "goods")
    public SeckillInfo findSeckillInfoById(Long id) {
        return seckillMapper.findSeckillInfoById(id);
    }

    @Override
    @Caching(evict = {@CacheEvict(cacheNames = "goodsList", allEntries = true)},
             put = {@CachePut(cacheNames = "goods", key = "#seckillInfo.id")})
    public int updateSeckillInfo(SeckillInfo seckillInfo) {
        return seckillMapper.updateSeckillInfo(seckillInfo);
    }

    /**
     * 这里定义缓存组的类型
     * @return
     */
    @Override
    @Cacheable(value = "goodsList")
    public List<SeckillInfo> findAllSeckillInfo() {
        return seckillMapper.findAllSeckillInfo();
    }
}

```



使用spring的缓存解决方案

最后实现控制器，给前端提供接口

```java

// 不展示相应的商品，只是测试系统的性能
@Controller
public class GoodsController {

    private SeckillService seckillService;

    @Autowired
    public void setSeckillService(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    @RequestMapping(value = "/getGoodsInfo/{id}", method = RequestMethod.GET)
    @ResponseBody
    public SeckillInfo getseckillInfo(@PathVariable(value = "id")Long id) {
        return seckillService.findSeckillInfoById(id);
    }
}

```



只测试性能，不展示前端页面，先用postman看看能不能获取到缓存数据

 

 

通过postman可以看到，通过redis cluster的缓存，可以获得相应的数据返回给前端：

![img](file:///C:/Users/CHIZHO~1/AppData/Local/Temp/msohtmlclip1/01/clip_image032.png)

 

可能是数据量太少了，QPS测出来非常大，后期需要加大数据量来测试真实的QPS的值，TPS为60.7/s

![img](file:///C:/Users/CHIZHO~1/AppData/Local/Temp/msohtmlclip1/01/clip_image034.png)