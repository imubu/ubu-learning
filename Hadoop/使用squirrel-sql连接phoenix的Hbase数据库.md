## 安装squirrel-sql
1. 下载：http://squirrel-sql.sourceforge.net/
  下载成功后，文件名为：squirrel-sql-3.9.0-standard.jar
2. 安装：
  双击jar文件或者在cmd中输入命令：java -jar C:\\squirrel\squirrel-sql-3.9.0-standard.jar
  ![](https://upload-images.jianshu.io/upload_images/6738270-37a05f212f4a87ba.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
  然后就点击下一步，下一步。。。完成安装

## 配置
1. 配置host文件
  打开C:\Windows\System32\drivers\etc目录下的hosts文件，添加上相关的服务名：
```
192.168.235.3 DB-test1
192.168.235.4 DB-test2
192.168.235.5 DB-test3
192.168.235.6 DB-test4
192.168.235.7 DB-test5
192.168.235.8 DB-test6
192.168.235.10 bd-test-hadoop-235-10
192.168.235.11 bd-test-hadoop-235-11
192.168.235.12 bd-test-DataAnalysis-235-12
192.168.235.13 bd-test-DataAnalysis-235-13
192.168.243.21 bd-test-scheduling-243-21
192.168.243.22 bd-test-scheduling-243-22
```
2、配置Driver
![image.png](https://upload-images.jianshu.io/upload_images/6738270-b6c7a5456b890b29.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

在Drivers中添加一个phoenix的driver，如上图
url中可以随便填写，如：`jdbc:phoenix:org.apache.phoenix.jdbc.PhoenixDriver`
下面的driver中需要填写：`org.apache.phoenix.jdbc.PhoenixDriver`
中间需要添加上一下配置文件和jar包：
phoenix-4.13.2-cdh5.11.2-client.jar，改jar包需要选择具体到文件的路径，如图
还需要添加hbase-site.xml、core-site.xml、hdfs-site.xml三个文件。
![image.png](https://upload-images.jianshu.io/upload_images/6738270-84466c5dd7b02ada.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
配置完成后，显示一个勾，表示配置正确。

3. 配置Aliases
  ![image.png](https://upload-images.jianshu.io/upload_images/6738270-8cc701f86cb79f4b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
  在aliases中添加上一个名为phoenix235的连接，名字可以随意。
  Driver选择上述2步骤中创建的driver
  url填写你的phoenix中的连接地址：`jdbc:phoenix:DB-test4,DB-test5,DB-test6:2181`
  有用户名和密码的话，就输入用户名和密码，我这里没有就不需要输入。
  然后点击test测试看连接是否完成。
  ![image.png](https://upload-images.jianshu.io/upload_images/6738270-f53e9da78d4eb046.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
  表示连接成功。

## 使用
双击phoenix235
![image.png](https://upload-images.jianshu.io/upload_images/6738270-20dbcef3233959c5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
点击SQL
![image.png](https://upload-images.jianshu.io/upload_images/6738270-7280f4876d845311.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
输入命令`select * from Bmbi_test.demo_user`，点击运行，得到结果
也可以使用命令创建表，插入数据等操作