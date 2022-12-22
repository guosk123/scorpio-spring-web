package com.machloop.fpc.cms.center.restapi;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.central.vo.FpcQueryVO;

@RestController
@RequestMapping("/restapi/fpc-cms-v1/central")
public class FpcRestAPIController {

  @Autowired
  private FpcService fpcService;

  @GetMapping("/fpc-devices")
  @RestApiSecured
  public List<Map<String, Object>> queryFpcList() {

    List<FpcBO> fpcList = fpcService.queryFpcs(new FpcQueryVO());

    List<Map<String, Object>> result = Lists.newArrayListWithExpectedSize(fpcList.size());
    for (FpcBO fpc : fpcList) {
      Map<String,
          Object> fpcMap = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      fpcMap.put("name", fpc.getName());
      fpcMap.put("ip", fpc.getIp());
      fpcMap.put("serialNumber", fpc.getSerialNumber());
      fpcMap.put("licenseStatus", fpc.getLicenseStatus());
      fpcMap.put("connectStatus", fpc.getConnectStatus());
      fpcMap.put("description", fpc.getDescription());

      result.add(fpcMap);
    }

    return result;
  }

}
