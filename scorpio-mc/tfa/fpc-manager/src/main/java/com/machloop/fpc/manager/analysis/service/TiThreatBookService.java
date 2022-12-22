package com.machloop.fpc.manager.analysis.service;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.manager.analysis.bo.TiThreatBookBO;
import com.machloop.fpc.manager.analysis.vo.TiThreatBookQueryVO;

/**
 * @author ChenXiao
 * create at 2022/9/6
 */
public interface TiThreatBookService {
  Page<TiThreatBookBO> queryTiThreatBooks(PageRequest page, TiThreatBookQueryVO queryVO);

  Map<String, Object> queryTiThreatBook(String id);

  List<String> queryTiThreatBooksBasicTags();

}
