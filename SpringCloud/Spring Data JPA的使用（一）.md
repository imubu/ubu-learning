## 1 简介
- JPA是Java Persistence API的简称，中文名Java持久层API，是JDK5.0注解或XML描述对象－关系表的映射关系，并将运行期的实体对象持久化到数据库中。   
- Sun引入新的JPA ORM规范出于两个原因：其一，简化现有Java EE和Java SE应用开发工作；其二，Sun希望整合ORM技术，实现天下归一。  
- Spring Data JPA是更大的Spring Data家族的一部分，它使得轻松实现基于JPA的存储库变得更加容易。这个模块处理增强对基于JPA的数据访问层的支持。它使得构建使用数据访问技术的spring驱动应用程序变得更加容易。  
- 官方文档：https://docs.spring.io/spring-data/jpa/docs/2.1.0.RC1/reference/html/

## 2 Spring Data JPA 的 maven坐标

```
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-jpa</artifctId>
</dependency>
```
## 3 application.yml文件中关于JPA的配置

```
# jpa配置
# jpa.hibernate.ddl-auto.create       ----每次运行该程序，没有表格会新建表格，表内有数据会清空
# jpa.hibernate.ddl-auto.create-drop  ----每次程序结束的时候会清空表
# jpa.hibernate.ddl-auto.update       ----每次运行程序，没有表格会新建表格，表内有数据不会清空，只会更新
# jpa.hibernate.ddl-auto.validate     ----运行程序会校验数据与数据库的字段类型是否相同，不同会报错
# jpa.show-sql  ----在控制台打印SQL语句，便于开发
  jpa: 
    hibernate: 
      ddl-auto: update
    show-sql: true
```

## 4 定义表对象

```
@Entity
@Table(name = "job_state_executor_info")
public class JobStateExecutorInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", columnDefinition = "bigint comment '编号'")
	private Long id;
	
	@Column(name = "name", columnDefinition = "varchar(20) default '' comment '名称'")
	private String name;

	@Column(name = "des", columnDefinition = "text comment '描述信息'")
	private String des;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDes() {
		return des;
	}

	public void setDes(String des) {
		this.des = des;
	}

}
```
###### 注解备注：

```
@Entity                     根据对象生成表 
@Table                      对应的表名
@Id                         主建
@GeneratedValue             自增长
@Column.name                属性对应的字段
@Column.columnDefinition    字段备注
```


## 5 Repository

继承JpaRepository<T,ID>，JpaSpecificationExecutor<T>两个接口

```
public interface JobStateExecutorInfoRepository
		extends JpaRepository<JobStateExecutorInfo, Long>, 
		JpaSpecificationExecutor<JobStateExecutorInfo> {
}
```
## 6 使用JPA自带的方法操作数据库

```
@RunWith(SpringRunner.class)
@SpringBootTest
public class JobStateExecutorInfoReposotoryTest {

	@Resource
	JobStateExecutorInfoRepository jobStateExecutorInfoRepository;
	
	
	//增
	@Test
	public void inset() {
		
		JobStateExecutorInfo jobStateExecutorInfo = new JobStateExecutorInfo();
		jobStateExecutorInfo.setName("测试执行器");
		jobStateExecutorInfo.setDes("测试执行器的描述");
		jobStateExecutorInfoRepository.save(jobStateExecutorInfo);
	}
	
	//改
	@Test
	public void update() {
		
		JobStateExecutorInfo jobStateExecutorInfo = new JobStateExecutorInfo();
		jobStateExecutorInfo.setId((long)1);
		jobStateExecutorInfo.setName("修改名称");
		jobStateExecutorInfo.setDes("修改描述");
		jobStateExecutorInfoRepository.save(jobStateExecutorInfo);
	}
	
	//删
	@Test
	public void delete() {
		
		JobStateExecutorInfo jobStateExecutorInfo = new JobStateExecutorInfo();
		jobStateExecutorInfo.setId((long)1);	
		jobStateExecutorInfoRepository.delete(jobStateExecutorInfo);
	}
	
	//查
	@Test
	public void select() {
		JobStateExecutorInfo jobStateExecutorInfo =jobStateExecutorInfoRepository.findById((long)1).get();
		System.out.println(jobStateExecutorInfo.getId());
		System.out.println(jobStateExecutorInfo.getName());
		System.out.println(jobStateExecutorInfo.getDes());
	}
}
```
## 7 分页
分页需要使用Pageable

```
@Test
public void selectPage() {
	Sort sort =  new Sort(Direction.ASC, "id");
	Pageable  pageable = PageRequest.of(0, 2, sort);
	Page<JobStateExecutorInfo> pageDate = jobStateExecutorInfoRepository.findAll(pageable);
	System.out.println(pageDate.getTotalElements());
	List<JobStateExecutorInfo> content = pageDate.getContent();
	for (JobStateExecutorInfo jobStateExecutorInfo : content) {
		System.out.println(jobStateExecutorInfo.getId());
		System.out.println(jobStateExecutorInfo.getName());
		System.out.println(jobStateExecutorInfo.getDes());
	}
}
```
## 8 自定义SQL语句
在接口上使用注解，执行自定义的SQL语句  
nativeQuery=true表明使用原生SQL，默认为fasle,为fasle则执行HQL

```
@Query(value="UPDATE job_state_executor_info SET name =? WHERE id=?",nativeQuery=true)
@Modifying
void myupdate(String name,long id);
```
