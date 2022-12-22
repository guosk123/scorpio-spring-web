package com.machloop.fpc.manager.system.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.system.service.DataClearCategoryService;

/**
 * @author chenshimiao
 * 
 * create at 2022/9/15 10:24 AM, fpc-manager
 * @version 1.0
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/system")
public class DataClearCategoryController {

  @Autowired
  private DataClearCategoryService dataClearCategoryService;

  @GetMapping("/data-clear/category")
  @Secured({"PERM_SYS_USER"})
  public Map<String, String> queryDataClearCategory() {

    Map<String, String> category = dataClearCategoryService.queryDataClearCategory();
    return category;
  }

  @PostMapping("/data-clear")
  @Secured({"PERM_SYS_USER"})
  public void doDataClear(@RequestParam String dataClearParams) {

    List<String> param = CsvUtils.convertCSVToList(dataClearParams);

    dataClearCategoryService.clearData(param, LoggedUserContext.getCurrentUser().getId());
  }

}
