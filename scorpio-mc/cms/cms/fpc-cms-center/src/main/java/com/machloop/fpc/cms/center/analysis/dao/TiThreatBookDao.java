package com.machloop.fpc.cms.center.analysis.dao;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.cms.center.analysis.data.TiThreatBookDO;
import com.machloop.fpc.cms.center.analysis.vo.TiThreatBookQueryVO;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/11/16 5:38 PM,cms
 * @version 1.0
 */
public interface TiThreatBookDao {

  Page<TiThreatBookDO> queryTiThreatBooks(PageRequest page, TiThreatBookQueryVO queryVO);

  Map<String, Object> queryTiThreatBook(String id);

  List<String> queryTiThreatBooksBasicTags();
}
