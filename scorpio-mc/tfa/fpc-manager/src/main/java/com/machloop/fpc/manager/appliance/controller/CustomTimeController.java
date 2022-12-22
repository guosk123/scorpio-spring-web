package com.machloop.fpc.manager.appliance.controller;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.appliance.bo.CustomTimeBO;
import com.machloop.fpc.manager.appliance.service.CustomTimeService;
import com.machloop.fpc.manager.appliance.vo.CustomTimeCreationVO;
import com.machloop.fpc.manager.appliance.vo.CustomTimeModificationVO;

/**
 * @author minjiajun
 *
 * create at 2022年6月9日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class CustomTimeController {

  @Autowired
  private CustomTimeService customTimeService;

  @GetMapping("/custom-times")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryCustomTimes(String type) {

    List<CustomTimeBO> customTimes = customTimeService.queryCustomTimes(type);

    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(customTimes.size());
    for (CustomTimeBO customTime : customTimes) {
      resultList.add(customTimeBO2Map(customTime));
    }

    return resultList;
  }

  @GetMapping("/custom-times/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryCustomTime(
      @NotEmpty(message = "自定义时间ID不能为空") @PathVariable String id) {
    CustomTimeBO customTime = customTimeService.queryCustomTime(id);

    return customTimeBO2Map(customTime);
  }

  @PostMapping("/custom-times")
  @Secured({"PERM_USER"})
  public void saveCustomTime(@Validated CustomTimeCreationVO customTimeVO) {
    CustomTimeBO customTimeBO = new CustomTimeBO();
    BeanUtils.copyProperties(customTimeVO, customTimeBO);

    customTimeBO = customTimeService.saveCustomTime(customTimeBO,
        LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, customTimeBO);
  }

  @PutMapping("/custom-times/{id}")
  @Secured({"PERM_USER"})
  public void updateCustomTime(@PathVariable @NotEmpty(message = "修改自定义时间时传入的id不能为空") String id,
      @Validated CustomTimeModificationVO customTimeVO) {
    CustomTimeBO customTimeBO = new CustomTimeBO();
    BeanUtils.copyProperties(customTimeVO, customTimeBO);

    customTimeBO = customTimeService.updateCustomTime(id, customTimeBO,
        LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, customTimeBO);
  }

  @DeleteMapping("/custom-times/{id}")
  @Secured({"PERM_USER"})
  public void deleteCustomTime(@PathVariable @NotEmpty(message = "删除自定义时间时传入的id不能为空") String id) {
    List<CustomTimeBO> customTimeBOS = customTimeService
        .batchDeleteCustomTime(Lists.newArrayList(id), LoggedUserContext.getCurrentUser().getId());
    for (CustomTimeBO customTimeBO : customTimeBOS) {
      LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, customTimeBO);
    }
  }

  @DeleteMapping("/custom-times/batch")
  @Secured({"PERM_USER"})
  public void batchDeleteCustomTimes(
      @RequestParam @NotEmpty(message = "自定义时间id不能为空") String idList) {
    List<String> deletedIdList = CsvUtils.convertCSVToList(idList);
    List<CustomTimeBO> customTimeBOS = customTimeService.batchDeleteCustomTime(deletedIdList,
        LoggedUserContext.getCurrentUser().getId());
    for (CustomTimeBO customTimeBO : customTimeBOS) {
      LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, customTimeBO);
    }
  }

  private static Map<String, Object> customTimeBO2Map(CustomTimeBO customTimeBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", customTimeBO.getId());
    map.put("name", customTimeBO.getName());
    map.put("type", customTimeBO.getType());
    map.put("period", customTimeBO.getPeriod());
    map.put("customTimeSetting", customTimeBO.getCustomTimeSetting());

    map.put("createTime", customTimeBO.getCreateTime());

    return map;
  }
}
