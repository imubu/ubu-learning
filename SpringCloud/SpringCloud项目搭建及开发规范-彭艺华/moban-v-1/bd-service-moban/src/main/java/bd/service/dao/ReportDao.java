package bd.service.dao;

import bd.service.domain.ReportVo;
//@Repository
public interface ReportDao {

	int insertSingle(ReportVo reportVo) ;
	int updateSingle(ReportVo reportVo) ;
	
}
