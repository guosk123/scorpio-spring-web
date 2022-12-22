package com.machloop.fpc.manager.knowledge.dao;

import java.util.List;

import com.machloop.fpc.manager.knowledge.data.SaApplicationDO;
import com.machloop.fpc.manager.knowledge.data.SaCategoryDO;
import com.machloop.fpc.manager.knowledge.data.SaKnowledgeInfoDO;
import com.machloop.fpc.manager.knowledge.data.SaSubCategoryDO;

import reactor.util.function.Tuple3;

/**
 * @author mazhiyuan
 *
 * create at 2020年5月21日, fpc-manager
 */
public interface SaKnowledgeDao {

  SaKnowledgeInfoDO queryKnowledgeInfos(String filePath);

  Tuple3<List<SaCategoryDO>, List<SaSubCategoryDO>, List<SaApplicationDO>> queryKnowledgeRules(
      String filePath);

}
