package com.machloop.fpc.manager.appliance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.appliance.bo.StorageSpaceBO;
import com.machloop.fpc.manager.appliance.service.StorageSpaceService;

/**
 * @author guosk
 *
 * create at 2021年4月1日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class StorageSpaceController {

  @Autowired
  private StorageSpaceService storageSpaceService;

  @GetMapping("/storage-spaces")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryStorageSpace() {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    storageSpaceService.queryStorageSpaces().forEach(storageSpace -> {
      Map<String,
          Object> storageSpaceMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      storageSpaceMap.put("spaceType", storageSpace.getSpaceType());
      storageSpaceMap.put("capacity", storageSpace.getCapacity());

      result.add(storageSpaceMap);
    });

    return result;
  }

  @PutMapping("/storage-spaces")
  @Secured({"PERM_USER"})
  public void updateStorageSpace(@Validated @RequestParam("storageSpaces") String storageSpaces) {
    List<StorageSpaceBO> storageSpaceList = JsonHelper.deserialize(storageSpaces,
        new TypeReference<List<StorageSpaceBO>>() {
        }, false);

    storageSpaceService.updateStorageSpaces(storageSpaceList,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate("修改存储空间：" + JsonHelper.serialize(storageSpaceList));
  }

}
