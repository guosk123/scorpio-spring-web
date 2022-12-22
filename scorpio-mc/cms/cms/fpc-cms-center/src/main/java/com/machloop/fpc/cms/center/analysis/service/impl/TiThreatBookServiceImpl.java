package com.machloop.fpc.cms.center.analysis.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.cms.center.analysis.bo.TiThreatBookBO;
import com.machloop.fpc.cms.center.analysis.dao.TiThreatBookDao;
import com.machloop.fpc.cms.center.analysis.data.TiThreatBookDO;
import com.machloop.fpc.cms.center.analysis.service.TiThreatBookService;
import com.machloop.fpc.cms.center.analysis.vo.TiThreatBookQueryVO;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/11/16 5:30 PM,cms
 * @version 1.0
 */
@Service
public class TiThreatBookServiceImpl implements TiThreatBookService {

  @Autowired
  private TiThreatBookDao tiThreatBookDao;


  @Override
  public Page<TiThreatBookBO> queryTiThreatBooks(PageRequest page, TiThreatBookQueryVO queryVO) {


    Page<TiThreatBookDO> tiThreatBookDOPage = tiThreatBookDao.queryTiThreatBooks(page, queryVO);
    long totalElements = tiThreatBookDOPage.getTotalElements();
    List<TiThreatBookBO> tiThreatBookBOList = Lists
        .newArrayListWithCapacity(tiThreatBookDOPage.getSize());
    for (TiThreatBookDO tiThreatBookDO : tiThreatBookDOPage) {
      TiThreatBookBO tiThreatBookBO = new TiThreatBookBO();
      BeanUtils.copyProperties(tiThreatBookDO, tiThreatBookBO);
      tiThreatBookBOList.add(tiThreatBookBO);
    }
    return new PageImpl<>(tiThreatBookBOList, page, totalElements);
  }

  @Override
  public Map<String, Object> queryTiThreatBook(String id) {

    return tiThreatBookDao.queryTiThreatBook(id);
  }

  @Override
  public List<String> queryTiThreatBooksBasicTags() {
    return tiThreatBookDao.queryTiThreatBooksBasicTags().stream()
        .filter(item -> !StringUtils.isEmpty(item)).collect(Collectors.toList());
  }
}
