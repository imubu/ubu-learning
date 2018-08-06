package bd.service.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "报表对象", description = "report")
public class ReportVo extends BaseVo {
	@ApiModelProperty(value = "报表ID", required = false)
	private Integer report_id;
	@ApiModelProperty(value = "报表名称", required = false)
	private String report_name;
	@ApiModelProperty(value = "报表描述", required = false)
	private String report_description;
	@ApiModelProperty(value = "报表内容", required = false)
	private String report_content;
	@ApiModelProperty(value = "所属目录ID", required = false)
	private Integer directory_id;
	@ApiModelProperty(value = "所属目录的名称", required = false)
	private String directory_name;
	@ApiModelProperty(value = "发布状态", required = false)
	private String publish_status;
	@ApiModelProperty(value = "发布路径", required = false)
	private String publish_path;
	@ApiModelProperty(value = "发布时间", required = false)
	private String publish_time;

	public String getPublish_status() {
		return publish_status;
	}

	public void setPublish_status(String publish_status) {
		this.publish_status = publish_status;
	}

	public String getPublish_path() {
		return publish_path;
	}

	public void setPublish_path(String publish_path) {
		this.publish_path = publish_path;
	}

	public String getPublish_time() {
		return publish_time;
	}

	public void setPublish_time(String publish_time) {
		this.publish_time = publish_time;
	}

	public void setDirectory_name(String directory_name) {
		this.directory_name = directory_name;
	}

	public String getDirectory_name() {
		return directory_name;
	}

	public Integer getReport_id() {
		return report_id;
	}

	public void setReport_id(Integer report_id) {
		this.report_id = report_id;
	}

	public String getReport_name() {
		return report_name;
	}

	public void setReport_name(String report_name) {
		this.report_name = report_name;
	}

	public String getReport_description() {
		return report_description;
	}

	public void setReport_description(String report_description) {
		this.report_description = report_description;
	}


	public String getReport_content() {
		return report_content;
	}

	public void setReport_content(String report_content) {
		this.report_content = report_content;
	}

	public Integer getDirectory_id() {
		return directory_id;
	}

	public void setDirectory_id(Integer directory_id) {
		this.directory_id = directory_id;
	}

}
