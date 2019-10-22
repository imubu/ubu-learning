## 9 条件查询
**ExampleMatcher实例查询三要素**  

实体对象：在ORM框架中与Table对应的域对象，一个对象代表数据库表中的一条记录，如上例中User对象，对应user表。在构建查询条件时，一个实体对象代表的是查询条件中的“数值”部分。如：要查询姓“X”的客户，实体对象只需要存储条件值“X”。  

匹配器：ExampleMatcher对象，它是匹配“实体对象”的，表示了如何使用“实体对象”中的“值”进行查询，它代表的是“查询方式”，解释了如何去查的问题。如：要查询姓“X”的客户，即姓名以“X”开头的客户，该对象就表示了“以某某开头的”这个查询方式，如上例中:withMatcher(“userName”, GenericPropertyMatchers.startsWith())  

实例：即Example对象，代表的是完整的查询条件。由实体对象（查询条件值）和匹配器（查询方式）共同创建。最终根据实例来findAll即可。

```
	public Page<JobStateTaskgroupInfo> pageList(PageInfo pageInfo) {
		Sort sort = new Sort(Direction.ASC, "id");
		Pageable pageable = PageRequest.of(pageInfo.getNumber(), pageInfo.getSize(), sort);
		
		//创建查询条件数据对象
		JobStateTaskgroupInfo jobStateTaskgroupInfo = new JobStateTaskgroupInfo();
		jobStateTaskgroupInfo.setName(pageInfo.getParam());
		jobStateTaskgroupInfo.setState(1);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() 
                .withMatcher("name", GenericPropertyMatchers.contains())
                .withMatcher("state", GenericPropertyMatchers.exact());
        
        //创建实例
        Example<JobStateTaskgroupInfo> example = Example.of(jobStateTaskgroupInfo, matcher); 
		
		return jobStateTaskgroupInfoRepository.findAll(example,pageable);
	}
```
**Matching生成的语句说明**  

DEFAULT (case-sensitive) firstname = ?0 默认（大小写敏感）  
DEFAULT (case-insensitive) LOWER(firstname) = LOWER(?0) 默认（忽略大小写）  
EXACT (case-sensitive) firstname = ?0 精确匹配（大小写敏感）  
EXACT (case-insensitive) LOWER(firstname) = LOWER(?0) 精确匹配（忽略大小写）  
STARTING (case-sensitive) firstname like ?0 + ‘%’ 前缀匹配（大小写敏感）  
STARTING (case-insensitive) LOWER(firstname) like LOWER(?0) + ‘%’ 前缀匹配（忽略大小写）  
ENDING (case-sensitive) firstname like ‘%’ + ?0 后缀匹配（大小写敏感）  
ENDING (case-insensitive) LOWER(firstname) like ‘%’ + LOWER(?0) 后缀匹配（忽略大小写）  
CONTAINING (case-sensitive) firstname like ‘%’ + ?0 + ‘%’ 模糊查询（大小写敏感）  
CONTAINING (case-insensitive) LOWER(firstname) like ‘%’ + LOWER(?0) + ‘%’ 模糊查询（忽略大小写）  
## 10 自定义SQL语句分页查询

```
@Query(value = "SELECT t2.* FROM job_state_project_star t1,job_state_project_info t2 WHERE t1.user_id = :userId AND t1.project_id = t2.id"
			, countQuery = "SELECT COUNT(*) FROM job_state_project_info"
			,nativeQuery=true)
	Page<JobStateProjectInfo> starPageList(@Param("userId") String userId,Pageable pageable);
```
**注意事项：**  
1.在方法中传入Pageable对象作为参数  
2.在@Query注解中增加查询总记录数的SQL，写在countQuery属性中   

## 11. 自定义SQL在修改Test报错解决方法
```
    @Test
	@Transactional // 开启事务操作 注意：@Transactional和@Test一起使用的时候，事务会自动回滚
	@Rollback(false) // 取消自动回滚
	public void myupdate() {
		
		jobStateExecutorInfoRepository.myupdate("233",4);
		
	}
```
分析：@Test标签会开启事务，在结束时进行自动回滚，因此在测试的时候需要取消自动回滚