package bd.service.domain;

import java.sql.Timestamp;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "基础对象", description = "base")
public class BaseVo {
	@ApiModelProperty(value = "排序", required = false)
	private String orderBy;
	@ApiModelProperty(value = "页数", required = false)
	private Integer pageSize;
	@ApiModelProperty(value = "页码", required = false)
	private Integer offSet;
	@ApiModelProperty(value = "创建人", required = false)
	private String create_user;
	@ApiModelProperty(value = "创建时间", required = false)
	private Timestamp create_time;
	@ApiModelProperty(value = "最后更新人", required = false)
	private String update_user;
	@ApiModelProperty(value = "最后更新时间", required = false)
	private Timestamp update_time;
	@ApiModelProperty(value = "状态", required = false)
	private String status;

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getOffSet() {
		return offSet;
	}

	public void setOffSet(Integer offSet) {
		this.offSet = offSet;
	}

	public String getCreate_user() {
		return create_user;
	}

	public void setCreate_user(String create_user) {
		this.create_user = create_user;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}

	public String getUpdate_user() {
		return update_user;
	}

	public void setUpdate_user(String update_user) {
		this.update_user = update_user;
	}

	public Date getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(Timestamp update_time) {
		this.update_time = update_time;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
