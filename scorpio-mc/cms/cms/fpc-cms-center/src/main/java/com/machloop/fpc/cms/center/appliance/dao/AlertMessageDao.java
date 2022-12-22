package com.machloop.fpc.cms.center.appliance.dao;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.cms.center.appliance.data.AlertMessageDO;
import com.machloop.fpc.cms.center.appliance.vo.AlertMessageQueryVO;

/**
 * @author guosk
 * <p>
 * create at 2020年10月29日, fpc-manager
 */
public interface AlertMessageDao {

    Page<AlertMessageDO> queryAlertMessages(Pageable page, AlertMessageQueryVO queryVO,
                                            List<String> solverIds);

    List<AlertMessageDO> queryAlertMessages(AlertMessageQueryVO queryVO);

    long countAlertMessages(AlertMessageQueryVO queryVO);

    AlertMessageDO queryAlertMessage(String id);

    int updateAlertMessageStatus(AlertMessageDO alertMessage, String status, String reason,
                                 String solverId);

    List<Map<String, Object>> queryAlertMessageAsHistogram(AlertMessageQueryVO queryVO);
}
