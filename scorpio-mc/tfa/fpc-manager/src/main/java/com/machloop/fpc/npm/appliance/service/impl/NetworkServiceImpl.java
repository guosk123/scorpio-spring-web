package com.machloop.fpc.npm.appliance.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.bo.FilterRuleBO;
import com.machloop.fpc.manager.appliance.bo.IngestPolicyBO;
import com.machloop.fpc.manager.appliance.service.AlertRuleService;
import com.machloop.fpc.manager.appliance.service.FilterRuleService;
import com.machloop.fpc.manager.appliance.service.IngestPolicyService;
import com.machloop.fpc.manager.cms.service.SyncConfigurationService;
import com.machloop.fpc.manager.cms.service.impl.MQAssignmentServiceImpl;
import com.machloop.fpc.manager.helper.MQMessageHelper;
import com.machloop.fpc.manager.system.bo.DeviceNetifBO;
import com.machloop.fpc.manager.system.service.DeviceNetifService;
import com.machloop.fpc.npm.appliance.bo.MetricSettingBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.NetworkPolicyBO;
import com.machloop.fpc.npm.appliance.bo.NetworkTopologyBO;
import com.machloop.fpc.npm.appliance.dao.*;
import com.machloop.fpc.npm.appliance.data.*;
import com.machloop.fpc.npm.appliance.service.BaselineService;
import com.machloop.fpc.npm.appliance.service.MetricSettingService;
import com.machloop.fpc.npm.appliance.service.NetworkService;

import reactor.util.function.Tuple2;

/**
 * @author guosk
 * <p>
 * create at 2020年11月10日, fpc-manager
 *
 */
