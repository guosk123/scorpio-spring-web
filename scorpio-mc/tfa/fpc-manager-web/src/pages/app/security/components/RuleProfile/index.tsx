import type { ConnectState } from '@/models/connect';
import {
  RuleDirectionOptions,
  RuleSignatureSeverityOptions,
  RuleStateOptions,
  RuleTargetOptions,
} from '@/pages/app/configuration/Suricata/common';
import {
  querySuricataRuleDetail,
  updateSuricataRule,
} from '@/pages/app/configuration/Suricata/service';
import type { ISuricataRule } from '@/pages/app/configuration/Suricata/typings';
import { ERuleSource, ERuleState } from '@/pages/app/configuration/Suricata/typings';
import { getLinkUrl } from '@/utils/utils';
import { EditOutlined } from '@ant-design/icons';
import { Button, Descriptions, message, Spin, Tooltip } from 'antd';
import { useCallback, useEffect, useState } from 'react';
import { history, useSelector } from 'umi';
import type { IMitreAttack, IRuleClasstype } from '../../typings';
import styles from './index.less';
interface Props {
  id: number;
}

const RuleProfile = (props: Props) => {
  const { id } = props;
  const [detail, setDetail] = useState<ISuricataRule>();
  const [loading, setLoading] = useState<boolean>(false);

  const mitreDict = useSelector<ConnectState, Record<string, IMitreAttack>>(
    (state) => state.suricataModel.mitreDict,
  );

  const classtypeDict = useSelector<ConnectState, Record<string, IRuleClasstype>>(
    (state) => state.suricataModel.classtypeDict,
  );

  useEffect(() => {
    setLoading(true);
    querySuricataRuleDetail(id).then((res) => {
      const { success, result } = res;
      if (success) {
        setDetail(result);
        setLoading(false);
      }
    });
  }, [id]);

  const updateSuricataRuleState = useCallback(() => {
    if (detail?.state) {
      setLoading(true);
      const suricateState = ERuleState.启用 == detail.state ? ERuleState.停用 : ERuleState.启用;
      updateSuricataRule({
        ...detail,
        state: suricateState,
      }).then((res) => {
        if (res.success) {
          setDetail({
            ...detail,
            state: suricateState,
          });
          message.success('修改成功');
          setLoading(false);
        }
      });
    }
  }, [detail]);

  // 当规则id为null时，表示该规则已被删除
  if (!detail?.sid) {
    return null;
  }

  return (
    <Spin spinning={loading} delay={1} className={styles.suricataRuleProfile}>
      <Descriptions
        title={
          <>
            <span style={{ fontSize: 15, fontWeight: 500 }}>规则详情</span>
            <Button
              style={{ border: 'none' }}
              size="small"
              icon={<EditOutlined />}
              disabled={detail?.source === ERuleSource.系统内置 && detail.id !== '1'}
              onClick={() => {
                history.push(
                  getLinkUrl(`/configuration/safety-analysis/suricata/rule/${id}/update`),
                );
              }}
            />
          </>
        }
        bordered
        column={4}
        size="small"
      >
        <Descriptions.Item label="源IP" span={1}>
          <Tooltip title={detail?.srcIp}>
            <span className="ant-form-text">{detail?.srcIp}</span>
          </Tooltip>
        </Descriptions.Item>
        <Descriptions.Item label="源端口">
          <span className="ant-form-text">{detail?.srcPort}</span>
        </Descriptions.Item>
        <Descriptions.Item label="目的IP" span={1}>
          <Tooltip title={detail?.destIp}>
            <span className="ant-form-text">{detail?.destIp}</span>
          </Tooltip>
        </Descriptions.Item>
        <Descriptions.Item label="目的端口">
          <span className="ant-form-text">{detail?.destPort}</span>
        </Descriptions.Item>
        <Descriptions.Item label="协议">
          <span className="ant-form-text">{detail?.protocol}</span>
        </Descriptions.Item>
        <Descriptions.Item label="方向">
          <span className="ant-form-text">
            {RuleDirectionOptions.find((item) => item.value === detail?.direction)?.label ||
              detail?.direction}
          </span>
        </Descriptions.Item>
        <Descriptions.Item label="规则描述" span={2}>
          <Tooltip title={detail?.msg}>
            <span className="ant-form-text">{detail?.msg}</span>
          </Tooltip>
        </Descriptions.Item>
        <Descriptions.Item label="规则正文" span={4}>
          <span className="ant-form-text">{detail?.content}</span>
        </Descriptions.Item>
        <Descriptions.Item label="动作">
          <span className="ant-form-text">{detail?.action === 'alert' ? '告警' : 'alert'}</span>
        </Descriptions.Item>
        <Descriptions.Item label="规则ID">
          <span className="ant-form-text">{detail?.sid}</span>
        </Descriptions.Item>
        <Descriptions.Item label="优先级">
          <span className="ant-form-text">{detail?.priority}</span>
        </Descriptions.Item>
        <Descriptions.Item label="规则分类">
          <span className="ant-form-text">{detail && classtypeDict[detail.classtypeId].name}</span>
        </Descriptions.Item>
        <Descriptions.Item label="战术分类">
          <span className="ant-form-text">{detail && mitreDict[detail.mitreTacticId].name}</span>
        </Descriptions.Item>
        <Descriptions.Item label="技术分类">
          <span className="ant-form-text">
            {detail?.mitreTechniqueId && mitreDict[detail.mitreTechniqueId].name}
          </span>
        </Descriptions.Item>

        <Descriptions.Item label="CVE编号">
          <span className="ant-form-text">{detail?.cve}</span>
        </Descriptions.Item>
        <Descriptions.Item label="CNNVD编号">
          <span className="ant-form-text">{detail?.cnnvd}</span>
        </Descriptions.Item>
        <Descriptions.Item label="严重级别">
          <span className="ant-form-text">
            {
              RuleSignatureSeverityOptions.find((item) => item.value === detail?.signatureSeverity)
                ?.label
            }
          </span>
        </Descriptions.Item>
        <Descriptions.Item label="受害方">
          <span className="ant-form-text">
            {RuleTargetOptions.find((item) => item.value === detail?.target)?.label}
          </span>
        </Descriptions.Item>
        <Descriptions.Item label="告警频率">
          <Tooltip title={detail?.threshold}>
            <span className="ant-form-text">{detail?.threshold}</span>
          </Tooltip>
        </Descriptions.Item>
        <Descriptions.Item label="应用状态">
          <span className="ant-form-text">
            {RuleStateOptions.find((item) => item.value === detail?.state)?.label}
            <Button
              size="small"
              disabled={detail?.source === ERuleSource.系统内置 && detail.id !== '1'}
              onClick={() => {
                updateSuricataRuleState();
              }}
              type="link"
              loading={loading}
            >
              {RuleStateOptions.find((item) => item.value !== detail?.state)?.label}
            </Button>
          </span>
        </Descriptions.Item>
      </Descriptions>
    </Spin>
  );
};

export default RuleProfile;
