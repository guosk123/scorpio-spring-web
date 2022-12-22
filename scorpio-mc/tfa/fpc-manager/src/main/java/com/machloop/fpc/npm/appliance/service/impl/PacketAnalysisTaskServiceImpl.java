package com.machloop.fpc.npm.appliance.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.TokenUtils;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.npm.appliance.bo.PacketAnalysisSubTaskBO;
import com.machloop.fpc.npm.appliance.bo.PacketAnalysisTaskBO;
import com.machloop.fpc.npm.appliance.bo.PacketAnalysisTaskLogBO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskDao;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskLogDao;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskPolicyDao;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisSubTaskDO;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisTaskDO;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisTaskLogDO;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisTaskPolicyDO;
import com.machloop.fpc.npm.appliance.service.MetricSettingService;
import com.machloop.fpc.npm.appliance.service.PacketAnalysisTaskService;

/**
 * @author guosk
 * <p>
 * create at 2021年6月16日, fpc-manager
 */
@Service
public class PacketAnalysisTaskServiceImpl implements PacketAnalysisTaskService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PacketAnalysisTaskServiceImpl.class);

  private static final Map<String, Process> processMap = Maps.newConcurrentMap();

  private static final String FILE_TYPE_DIR = "DIR";
  private static final String FILE_TYPE_FILE = "FILE";

  private static final List<String> SINGLE_TASK = Lists
      .newArrayList("SINGLE_DIRECTORY_TO_SINGLE_TASK", "MULTIPLE_FILES_TO_SINGLE_TASK");

  @Value("${offline.packet.file.root.path}")
  private String offlineFileRootPath;

  @Autowired
  private PacketAnalysisTaskDao packetAnalysisTaskDao;

  @Autowired
  private PacketAnalysisSubTaskDao packetAnalysisSubTaskDao;

  @Autowired
  private PacketAnalysisTaskLogDao packetAnalysisTaskLogDao;

  @Autowired
  private MetricSettingService metricSettingService;

  @Autowired
  private DictManager dictManager;

  @Autowired
  private PacketAnalysisTaskPolicyDao packetAnalysisTaskPolicyDao;

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisTaskService#queryPacketAnalysisTasks(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String)
   */
  @Override
  public Page<PacketAnalysisTaskBO> queryPacketAnalysisTasks(Pageable page, String name,
      String status) {
    Map<String, String> statusDict = dictManager.getBaseDict()
        .getItemMap("appliance_packet_analysis_task_status");
    Map<String, String> sourceDict = dictManager.getBaseDict()
        .getItemMap("appliance_packet_analysis_task_source");
    Map<String, String> modeDict = dictManager.getBaseDict()
        .getItemMap("appliance_packet_analysis_task_mode");

    Page<PacketAnalysisTaskDO> packetAnalysisTasks = packetAnalysisTaskDao
        .queryPacketAnalysisTasks(page, name, status);

    List<PacketAnalysisTaskBO> list = packetAnalysisTasks.getContent().stream()
        .map(packetAnalysisTaskDO -> {
          PacketAnalysisTaskBO packetAnalysisTaskBO = new PacketAnalysisTaskBO();
          BeanUtils.copyProperties(packetAnalysisTaskDO, packetAnalysisTaskBO);
          packetAnalysisTaskBO
              .setModeText(MapUtils.getString(modeDict, packetAnalysisTaskDO.getMode()));
          packetAnalysisTaskBO
              .setSourceText(MapUtils.getString(sourceDict, packetAnalysisTaskDO.getSource()));
          packetAnalysisTaskBO
              .setStatusText(MapUtils.getString(statusDict, packetAnalysisTaskDO.getStatus()));
          packetAnalysisTaskBO
              .setCreateTime(DateUtils.toStringISO8601(packetAnalysisTaskDO.getCreateTime()));

          return packetAnalysisTaskBO;
        }).collect(Collectors.toList());

    return new PageImpl<>(list, page, packetAnalysisTasks.getTotalElements());
  }


  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisTaskService#queryPacketAnalysisTask(java.lang.String)
   */
  @Override
  public PacketAnalysisTaskBO queryPacketAnalysisTask(String id) {
    Map<String, String> statusDict = dictManager.getBaseDict()
        .getItemMap("appliance_packet_analysis_task_status");
    Map<String, String> sourceDict = dictManager.getBaseDict()
        .getItemMap("appliance_packet_analysis_task_source");
    Map<String, String> modeDict = dictManager.getBaseDict()
        .getItemMap("appliance_packet_analysis_task_mode");

    PacketAnalysisTaskDO packetAnalysisTaskDO = packetAnalysisTaskDao.queryPacketAnalysisTask(id);
    PacketAnalysisTaskBO packetAnalysisTaskBO = new PacketAnalysisTaskBO();
    BeanUtils.copyProperties(packetAnalysisTaskDO, packetAnalysisTaskBO);
    packetAnalysisTaskBO.setModeText(MapUtils.getString(modeDict, packetAnalysisTaskDO.getMode()));
    packetAnalysisTaskBO
        .setSourceText(MapUtils.getString(sourceDict, packetAnalysisTaskDO.getSource()));
    packetAnalysisTaskBO
        .setStatusText(MapUtils.getString(statusDict, packetAnalysisTaskDO.getStatus()));
    packetAnalysisTaskBO
        .setCreateTime(DateUtils.toStringISO8601(packetAnalysisTaskDO.getCreateTime()));

    List<PacketAnalysisTaskPolicyDO> policyDOList = packetAnalysisTaskPolicyDao
        .queryPolicyIdsByIdAndPolicyType(id,
            FpcConstants.APPLIANCE_PACKET_ANALYSIS_TASK_POLICY_SEND);
    List<String> policyIds = policyDOList.stream().map(PacketAnalysisTaskPolicyDO::getPolicyId)
        .collect(Collectors.toList());
    packetAnalysisTaskBO.setSendPolicyIds(CsvUtils.convertCollectionToCSV(policyIds));

    return packetAnalysisTaskBO;
  }


  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisTaskService#queryPacketFileDirectory(java.lang.String, java.lang.String, int)
   */
  @Override
  public Map<String, Object> queryPacketFileDirectory(String type, String filename, int count) {
    Map<String, Object> fileMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 结束当前用户上次未结束的请求
    String remoteAddress = LoggedUserContext.getCurrentUser().getRemoteAddress();
    remoteAddress = StringUtils.isNotBlank(remoteAddress) ? remoteAddress : "RESTAPI";

    if (processMap.containsKey(remoteAddress)) {
      processMap.get(remoteAddress).destroyForcibly();
      processMap.remove(remoteAddress);
    }

    // 判断离线文件目录根目录是否存在
    File offlineFileRootDir = new File(offlineFileRootPath);
    if (!offlineFileRootDir.exists()) {
      try {
        LOGGER.info("can not found offline root directory: {}, auto created.", offlineFileRootPath);
        FileUtils.forceMkdir(offlineFileRootDir);
      } catch (IOException e) {
        LOGGER.warn("mkdir directory faild.", e);
      }
    }

    // 遍历根目录下所有文件及子目录
    List<File> filesAndDirs = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Process process = null;
    try {
      // find xxx -type f|d
      ProcessBuilder builder = new ProcessBuilder("sh", "-c", "find " + offlineFileRootPath
          + (StringUtils.equals(type, FILE_TYPE_DIR) ? " -type d" : ""));

      builder.redirectErrorStream(true);

      process = builder.start();
      processMap.put(remoteAddress, process);

      // 获取输出信息
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          filesAndDirs.add(new File(line));
        }
      } catch (IOException e) {
        LOGGER.warn("failed to read result from process.", e);
        return fileMap;
      }
      process.waitFor();
    } catch (Exception e) {
      LOGGER.warn("find file failed: {}", offlineFileRootPath, e);
      return fileMap;
    } finally {
      if (process != null) {
        process.destroy();
        processMap.remove(remoteAddress);
      }
    }

    // 根据名称过滤
    if (StringUtils.isNotBlank(filename)) {
      if (StringUtils.equals(type, FILE_TYPE_DIR)) {
        filesAndDirs = filesAndDirs.stream()
            .filter(file -> StringUtils.countMatches(file.getName(), filename) > 0)
            .collect(Collectors.toList());
      } else {
        filesAndDirs = filesAndDirs.stream().filter(
            file -> file.isDirectory() || StringUtils.countMatches(file.getName(), filename) > 0)
            .collect(Collectors.toList());
      }
    }

    // 限制返回数量
    filesAndDirs = filesAndDirs.stream().limit(count).collect(Collectors.toList());

    fileMap.put("fileName", offlineFileRootDir.getName());
    fileMap.put("filePath", offlineFileRootDir.getAbsolutePath());
    fileMap.put("fileType", offlineFileRootDir.isDirectory() ? FILE_TYPE_DIR : FILE_TYPE_FILE);
    fileMap.put("child", traverseDirectory(filesAndDirs, offlineFileRootDir.getAbsolutePath()));
    return fileMap;
  }

  private List<Map<String, Object>> traverseDirectory(List<File> filesAndDirs,
      String directoryPath) {
    List<Map<String, Object>> childs = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    filesAndDirs.stream().filter(file -> StringUtils.equals(directoryPath, file.getParent()))
        .forEach(file -> {
          Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          map.put("fileName", file.getName());
          map.put("filePath", file.getAbsolutePath());
          map.put("fileType", file.isDirectory() ? FILE_TYPE_DIR : FILE_TYPE_FILE);
          map.put("child",
              file.isDirectory() ? traverseDirectory(filesAndDirs, file.getAbsolutePath())
                  : Lists.newArrayListWithCapacity(0));

          childs.add(map);
        });

    return childs;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisTaskService#savePacketAnalysisTask(com.machloop.fpc.npm.appliance.bo.PacketAnalysisTaskBO, java.lang.String)
   */
  @Override
  public PacketAnalysisTaskBO savePacketAnalysisTask(PacketAnalysisTaskBO packetAnalysisTaskBO,
      String operatorId) {
    PacketAnalysisTaskDO existName = packetAnalysisTaskDao
        .queryPacketAnalysisTaskByName(packetAnalysisTaskBO.getName());
    if (StringUtils.isNotBlank(existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "任务名称已经存在");
    }

    Map<String, String> modeDict = dictManager.getBaseDict()
        .getItemMap("appliance_packet_analysis_task_mode");
    if (!modeDict.containsKey(packetAnalysisTaskBO.getMode())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的分析模式");
    }

    PacketAnalysisTaskDO packetAnalysisTaskDO = new PacketAnalysisTaskDO();
    BeanUtils.copyProperties(packetAnalysisTaskBO, packetAnalysisTaskDO);
    packetAnalysisTaskDO.setSource(FpcConstants.OFFLINE_ANALYSIS_TASK_SOURCE_EXTERNAL_STORAGE);
    packetAnalysisTaskDO.setOperatorId(operatorId);
    packetAnalysisTaskDao.savePacketAnalysisTask(packetAnalysisTaskDO);

    List<PacketAnalysisTaskPolicyDO> policyList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> sendPolicies = CsvUtils.convertCSVToList(packetAnalysisTaskBO.getSendPolicyIds());
    if (CollectionUtils.isNotEmpty(sendPolicies)) {
      sendPolicies.forEach(sendPolicy -> {
        PacketAnalysisTaskPolicyDO packetAnalysisTaskPolicyDO = new PacketAnalysisTaskPolicyDO();
        packetAnalysisTaskPolicyDO.setPacketAnalysisTaskId(packetAnalysisTaskDO.getId());
        packetAnalysisTaskPolicyDO.setPolicyId(sendPolicy);
        packetAnalysisTaskPolicyDO
            .setPolicyType(FpcConstants.APPLIANCE_PACKET_ANALYSIS_TASK_POLICY_SEND);
        packetAnalysisTaskPolicyDO.setOperatorId(operatorId);
        policyList.add(packetAnalysisTaskPolicyDO);
      });
    }
    if (CollectionUtils.isNotEmpty(policyList)) {
      packetAnalysisTaskPolicyDao.mergePacketAnalysisTaskPolicies(policyList);
    }


    return queryPacketAnalysisTask(packetAnalysisTaskDO.getId());
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisTaskService#deletePacketAnalysisTask(java.lang.String, java.lang.String)
   */
  @Transactional
  @Override
  public PacketAnalysisTaskBO deletePacketAnalysisTask(String id, String operatorId) {
    PacketAnalysisTaskDO packetAnalysisTaskDO = packetAnalysisTaskDao.queryPacketAnalysisTask(id);
    if (StringUtils.isBlank(packetAnalysisTaskDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "离线分析任务不存在");
    }

    // 删除离线任务
    packetAnalysisTaskDao.deletePacketAnalysisTask(id, operatorId);

    // 删除子任务
    packetAnalysisSubTaskDao.deleteSubTaskByTaskId(id, operatorId);

    // 删除离线文件下的统计配置
    metricSettingService.deleteMetricSetting(FpcConstants.SOURCE_TYPE_PACKET_FILE, null, null, id);

    // 删除离线文件与策略的关联
    packetAnalysisTaskPolicyDao.deletePacketAnalysisTaskPolicyByPacketAnalysisTaskId(id);

    PacketAnalysisTaskBO packetAnalysisTaskBO = new PacketAnalysisTaskBO();
    BeanUtils.copyProperties(packetAnalysisTaskDO, packetAnalysisTaskBO);
    return packetAnalysisTaskBO;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisTaskService#queryPacketAnalysisSubTasks(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Page<PacketAnalysisSubTaskBO> queryPacketAnalysisSubTasks(Pageable page, String name,
      String taskId, String source) {
    Map<String, String> statusDict = dictManager.getBaseDict()
        .getItemMap("appliance_packet_analysis_task_status");

    Page<PacketAnalysisSubTaskDO> packetAnalysisSubTasks = packetAnalysisSubTaskDao
        .queryPacketAnalysisSubTasks(page, name, taskId, source);

    List<PacketAnalysisSubTaskBO> list = packetAnalysisSubTasks.getContent().stream()
        .map(packetAnalysisSubTaskDO -> {
          PacketAnalysisSubTaskBO packetAnalysisSubTaskBO = new PacketAnalysisSubTaskBO();
          BeanUtils.copyProperties(packetAnalysisSubTaskDO, packetAnalysisSubTaskBO);
          packetAnalysisSubTaskBO.setPacketStartTime(
              DateUtils.toStringISO8601(packetAnalysisSubTaskDO.getPacketStartTime()));
          packetAnalysisSubTaskBO.setPacketEndTime(
              DateUtils.toStringISO8601(packetAnalysisSubTaskDO.getPacketEndTime()));
          packetAnalysisSubTaskBO
              .setStatusText(MapUtils.getString(statusDict, packetAnalysisSubTaskDO.getStatus()));
          packetAnalysisSubTaskBO
              .setCreateTime(DateUtils.toStringISO8601(packetAnalysisSubTaskDO.getCreateTime()));

          return packetAnalysisSubTaskBO;
        }).collect(Collectors.toList());

    return new PageImpl<>(list, page, packetAnalysisSubTasks.getTotalElements());
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisTaskService#queryPacketAnalysisSubTask(java.lang.String)
   */
  @Override
  public PacketAnalysisSubTaskBO queryPacketAnalysisSubTask(String id) {
    Map<String, String> statusDict = dictManager.getBaseDict()
        .getItemMap("appliance_packet_analysis_task_status");

    PacketAnalysisSubTaskDO packetAnalysisSubTaskDO = packetAnalysisSubTaskDao
        .queryPacketAnalysisSubTask(id);

    PacketAnalysisSubTaskBO packetAnalysisSubTaskBO = new PacketAnalysisSubTaskBO();
    BeanUtils.copyProperties(packetAnalysisSubTaskDO, packetAnalysisSubTaskBO);
    packetAnalysisSubTaskBO.setPacketStartTime(
        DateUtils.toStringISO8601(packetAnalysisSubTaskDO.getPacketStartTime()));
    packetAnalysisSubTaskBO
        .setPacketEndTime(DateUtils.toStringISO8601(packetAnalysisSubTaskDO.getPacketEndTime()));
    packetAnalysisSubTaskBO
        .setStatusText(MapUtils.getString(statusDict, packetAnalysisSubTaskDO.getStatus()));
    packetAnalysisSubTaskBO
        .setCreateTime(DateUtils.toStringISO8601(packetAnalysisSubTaskDO.getCreateTime()));

    return packetAnalysisSubTaskBO;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisTaskService#queryFileUploadUrl(javax.servlet.http.HttpServletRequest, java.lang.String)
   */
  @Override
  public String queryFileUploadUrl(HttpServletRequest request, String operatorId) {
    String url = "";

    try {
      // 使用UUID作为凭证，并取token进行签名
      String credential = IdGenerator.generateUUID();
      String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
      String date = DateUtils.toStringISO8601(DateUtils.now());
      String path = ManagerConstants.REST_ENGINE_OFFLINE_UPLOAD;

      // 拼接地址
      StringBuilder urlBuilder = new StringBuilder();
      urlBuilder.append(path);
      urlBuilder.append("?operatorId=");
      urlBuilder.append(operatorId);
      urlBuilder.append("&X-Machloop-Date=");
      urlBuilder.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      urlBuilder.append("&X-Machloop-Credential=");
      urlBuilder.append(credential);
      urlBuilder.append("&X-Machloop-Signature=");
      urlBuilder.append(TokenUtils.makeSignature(token, credential, "POST", date, path));
      LOGGER.debug("invoke upload rest api:{}", urlBuilder.toString());

      url = urlBuilder.toString();
    } catch (Exception e) {
      LOGGER.warn("failed to encapsulation offline packets file upload url.", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "获取离线数据包文件上传地址异常");
    }

    return url;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisTaskService#deletePacketAnalysisSubTask(java.lang.String, java.lang.String)
   */
  @Override
  public PacketAnalysisSubTaskBO deletePacketAnalysisSubTask(String id, String operatorId) {
    PacketAnalysisSubTaskDO packetAnalysisSubTask = packetAnalysisSubTaskDao
        .queryPacketAnalysisSubTask(id);
    if (StringUtils.isBlank(packetAnalysisSubTask.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "离线分析子任务不存在");
    }

    // 删除子任务
    packetAnalysisSubTaskDao.deleteSubTaskById(id, operatorId);

    // 联动删除一对一的主任务
    PacketAnalysisTaskDO packetAnalysisTaskDO = packetAnalysisTaskDao
        .queryPacketAnalysisTask(packetAnalysisSubTask.getTaskId());
    if (StringUtils.equals(packetAnalysisTaskDO.getSource(),
        FpcConstants.OFFLINE_ANALYSIS_TASK_SOURCE_UPLOAD)
        || SINGLE_TASK.contains(packetAnalysisTaskDO.getMode())) {
      packetAnalysisTaskDao.deletePacketAnalysisTask(packetAnalysisTaskDO.getId(), operatorId);
      metricSettingService.deleteMetricSetting(FpcConstants.SOURCE_TYPE_PACKET_FILE, null, null,
          packetAnalysisTaskDO.getId());
    }

    // 删除离线文件下的统计配置
    metricSettingService.deleteMetricSetting(FpcConstants.SOURCE_TYPE_PACKET_FILE, null, null, id);

    PacketAnalysisSubTaskBO packetAnalysisSubTaskBO = new PacketAnalysisSubTaskBO();
    BeanUtils.copyProperties(packetAnalysisSubTask, packetAnalysisSubTaskBO);
    return packetAnalysisSubTaskBO;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.PacketAnalysisTaskService#queryPacketAnalysisLog(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String)
   */
  @Override
  public Page<PacketAnalysisTaskLogBO> queryPacketAnalysisLog(Pageable page, String taskId,
      String subTaskId) {
    Page<PacketAnalysisTaskLogDO> analysisLog = packetAnalysisTaskLogDao.queryAnalysisLog(page,
        taskId, subTaskId);

    List<PacketAnalysisTaskLogBO> list = analysisLog.getContent().stream()
        .map(packetAnalysisTaskLogDO -> {
          PacketAnalysisTaskLogBO packetAnalysisTaskLogBO = new PacketAnalysisTaskLogBO();
          BeanUtils.copyProperties(packetAnalysisTaskLogDO, packetAnalysisTaskLogBO);
          packetAnalysisTaskLogBO
              .setAriseTime(DateUtils.toStringISO8601(packetAnalysisTaskLogDO.getAriseTime()));

          return packetAnalysisTaskLogBO;
        }).collect(Collectors.toList());

    return new PageImpl<>(list, page, analysisLog.getTotalElements());
  }


}