@Order(9)
@Transactional
@Service
public class NetworkServiceImpl implements NetworkService, SyncConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(NetworkServiceImpl.class);

  private static final int MAX_NETWORK_NUMBER = 32;

  @Autowired
  private NetworkDao networkDao;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private NetworkNetifDao networkNetifDao;

  @Autowired
  private NetworkInsideIpDao networkInsideIpDao;

  @Autowired
  private NetworkPolicyDao networkPolicyDao;

  @Autowired
  private NetworkTopologyDao networkTopologyDao;

  @Autowired
  private LogicalSubnetDao logicalSubnetDao;

  @Autowired
  private ServiceNetworkDao serviceNetworkDao;

  @Autowired
  private IngestPolicyService ingestPolicyService;

  @Autowired
  private FilterRuleService filterRuleService;

  @Autowired
  private DeviceNetifService deviceNetifService;

  @Autowired
  private MetricSettingService metricSettingService;

  @Autowired
  private BaselineService baselineService;

  @Autowired
  private AlertRuleService alertRuleService;

  @Autowired
  private DictManager dictManager;

  /**
   * @see com.machloop.fpc.npm.appliance.service.NetworkService#queryNetworks()
   */
  @Override
  public List<NetworkBO> queryNetworks() {
    Map<String, String> netifDirectionDict = dictManager.getBaseDict()
        .getItemMap("appliance_network_netif_type");

    List<NetworkDO> networks = networkDao.queryNetworks();

    List<NetworkNetifDO> networkNetifs = networkNetifDao.queryAllNetworkNetifs();
    Map<String, List<NetworkNetifDO>> networkNetifMap = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    networkNetifs.forEach(networkNetif -> {
      String networkId = networkNetif.getNetworkId();
      List<NetworkNetifDO> list = networkNetifMap.get(networkId);
      if (CollectionUtils.isEmpty(list)) {
        list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      list.add(networkNetif);
      networkNetifMap.put(networkId, list);
    });

    List<NetworkBO> result = Lists.newArrayListWithCapacity(networks.size());
    networks.forEach(networkDO -> {
      NetworkBO networkBO = new NetworkBO();
      BeanUtils.copyProperties(networkDO, networkBO);
      networkBO.setCreateTime(DateUtils.toStringISO8601(networkDO.getCreateTime()));
      networkBO
          .setNetifTypeText(MapUtils.getString(netifDirectionDict, networkBO.getNetifType(), ""));
      networkBO.setNetif(networkNetifMap.get(networkBO.getId()));
      result.add(networkBO);
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.NetworkService#queryNetworks(java.util.List)
   */
  @Override
  public List<NetworkBO> queryNetworks(List<String> ids) {
    List<NetworkDO> networks = networkDao.queryNetworks(ids);

    return networks.stream().map(networkDO -> {
      NetworkBO networkBO = new NetworkBO();
      BeanUtils.copyProperties(networkDO, networkBO);

      return networkBO;
    }).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.NetworkService#queryNetworksWithDetail()
   */
  @Override
  public List<NetworkBO> queryNetworksWithDetail() {
    List<NetworkDO> networks = networkDao.queryNetworks();

    // 网络接口汇总
    List<NetworkNetifDO> networkNetifs = networkNetifDao.queryAllNetworkNetifs();
    Map<String, List<NetworkNetifDO>> networkNetifMap = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    networkNetifs.forEach(networkNetif -> {
      String networkId = networkNetif.getNetworkId();
      List<NetworkNetifDO> list = networkNetifMap.get(networkId);
      if (CollectionUtils.isEmpty(list)) {
        list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      list.add(networkNetif);
      networkNetifMap.put(networkId, list);
    });

    // 网络内网地址汇总
    Map<String,
        List<String>> networkIpMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    networkInsideIpDao.queryNetworkInsideIps(null).forEach(networkInsideIp -> {
      String networkId = networkInsideIp.getNetworkId();
      List<String> insideIps = networkIpMap.get(networkId);
      if (CollectionUtils.isEmpty(insideIps)) {
        insideIps = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      insideIps.add(networkInsideIp.getIpAddress());
      networkIpMap.put(networkId, insideIps);
    });

    // 网络策略汇总
    Map<String, Map<String, String>> networkPolicyMap = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<NetworkPolicyDO> networkPolicys = networkPolicyDao.queryNetworkPolicys();
    networkPolicys.forEach(networkPolicy -> {
      String networkId = networkPolicy.getNetworkId();
      String policyType = networkPolicy.getPolicyType();
      Map<String, String> policys = networkPolicyMap.get(networkId);
      if (MapUtils.isEmpty(policys)) {
        policys = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      }
      if (StringUtils.equals(policyType, FpcConstants.APPLIANCE_NETWORK_POLICY_INGEST)) {
        policys.put("ingestPolicyId", networkPolicy.getPolicyId());
      }
      if (StringUtils.equals(policyType, FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE)) {
        policys.put("filterRuleId", networkPolicy.getPolicyId());
      }
      networkPolicyMap.put(networkId, policys);
    });

    List<NetworkBO> result = Lists.newArrayListWithCapacity(networks.size());
    networks.forEach(networkDO -> {
      NetworkBO networkBO = new NetworkBO();
      BeanUtils.copyProperties(networkDO, networkBO);
      networkBO.setCreateTime(DateUtils.toStringISO8601(networkDO.getCreateTime()));
      networkBO.setNetif(networkNetifMap.get(networkBO.getId()));
      List<String> insideIps = networkIpMap.get(networkBO.getId());
      networkBO.setInsideIpAddress(
          CollectionUtils.isEmpty(insideIps) ? "" : String.join(",", insideIps));
      Map<String, String> policys = networkPolicyMap.get(networkBO.getId());
      networkBO.setIngestPolicyId(policys.get("ingestPolicyId"));
      networkBO.setFilterRuleIds(policys.get("filterRuleId"));
      result.add(networkBO);
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.NetworkService#queryNetworkPolicy(java.lang.String)
   */
  @Override
  public List<Map<String, String>> queryNetworkPolicy(String policyType) {
    List<NetworkDO> networks = networkDao.queryNetworks();

    List<NetworkPolicyDO> networkPolicys = networkPolicyDao
        .queryNetworkPolicyByPolicyType(policyType);
    Map<String, NetworkPolicyDO> networkPolicyMap = networkPolicys.stream()
        .collect(Collectors.toMap(NetworkPolicyDO::getNetworkId, networkPolicy -> networkPolicy));

    Map<String, String> policyNameMap = Maps.newHashMapWithExpectedSize(networkPolicys.size());
    if (StringUtils.equals(policyType, FpcConstants.APPLIANCE_NETWORK_POLICY_INGEST)) {
      List<IngestPolicyBO> ingestPolicys = ingestPolicyService.queryIngestPolicys();
      ingestPolicys
          .forEach(ingestPolicy -> policyNameMap.put(ingestPolicy.getId(), ingestPolicy.getName()));
    } else {
      List<FilterRuleBO> filterRuleBOS = filterRuleService.queryFilterRule();
      filterRuleBOS
          .forEach(filterRuleBO -> policyNameMap.put(filterRuleBO.getId(), filterRuleBO.getName()));
    }

    List<Map<String, String>> result = Lists.newArrayListWithCapacity(networks.size());
    networks.forEach(network -> {
      Map<String, String> policy = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      policy.put("networkId", network.getId());
      policy.put("networkName", network.getName());
      NetworkPolicyDO networkPolicyDO = networkPolicyMap.get(network.getId());
      if (networkPolicyDO != null) {
        policy.put("policyId", networkPolicyDO.getPolicyId());
        policy.put("policyName",
            MapUtils.getString(policyNameMap, networkPolicyDO.getPolicyId(), ""));
      }
      result.add(policy);

    });

    return result;
  }

  @Override
  public Map<String, List<String>> queryNetworkPolicies() {
    List<NetworkPolicyDO> networkPolicys = networkPolicyDao
        .queryNetworkPolicyByPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_FORWARD);
    Map<String, List<String>> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    networkPolicys.forEach(networkPolicy -> {
      String networkId = networkPolicy.getNetworkId();
      if (result.get(networkId) == null) {
        List<String> policyIds = new ArrayList<>();
        policyIds.add(networkPolicy.getPolicyId());
        result.put(networkId, policyIds);
      } else {
        List<String> policiesIdList = result.get(networkId);
        policiesIdList.add(networkPolicy.getPolicyId());
      }
    });
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.NetworkService#queryNetworkNetif()
   */
  @Override
  public List<Map<String, Object>> queryNetworkNetif() {
    List<NetworkNetifDO> networkNetifs = networkNetifDao.queryAllNetworkNetifs();

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(networkNetifs.size());
    networkNetifs.forEach(networkNetif -> {
      Map<String,
          Object> networkNetifMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      networkNetifMap.put("id", networkNetif.getId());
      networkNetifMap.put("networkId", networkNetif.getNetworkId());
      networkNetifMap.put("netifName", networkNetif.getNetifName());
      networkNetifMap.put("specification", networkNetif.getSpecification());
      networkNetifMap.put("direction", networkNetif.getDirection());
      result.add(networkNetifMap);
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.NetworkService#queryNetwork(java.lang.String)
   */
  @Override
  public NetworkBO queryNetwork(String id) {
    Map<String, String> netifDirectionDict = dictManager.getBaseDict()
        .getItemMap("appliance_network_netif_type");

    NetworkDO networkDO = networkDao.queryNetwork(id);

    NetworkBO networkBO = new NetworkBO();
    BeanUtils.copyProperties(networkDO, networkBO);
    networkBO.setCreateTime(DateUtils.toStringISO8601(networkDO.getCreateTime()));
    networkBO
        .setNetifTypeText(MapUtils.getString(netifDirectionDict, networkBO.getNetifType(), ""));

    networkBO.setNetif(networkNetifDao.queryNetworkNetifs(id));
    List<NetworkInsideIpDO> insideIps = networkInsideIpDao.queryNetworkInsideIps(id);
    if (CollectionUtils.isNotEmpty(insideIps)) {
      StringBuilder insideIpAddress = new StringBuilder();
      insideIps.forEach(ip -> insideIpAddress.append(ip.getIpAddress()).append(","));
      networkBO.setInsideIpAddress(
          insideIpAddress.toString().substring(0, insideIpAddress.length() - 1));
    }
    String ingestPolicyId = "";
    List<String> filterRuleIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> sendPolicyIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<NetworkPolicyDO> networkPolicys = networkPolicyDao.queryNetworkPolicyByNetworkId(id);
    for (NetworkPolicyDO networkPolicy : networkPolicys) {
      String policyType = networkPolicy.getPolicyType();
      if (StringUtils.equals(policyType, FpcConstants.APPLIANCE_NETWORK_POLICY_INGEST)) {
        ingestPolicyId = networkPolicy.getPolicyId();
      }
      if (StringUtils.equals(policyType, FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE)) {
        filterRuleIds.add(networkPolicy.getPolicyId());
      }
      if (StringUtils.equals(policyType, FpcConstants.APPLIANCE_NETWORK_POLICY_SEND)) {
        sendPolicyIds.add(networkPolicy.getPolicyId());
      }
    }
    networkBO.setSendPolicyIds(CsvUtils.convertCollectionToCSV(sendPolicyIds));
    networkBO.setIngestPolicyId(ingestPolicyId);
    networkBO.setFilterRuleIds(StringUtils.strip(filterRuleIds.toString(), "[]").replace(" ", ""));

    return networkBO;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.NetworkService#saveNetwork(com.machloop.fpc.npm.appliance.bo.NetworkBO, java.lang.String)
   */
  @Override
  public NetworkBO saveNetwork(NetworkBO networkBO, String operatorId) {
    NetworkDO exist = networkDao.queryNetworkByName(networkBO.getName());
    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "网络名称不能重复");
    }

    if (CsvUtils.convertCSVToList(networkBO.getFilterRuleIds()).size() > 50) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "每个网络最多配置50个存储过滤规则");
    }

    List<NetworkDO> networks = networkDao.queryNetworks();
    if (networks.size() >= MAX_NETWORK_NUMBER) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          String.format("网络数量已超过最大上限[%s]", MAX_NETWORK_NUMBER));
    }

    NetworkDO networkDO = new NetworkDO();
    BeanUtils.copyProperties(networkBO, networkDO);
    networkDO.setOperatorId(operatorId);
    networkDO.setReportState(Constants.BOOL_NO);
    networkDO.setReportAction(FpcCmsConstants.SYNC_ACTION_ADD);
    // 保存网络基本属性
    networkDao.saveNetwork(networkDO);

    // 保存配置的接口信息
    List<NetworkNetifDO> networkNetifs = networkBO.getNetif();
    if (CollectionUtils.isEmpty(networkNetifs)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "未配置业务接口");
    }
    // 判断是否为流量接收口，并且是否已经被其他网络配置
    List<DeviceNetifBO> ingestNetifs = deviceNetifService
        .queryDeviceNetifsByCategories(FpcConstants.DEVICE_NETIF_CATEGORY_INGEST);
    List<String> ingestNetifNames = ingestNetifs.stream().map(netif -> netif.getName())
        .collect(Collectors.toList());
    List<String> networkNetifNames = networkNetifs.stream()
        .map(networkNetif -> networkNetif.getNetifName()).collect(Collectors.toList());
    List<String> configuredNetifs = networkNetifDao.queryAllNetworkNetifs().stream()
        .map(netif -> netif.getNetifName()).collect(Collectors.toList());
    if (!ingestNetifNames.containsAll(networkNetifNames)
        || CollectionUtils.containsAny(networkNetifNames, configuredNetifs)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "接口类型不正确或已经被其他网络使用");
    }

    networkNetifs.forEach(networkNetif -> {
      networkNetif.setNetworkId(networkDO.getId());
      networkNetif.setOperatorId(operatorId);
    });
    networkNetifDao.mergeNetworkNetifs(networkNetifs);

    // 保存内网IP信息
    String insideIpAddress = networkBO.getInsideIpAddress();
    if (StringUtils.isNotBlank(insideIpAddress)) {
      List<String> ipList = CsvUtils.convertCSVToList(insideIpAddress);
      List<NetworkInsideIpDO> insideIpList = Lists.newArrayListWithCapacity(ipList.size());
      ipList.forEach(ip -> {
        NetworkInsideIpDO networkInsideIpDO = new NetworkInsideIpDO();
        networkInsideIpDO.setNetworkId(networkDO.getId());
        networkInsideIpDO.setIpAddress(ip);
        if (NetworkUtils.isInetAddress(ip, IpVersion.V4)) {
          Tuple2<Long, Long> ip2Range = NetworkUtils.ip2Range(ip);
          networkInsideIpDO.setIpStart(ip2Range.getT1());
          networkInsideIpDO.setIpEnd(ip2Range.getT2());
        }
        networkInsideIpDO.setOperatorId(operatorId);
        insideIpList.add(networkInsideIpDO);
      });
      networkInsideIpDao.mergeNetworkInsideIps(insideIpList);
    }

    // 保存所选策略
    List<NetworkPolicyDO> policyList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    String ingestPolicyId = networkBO.getIngestPolicyId();
    if (StringUtils.isNotBlank(ingestPolicyId)) {
      NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
      networkPolicyDO.setNetworkId(networkDO.getId());
      networkPolicyDO.setPolicyId(ingestPolicyId);
      networkPolicyDO.setPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_INGEST);
      networkPolicyDO.setOperatorId(operatorId);
      policyList.add(networkPolicyDO);
    }
    List<String> filterRules = CsvUtils.convertCSVToList(networkBO.getFilterRuleIds());
    for (String filterRule : filterRules) {
      if (StringUtils.isNotBlank(filterRule)) {
        NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
        networkPolicyDO.setNetworkId(networkDO.getId());
        networkPolicyDO.setPolicyId(filterRule);
        networkPolicyDO.setPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
        networkPolicyDO.setOperatorId(operatorId);
        policyList.add(networkPolicyDO);
      }
    }
    List<String> sendPolicies = CsvUtils.convertCSVToList(networkBO.getSendPolicyIds());
    if (CollectionUtils.isNotEmpty(sendPolicies)) {
      sendPolicies.forEach(sendPolicy -> {
        NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
        networkPolicyDO.setNetworkId(networkDO.getId());
        networkPolicyDO.setPolicyId(sendPolicy);
        networkPolicyDO.setPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
        networkPolicyDO.setOperatorId(operatorId);
        policyList.add(networkPolicyDO);
      });
    }
    if (CollectionUtils.isNotEmpty(policyList)) {
      networkPolicyDao.mergeNetworkPolicys(policyList);
    }

    // 配置网络默认的统计度量值
    MetricSettingBO metricSettingBO = new MetricSettingBO();
    metricSettingBO.setSourceType(FpcConstants.SOURCE_TYPE_NETWORK);
    metricSettingBO.setNetworkId(networkDO.getId());
    metricSettingService.saveDefaultMetricSettings(Lists.newArrayList(metricSettingBO), operatorId);

    networkBO.setId(networkDO.getId());
    return networkBO;
  }

  /**
   * 存储过滤规则`存储`网络与规则关系时使用
   * @param networkPolicyBOList
   * @param operatorId
   * @return
   */
  @Override
  public int saveNetworkPolicy(List<NetworkPolicyBO> networkPolicyBOList, String operatorId) {

    List<NetworkPolicyDO> networkPolicyDOList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (NetworkPolicyBO networkPolicyBO : networkPolicyBOList) {
      NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
      BeanUtils.copyProperties(networkPolicyBO, networkPolicyDO);
      networkPolicyDOList.add(networkPolicyDO);
    }


    return networkPolicyDao.saveNetworkPolicy(networkPolicyDOList, operatorId);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.NetworkService#updateNetwork(java.lang.String, com.machloop.fpc.npm.appliance.bo.NetworkBO, java.lang.String)
   */

  // TODO 等待修改
  @Override
  public NetworkBO updateNetwork(String id, NetworkBO networkBO, String operatorId) {
    NetworkDO exist = networkDao.queryNetwork(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "网络不存在");
    }

    if (CsvUtils.convertCSVToList(networkBO.getFilterRuleIds()).size() > 50) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "每个网络最多配置50条规则");
    }

    NetworkDO networkByName = networkDao.queryNetworkByName(networkBO.getName());
    if (StringUtils.isNotBlank(networkByName.getId())
        && !StringUtils.equals(id, networkByName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "网络名称不能重复");
    }

    // 修改网络信息
    NetworkDO networkDO = new NetworkDO();
    networkBO.setId(id);
    BeanUtils.copyProperties(networkBO, networkDO);
    networkDO.setOperatorId(operatorId);
    networkDO.setReportState(Constants.BOOL_NO);
    networkDO.setReportAction(FpcCmsConstants.SYNC_ACTION_MODIFY);
    networkDao.updateNetwork(networkDO);

    // 修改与接口关联关系
    List<NetworkNetifDO> networkNetifs = networkBO.getNetif();
    if (CollectionUtils.isEmpty(networkNetifs)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "未配置业务接口");
    }
    // 判断是否为流量接收口，并且是否已经被其他网络配置
    List<DeviceNetifBO> ingestNetifs = deviceNetifService
        .queryDeviceNetifsByCategories(FpcConstants.DEVICE_NETIF_CATEGORY_INGEST);
    List<String> ingestNetifNames = ingestNetifs.stream().map(netif -> netif.getName())
        .collect(Collectors.toList());
    List<String> networkNetifNames = networkNetifs.stream()
        .map(networkNetif -> networkNetif.getNetifName()).collect(Collectors.toList());
    List<String> configuredNetifs = networkNetifDao.queryAllNetworkNetifs().stream()
        .filter(netif -> !StringUtils.equals(id, netif.getNetworkId()))
        .map(netif -> netif.getNetifName()).collect(Collectors.toList());
    if (!ingestNetifNames.containsAll(networkNetifNames)
        || CollectionUtils.containsAny(networkNetifNames, configuredNetifs)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "接口类型不正确或已经被其他网络使用");
    }

    networkNetifs.forEach(networkNetif -> {
      networkNetif.setNetworkId(networkDO.getId());
      networkNetif.setOperatorId(operatorId);
    });
    networkNetifDao.mergeNetworkNetifs(networkNetifs);

    // 修改关联ip
    String insideIpAddress = networkBO.getInsideIpAddress();
    if (StringUtils.isNotBlank(insideIpAddress)) {
      List<String> ipList = CsvUtils.convertCSVToList(insideIpAddress);
      List<NetworkInsideIpDO> insideIpList = Lists.newArrayListWithCapacity(ipList.size());
      ipList.forEach(ip -> {
        NetworkInsideIpDO networkInsideIpDO = new NetworkInsideIpDO();
        networkInsideIpDO.setNetworkId(networkDO.getId());
        networkInsideIpDO.setIpAddress(ip);
        if (NetworkUtils.isInetAddress(ip, IpVersion.V4)) {
          Tuple2<Long, Long> ip2Range = NetworkUtils.ip2Range(ip);
          networkInsideIpDO.setIpStart(ip2Range.getT1());
          networkInsideIpDO.setIpEnd(ip2Range.getT2());
        }
        networkInsideIpDO.setOperatorId(operatorId);
        insideIpList.add(networkInsideIpDO);
      });
      networkInsideIpDao.mergeNetworkInsideIps(insideIpList);
    } else {
      networkInsideIpDao.deleteNetworkInsideIp(id);
    }

    // 修改与策略关联关系
    List<NetworkPolicyDO> policyList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    String ingestPolicyId = networkBO.getIngestPolicyId();
    if (StringUtils.isNotBlank(ingestPolicyId)) {
      NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
      networkPolicyDO.setNetworkId(networkDO.getId());
      networkPolicyDO.setPolicyId(ingestPolicyId);
      networkPolicyDO.setPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_INGEST);
      networkPolicyDO.setOperatorId(operatorId);
      policyList.add(networkPolicyDO);
    }
    List<String> filterRuleIds = CsvUtils.convertCSVToList(networkBO.getFilterRuleIds());
    for (String filterRuleId : filterRuleIds) {
      if (StringUtils.isNotBlank(filterRuleId)) {
        NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
        networkPolicyDO.setNetworkId(networkDO.getId());
        networkPolicyDO.setPolicyId(filterRuleId);
        networkPolicyDO.setPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE);
        networkPolicyDO.setOperatorId(operatorId);
        policyList.add(networkPolicyDO);
      }
    }
    List<String> sendPolicies = CsvUtils.convertCSVToList(networkBO.getSendPolicyIds());
    if (CollectionUtils.isNotEmpty(sendPolicies)) {
      sendPolicies.forEach(sendPolicy -> {
        NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
        networkPolicyDO.setNetworkId(networkDO.getId());
        networkPolicyDO.setPolicyId(sendPolicy);
        networkPolicyDO.setPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
        networkPolicyDO.setOperatorId(operatorId);
        policyList.add(networkPolicyDO);
      });
    }
    List<NetworkPolicyDO> networkPolicyDOS = networkPolicyDao
        .queryNetworkPolicyByNetworkIdAndPolicyType(id,
            FpcConstants.APPLIANCE_NETWORK_POLICY_FORWARD);
    policyList.addAll(networkPolicyDOS);
    if (CollectionUtils.isNotEmpty(policyList)) {
      networkPolicyDao.mergeNetworkPolicys(policyList);
    }

    return networkBO;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.NetworkService#updateNetworkPolicy(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void updateNetworkPolicy(String id, String policyId, String policyType,
      String operatorId) {
    networkPolicyDao.updateNetworkPolicy(id, policyId, policyType, operatorId);
  }

  @Override
  public int deleteNetworkPolicy(String policyId, String policyType) {
    return networkPolicyDao.deleteNetworkPolicyByPolicyId(policyId, policyType);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.NetworkService#deleteNetwork(java.lang.String, java.lang.String)
   */
  @Override
  public NetworkBO deleteNetwork(String id, String operatorId) {
    NetworkDO networkDO = networkDao.queryNetwork(id);
    if (StringUtils.isBlank(networkDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "网络不存在");
    }

    // 删除网络时查看该网络是否配置在某个业务下，如果有则不能删除
    if (CollectionUtils.isNotEmpty(serviceNetworkDao.queryServiceNetworks(null, id))) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该网络存在相关联的业务，无法删除");
    }

    // 删除网络时查看该网络是否包含子网，如果有则不能删除
    if (CollectionUtils.isNotEmpty(logicalSubnetDao.queryLogicalSubnetByNetworkId(id))) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该网络下包含子网络，无法删除");
    }

    // 是否有告警作用于该网络
    if (alertRuleService.queryAlertRulesBySource(FpcConstants.SOURCE_TYPE_NETWORK, id, null)
        .size() > 0) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该网络已作用于告警，无法删除");
    }

    // 该网络是否已配置到网络拓扑中
    NetworkTopologyDO networkTopologyDO = networkTopologyDao.queryNetworkTopologyByNetworkId(id);
    if (StringUtils.isNotBlank(networkTopologyDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该网络已配置到网络拓扑中，无法删除");
    }
    List<NetworkPolicyDO> networkPolicyDOS = networkPolicyDao
        .queryNetworkPolicyByNetworkIdAndPolicyType(id,
            FpcConstants.APPLIANCE_NETWORK_POLICY_FORWARD);
    if (CollectionUtils.isNotEmpty(networkPolicyDOS)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "该网络已配置到实时转发策略中，删除前请先修改实时转发策略");
    }

    // 删除网络
    networkDao.deleteNetwork(id, operatorId);

    // 删除网络与接口的关联关系
    networkNetifDao.deleteNetworkNetif(id);

    // 删除网络与ip的关联关系
    networkInsideIpDao.deleteNetworkInsideIp(id);

    // 删除网络与策略的关联关系
    networkPolicyDao.deleteNetworkPolicyByNetworkId(id);

    // 删除网络下基线定义
    baselineService.deleteBaselineSettings(FpcConstants.SOURCE_TYPE_NETWORK, id, null);

    // 删除网络下的统计配置
    metricSettingService.deleteMetricSetting(FpcConstants.SOURCE_TYPE_NETWORK, id, null, null);

    NetworkBO networkBO = new NetworkBO();
    BeanUtils.copyProperties(networkDO, networkBO);
    return networkBO;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.NetworkService#queryNetworkTopology()
   */
  @Override
  public NetworkTopologyBO queryNetworkTopology() {
    NetworkTopologyDO networkTopologyDO = networkTopologyDao.queryNetworkTopology();

    NetworkTopologyBO networkTopologyBO = new NetworkTopologyBO();
    BeanUtils.copyProperties(networkTopologyDO, networkTopologyBO);
    networkTopologyBO.setTimestamp(DateUtils.toStringISO8601(networkTopologyDO.getTimestamp()));

    return networkTopologyBO;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.NetworkService#updateNetworkTopology(com.machloop.fpc.npm.appliance.bo.NetworkTopologyBO, java.lang.String)
   */
  @Override
  public NetworkTopologyBO updateNetworkTopology(NetworkTopologyBO networkTopologyBO,
      String operatorId) {
    NetworkTopologyDO networkTopologyDO = new NetworkTopologyDO();
    BeanUtils.copyProperties(networkTopologyBO, networkTopologyDO);
    networkTopologyDO.setOperatorId(operatorId);

    networkTopologyDao.saveOrUpdateNetworkTopology(networkTopologyDO);

    return queryNetworkTopology();
  }


  @PostConstruct
  public void init() {
    MQAssignmentServiceImpl.register(this,
        Lists.newArrayList(FpcCmsConstants.MQ_TAG_NETWORKPOLICY));
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#syncConfiguration(org.apache.rocketmq.common.message.Message)
   */
  @Override
  public int syncConfiguration(Message message) {
    Map<String, Object> messageBody = MQMessageHelper.convertToMap(message);

    List<Map<String, Object>> messages = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (MapUtils.getBoolean(messageBody, "batch", false)) {
      messages.addAll(JsonHelper.deserialize(JsonHelper.serialize(messageBody.get("data")),
          new TypeReference<List<Map<String, Object>>>() {
          }));
    } else {
      messages.add(messageBody);
    }
    Set<Map<String, Object>> messagesOfSendPolicy = messages.stream().filter(map -> StringUtils
        .equals(MapUtils.getString(map, "policyType"), FpcConstants.APPLIANCE_NETWORK_POLICY_SEND))
        .collect(Collectors.toSet());
    Set<Map<String, Object>> messagesExceptOfSendPolicy = messages.stream()
        .filter(map -> !StringUtils.equals(MapUtils.getString(map, "policyType"),
            FpcConstants.APPLIANCE_NETWORK_POLICY_SEND))
        .collect(Collectors.toSet());

    int syncTotalCountExceptOfSendPolicy = messagesExceptOfSendPolicy.stream()
        .mapToInt(this::syncNetworkPolicy).sum();
    int syncTotalCountOfSendPolicy = CollectionUtils.isEmpty(messagesOfSendPolicy) ? 0
        : syncNetworkPolicyOfSendPolicy(messagesOfSendPolicy);

    int syncTotalCount = syncTotalCountExceptOfSendPolicy + syncTotalCountOfSendPolicy;

    LOGGER.info("current sync networkPolicy total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int syncNetworkPolicyOfSendPolicy(Set<Map<String, Object>> messagesOfSendPolicy) {
    // 判断下发的网络策略中的网络是否存在
    List<String> vaildNetworkIds = networkService.queryNetworks().stream().map(NetworkBO::getId)
        .collect(Collectors.toList());
    List<Map<String, Object>> messages = messagesOfSendPolicy.stream()
        .filter(map -> vaildNetworkIds.contains(MapUtils.getString(map, "networkId")))
        .collect(Collectors.toList());
    // 去重
    messages = new ArrayList<>(
        messages.stream()
            .collect(
                Collectors
                    .toMap(
                        message -> MapUtils.getString(message, "networkId")
                            + MapUtils.getString(message, "policyId"),
                        message -> message, (x, y) -> x))
            .values());
    // 按照网络分组
    Map<String, List<Map<String, Object>>> networkIdListMap = messages.stream()
        .collect(Collectors.groupingBy(x -> MapUtils.getString(x, "networkId")));
    int syncCount = 0;
    for (String networkId : networkIdListMap.keySet()) {
      List<Map<String, Object>> networkList = networkIdListMap.get(networkId);
      int modifyCount = 0;
      int deleteCount = 0;
      String action = MapUtils.getString(networkList.get(0), "action");
      List<NetworkPolicyDO> networkPolicyDOList = networkList.stream()
          .map(this::tranMap2NetworkPolicyDO).collect(Collectors.toList());
      try {
        switch (action) {
          case FpcCmsConstants.SYNC_ACTION_MODIFY:
            networkPolicyDao.mergeNetworkPolicysOfSend(networkPolicyDOList);
            modifyCount += networkPolicyDOList.size();
            break;
          case FpcCmsConstants.SYNC_ACTION_DELETE:
            List<NetworkPolicyDO> deleteNetworkPolicyDOSList = networkPolicyDao
                .queryNetworkPolicyByNetworkIdAndPolicyType(networkId,
                    FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
            networkPolicyDao.deleteNetworkPolicyByNetworkIdAndPolicyType(networkId,
                FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
            deleteCount += deleteNetworkPolicyDOSList.size();
            break;
          default:
            break;
        }
        // 本次同步数据量
        syncCount += (modifyCount + deleteCount);

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("current sync networkPolicy status: [modify: {}, delete: {}]", modifyCount,
              deleteCount);
        }
      } catch (BusinessException e) {
        LOGGER.warn("sync failed. error msg: {}", e.getMessage());
      }
    }
    return syncCount;
  }

  private NetworkPolicyDO tranMap2NetworkPolicyDO(Map<String, Object> map) {
    NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
    String id = MapUtils.getString(map, "id");
    String networkId = MapUtils.getString(map, "networkId");
    String policyId = MapUtils.getString(map, "policyId");
    String policyType = MapUtils.getString(map, "policyType");
    networkPolicyDO.setId(id);
    networkPolicyDO.setNetworkId(networkId);
    networkPolicyDO.setNetworkPolicyInCmsId(id);
    networkPolicyDO.setPolicyId(policyId);
    networkPolicyDO.setPolicyType(policyType);
    networkPolicyDO.setOperatorId(CMS_ASSIGNMENT);
    return networkPolicyDO;
  }

  private int syncNetworkPolicy(Map<String, Object> messageBody) {
    int syncCount = 0;

    String policyId = MapUtils.getString(messageBody, "policyId");
    if (StringUtils.isBlank(policyId)) {
      return syncCount;
    }
    String policyType = MapUtils.getString(messageBody, "policyType");
    String id = MapUtils.getString(messageBody, "id");
    String action = MapUtils.getString(messageBody, "action");
    String networkId = MapUtils.getString(messageBody, "networkId");

    NetworkPolicyBO networkPolicyBO = new NetworkPolicyBO();
    networkPolicyBO.setId(id);
    networkPolicyBO.setNetworkPolicyInCmsId(id);
    networkPolicyBO.setPolicyId(policyId);
    networkPolicyBO.setPolicyType(policyType);
    networkPolicyBO.setNetworkId(networkId);

    // 判断下发的网络策略中的网络是否存在
    if (!StringUtils.equals(action, FpcConstants.SYNC_ACTION_DELETE)) {
      List<String> vaildNetworkIds = networkService.queryNetworks().stream().map(NetworkBO::getId)
          .collect(Collectors.toList());
      List<String> networkIds = CsvUtils.convertCSVToList(networkId);
      Iterator<String> iterator = networkIds.iterator();
      while (iterator.hasNext()) {
        if (!vaildNetworkIds.contains(iterator.next())) {
          iterator.remove();
        }
      }
      if (networkIds.isEmpty()) {
        // 不存在网络策略中的网络
        return syncCount;
      }
    }

    // 捕获过滤更新之前，暂时使用
    NetworkPolicyDO exist = new NetworkPolicyDO();
    if (StringUtils.equals(policyType, FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE)) {
      exist = networkPolicyDao.queryNetworkPolicyByPolicyId(id);
    } else {
      exist = networkPolicyDao.queryNetworkPolicyByNetworkId(networkId, policyType);
    }

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcConstants.SYNC_ACTION_ADD:
        case FpcConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isBlank(exist.getId())) {
            saveNetworkPolicy(Lists.newArrayList(networkPolicyBO), CMS_ASSIGNMENT);
            addCount++;
          } else {
            // TODO 捕获过滤临时操作
            if (StringUtils.equals(policyType, FpcConstants.APPLIANCE_NETWORK_POLICY_STORAGE)) {
              networkPolicyDao.updateNetworkPolicyByPolicyId(id, networkId, policyId,
                  CMS_ASSIGNMENT);
            } else {
              networkPolicyDao.updateNetworkPolicy(networkId, policyId, policyType, CMS_ASSIGNMENT);
            }

            modifyCount++;
          }
          break;
        case FpcConstants.SYNC_ACTION_DELETE:
          deleteNetworkPolicy(policyId, policyType);
          deleteCount++;
          break;
        default:
          break;
      }
      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(
            "current sync networkPolicy status: [addCount: {}, modifyCount: {}, deleteCount: {}]",
            addCount, modifyCount, deleteCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync faild. error msg: {}", e.getMessage());
      return -1;
    }

    return syncCount;
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#clearLocalConfiguration(java.lang.String, java.util.Date, boolean)
   */
  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    // TODO 删除全部与存储过滤相关规则
    return networkPolicyDao.deleteNetworkPolicyByFilterRule(onlyLocal);
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#getAssignConfigurationIds(java.lang.String, java.util.Date)
   */
  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {
    return networkPolicyDao.queryNetworkPolicyOfNetworkIdAndPolicyId(beforeTime);
  }
}
