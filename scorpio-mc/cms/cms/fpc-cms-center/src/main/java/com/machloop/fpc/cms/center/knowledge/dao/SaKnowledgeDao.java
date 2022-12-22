package com.machloop.fpc.cms.center.knowledge.dao;

import com.machloop.fpc.cms.center.knowledge.data.SaApplicationDO;
import com.machloop.fpc.cms.center.knowledge.data.SaCategoryDO;
import com.machloop.fpc.cms.center.knowledge.data.SaKnowledgeInfoDO;
import com.machloop.fpc.cms.center.knowledge.data.SaSubCategoryDO;
import reactor.util.function.Tuple3;

import java.util.List;

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
