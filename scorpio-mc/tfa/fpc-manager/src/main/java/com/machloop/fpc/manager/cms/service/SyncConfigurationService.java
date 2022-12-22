package com.machloop.fpc.manager.cms.service;

import java.util.Date;
import java.util.List;

import org.apache.rocketmq.common.message.Message;

/**
 * @author guosk
 * 
 * 所有需要同步上级配置的类均需要实现该接口
 *
 * create at 2021年11月25日, fpc-manager
 */
public interface SyncConfigurationService {

  static final String CMS_ASSIGNMENT = "cms_assignment";

  /**
   * 同步上级下发的配置(判断消息体是否为空，如果未空直接返回0，出现异常返回-1)
   * @param message
   * @return
   */
  int syncConfiguration(Message message);

  /**
   * 首次注册时，需要清除本地“多实例配置”，清除配置时需要注意规避各个配置之间的相互依赖
   * （注意：需要清除的多实例配置指可创建多个，非全局唯一的配置，全局唯一配置和特殊配置不可清除！
   * 全局唯一配置：SA规则库、地区库；
   * 特殊配置：捕获过滤规则和策略，因该配置直接和本地网络绑定，是网络的基本属性，删除会影响业务正常运行；
   * 多实例配置：IP地址组、逻辑子网、业务...）
   * @param tag
   * @return
   */
  int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime);

  /**
   * 获取下发的配置ID集合(根据tag查询下发的配置ID集合)
   * @param tag
   * @param beforeTime
   * @return
   */
  List<String> getAssignConfigurationIds(String tag, Date beforeTime);

}
