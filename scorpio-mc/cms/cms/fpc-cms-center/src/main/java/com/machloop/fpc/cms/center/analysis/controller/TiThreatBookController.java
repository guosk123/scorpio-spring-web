package com.machloop.fpc.cms.center.analysis.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.cms.center.analysis.bo.TiThreatBookBO;
import com.machloop.fpc.cms.center.analysis.vo.TiThreatBookQueryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.machloop.fpc.cms.center.analysis.service.TiThreatBookService;

import java.util.List;
import java.util.Map;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/11/16 5:24 PM,cms
 * @version 1.0
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/analysis")
public class TiThreatBookController {

  @Autowired
  private TiThreatBookService tiThreatBookService;

  @GetMapping("/ti-threatbook")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryTiThreatBooks(
          @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
          @RequestParam(
                  name = "pageSize", required = false,
                  defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
          TiThreatBookQueryVO queryVO) {

    Sort sort = new Sort(Sort.Direction.DESC, "time");
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    Page<TiThreatBookBO> tiThreatBookBOPage = tiThreatBookService.queryTiThreatBooks(page, queryVO);

    List<Map<String, Object>> resultList = Lists
            .newArrayListWithCapacity(tiThreatBookBOPage.getSize());

    for (TiThreatBookBO tiThreatBookBO : tiThreatBookBOPage) {
      resultList.add(tiThreatBookBO2Map(tiThreatBookBO));
    }

    return new PageImpl<>(resultList, page, tiThreatBookBOPage.getTotalElements());
  }

  @GetMapping("/ti-threatbook/basic-tags")
  @Secured({"PERM_USER"})
  public List<String> queryTiThreatBooksBasicTags() {

    return tiThreatBookService.queryTiThreatBooksBasicTags();
  }

  private Map<String, Object> tiThreatBookBO2Map(TiThreatBookBO tiThreatBookBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", tiThreatBookBO.getId());
    map.put("iocRaw", tiThreatBookBO.getIocRaw());
    map.put("basicTag", tiThreatBookBO.getBasicTag());
    map.put("tag", tiThreatBookBO.getTag());
    map.put("intelType", tiThreatBookBO.getIntelType());
    map.put("source", tiThreatBookBO.getSource());
    map.put("time", tiThreatBookBO.getTime());
    map.put("iocType", tiThreatBookBO.getIocType());
    return map;
  }

  @GetMapping("/ti-threatbook/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryTiThreatBook(@PathVariable String id) {

    return tiThreatBookService.queryTiThreatBook(id);
  }
}
