package com.machloop.fpc.cms.center.helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.helper.HotPropertiesHelper;

/**
 * @author guosk
 *
 * create at 2022年2月8日, fpc-cms-center
 */
@Component
public class ClickhouseRemoteServerHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClickhouseRemoteServerHelper.class);

  public static final String CLICKHOUSE_SERVER_NAME = "clickhouse_servers";
  public static final String CH_STATS_SERVER_NAME = "ch_stats_servers";

  private static final String SHARD_ATTRIBUTE = "node_ip";

  private static final String DEFAULT_NODE = "localhost";

  // 0 不加密； 1 加密
  private static final String SECURE = "1";

  private static final String FORMAT_ENCODE = "UTF-8";
  private static final int FORMAT_INDENT_SIZE = 4;

  @SuppressWarnings("unchecked")
  public static List<String> queryNodes() {
    List<String> nodes = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      String configFilePath = HotPropertiesHelper.getProperty("file.clickhouse.config.path");
      if (!Paths.get(configFilePath).toFile().exists()) {
        LOGGER.warn("configuration file not found.");
        return nodes;
      }

      SAXReader saxReader = new SAXReader();
      Document document = saxReader.read(new File(configFilePath));

      // 根节点
      Element root = document.getRootElement();
      if (root == null || !root.hasContent()) {
        LOGGER.warn("configuration file content parsing failed.");
        return nodes;
      }

      // 集群根节点
      Element remoteServers = root.element("remote_servers");
      if (remoteServers == null || !remoteServers.hasContent()) {
        LOGGER.warn("configuration item [remote_servers] not found.");
        return nodes;
      }

      // clickhouse_servers节点集合
      Element clickhouseServers = remoteServers.element(CLICKHOUSE_SERVER_NAME);
      if (clickhouseServers == null) {
        return nodes;
      }
      List<String> clickhouseNodes = clickhouseServers.elements().stream()
          .map(e -> e.attributeValue(SHARD_ATTRIBUTE)).collect(Collectors.toList());

      // ch_stats_servers节点集合
      Element chStatsServers = remoteServers.element(CH_STATS_SERVER_NAME);
      if (chStatsServers == null) {
        return nodes;
      }
      List<String> chStatsNodes = chStatsServers.elements().stream()
          .map(e -> e.attributeValue(SHARD_ATTRIBUTE)).collect(Collectors.toList());

      nodes.addAll(CollectionUtils.intersection(clickhouseNodes, chStatsNodes));
    } catch (DocumentException e) {
      LOGGER.warn("query node error.", e);
      return nodes;
    }

    return nodes;
  }

  /**
   * 新增探针设备时增加集群节点
   * @param host
   * @return
   */
  public static boolean addNode(String host) {
    if (!GracefulShutdownHelper.isShutdownNow()) {
      try {
        String configFilePath = HotPropertiesHelper.getProperty("file.clickhouse.config.path");
        if (!Paths.get(configFilePath).toFile().exists()) {
          LOGGER.warn("configuration file not found.");
          return false;
        }

        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File(configFilePath));

        // 根节点
        Element root = document.getRootElement();
        if (root == null || !root.hasContent()) {
          LOGGER.warn("configuration file content parsing failed.");
          return false;
        }

        // 集群根节点
        Element remoteServers = root.element("remote_servers");
        if (remoteServers == null || !remoteServers.hasContent()) {
          LOGGER.warn("configuration item [remote_servers] not found.");
          remoteServers = root.addElement("remote_servers");
          remoteServers.addAttribute("incl", "clickhouse_remote_servers");
        }

        // clickhouse_servers集群添加shard
        Element clickhouseServers = remoteServers.element(CLICKHOUSE_SERVER_NAME);
        if (clickhouseServers == null) {
          // 集群不存在则创建
          clickhouseServers = remoteServers.addElement(CLICKHOUSE_SERVER_NAME);
        }
        // 删除默认节点
        removeShardElement(clickhouseServers, DEFAULT_NODE);
        // 先删除将要添加的节点，防止重复
        removeShardElement(clickhouseServers, host);
        // 添加新的节点
        clickhouseServers.add(
            buildShardElement(host, HotPropertiesHelper.getProperty("clickhouse.tcp.ssl.port")));

        // ch_stats_servers集群添加shard
        Element chStatsServers = remoteServers.element(CH_STATS_SERVER_NAME);
        if (chStatsServers == null) {
          // 集群不存在则创建
          chStatsServers = remoteServers.addElement(CH_STATS_SERVER_NAME);
        }
        // 删除默认节点
        removeShardElement(chStatsServers, DEFAULT_NODE);
        // 先删除将要添加的节点，防止重复
        removeShardElement(chStatsServers, host);
        // 添加新的节点
        chStatsServers
            .add(buildShardElement(host, HotPropertiesHelper.getProperty("ch_stats.tcp.ssl.port")));

        // 指定XML输出样式
        OutputFormat xmlFormat = OutputFormat.createPrettyPrint();
        xmlFormat.setEncoding(FORMAT_ENCODE);
        xmlFormat.setIndentSize(FORMAT_INDENT_SIZE);

        XMLWriter writer = new XMLWriter(new FileWriter(new File(configFilePath)), xmlFormat);
        writer.write(document);
        writer.close();
      } catch (IOException e) {
        LOGGER.warn("add node error.", e);
        return false;
      } catch (DocumentException e) {
        LOGGER.warn("add node error.", e);
        return false;
      }
    }

    return true;
  }

  /**
   * 探针设备IP变更时，修改集群内节点host
   * @param sourceHost
   * @param targetHost
   * @return
   */
  public static boolean updateNode(String sourceHost, String targetHost) {
    if (!GracefulShutdownHelper.isShutdownNow()) {
      try {
        String configFilePath = HotPropertiesHelper.getProperty("file.clickhouse.config.path");
        if (!Paths.get(configFilePath).toFile().exists()) {
          LOGGER.warn("configuration file not found.");
          return false;
        }

        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File(configFilePath));

        // 根节点
        Element root = document.getRootElement();
        if (root == null || !root.hasContent()) {
          LOGGER.warn("configuration file content parsing failed.");
          return false;
        }

        // 集群根节点
        Element remoteServers = root.element("remote_servers");
        if (remoteServers == null || !remoteServers.hasContent()) {
          LOGGER.warn("configuration item [remote_servers] not found.");
          remoteServers = root.addElement("remote_servers");
          remoteServers.addAttribute("incl", "clickhouse_remote_servers");
        }

        // clickhouse_servers集群变更shard
        Element clickhouseServers = remoteServers.element(CLICKHOUSE_SERVER_NAME);
        if (clickhouseServers == null) {
          // 不存在则创建集群项
          clickhouseServers = remoteServers.addElement(CLICKHOUSE_SERVER_NAME);
          // 添加节点
          clickhouseServers.add(buildShardElement(targetHost,
              HotPropertiesHelper.getProperty("clickhouse.tcp.ssl.port")));
        } else {
          // 存在则先删除原有节点，再新建节点
          removeShardElement(clickhouseServers, sourceHost);
          removeShardElement(clickhouseServers, targetHost);
          clickhouseServers.add(buildShardElement(targetHost,
              HotPropertiesHelper.getProperty("clickhouse.tcp.ssl.port")));

          // 删除默认节点
          removeShardElement(clickhouseServers, DEFAULT_NODE);
        }

        // ch_stats_servers集群变更shard
        Element chStatsServers = remoteServers.element(CH_STATS_SERVER_NAME);
        if (chStatsServers == null) {
          // 不存在则创建集群项
          chStatsServers = remoteServers.addElement(CH_STATS_SERVER_NAME);
          // 添加节点
          chStatsServers.add(buildShardElement(targetHost,
              HotPropertiesHelper.getProperty("ch_stats.tcp.ssl.port")));
        } else {
          // 存在则先删除原有节点，再新建节点
          removeShardElement(chStatsServers, sourceHost);
          removeShardElement(chStatsServers, targetHost);
          chStatsServers.add(buildShardElement(targetHost,
              HotPropertiesHelper.getProperty("ch_stats.tcp.ssl.port")));

          // 删除默认节点
          removeShardElement(chStatsServers, DEFAULT_NODE);
        }

        // 指定XML输出样式
        OutputFormat xmlFormat = OutputFormat.createPrettyPrint();
        xmlFormat.setEncoding(FORMAT_ENCODE);
        xmlFormat.setIndentSize(FORMAT_INDENT_SIZE);

        XMLWriter writer = new XMLWriter(new FileWriter(new File(configFilePath)), xmlFormat);
        writer.write(document);
        writer.close();
      } catch (IOException e) {
        LOGGER.warn("update node error.", e);
        return false;
      } catch (DocumentException e) {
        LOGGER.warn("update node error.", e);
        return false;
      }
    }

    return true;
  }

  /**
   * 删除设备时，删除集群内节点
   * @param host
   * @return
   */
  public static boolean deleteNode(String host) {
    if (!GracefulShutdownHelper.isShutdownNow()) {
      try {
        String configFilePath = HotPropertiesHelper.getProperty("file.clickhouse.config.path");
        if (!Paths.get(configFilePath).toFile().exists()) {
          LOGGER.warn("configuration file not found.");
          return false;
        }

        SAXReader saxReader = new SAXReader(); // 用来读取xml文档
        Document document = saxReader.read(new File(configFilePath));

        // 根节点
        Element root = document.getRootElement();
        if (root == null || !root.hasContent()) {
          LOGGER.warn("configuration file content parsing failed.");
          return false;
        }

        // 集群根节点
        Element remoteServers = root.element("remote_servers");
        if (remoteServers == null || !remoteServers.hasContent()) {
          LOGGER.warn("configuration item [remote_servers] not found.");
          return false;
        }

        // clickhouse_servers集群删除shard
        Element clickhouseServers = remoteServers.element(CLICKHOUSE_SERVER_NAME);
        if (removeShardElement(clickhouseServers, host) == 0) {
          // clickhouse_servers集群内已经不存在节点，为避免报错，添加默认节点
          clickhouseServers.add(buildShardElement(DEFAULT_NODE,
              HotPropertiesHelper.getProperty("clickhouse.tcp.ssl.port")));
        }

        // ch_stats_servers集群删除shard
        Element chStatsServers = remoteServers.element(CH_STATS_SERVER_NAME);
        if (removeShardElement(chStatsServers, host) == 0) {
          // ch_stats_servers集群内已经不存在节点，为避免报错，添加默认节点
          chStatsServers.add(buildShardElement(DEFAULT_NODE,
              HotPropertiesHelper.getProperty("ch_stats.tcp.ssl.port")));
        }

        // 指定XML输出样式
        OutputFormat xmlFormat = OutputFormat.createPrettyPrint();
        xmlFormat.setEncoding(FORMAT_ENCODE);
        xmlFormat.setIndentSize(FORMAT_INDENT_SIZE);

        XMLWriter writer = new XMLWriter(new FileWriter(new File(configFilePath)), xmlFormat);
        writer.write(document);
        writer.close();
      } catch (IOException e) {
        LOGGER.warn("delete node error.", e);
        return false;
      } catch (DocumentException e) {
        LOGGER.warn("delete node error.", e);
        return false;
      }
    }

    return true;
  }

  /**
   * 构建shard
   * @param host 主机IP 
   * @param port 端口
   * @return 
   */
  private static Element buildShardElement(String host, String port) {
    Element replicaElement = DocumentHelper.createElement("replica");

    Element hostElement = replicaElement.addElement("host");
    hostElement.setText(host);

    Element portElement = replicaElement.addElement("port");
    portElement.setText(port);

    Element userElement = replicaElement.addElement("user");
    userElement.setText(HotPropertiesHelper.getProperty("clickhouse.remote.server.username"));

    Element passwordElement = replicaElement.addElement("password");
    passwordElement.setText(HotPropertiesHelper.getProperty("clickhouse.remote.server.password"));

    Element secureElement = replicaElement.addElement("secure");
    secureElement.setText(SECURE);

    Element shardElement = DocumentHelper.createElement("shard");
    shardElement.addAttribute(SHARD_ATTRIBUTE, host);
    shardElement.add(replicaElement);

    return shardElement;
  }

  /**
   * 移除节点
   * @param servers 集群节点
   * @param host 移除主机IP
   * @return 集群最终剩余节点数量
   */
  private static int removeShardElement(Element servers, String host) {
    if (servers == null) {
      return -1;
    }

    Iterator<Element> iterator = servers.elementIterator();
    while (iterator.hasNext()) {
      Element shardElement = iterator.next();

      Attribute nodeIp = shardElement.attribute(SHARD_ATTRIBUTE);
      if (nodeIp != null && StringUtils.equals(nodeIp.getValue(), host)) {
        servers.remove(shardElement);
      }
    }

    return servers.elements().size();
  }

}
