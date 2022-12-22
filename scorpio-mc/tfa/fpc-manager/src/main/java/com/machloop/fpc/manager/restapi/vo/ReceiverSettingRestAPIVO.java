package com.machloop.fpc.manager.restapi.vo;

import org.apache.commons.lang.StringUtils;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.base.LogAudit;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.fpc.common.FpcConstants;

import java.util.Map;

/**
 * @author fengtianyou
 *
 * create at 2021年9月27日, fpc-manager
*/
public class ReceiverSettingRestAPIVO implements LogAudit {

    private String id;
    private String name;
    private Map<String,String> protocolTopic;
    private String httpAction;
    private String httpActionExculdeUriSuffix;

    private String receiverAddress;
    private String kerberosCertification;
    private String keytabFilePath;
    private int keyRestoreTime;
    private String saslKerberosServiceName;
    private String saslKerberosPrincipal;
    private String securityProtocol;
    private String authenticationMechanism;

    private String state;

    private String operatorId;

    @Override
    public String toAuditLogText(int auditLogAction) {
        StringBuilder builder = new StringBuilder();
        switch (auditLogAction) {
            case LogHelper.AUDIT_LOG_ACTION_UPDATE:
                builder.append("更新发送配置：");
                break;
            default:
                return "";
        }
        builder.append("配置名称=").append(name).append(";");
        builder.append("Topic=").append(protocolTopic).append(";");
        if (protocolTopic.keySet().contains("HTTP")) {
            builder.append("http动作=")
                    .append(StringUtils.equals(httpAction, FpcConstants.HTTP_ACTION_SEND) ? "发送" : "过滤")
                    .append(";");
            builder.append("http后缀过滤/发送=").append(httpActionExculdeUriSuffix).append(";");
        }
        builder.append("接收者地址=").append(receiverAddress).append(";");
        builder.append("是否启用KERBEROS认证=")
                .append(StringUtils.equals(kerberosCertification, Constants.BOOL_NO) ? "未启用" : "启用")
                .append(";");
        builder.append("keytab文件路径=").append(keytabFilePath).append(";");
        builder.append("key尝试恢复时间（单位为ms）=").append(keyRestoreTime).append(";");
        builder.append("sasl.kerberos.service.name=").append(saslKerberosServiceName).append(";");
        builder.append("sasl.kerberos.principal=").append(saslKerberosPrincipal).append(";");
        builder.append("安全协议=").append(securityProtocol).append(";");
        builder.append("鉴权机制=").append(authenticationMechanism).append(";");
        builder.append("启用状态=").append(state).append(";");

        return builder.toString();
    }

    @Override
    public String toString() {
        return "ReceiverSettingVO [id=" + id + ", name=" + name + ", protocolTopic=" + protocolTopic
                + ", httpAction=" + httpAction + ", httpActionExculdeUriSuffix="
                + httpActionExculdeUriSuffix + ", receiverAddress=" + receiverAddress
                + ", kerberosCertification=" + kerberosCertification + ", keytabFilePath=" + keytabFilePath
                + ", keyRestoreTime=" + keyRestoreTime + ", saslKerberosServiceName="
                + saslKerberosServiceName + ", saslKerberosPrincipal=" + saslKerberosPrincipal
                + ", securityProtocol=" + securityProtocol + ", authenticationMechanism="
                + authenticationMechanism + ", state=" + state + ", operatorId=" + operatorId + "]";
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getProtocolTopic() {
        return protocolTopic;
    }

    public void setProtocolTopic(Map<String,String> protocolTopic) {
        this.protocolTopic = protocolTopic;
    }

    public String getHttpAction() {
        return httpAction;
    }

    public void setHttpAction(String httpAction) {
        this.httpAction = httpAction;
    }

    public String getHttpActionExculdeUriSuffix() {
        return httpActionExculdeUriSuffix;
    }

    public void setHttpActionExculdeUriSuffix(String httpActionExculdeUriSuffix) {
        this.httpActionExculdeUriSuffix = httpActionExculdeUriSuffix;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getKerberosCertification() {
        return kerberosCertification;
    }

    public void setKerberosCertification(String kerberosCertification) {
        this.kerberosCertification = kerberosCertification;
    }

    public String getKeytabFilePath() {
        return keytabFilePath;
    }

    public void setKeytabFilePath(String keytabFilePath) {
        this.keytabFilePath = keytabFilePath;
    }

    public int getKeyRestoreTime() {
        return keyRestoreTime;
    }

    public void setKeyRestoreTime(int keyRestoreTime) {
        this.keyRestoreTime = keyRestoreTime;
    }

    public String getSaslKerberosServiceName() {
        return saslKerberosServiceName;
    }

    public void setSaslKerberosServiceName(String saslKerberosServiceName) {
        this.saslKerberosServiceName = saslKerberosServiceName;
    }

    public String getSaslKerberosPrincipal() {
        return saslKerberosPrincipal;
    }

    public void setSaslKerberosPrincipal(String saslKerberosPrincipal) {
        this.saslKerberosPrincipal = saslKerberosPrincipal;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public String getAuthenticationMechanism() {
        return authenticationMechanism;
    }

    public void setAuthenticationMechanism(String authenticationMechanism) {
        this.authenticationMechanism = authenticationMechanism;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
