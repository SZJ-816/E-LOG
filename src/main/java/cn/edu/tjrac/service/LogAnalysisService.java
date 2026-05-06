package cn.edu.tjrac.service;

import cn.edu.tjrac.vo.DashboardVO;
import cn.edu.tjrac.vo.ErrorLogVO;
import java.util.List;
import java.util.Map;

public interface LogAnalysisService {
    DashboardVO getOverview();
    List<DashboardVO.HourlyStats> getPvUvStats();
    List<DashboardVO.TopApiVO> getTopApis(int limit);
    List<ErrorLogVO> getRecentErrors(int limit);
    DashboardVO.SystemHealthVO getSystemHealth();
    List<Map<String, Object>> getLogLevelStats();
}
