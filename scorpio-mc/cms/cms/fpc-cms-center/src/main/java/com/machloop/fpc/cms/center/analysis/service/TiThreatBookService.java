package com.machloop.fpc.cms.center.analysis.service;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.cms.center.analysis.bo.TiThreatBookBO;
import com.machloop.fpc.cms.center.analysis.vo.TiThreatBookQueryVO;

import java.util.List;
import java.util.Map;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/11/16 5:29 PM,cms
 * @version 1.0
 */
public interface TiThreatBookService {

    Page<TiThreatBookBO> queryTiThreatBooks(PageRequest page, TiThreatBookQueryVO queryVO);

    Map<String, Object> queryTiThreatBook(String id);

    List<String> queryTiThreatBooksBasicTags();
}
