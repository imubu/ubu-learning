package bd.service.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bd.service.dao.ReportDao;
import bd.service.domain.ReportVo;
import bd.service.service.ReportService;

@Service("reportService")
public class ReportServiceImp implements ReportService {

	@Resource
	private ReportDao reportDao;


	@Override
	@Transactional
	public int insertSingle(ReportVo reportVo) {
		// TODO Auto-generated method stub
		int result = 0;
		result = reportDao.insertSingle(reportVo);
		return result;
	}

	@Override
	public int updateSingle(ReportVo reportVo) {
		// TODO Auto-generated method stub
		int result = 0;
		result = reportDao.updateSingle(reportVo);
		return result;
	}



}
