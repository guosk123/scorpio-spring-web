package com.machloop.fpc.cms.npm.analysis.dao;

import java.util.List;

import com.machloop.fpc.cms.npm.analysis.data.MitreAttackDO;

/**
 * @author ChenXiao
 * create at 2022/9/19
 */
public interface MitreAttackDao {

  List<MitreAttackDO> queryMitreAttacks();
}
