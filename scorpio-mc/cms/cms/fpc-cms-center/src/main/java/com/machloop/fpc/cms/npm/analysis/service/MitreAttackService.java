package com.machloop.fpc.cms.npm.analysis.service;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.cms.npm.analysis.bo.MitreAttackBO;

/**
 * @author chenshimiao
 *
 * create at 2022/10/13 10:32 AM,cms
 * @version 1.0
 */
public interface MitreAttackService {


  List<MitreAttackBO> queryMitreAttacks(Date startTimeDate, Date endTimeDate);
}
