package com.scorpio.test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang.time.DateUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.jasypt.encryption.StringEncryptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scorpio.rest.common.SignatureUtil;
import com.scorpio.rest.vo.SixTuple;
import com.scorpio.rest.vo.TransmitTaskVO;

import net.sf.json.JSONObject;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FpcManageRestApiTest {

  @Value("${scorpio.system.user.app_key}")
  private String appkey;

  @Value("${scorpio.system.user.app_token}")
  private String appToken;

  @Value("${scorpio.system.manager.ip}")
  private String centerIP;

  @Value("${scorpio.system.manager.port}")
  private String centerPort;

  @Value("${scorpio.application.secret}")
  private String secret;

  private static HttpClient httpClient;

  @Autowired
  private StringEncryptor stringEncryptor;

  @Test
  public void t() {

    // machloop.application.secret : Machloop@123456!

    System.out.println(secret);
    String decrypt = stringEncryptor.decrypt("I4Z6DaO8l2FF6nyDeZE+WEWgAetH66YpTV5+u7hyB6I=");
    System.out.println(decrypt);

    String encrypt = stringEncryptor.encrypt("1024@Machloop@1024");
    System.out.println(encrypt);
  }

  @Before
  public void initHttpClient() {

    /*try {
      httpClient = new SSLClient();
      // httpClient = HttpClients.createDefault();
    } catch (Exception e) {
      e.printStackTrace();
    }*/

    SSLContext sslContext = null;
    try {
      sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
        @Override
        public boolean isTrusted(X509Certificate[] x509Certificates, String s)
            throws CertificateException {
          return true;
        }
      }).build();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    } catch (KeyStoreException e) {
      e.printStackTrace();
    }

    // 创建httpClient
    httpClient = HttpClients.custom().setSSLContext(sslContext)
        .setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

  }

  @Test
  public void webshark() {
    HttpGet httpGet = new HttpGet(
        "https://10.0.4.161:443/manager/restapi/fpc-v1/webshark/analysis?queryId=c0e14950-a8f0-11ec-855e-938b366647a3&filePath=/opt/data1/capcache/session/c0e14950-a8f0-11ec-855e-938b366647a3.pcap&type=analyze&parameter=%7B%22req%22%3A%22status%22%2C%22type%22%3A%22analyze%22%7D");
    sendRequest(httpGet);
  }

  /*******************************************************网络接口查询测试-begin*********************************************************/

  /**
   * 	网络接口查询测试
   */
  @Test
  public void queryDeviceNetifs() {
    HttpGet httpGet = new HttpGet(
        "http://" + centerIP + ":" + centerPort + "/restapi/fpc-v1/appliance/device-netifs");
    sendRequest(httpGet);
  }

  /*******************************************************网络接口查询测试-end*********************************************************/

  /*******************************************************查询任务测试-begin*********************************************************/

  /**
   * 	新建查询任务接口测试(任务类型：导出PCAP文件)
   */
  @Test
  public void createPcapTask() throws JsonProcessingException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    HttpPost httpPost = new HttpPost("https://" + centerIP + ":" + centerPort
        + "/manager/restapi/fpc-v1/appliance/transmition-tasks");
    TransmitTaskVO transmitTaskVO = new TransmitTaskVO();
    transmitTaskVO.setName("pcap_test_3");
    transmitTaskVO.setFilterStartTime(sdf.format(DateUtils.addHours(new Date(), -1)));
    transmitTaskVO.setFilterEndTime(sdf.format(new Date()));
    transmitTaskVO.setFilterIngestNetif("ALL");
    transmitTaskVO.setMode(0);
    transmitTaskVO.setFilterConditionType("0");

    List<SixTuple> list = new ArrayList<>();
    list.add(new SixTuple("192.168.0.1/8", 8080, "10.0.0.54", 443, "UDP", 1000));
    list.add(new SixTuple("192.168.0.1", 8080, "10.0.0.54", 443, "TCP", 1000));

    transmitTaskVO.setFilterTupleArray(list);
    // transmitTaskVO.setFilterSixTuple("");
    // transmitTaskVO.setFilterBpf("src host 106.75.240.122 and dst host 10.0.0.15 and src port 80
    // and dst port 51395 and tcp");
    transmitTaskVO.setDescription("test");

    JSONObject taskJsonObject = JSONObject.fromObject(transmitTaskVO);
    String content = taskJsonObject.toString();
    HttpEntity params = new StringEntity(content, "UTF-8");
    httpPost.setEntity(params);
    sendRequest(httpPost);
  }

  public static void main(String[] args) {
    List<SixTuple> list = new ArrayList<>();
    list.add(new SixTuple("10.0.0.121", 8080, "10.0.0.54", 443, "TCP", 1000));
    list.add(new SixTuple("10.0.0.122", 8080, "10.0.0.54", 443, "UDP", 1000));
    System.out.println("");
  }

  /**
   *  新建查询任务接口测试(任务类型：重放)
   */
  @Test
  public void createReplayTask() throws JsonProcessingException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    HttpPost httpPost = new HttpPost("https://" + centerIP + ":" + centerPort
        + "/manager/restapi/fpc-v1/appliance/transmition-tasks");
    TransmitTaskVO transmitTaskVO = new TransmitTaskVO();
    transmitTaskVO.setName("replay_test");
    transmitTaskVO.setFilterStartTime(sdf.format(DateUtils.addHours(new Date(), -1)));
    transmitTaskVO.setFilterEndTime(sdf.format(new Date()));
    transmitTaskVO.setFilterIngestNetif("ALL");
    transmitTaskVO.setMode(1);
    transmitTaskVO.setReplayRateUnit(0);
    transmitTaskVO.setReplayNetif("Port1");
    transmitTaskVO.setReplayRate(131072);
    transmitTaskVO.setFilterBpf("10.0.0.30");
    transmitTaskVO.setDescription("test");

    JSONObject taskJsonObject = JSONObject.fromObject(transmitTaskVO);
    String content = taskJsonObject.toString();
    HttpEntity params = new StringEntity(content, "UTF-8");
    httpPost.setEntity(params);
    sendRequest(httpPost);
  }

  /**
   * 	停止查询任务接口测试
   */
  @Test
  public void stopTask() {
    HttpPost httpPost = new HttpPost("https://" + centerIP + ":" + centerPort
        + "/manager/restapi/fpc-v1/appliance/transmition-tasks/pBmi-2wBrkn1Svtioun2/operations");
    HttpEntity params = new StringEntity("{\"action\":3}", "UTF-8");
    httpPost.setEntity(params);
    sendRequest(httpPost);
  }

  /**
   *  删除查询任务接口测试
   */
  @Test
  public void deleteTask() {
    HttpPost httpPost = new HttpPost("https://" + centerIP + ":" + centerPort
        + "/manager/restapi/fpc-v1/appliance/transmition-tasks/AkbYuW0BEndIlJndSDXO/operations");
    HttpEntity params = new StringEntity("{\"action\":4}", "UTF-8");
    httpPost.setEntity(params);
    sendRequest(httpPost);
  }

  /**
   * 	下载任务文件接口测试
   */
  @Test
  public void downloadTaskFile() {
    HttpGet httpGet = new HttpGet("https://" + centerIP + ":" + centerPort
        + "/manager/restapi/fpc-v1/appliance/transmition-tasks/mRmS-2wBrkn1SvtiK-n9/files");
    sendRequest(httpGet);
  }

  /*******************************************************查询任务测试-end*********************************************************/

  /**
   * send request code
   * @param request 请求体
   */
  private void sendRequest(HttpRequestBase request) {
    HttpResponse httpResponse;
    String timestamp = Long.toString(System.currentTimeMillis());

    try {
      request.setHeader("Content-Type", "application/json");
      request.setHeader("appKey", appkey);
      request.setHeader("timestamp", timestamp);
      request.setHeader("signature",
          SignatureUtil.generateManageApiSig(appkey, appToken, timestamp));

      httpResponse = httpClient.execute(request);
      HttpEntity httpEntity = httpResponse.getEntity();

      String result = "";
      if (httpEntity != null) {
        result = EntityUtils.toString(httpEntity, "utf-8");
      }
      System.out.println(format(result));
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      request.releaseConnection();
    }
  }

  /**
   * 	格式化json
   * @param jsonStr
   * @return
   */
  private String format(String jsonStr) {
    int level = 0;
    StringBuffer jsonForMatStr = new StringBuffer();
    for (int i = 0; i < jsonStr.length(); i++) {
      char c = jsonStr.charAt(i);
      if (level > 0 && '\n' == jsonForMatStr.charAt(jsonForMatStr.length() - 1)) {
        jsonForMatStr.append(getLevelStr(level));
      }
      switch (c) {
        case '{':
        case '[':
          jsonForMatStr.append(c + "\n");
          level++;
          break;
        case ',':
          jsonForMatStr.append(c + "\n");
          break;
        case '}':
        case ']':
          jsonForMatStr.append("\n");
          level--;
          jsonForMatStr.append(getLevelStr(level));
          jsonForMatStr.append(c);
          break;
        default:
          jsonForMatStr.append(c);
          break;
      }
    }

    return jsonForMatStr.toString();

  }

  private String getLevelStr(int level) {
    StringBuffer levelStr = new StringBuffer();
    for (int levelI = 0; levelI < level; levelI++) {
      levelStr.append("\t");
    }
    return levelStr.toString();
  }

}
