## 安装mysql数据库



## 安装kafka

从官网下载Kafka安装包，解压安装，或直接使用命令下载。

wget [**http://mirror.bit.edu.cn/apache/kafka/2.1.1/kafka_2.11-2.1.1.tgz**](http://mirror.bit.edu.cn/apache/kafka/2.1.1/kafka_2.11-2.1.1.tgz) 

解压安装

> tar -zvxf kafka_2.11-2.1.1.tgz -C /usr/local/
> cd /usr/local/kafka_2.11-2.1.1/

修改配置文件

> vim config/server.properties 

修改其中

> broker.id=1
> log.dirs=data/kafka-logs

### 功能验证

#### 启动zookeeper

使用安装包中的脚本启动单节点Zookeeper实例：
bin/zookeeper-server-start.sh -daemon config/zookeeper.properties

#### 启动Kafka服务

使用kafka-server-start.sh启动kafka服务：
bin/kafka-server-start.sh config/server.properties
进程守护模式启动kafka

nohup bin/kafka-server-start.sh config/server.properties >/dev/null 2>&1 &

#### Kafka关闭命令(备注：先进入kafka目录)

bin/kafka-server-stop.sh

#### 创建Topic

使用kafka-topics.sh 创建但分区单副本的topic test
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test

#### 查看Topic

bin/kafka-topics.sh --list --zookeeper localhost:2181

#### 产生消息

使用kafka-console-producer.sh 发送消息
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test 

#### 消费消息

使用kafka-console-consumer.sh 接收消息并在终端打印
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning

列出所有的consumer

```shell
sh kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```

#### 删除Topic

bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic test

#### 查看描述 Topic 信息

bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic test
第一行给出了所有分区的摘要，每个附加行给出了关于一个分区的信息。 由于我们只有一个分区，所以只有一行。
“Leader”: 是负责给定分区的所有读取和写入的节点。 每个节点将成为分区随机选择部分的领导者。
“Replicas”: 是复制此分区日志的节点列表，无论它们是否是领导者，或者即使他们当前处于活动状态。
“Isr”: 是一组“同步”副本。这是复制品列表的子集，当前活着并被引导到领导者。



## 运行kafka connect

1. 将confluent-oss下的kafka-connect-jdbc-5.0.0.jar放到kafka的libs目录
2. 将mysql的mysql-connector-java-5.1.46.jar包放到kafka的libs目录

#### 准备工作

创建  A数据库源表comments

```
CREATE TABLE `comments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `commenttime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ;
```

timestamp-mysql-source.properties（source）

```
name=mysql-b-source-comments
connector.class=io.confluent.connect.jdbc.JdbcSourceConnector
tasks.max=1
connection.url=jdbc:mysql://192.168.23.128:3306/kafka_test?user=root&password=Root123#
table.whitelist=comments
mode=timestamp
timestamp.column.name=commenttime
topic.prefix=mysql-kafka-
```

timestamp-mysql-sink.properties（sink）

```
name=mysql-b-sink-comments
connector.class=io.confluent.connect.jdbc.JdbcSinkConnector
tasks.max=1
#kafka的topic名称
topics=mysql-kafka-comments
# 配置JDBC链接
connection.url=jdbc:mysql://192.168.243.20:9097/demo_test?user=root&password=b#12345678
# 不自动创建表，如果为true，会自动创建表，表名为topic名称
auto.create=true
# upsert model更新和插入
insert.mode=upsert
# 下面两个参数配置了以id为主键更新
pk.mode = record_value
pk.fields = id
#表名为kafkatable
table.name.format=kafkacomments
```

启动 Kafka Connect

```
bin/connect-standalone.sh config/connect-standalone.properties /data/kafka-config/timestamp-mysql-source.properties /data/kafka-config/timestamp-mysql-sink.properties  
```

A库comments表插入四条数据，此时发现B库中的kafkacomments表中会同样生成四条数据

可以直接打开消费者

```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic mysql-kafka-comments --from-beginning
```

能看到每次新增、修改数据时，都会在kafka中对应的topic里面有新的数据信息



## 使用debezium

已知kafka connect和debezium作用，会使用kafka的基础上，学会使用debezium来读取binlog，并通过kafka connect将读取的内容放入kafka topic中。 

### 准备操作
1. 登陆mysql命令：mysql -uroot -pRoot123#

2. 查看log_bin是否已开启：show variables like 'log_%';

3. 在`/etc/my.cnf`中添加下面的语句来开启binlog

  ```
  [mysqld]
  log-bin=mysql-bin #添加这一行就ok
  binlog-format=ROW #选择row模式
  server_id=1918 #配置mysql replaction需要定义，不能和canal的slaveId重复
  ```

4. 重启mysql：service mysqld restart
5. 重新查看1，2步骤

mysql需创建一个有mysql slave相关权限的账号，若mysql不在本机，则需要远程权限，防火墙放行。

```
//mysql slave相关权限
CREATE USER debezium IDENTIFIED BY 'Debezium123#';  
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'debezium'@'%';
GRANT ALL PRIVILEGES ON *.* TO 'debezium'@'%' ;
FLUSH PRIVILEGES;
```
### 操作
1. 下载Debezium的mysql连接器https://debezium.io/docs/install/并解压

2. 安装debezium

  ```
  mkdir connect //这个目录要自己建
  tar -zvxf debezium-connector-mysql-0.9.2.Final-plugin.tar.gz -C /usr/local/kafka_2.11-2.1.1/connect
  ```

3. 修改worker配置文件，即修改文件config/connect-standalone.properties，加上

  ```
  plugin.path=/usr/local/kafka_2.11-2.1.1/connect
  ```

4. 写配置文件mysql.properties,
     参考https://debezium.io/docs/connectors/mysql/的配置文件示例。

     ```
     
     name=inventory-connector
     connector.class=io.debezium.connector.mysql.MySqlConnector
     database.hostname=192.168.23.128
     database.port=3306
     database.user=debezium
     database.password=Debezium123#
     database.server.id=184054
     database.server.name=fullfillment
     database.whitelist=inventory,kafka_test
     database.history.kafka.bootstrap.servers=localhost:9092
     database.history.kafka.topic=dbhistory.fullfillment
     include.schema.changes=true
     ```

5. 以独立模式启动kafka connect，此时debezium会对数据库中的每一个表创建一个topic，消费相应的topic，即可获取binlog解析信息。

     ```
     //启动kafka connect
       bin/connect-standalone.sh config/connect-standalone.properties /data/kafka-config/mysql.properties
     ```

     

6. 查看topic列表

     ```
      bin/kafka-topics.sh --list --zookeeper localhost:2181
     ```

     看到多出了这几个topic,其中kafka_test是数据库名称，accounts、comments、person是表名称

     dbhistory.fullfillment
     fullfillment
     fullfillment.kafka_test.accounts
     fullfillment.kafka_test.comments
     fullfillment.kafka_test.person

7. 查看消费者

     ```
     bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic fullfillment.kafka_test.accounts --from-beginning
     ```

     对表进行增删改的时候，该topic都会多出一条数据

     修改表结构的时候，信息都是在fullfillment中


## 将采集到的数据入库

#### 拓扑图

总体的拓扑图如下所示:

![Scenario topology](https://debezium.io/images/dbz-to-jdbc.svg)

Figure 1: A General topology

 

为了简化设置，我们将只使用一个Kafka Connect实例包含所有的连接器。即此Kafka Connect实例将充当事件的生产者和事件的消费者:

 

![Scenario topology](https://debezium.io/images/dbz-to-jdbc-simplified.svg)

Figure 2: A Simplified topology

---------------------

想要找到更多的原理内容，请点击[这里](https://debezium.io/blog/2017/09/25/streaming-to-another-database/)

### 实现操作

1. 在接收配置文件中添加transforms，将debezium生成的详细信息转换成简单信息，mysql-sink.properties文件如下：

   ```
   name=mysql-sink-comments
   connector.class=io.confluent.connect.jdbc.JdbcSinkConnector
   tasks.max=1
   #kafka的topic名称
   topics=accounts
   # 配置JDBC链接
   connection.url=jdbc:mysql://192.168.243.20:9097/demo_test?user=root&password=b#12345678    
   transforms=unwrap
   transforms.unwrap.type=io.debezium.transforms.UnwrapFromEnvelope
   # 不自动创建表，如果为true，会自动创建表，表名为topic名称
   auto.create=true
   # upsert model更新和插入
   insert.mode=upsert
   # 下面两个参数配置了以id为主键更新
   pk.mode = record_value
   pk.fields = id
   pk.mode=record_value
   ```

2. 修改源配置文件mysql.properties，添加transforms，将kafka的topic名称由`<logical-name>.<database-name>.<table-name>`转成`<table-name>`,新的mysql.properties如下

   ```
   name=inventory-connector
   connector.class=io.debezium.connector.mysql.MySqlConnector
   database.hostname=192.168.23.128
   database.port=3306
   database.user=debezium
   database.password=Debezium123#
   database.server.id=184054
   database.server.name=fullfillment
   database.whitelist=inventory,kafka_test
   database.history.kafka.bootstrap.servers=localhost:9092
   database.history.kafka.topic=dbhistory.fullfillment
   transforms=route
   transforms.route.type=org.apache.kafka.connect.transforms.RegexRouter
   transforms.route.regex=([^.]+)\\.([^.]+)\\.([^.]+)
   transforms.route.replacement=$3
   ```

3. 将debezium下的debezium-core-0.9.2.Final.jar包放到kafka的libs目录下面

4. 启动kafka connect

   ```
   bin/connect-standalone.sh config/connect-standalone.properties /data/kafka-config/mysql.properties /data/kafka-config/mysql-sink.properties
   ```

5. 查看topic列表

   ```
    bin/kafka-topics.sh --list --zookeeper localhost:2181
   ```

   看到的topic，其实accounts就相当于fullfillment.kafka_test.accounts，只是名字改变了而已

   dbhistory.fullfillment
   accounts
   fullfillment
   fullfillment.kafka_test.accounts

6. 查看消费者

   ```
   bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic accounts --from-beginning
   ```

   对表进行增删改的时候，该topic都会多出一条数据，但是因为Confluent的JDBC并不支持删除数据，所以删除记录都会被过滤掉，到目标数据库的只有新增和修改数据。

## kafka集群

#### 配置和安装zookeeper

下载Zookeeper

首先创建Zookeeper项目目录

```
mkdir /data/zookeeper     #项目目录
mkdir  /data/zookeeper/zkdata          #存放快照日志
mkdir  /data/zookeeper/zkdatalog     #存放事物日志
```

下载,解压软件

```
cd /data/zookeeper
wget http://mirror.bit.edu.cn/apache/zookeeper/zookeeper-3.4.13/zookeeper-3.4.13.tar.gz
tar -zxvf zookeeper-3.4.13.tar.gz
mv zookeeper-3.4.13 /usr/local/zookeeper
```

**3、修改配置文件**

进入到解压好的目录里面的conf目录中，查看

```
ll /usr/local/zookeeper/conf
#查看
-rw-rw-r-- 1 1000 1000  535 Mar 27 12:32 configuration.xsl
-rw-rw-r-- 1 1000 1000 2161 Mar 27 12:32 log4j.properties
-rw-rw-r-- 1 1000 1000  922 Mar 27 12:32 zoo_sample.cfg
```

\#zoo_sample.cfg 这个文件是官方给我们的zookeeper的样板文件，给他复制一份命名为zoo.cfg，zoo.cfg是官方指定的文件命名规则。

```
cd /usr/local/zookeeper/conf
cp zoo_sample.cfg zoo.cfg
```

3台服务器的配置文件

```
# vim zoo.cfg

tickTime=2000
initLimit=10
syncLimit=5
dataDir=/data/zookeeper/zkdata
dataLogDir=/data/zookeeper/zkdatalog
clientPort=12181
server.1=192.168.30.204:12888:13888
server.2=192.168.30.205:12888:13888
server.3=192.168.30.206:12888:13888
```

 **Zookeeper配置文件解释**
```
#tickTime：
这个时间是作为 Zookeeper 服务器之间或客户端与服务器之间维持心跳的时间间隔，也就是每个 tickTime 时间就会发送一个心跳。
#initLimit：
这个配置项是用来配置 Zookeeper 接受客户端（这里所说的客户端不是用户连接 Zookeeper 服务器的客户端，而是 Zookeeper 服务器集群中连接到 Leader 的 Follower 服务器）初始化连接时最长能忍受多少个心跳时间间隔数。当已经超过 5个心跳的时间（也就是 tickTime）长度后 Zookeeper 服务器还没有收到客户端的返回信息，那么表明这个客户端连接失败。总的时间长度就是 5*2000=10 秒
#syncLimit：
这个配置项标识 Leader 与Follower 之间发送消息，请求和应答时间长度，最长不能超过多少个 tickTime 的时间长度，总的时间长度就是5*2000=10秒
#dataDir：
快照日志的存储路径
#dataLogDir：
事物日志的存储路径，如果不配置这个那么事物日志会默认存储到dataDir制定的目录，这样会严重影响zk的性能，当zk吞吐量较大的时候，产生的事物日志、快照日志太多
#clientPort：
这个端口就是客户端连接 Zookeeper 服务器的端口，Zookeeper 会监听这个端口，接受客户端的访问请求。修改他的端口改大点
#server.1 这个1是服务器的标识也可以是其他的数字， 表示这个是第几号服务器，用来标识服务器，这个标识要写到快照目录下面myid文件里
#192.168.7.107为集群里的IP地址，第一个端口是master和slave之间的通信端口，默认是2888，第二个端口是leader选举的端口，集群刚启动的时候选举或者leader挂掉之后进行新的选举的端口默认是3888
```

**1、三台服务器上分别创建myid文件**

```
#server1（192.168.23.128）
echo "1" > /data/zookeeper/zkdata/myid
#server2（192.168.23.133）
echo "2" > /data/zookeeper/zkdata/myid
#server3（192.168.30.206）
echo "3" > /data/zookeeper/zkdata/myid
```

**2、重要配置说明**

2-1、myid文件和server.myid 在快照目录下存放的标识本台服务器的文件，他是整个zk集群用来发现彼此的一个重要标识。
2-2、zoo.cfg 文件是zookeeper配置文件 在conf目录里。
2-3、log4j.properties文件是zk的日志输出文件 在conf目录里用java写的程序基本上有个共同点日志都用log4j，来进行管理。

2-4、zkEnv.sh和zkServer.sh文件

```
# ll /usr/local/zookeeper/bin/
zkServer.sh 主的管理程序文件
zkEnv.sh 是主要配置，zookeeper集群启动时配置环境变量的文件
```

**3、zookeeper定期清理快照和日志文件**
zookeeper不会主动的清除旧的快照和日志文件，这个是操作者的责任。但是可以通过命令去定期的清理。

> 从3.4.0开始，zookeeper提供了自动清理snapshot和事务日志的功能，通过配置 autopurge.snapRetainCount 和 autopurge.purgeInterval 这两个参数能够实现定时清理了。这两个参数都是在zoo.cfg中配置的：
>
> autopurge.purgeInterval 这个参数指定了清理频率，单位是小时，需要填写一个1或更大的整数，默认是0，表示不开启自己清理功能。
> autopurge.snapRetainCount 这个参数和上面的参数搭配使用，这个参数指定了需要保留的文件数目。默认是保留3个。

**4、配置zookeeper的环境变量**

```
# vim /etc/profile
export ZOOKEEPER_HOME=/usr/local/zookeeper
export PATH=$PATH:$ZOOKEEPER_HOME/bin

# source /etc/profile
```

5、同步到其他服务器

```
scp -r /usr/local/zookeeper root@192.168.23.133:/usr/local/
scp -r /data/zookeeper root@192.168.23.133:/data/
#修改文件的所有者
chown -R vicente:vicente /data/zookeeper
#修改myid为2，配置环境变量
```

#### 额外配置

```
hostnamectl set-hostname centos102
vim /etc/hosts
192.168.23.128 centos101
192.168.23.133 centos102
192.168.23.134 centos103
vim zoo.cfg
server.1=centos101:12888:13888
server.2=centos102:12888:13888
server.3=centos103:12888:13888
#开放端口
[root@centos103 conf]# firewall-cmd --query-port=12181/tcp
[root@centos103 conf]# firewall-cmd --add-port=12181/tcp --permanent
[root@centos103 conf]# firewall-cmd --add-port=12888/tcp --permanent
[root@centos103 conf]# firewall-cmd --add-port=13888/tcp --permanent
[root@centos103 conf]# firewall-cmd --reload
#重启
./zkServer.sh restart
```



#### 启动Zookeeper服务并查看

```
#进入bin目录
cd /usr/local/zookeeper/bin/

#启动服务（3台都需要操作）
zkServer.sh start

#检查服务器状态
zkServer.sh status
----------------------------------------------------------------------------------------------------------
ZooKeeper JMX enabled by default
Using config: /data/zookeeper/zookeeper-3.4.12/bin/../conf/zoo.cfg   #配置文件
Mode: leader    #他是否为领导

#zk集群一般只有一个leader，多个follower，主一般是相应客户端的读写请求，而从主同步数据，当主挂掉之后就会从follower里投票选举一个leader出来。
```

可以用“jps”查看zk的进程， QuorumPeerMain 是 zookeeper 进程

```
#执行命令jps
1744 Jps
1674 QuorumPeerMain
```

\#连接客户端,使用 ls 命令来查看当前 ZooKeeper 中所包含的内容
运行Java版本的客户端使用bash zkCli.sh -server IP:port ，运行C语言版本的使用./cli_mt IP:port，下面介绍Java版本的，C语言版差不多。

```
./zkCli.sh -server 127.0.0.1:12181

-----
................................................
[zk: 127.0.0.1:12181(CONNECTED) 0] ls /
[zookeeper]
[zk: 127.0.0.1:12181(CONNECTED) 1] quit
```

\#配置 zookeeper 开机启动

```
echo '/usr/local/zookeeper/bin/zkServer.sh start' >>/etc/rc.local 
```

# Kafka集群搭建

1、安装好kafka

**3、修改配置文件**
进入到config目录
`ll /usr/local/kafka/config/`
主要关注：server.properties 这个文件即可，我们可以发现在目录下：

有很多文件，这里可以发现有Zookeeper文件，我们可以根据Kafka内带的zk集群来启动，但是建议使用独立的zk集群

```
-rw-r--r-- 1 root root 5807 Feb 22 06:26 connect-distributed.properties
-rw-r--r-- 1 root root 2730 Feb 22 06:26 connect-standalone.properties
-rw-r--r-- 1 root root 6852 Feb 22 06:26 server.properties
-rw-r--r-- 1 root root 1023 Feb 22 06:26 zookeeper.properties
```

# Kafka配置参数解释

```
# cat /usr/local/kafka/config/server.properties
----------------------------------------------------------------------------------------------------------------------------------
broker.id=0  #当前机器在集群中的唯一标识，和zookeeper的myid性质一样,每台服务器的broker.id都不能相同
port=19092 #当前kafka对外提供服务的端口默认是9092
host.name=192.168.30.204 #这个参数默认是关闭的，在0.8.1有个bug，DNS解析问题，失败率的问题。
num.network.threads=3 #这个是borker进行网络处理的线程数
num.io.threads=8 #这个是borker进行I/O处理的线程数
log.dirs=/data/kafka/kafkalogs/ #消息存放的目录，这个目录可以配置为“，”逗号分割的表达式，上面的num.io.threads要大于这个目录的个数，如果配置多个目录，新创建的topic将消息持久化的地方是，当前以逗号分割的目录中，哪个分区数最少就放那一个
socket.send.buffer.bytes=102400 #发送缓冲区buffer大小，数据不是一下子就发送的，会先存储到缓冲区，到达一定的大小后在发送，能提高性能
socket.receive.buffer.bytes=102400 #kafka接收缓冲区大小，当数据到达一定大小后在序列化到磁盘
socket.request.max.bytes=104857600 #这个参数是向kafka请求消息或者向kafka发送消息的请求的最大数，这个值不能超过java的堆栈大小
num.partitions=1 #默认的分区数，一个topic默认1个分区数
log.retention.hours=168 #默认消息的最大持久化时间，168小时，7天
message.max.byte=5242880  #消息保存的最大值5M
default.replication.factor=2  #kafka保存消息的副本数，如果一个副本失效了，另一个还可以继续提供服务
replica.fetch.max.bytes=5242880  #取消息的最大直接数
log.segment.bytes=1073741824 #这个参数是：因为kafka的消息是以追加的形式落地到文件，当超过这个值的时候，kafka会新起一个文件
log.retention.check.interval.ms=300000 #每隔300000毫秒去检查上面配置的log失效时间（log.retention.hours=168 ），到目录查看是否有过期的消息如果有，删除
log.cleaner.enable=false #是否启用log压缩，一般不用启用，启用的话可以提高性能
zookeeper.connect=192.168.30.204:12181,192.168.30.205:12181,192.168.30.206:12181 #设置zookeeper的连接端口
```

上面是参数的解释，实际的修改项为：

```
broker.id=0  每台服务器的broker.id都不能相同

listeners=PLAINTEXT://:9092 kafka实例broker监听默认端口9092

#hostname
host.name=centos101

#在log.retention.hours=168 下面新增下面三项
message.max.byte=5242880
default.replication.factor=2
replica.fetch.max.bytes=5242880

#设置zookeeper的连接端口
zookeeper.connect=centos101:12181,centos102:12181,centos103:12181
```

# 启动Kafka集群并测试

**1、配置Kafka的环境变量**

```
# vim /etc/profile
export KAFKA_HOME=/usr/local/kafka
export PATH=$PATH:$KAFKA_HOME/bin

# source /etc/profile
```

**2、启动Kafka服务**

```
#从后台启动Kafka集群（3台都需要启动）
bin/kafka-server-start.sh -daemon config/server.properties

# 官方推荐启动方式：
bin/kafka-server-start.sh config/server.properties &
```

**3、验证服务是否启动**

```
#执行命令jps
4289 Jps
4216 Kafka
1674 QuorumPeerMain

#看到Kafka的进程，说明Kafka已经启动
```

# 验证Kafka

**1、创建topic**

```
#创建Topic
kafka-topics.sh --create --zookeeper centos101:12181,centos102:12181,centos103:12181 --partitions 3 --replication-factor 3 --topic qsh
#解释
--partitions 3   #创建3个分区
--replication-factor 3     #复制3份
--topic     #主题为qsh

#查看topic状态
kafka-topics.sh --describe --zookeeper localhost:12181 --topic qsh

#下面是显示信息
Topic:qsh   PartitionCount:3    ReplicationFactor:3 Configs:
       Topic: qsh   Partition: 0    Leader: 1   Replicas: 1,2,3 Isr: 1,2,3
         Topic: qsh Partition: 1    Leader: 2   Replicas: 2,3,1 Isr: 2,3,1
       Topic: qsh   Partition: 2    Leader: 3   Replicas: 3,1,2 Isr: 3,1,2

状态说明：
#qsh有三个分区分别为1、2、3;
#分区0的leader是1（broker.id），分区0有三个副本，并且状态都为lsr（ln-sync，表示可以参加选举成为leader）。

#删除topic
    在config/server.properties中加入delete.topic.enable=true并重启服务，在执行如下命令
# kafka-topics.sh --delete --zookeeper localhost:12181 --topic qsh
```

**2、测试使用Kafka**

```
kafka-topics.sh --list --zookeeper localhost:12181
#在一台服务器上创建一个发布者-发送消息
kafka-console-producer.sh --broker-list centos101:9092 --topic qsh
输入以下信息：
　　This is a message
　　This is another message

#在另一台服务器上创建一个订阅者接收消息
kafka-console-consumer.sh --bootstrap-server centos101:9092 --topic qsh --from-beginning

#--from-beginning 表示从开始第一个消息开始接收
#测试（订阅者那里能正常收到发布者发布的消息，则说明已经搭建成功）
```

**3、其他命令**

更多请看官方文档：<http://kafka.apache.org/documentation.html>

```
#查看topic
kafka-topics.sh --list --zookeeper localhost:12181

#就会显示我们创建的所有topic
```

**4、日志说明**

默认kafka的日志是保存在/usr/local/kafka/logs/目录下的，这里说几个需要注意的日志

```
server.log     #kafka的运行日志
state-change.log    #kafka是用zookeeper来保存状态，所以他可能会进行切换，切换的日志就保存在这里
controller.log     #kafka选择一个节点作为“controller”,当发现有节点down掉的时候它负责在有用分区的所有节点中选择新的leader,这使得Kafka可以批量的高效的管理所有分区节点的主从关系。如果controller down掉了，活着的节点中的一个会备切换为新的controller.
```

**5、登录zk查看目录情况**

```
#使用客户端进入zk
zkCli.sh -server 127.0.0.1:12181    #默认是不用加’-server‘参数的因为我们修改了他的端口

#查看目录情况 执行“ls /”
[zk: 127.0.0.1:12181(CONNECTED) 0] ls /
---------------------------------------------------------------------------------------------------------------------------------------
#显示结果：
[cluster, controller, controller_epoch, brokers, zookeeper, admin, isr_change_notification, consumers, log_dir_event_notification, latest_producer_id_block, config]
'''
上面的显示结果中：只有zookeeper是zookeeper原生的，其他都是Kafka创建的
'''

#标注一个重要的
[zk: 127.0.0.1:12181(CONNECTED) 1] get /brokers/ids/1
---------------------------------------------------------------------------------------------------------------------------------------
{"listener_security_protocol_map":{"PLAINTEXT":"PLAINTEXT"},"endpoints":["PLAINTEXT://192.168.30.204:19092"],"jmx_port":-1,"host":"192.168.30.204","timestamp":"1525489051752","port":19092,"version":4}

#还有一个是查看partion
[zk: 127.0.0.1:12181(CONNECTED) 7] get /brokers/topics/qsh/partitions/1

```

[详情请见](https://blog.51cto.com/qiangsh/2112675?source=drt)

## 配置Connector

接下来要对Connector进行配置，此时可以回顾一下 [Kafka Connect 基本概念](http://www.tracefact.net/tech/086.html) 。Connector是一组独立的集群，并且是作为Kafka集群的客户端，我们首先需要对Connector进行配置，配置文件位于 $KAFKA_HOME/config/connect-distributed.properties：

```
# kafka集群地址
bootstrap.servers=kafka1:9092,kafka2:9092,kafka3:9092

# Connector集群的名称，同一集群内的Connector需要保持此group.id一致
group.id=connect-cluster

# 存储到kafka的数据格式
key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=false
value.converter.schemas.enable=false

# 内部转换器的格式，针对offsets、config和status，一般不需要修改
internal.key.converter=org.apache.kafka.connect.json.JsonConverter
internal.value.converter=org.apache.kafka.connect.json.JsonConverter
internal.key.converter.schemas.enable=false
internal.value.converter.schemas.enable=false

# 用于保存offsets的topic，应该有多个partitions，并且拥有副本(replication)
# Kafka Connect会自动创建这个topic，但是你可以根据需要自行创建
# 如果kafka单机运行，replication.factor设置为1；当kafka为集群时，可以设置不大于集群中主机数
# 因为我这里的环境是3主机的集群，因此设为2
offset.storage.topic=connect-offsets
offset.storage.replication.factor=2
offset.storage.partitions=12

# 保存connector和task的配置，应该只有1个partition，并且有多个副本
config.storage.topic=connect-configs
config.storage.replication.factor=2

# 用于保存状态，可以拥有多个partition和replication
status.storage.topic=connect-status
status.storage.replication.factor=2
status.storage.partitions=6

# Flush much faster than normal, which is useful for testing/debugging
offset.flush.interval.ms=10000

# RESET主机名，默认为本机
#rest.host.name=
# REST端口号
rest.port=18083

# The Hostname & Port that will be given out to other workers to connect to i.e. URLs that are routable from other servers.
#rest.advertised.host.name=
#rest.advertised.port=

# 保存connectors的路径
# plugin.path=/usr/local/share/java,/usr/local/share/kafka/plugins,/opt/connectors,
plugin.path=/opt/kafka/kafka_2.11-1.1.0/connectors
```

注意到connect-distributed.properties中的distributed。Kafka Connector有两种运行模式，单机（Standalone）和分布式（Distrubited）。因为单机通常作为测试运行，因此这篇文章只演示分布式运行模式。在config文件夹下，还有一个单机运行的配置文件，叫做connect-standalone.properties，内容大同小异。

只需要修改以下内容即可：

```
bootstrap.servers=contos101:9092,contos102:9092,contos103:9092
rest.port=18083
plugin.path=/usr/local/kafka_2.11-2.1.1/connect
```

同步到其他服务器

```
scp /usr/local/kafka_2.11-2.1.1/config/connect-distributed.properties root@192.168.23.128:/usr/local/kafka_2.11-2.1.1/config/
scp /usr/local/kafka_2.11-2.1.1/config/connect-distributed.properties root@192.168.23.133:/usr/local/kafka_2.11-2.1.1/config/
```



## 创建Topic

尽管首次运行Kafka connector时，会自动创建上面的topic，但是如果创建出错，那么Connector就会启动失败。保险起见，可以在运行Connector之前，手动创建好上面的三个特殊topic。

```
# bin/kafka-topics.sh --zookeeper zookeeper1:2181/kafka --create --topic connect-offsets --replication-factor 2 --partitions 12

# bin/kafka-topics.sh --zookeeper zookeeper1:2181/kafka --create --topic connect-configs --replication-factor 2 --partitions 1

# bin/kafka-topics.sh --zookeeper zookeeper1:2181/kafka --create --topic connect-status --replication-factor 2 --partitions 6
```

## 运行Connector

接下来就可以运行Connctor了，此时还没有涉及到任何业务或者数据库相关的配置和操作（即 [Kafka Connect 基本概念](http://www.tracefact.net/tech/086.html) 中提到的用户配置）。

执行下面的代码以运行Connector：

```
# bin/connect-distributed.sh config/connect-distributed.properties
```

上面这样是前台运行，当退出shell后进程也就结束了，前台运行的好处就是在开始运行时便于调试。如果想要后台运行，则需加上-daemon选项：

```
# bin/connect-distributed.sh -daemon config/connect-distributed.properties
```

运行connect时，会看到不停地涌现大量INFO信息，此时可以修改一下connect-log4j.properties，只显示WARN信息。

```
# vim config/connect-log4j.properties
log4j.rootLogger=WARN, stdout
```

## Kafka Connector REST API

当Kafka Connector运行起来以后，它就开启了REST API端口，像我们上面配置的是：18083。如果我们需要运行Task，比如实时捕捉数据库数据变化并写入Kafka，那么就需要像这个REST API提交用户配置（User Config）。在提交用户配置之前，我们先看看Kafka Connector REST API都包含哪些常见功能：

### 获取Worker的信息

因为我的kafka（主机名分别为kafka1、kafka2、kafka3）和kafka connector集群是共用主机的，因此可以使用下面的命令获取（你需要将下面的kafka1改成ip或者相应的主机名）：

```
# curl -s centos101:18083/
{
  "version": "1.1.0",
  "commit": "fdcf75ea326b8e07",
  "kafka_cluster_id": "N93UISCxTS-SYZPfM8p1sQ"
}
```

### 获取Worker上已经安装的Connector

此时的Connector是静态概念，即上面第一节安装的Confluent MSSQL Connector，从下面的显示可以看到，我安装了好几个Connector：

```
# curl -s centos101:18083/connector-plugins
[
  {
    "class": "org.apache.kafka.connect.file.FileStreamSinkConnector",
    "type": "sink",
    "version": "1.1.0"
  },
  {
    "class": "org.apache.kafka.connect.file.FileStreamSourceConnector",
    "type": "source",
    "version": "1.1.0"
  }
]
```

对于你来说，可能就只有io.confluent.connect.cdc.mssql.MsSqlSourceConnector这一个connector。

### 列出当前运行的connector(task)

```
# curl -s localhost:18083/connectors 
[]
```

因为我们当前Connector中没有提交过任何的用户配置（即没有启动Task），因此上面返回空数组。

### 提交Connector用户配置

当提交用户配置时，就会启动一个Connector Task，Connector Task执行实际的作业。用户配置是一个 [Json](http://www.codercto.com/category/json.html) 文件，同样通过REST API提交：

```
curl -s -X POST -H "Content-Type: application/json"  http://localhost:18083/connectors -d @debezium-mysql-source.json
```

然后debezium-mysql-source.json如下：

```
{
  "name": "inventory-connector",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "database.hostname": "192.168.23.128",
    "database.port": "3306",
    "database.user": "debezium",
    "database.password": "Debezium123#",
    "database.server.id": "184054",
    "database.server.name": "fullfillment",
    "database.whitelist": "kafka_test",
    "database.history.kafka.bootstrap.servers": "centos101:9092,centos102:9092,centos103:9092",
    "database.history.kafka.topic": "dbhistory.fullfillment",
    "transforms": "route",                                                       
	"transforms.route.type": "org.apache.kafka.connect.transforms.RegexRouter",
	"transforms.route.regex": "([^.]+)\\.([^.]+)\\.([^.]+)",               
	"transforms.route.replacement": "$3"     
  }
}
```

**注意：不能在数据库所在的服务器运行，否则连接不上数据库。报错：NoRouteToHostException: No route to host**

提交完成后，再次执行上一小节的命令，会看到已经有一个connector在运行了：

```
curl -s localhost:18083/connectors 
[
  "inventory-connector"
]
```

入库的sink部分，同样通过REST API提交：

```
curl -s -X POST -H "Content-Type: application/json"  http://localhost:18083/connectors -d @mysql-sink-accounts.json
```

然后mysql-sink-accounts.json如下：

```
{
    "name": "mysql-sink-accounts",
    "config": {
        "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
        "tasks.max": "1",
        "topics": "accounts",
        "connection.url": "jdbc:mysql://192.168.243.20:9097/demo_test?user=root&password=b#12345678",
        "transforms": "unwrap",                                                  
        "transforms.unwrap.type": "io.debezium.transforms.UnwrapFromEnvelope",   
        "auto.create": "true",                                                   
        "insert.mode": "upsert",                                                 
        "pk.fields": "id",                                                       
        "pk.mode": "record_value"                                                
    }
}
```

### 查看connector的信息

```
# curl -s localhost:18083/connectors/inventory-connector
```

上面task:0，不是说有0个task，是task的id是0。

### 查看connector下运行的task信息

使用下面的命令，可以查看connector下运行的task的信息：

```
# curl -s localhost:18083/connectors/inventory-connector/tasks 
```

这里task的配置信息继承自connector的配置。

### 查看connector当前状态

```
# curl -s localhost:18083/connectors/inventory-connector/status
{
    "name":"inventory-connector",
    "connector":{
        "state":"RUNNING",
        "worker_id":"192.168.23.133:18083"
    },
    "tasks":[
        {
            "id":0,
            "state":"RUNNING",
            "worker_id":"192.168.23.134:18083"
        }
    ],
    "type":"source"
}
```

### 暂停/重启 Connector

```
# curl -s -X PUT localhost:18083/connectors/inventory-connector/pause
# curl -s -X PUT localhost:18083/connectors/inventory-connector/resume
```

### 删除 Connector

```
# curl -s -X DELETE localhost:18083/connectors/inventory-connector
```

## 从Kafka中读取变动数据

默认情况下，MYSQL Connector会将表的变动写入到：${databaseName}.${tableName} 这个topic中，这个topic的名称可以通过 topic.format 这个用户配置参数中进行设置，因为我们并没有配置，因此，topic的名称为fullfillment.kafka_test.accounts。

查看所有的topic有哪些

```
bin/kafka-topics.sh --list --zookeeper centos101:12181
```

运行下面的控制台脚本，从Kafka中实时读取topic的内容：

```
# bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic fullfillment.kafka_test.accounts --from-beginning
```

此时因为没有任何数据，因此控制台会阻塞。

## 对accounts表进行修改

依次执行下面的增删改语句，对accounts表进行修改：

```
insert into accounts(name)
values('gutianle')

update accounts Set name='xuexiao' where name='gutianle'
Delete FROM accounts Where name='xuexiao'
```

现在查看Kafka读取端控制台，可以看到以Json格式实时收到了数据库变动的消息：

```
# bin/kafka-console-consumer.sh --bootstrap-server kafka1:9092 --topic fullfillment.kafka_test.accounts
```

至此，我们就配置完了Kafka Connector，并且实时获取到了数据库变更的消息。后续可以使用Spark Stream连接至此Topic，进行实时的数据运算和分析。

想看更多信息，请点击[这里](https://www.codercto.com/a/34753.html)

同一个group.id的消费者，只有一个才能消费，消费后余下的就不会接收到消息。不同group.id可以接收到同一条消息，可如下验证：

```
kafka-console-consumer.sh --bootstrap-server kafka1:9092 --topic accounts --group wodegroupid
```

####  connector配置

注意如下配置：

1. "name"：连接器任务名称
2. “connector.class”：连接器实现类
3. “connection.url”：连接数据源url
4. “tasks.max”：连接任务最大数
5. "database.server.id"，对应Mysql中的server-id的配置。
6. "database.whitelist" : 待同步的Mysql数据库名。
7. "table.whitlelist" :待同步的Mysq表名。
8. 重要：“database.history.kafka.topic”：存储数据库的Shcema的记录信息，而非写入数据的topic、
9. "database.server.name":逻辑名称，每个connector确保唯一，作为写入数据的kafka topic的前缀名称。

#### REST API

由于Kafka Connect的目的是作为一个服务运行，提供了一个用于管理connector的REST API。默认情况下，此服务的端口是`8083`。以下是当前支持的终端入口：

GET /connectors - 查询所有connectors
POST /connectors - 提交一个connector。比如是JSON格式，例子：

```
{
 "name": "jdbc-sink",
 "config": {
   "name" : "jdbc-sink",
   "connector.class" : "com.cimc.maxwell.sink.MySqlSinkConnector",
   "tasks.max": 1,
   "topics": "servername.databasename.tablename",
   }
 }
```

GET /connectors/{name} - 查询指定connector信息的
GET /connectors/{name}/config - 查询指定connector配置的
PUT /connectors/{name}/config - 更新指定connector配置的
GET /connectors/{name}/status - 查询指定connector状态的
GET /connectors/{name}/tasks - 查询指定connector的所有tasks
GET /connectors/{name}/tasks/{taskid}/status - 查询指定connector的指定task的状态的，taskid一般是0，1，2之类
PUT /connectors/{name}/pause - 暂停指定connector的，慎用，比如因为系统更新升级，想停掉source connector拉取消息
PUT /connectors/{name}/resume - 恢复上面暂停的connector的
POST /connectors/{name}/restart - 重启一个connector（connector因为一些原因挂掉了，比如被强行杀死，一般不是异常造成）
POST /connectors/{name}/tasks/{taskId}/restart - 重启一个指定的task的
DELETE /connectors/{name} - 删除一个connector
GET /connector-plugins - 获取所有已安装的connector插件
PUT /connector-plugins/{connector-type}/config/validate - 校验connector的配置的属性类型。



### 常用命令

#### zookeeper启动

#启动命令

```
/usr/local/zookeeper/bin/zkServer.sh start
```

#### kafka启动

#启动命令

```
nohup bin/kafka-server-start.sh -daemon config/server.properties >/dev/null 2>&1 &

#从后台启动Kafka集群（3台都需要启动）
bin/kafka-server-start.sh -daemon config/server.properties

# 官方推荐启动方式：
bin/kafka-server-start.sh config/server.properties &
```

#### 分布式模式启动kafka connect

启动connect

```
bin/connect-distributed.sh -daemon config/connect-distributed.properties
```

####  kafka 查看主题列表

```
bin/kafka-topics.sh --list --zookeeper localhost:12181
```



#### kafka消费主题消息

```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic fullfillment.kafka_test.accounts
```



#### kafka删除主题

```
bin/kafka-topics.sh --delete --zookeeper localhost:12181 --topic fullfillment.kafka_test.accounts
```



####  删除主题数据

如果想保留主题，只删除主题现有数据（log）。可以通过修改数据保留时间实现

```
bin/kafka-configs.sh --zookeeper localhost:12181 --entity-type topics --entity-name fullfillment.kafka_test.accounts --alter --add-config retention.ms=3000
```

#### REST API

查看已有连接

```
curl -i -H "Accept:application/json" localhost:8083/connectors
```

根据name删除 connector 

```
curl -i -X DELETE http://localhost:8083/connectors/debezium-mysql-source-3306
```

当提交用户配置时，就会启动一个Connector Task，Connector Task执行实际的作业。用户配置是一个 [Json](http://www.codercto.com/category/json.html) 文件，同样通过REST API提交：

```
curl -s -X POST -H "Content-Type: application/json"  http://localhost:18083/connectors -d @debezium-mysql-source.json
```



**参考文章**

[kafka Quickstart](http://kafka.apache.org/quickstart)

[Streaming data to a downstream database](https://debezium.io/blog/2017/09/25/streaming-to-another-database/)

[Debezium Connector for MySQL](https://debezium.io/docs/connectors/mysql/)

[Kafka Connect 实时读取MSSQL数据到Kafka](https://www.codercto.com/a/34753.html)