# 一、安装redis

1. 使用 yum 命令安装redis数据库
```
yum install redis
```
![20180920155940.png](https://upload-images.jianshu.io/upload_images/6738270-22804d282266b90d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
2. 安装完毕后，使用下面的命令启动redis服务
```
# 启动redis
service redis start
# 停止redis
service redis stop
# 查看redis运行状态
service redis status
# 查看redis进程
ps -ef | grep redis
```
3. 设置redis为开机自动启动

```
chkconfig redis on
```
4. 进入redis服务

```
# 进入本机redis
redis-cli
# 列出所有key
keys *
```

5. 防火墙开放相应端口
```
1. 查看已打开的端口 # netstat -ntlp
查看所有打开的端口： firewall-cmd --zone=public --list-ports
2. 查看想开的端口是否已开 # firewall-cmd --query-port=6379/tcp
  若此提示 FirewallD is not running 
  表示为不可知的防火墙 需要查看状态并开启防火墙
 3. 查看防火墙状态 # systemctl status firewalld
 running 状态即防火墙已经开启
 dead 状态即防火墙未开启
 4. 开启防火墙，# systemctl start firewalld 没有任何提示即开启成功
 5. 开启防火墙 # service firewalld start  
   关闭防火墙 # systemctl stop firewalld
   centos7.3 上述方式可能无法开启，可以先#systemctl unmask firewalld.service 
然后 # systemctl start firewalld.service
 7. 开永久端口号 firewall-cmd --add-port=6379/tcp --permanent 提示 success 表示成功
 7. 开永久端口号 firewall-cmd --add-port=6380/tcp --permanent 提示 success 表示成功
 8. 重新载入配置 # firewall-cmd --reload 比如添加规则之后，需要执行此命令
 9. 若移除端口 # firewall-cmd --permanent --remove-port=6379/tcp
 10. 修改iptables 有些版本需要安装iptables-services # yum install iptables-services 然后修改进目录 /etc/sysconfig/iptables 修改内容
```
**注意：这里开放端口后，在本地使用telnet一直不通。也检查过云服务的开放端口安全策略，还是不成功。最后原来是redis的配置里面的bind 127.0.0.1 配置问题。下面有说到。**

#二、修改redis默认端口和密码

1、打开配置文件

```
vi /etc/redis.conf
```

2、修改默认端口，查找 port 6379 修改为相应端口即可

![image](http://upload-images.jianshu.io/upload_images/6738270-5ff960b9b37462a1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

3、修改默认密码，查找 requirepass foobared 将 foobared 修改为你的密码

 ![image](http://upload-images.jianshu.io/upload_images/6738270-befd8d7a04d184d2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

4、使用配置文件启动 redis
```
redis-server /etc/redis.conf &
```
5、使用端口登录
```
redis-cli -h 127.0.0.1 -p 6179
```
6、此时再输入命令则会报错

![image](http://upload-images.jianshu.io/upload_images/6738270-ad8a299bb8bd35e8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

7、输入刚才输入的密码

```
auth 111
```

![image](http://upload-images.jianshu.io/upload_images/6738270-0c519be6d9948bf9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

 8、停止redis

　　命令方式关闭redis

```
redis-cli -h 127.0.0.1 -p 6179
shutdown
```
　　进程号杀掉redis

```
ps -ef | grep redis
kill -9 XXX
```

#三、使用redis desktop manager远程连接redis

1、访问如下网址下载redis desktop manager

[下载redis desktop manager](https://redisdesktop.com/download)

2、安装后启动，新建一个连接

![image](http://upload-images.jianshu.io/upload_images/6738270-fe738cb44f8c75c5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

 3、填写如下信息后点击“Test Connection”测试是否连接成功

![image](http://upload-images.jianshu.io/upload_images/6738270-49c5f101a7b86a67.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

4、如果长时间连接不上，可能有两种可能性

　　a）bind了127.0.01：只允许在本机连接redis

　　b）protected-mode设置了yes（使用redis desktop manager工具需要配置，其余不用）

　　解决办法：

```
# 打开redis配置文件
vi /etc/redis.conf
# 找到 bind 127.0.0.1 将其注释
# 找到 protected-mode yes 将其改为
protected-mode no
```
5、重启redis

```
service redis stop
service redis start
```

6、再次连接即可

![image](http://upload-images.jianshu.io/upload_images/6738270-ccaf6b694b26ec24.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


[参考：https://www.cnblogs.com/rslai/p/8249812.html](https://www.cnblogs.com/rslai/p/8249812.html)
