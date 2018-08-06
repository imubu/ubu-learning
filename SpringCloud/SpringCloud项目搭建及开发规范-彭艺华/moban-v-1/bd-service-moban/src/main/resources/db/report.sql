/*==============================================================*/
/* DBMS name:      MySQL 5.5                                    */
/* Created on:     2018/2/7 11:29:02                            */
/*==============================================================*/


drop table if exists report_engine_report;

/*==============================================================*/
/* Table: report_engine_t_report                                          */
/*==============================================================*/
create table report_engine_report
(
   report_id                   bigint not null auto_increment comment '报表ID',
   report_name                 varchar(256) not null comment '报表名称',
   report_description       text comment '报表描述信息',
   report_content            text  comment '报表内容',
   publish_status         varchar(10)  comment '发布状态',
   publish_time        datetime comment '发布时间',
   publish_path     varchar(256) comment '发布地址',
   directory_id      bigint comment '所属目录ID',
   create_user            varchar(256)  comment '创建人',
   create_time         TIMESTAMP  DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    update_user           varchar(256)  comment '更新人',
     update_time         TIMESTAMP  comment '更新时间',
      status          varchar(10)   comment '状态',
   primary key (report_id)
);



drop table if exists report_engine_directory;

/*==============================================================*/
/* Table: report_engine_directory                                          */
/*==============================================================*/
create table report_engine_directory
(
   directory_id                   bigint not null auto_increment comment '目录ID',
   directory_name                 varchar(256) not null comment '目录名称',
   directory_description       text comment '目录描述信息',
   create_user            varchar(256)  comment '创建人',
   create_time         TIMESTAMP  DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    update_user           varchar(256)  comment '更新人',
     update_time         TIMESTAMP  comment '更新时间',
      status          varchar(10)   comment '状态',
   primary key (directory_id)
);



