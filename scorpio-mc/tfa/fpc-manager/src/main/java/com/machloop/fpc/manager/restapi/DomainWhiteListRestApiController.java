package com.machloop.fpc.manager.restapi;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.manager.appliance.service.DomainWhiteListService;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/12/13 9:23 PM,cms
 * @version 1.0
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class DomainWhiteListRestApiController {

  @Autowired
  private UserService userService;

  @Autowired
  private DomainWhiteListService domainWhiteListService;

  @PostMapping("/domain-white")
    @RestApiSecured
    public RestAPIResultVO importDomainWhiteList(@RequestParam MultipartFile file, HttpServletRequest request){

        UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
        domainWhiteListService.importIssuedDomain(file, userBO.getId());
        
        return RestAPIResultVO.resultSuccess("导入成功，");
    }
}
