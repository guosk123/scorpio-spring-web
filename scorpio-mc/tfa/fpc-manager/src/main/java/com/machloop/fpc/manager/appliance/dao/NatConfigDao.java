package com.machloop.fpc.manager.appliance.dao;

import com.machloop.fpc.manager.appliance.data.NatConfigDO;

/**
 * @author ChenXiao
 * create at 2022/11/9
 */
public interface NatConfigDao {
    NatConfigDO queryNatConfig();

    void updateNatConfig(NatConfigDO natConfigDO);
}
