package com.machloop.fpc.npm.analysis.dao;

import java.util.List;

import com.machloop.fpc.npm.analysis.data.MitreAttackDO;

/**
 * @author guosk
 *
 * create at 2022年4月6日, fpc-manager
 */
public interface MitreAttackDao {

  List<MitreAttackDO> queryMitreAttacks();

}
