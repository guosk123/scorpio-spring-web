package com.machloop.fpc.npm.analysis.service;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.npm.analysis.bo.MitreAttackBO;

/**
 * @author guosk
 *
 * create at 2022年4月6日, fpc-manager
 */
public interface MitreAttackService {

  List<MitreAttackBO> queryMitreAttacks(Date startTime, Date endTime);

}
