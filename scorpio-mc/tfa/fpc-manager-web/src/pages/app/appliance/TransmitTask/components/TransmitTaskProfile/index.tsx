import { ONE_KILO_1024 } from '@/common/dict';
import type { ConnectState } from '@/models/connect';
import type {
  ICityMap,
  ICountryMap,
  IProvinceMap,
} from '@/pages/app/configuration/Geolocation/typings';
import type { ILogicalSubnetMap } from '@/pages/app/configuration/LogicalSubnet/typings';
import type { INetworkMap } from '@/pages/app/configuration/Network/typings';
import type { IApplicationMap } from '@/pages/app/configuration/SAKnowledge/typings';
import { bytesToSize, formatDuration, parseObjJson } from '@/utils/utils';
import { CaretRightOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import {
  Alert,
  Card,
  Col,
  Collapse,
  Descriptions,
  Divider,
  Empty,
  Form,
  Progress,
  Row,
  Spin,
  Tag,
  Tooltip,
} from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import numeral from 'numeral';
import React, { useMemo } from 'react';
import type { IL7ProtocolMap } from '../../../Metadata/typings';
import {
  FILTER_RAW_TYPE_MAP,
  IExecutionTrace,
  IFilterTuple,
  IIpChg,
  IIpTunnel,
  IMacChg,
  ITransmitTask,
  IVlanProcess,
} from '../../typings';
import { EIpChgMode, EMacChgMode, EVlanProcesslMode, EVlanProcessType } from '../../typings';
import {
  EFilterConditionType,
  EIpTunnelCheckSum,
  EIpTunnelMode,
  EReplayRateUnit,
  ETaskSeekStatus,
  ETransmitMode,
  ETransmitTaskState,
  EWriteAllData,
  FILTER_CONDITION_TYPE_MAP,
  REPLAY_RATE_UNIT_MAP,
  TASK_MODE_MAP,
  TASK_SEEK_STATUS_MAP,
} from '../../typings';
import { extraDesc, parseFilterRawJson } from '../FilterRaw';
import {
  FIELD_APPLICATION_ID,
  FIELD_CITY_ID,
  FIELD_COUNTRY_ID,
  FIELD_DEST_CITY_ID,
  FIELD_DEST_COUNTRY_ID,
  FIELD_DEST_PROVINCE_ID,
  FIELD_L7_PROTOCOL,
  FIELD_PROVINCE_ID,
  FIELD_SOURCE_CITY_ID,
  FIELD_SOURCE_COUNTRY_ID,
  FIELD_SOURCE_PROVINCE_ID,
  filterTupleExtra,
  FILTER_RULE_FIELD_LIST,
  parseFilterTupleJson,
} from '../FilterTuple';
import { parseIpTunnelJson } from '../IpTunnel';
import {
  ALL_NETWORK_KEY,
  getReplayForwardActionInfo,
  MAX_GIGA,
  MAX_KBPS,
  MAX_PPS,
  MIN_KBPS,
} from '../TransmitTaskForm';
import styles from './index.less';

const FormItem = Form.Item;
const formLayout = {
  labelCol: { span: 3 },
  wrapperCol: { span: 20 },
};

const metricFieldLayout = {
  labelCol: { span: 7 },
};

const formTailLayout = {
  labelCol: { span: 3 },
  wrapperCol: { span: 20, offset: 3 },
};

/** 计算任务状态的显示标签 */
export const computedTaskStateText = (task: ITransmitTask): string => {
  // 任务状态
  let taskStateText = '--';
  const { state, executionStartTime, executionEndTime } = task;
  // 任务开始，有开始时间，没有结束时间：正在执行
  if (state === ETransmitTaskState.START && executionStartTime && !executionEndTime) {
    taskStateText = '执行中';
  } else if (state === ETransmitTaskState.START && !executionStartTime) {
    // 任务开始，并且没有开始时间：等待执行
    taskStateText = '等待执行';
  } else if (state === ETransmitTaskState.STOPPED && executionStartTime) {
    taskStateText = '正在停止';
  } else if (state === ETransmitTaskState.STOPPED && !executionStartTime) {
    taskStateText = '已停止';
  } else if (state === ETransmitTaskState.FINISHED) {
    taskStateText = '已完成';
  }
  return taskStateText;
};

const findNameByValue = (value: string, list: any[]) => {
  if (!Array.isArray(list)) {
    return '--';
  }
  const result = list.find((item) => item.value === value);
  return result ? result.label : '--';
};

interface ITransmitTaskProfileProps {
  loading: boolean;
  detail: ITransmitTask;

  allApplicationMap: IApplicationMap;
  allL7ProtocolMap: IL7ProtocolMap;
  allNetworkMap: INetworkMap;
  allLogicalSubnetMap: ILogicalSubnetMap;
  allCountryMap: ICountryMap;
  allProvinceMap: IProvinceMap;
  allCityMap: ICityMap;
  transmitTaskFileLimitBytes: number;
  queryStorageSpaceLoading: boolean | undefined;
}
const TransmitTaskProfile: React.FC<ITransmitTaskProfileProps> = ({
  detail,
  allApplicationMap,
  allL7ProtocolMap,
  allNetworkMap,
  allLogicalSubnetMap,
  allCountryMap,
  allProvinceMap,
  allCityMap,
  transmitTaskFileLimitBytes,

  loading,
  queryStorageSpaceLoading,
}) => {
  /** 根据字段计判断显示字段的显示标签 */
  const renderOperandText = (field: string, ruleGroup: IFilterTuple, index?: number) => {
    const targetValue = index !== undefined ? ruleGroup[field][index] : ruleGroup[field];
    if (field === FIELD_APPLICATION_ID) {
      return allApplicationMap[targetValue] ? allApplicationMap[targetValue].nameText : targetValue;
    }
    if (field === FIELD_L7_PROTOCOL) {
      return allL7ProtocolMap[targetValue] ? allL7ProtocolMap[targetValue].nameText : targetValue;
    }
    if (
      field === FIELD_COUNTRY_ID ||
      field === FIELD_SOURCE_COUNTRY_ID ||
      field === FIELD_DEST_COUNTRY_ID
    ) {
      return allCountryMap[targetValue] ? allCountryMap[targetValue].fullName : targetValue;
    }
    if (
      field === FIELD_PROVINCE_ID ||
      field === FIELD_SOURCE_PROVINCE_ID ||
      field === FIELD_DEST_PROVINCE_ID
    ) {
      return allProvinceMap[targetValue] ? allProvinceMap[targetValue].fullName : targetValue;
    }
    if (field === FIELD_CITY_ID || field === FIELD_SOURCE_CITY_ID || field === FIELD_DEST_CITY_ID) {
      return allCityMap[targetValue] ? allCityMap[targetValue].fullName : targetValue;
    }
    return targetValue;
  };

  const {
    id,
    state,
    filterConditionType,
    executionStartTime,
    executionEndTime,
    filterNetworkId,
    filterPacketFileName,
  } = detail;

  // 处理规则条件
  let tupleList: IFilterTuple[] = [];
  if (detail.filterTuple) {
    tupleList = parseFilterTupleJson(detail.filterTuple);
  }

  // 内容匹配
  const filterRawList = useMemo(() => {
    return parseFilterRawJson(detail.filterRaw);
  }, [detail.filterRaw]);

  // 隧道封装
  const ipTunnelObj: IIpTunnel = useMemo(() => {
    return parseIpTunnelJson(detail.ipTunnel);
  }, [detail.ipTunnel]);

  const [vlanProcess, macChg, ipChg] = useMemo(() => {
    if (!detail.replayRule) {
      return [];
    }
    const replayRule = JSON.parse(detail.replayRule);
    const ipChg = replayRule.find((rule: IIpChg) => rule.type === 'ipChg');
    const macChg = replayRule.find((rule: IMacChg) => rule.type === 'macChg');
    const vlanProcess = replayRule.find((rule: IVlanProcess) => rule.type === 'vlanProcess');
    return [vlanProcess || undefined, macChg || undefined, ipChg || undefined];
  }, [detail.replayRule]);
  // 转发策略
  const currentForwardAction = getReplayForwardActionInfo(
    detail.forwardAction,
    transmitTaskFileLimitBytes,
  );
  console.log(vlanProcess, macChg, ipChg);
  // 任务执行 json
  const executionTraceObj = parseObjJson(detail.executionTrace) as IExecutionTrace;

  /** 任务检索状态显示文案 */
  const taskSeekStatusText = useMemo(() => {
    let seekStatusText = TASK_SEEK_STATUS_MAP[executionTraceObj?.seekStatus];
    if (
      executionTraceObj?.seekStatus === ETaskSeekStatus.FINISHED &&
      state !== ETransmitTaskState.FINISHED
    ) {
      seekStatusText = '检索完成（正在读取数据）';
    }
    return seekStatusText;
  }, [executionTraceObj?.seekStatus, state]);

  if (!id) {
    return <Empty description="没有找到相关查询任务" />;
  }

  const getNetworkName = () => {
    if (filterPacketFileName !== null) {
      return filterPacketFileName;
    }
    if (!filterNetworkId) {
      return;
    }
    if (filterNetworkId === ALL_NETWORK_KEY) {
      return '全部网络';
    }
    const info = { ...allNetworkMap, ...allLogicalSubnetMap }[filterNetworkId];
    return info?.name || `[已删除: ${filterNetworkId}]`;
  };

  return (
    <Spin spinning={loading || queryStorageSpaceLoading}>
      <Form className={styles.profileForm}>
        <FormItem key="id" {...formLayout} label="任务id">
          <span className="ant-form-text">{detail.id}</span>
        </FormItem>
        <FormItem key="name" {...formLayout} label="名称">
          <span className="ant-form-text">{detail.name}</span>
        </FormItem>
        <FormItem key="taskState" {...formLayout} label="任务状态">
          <span className="ant-form-text">{computedTaskStateText(detail)}</span>
        </FormItem>
        <FormItem key="executionProgress" {...formLayout} label="任务执行进度">
          <Progress
            style={{ width: 200 }}
            percent={detail.executionProgress || 0}
            status="active"
          />
        </FormItem>
        <FormItem key="executionStartTime" {...formLayout} label="任务开始时间">
          <span className="ant-form-text">
            {executionStartTime ? moment(executionStartTime).format('YYYY-MM-DD HH:mm:ss') : '--'}
          </span>
        </FormItem>
        <FormItem key="executionEndTime" {...formLayout} label="任务结束时间">
          <span className="ant-form-text">
            {executionEndTime ? moment(executionEndTime).format('YYYY-MM-DD HH:mm:ss') : '--'}
          </span>
        </FormItem>
        <Divider dashed />
        <FormItem key="filterStartTime" {...formLayout} label="时间范围">
          <span className="ant-form-text">
            开始时间：
            {detail.filterStartTime && moment(detail.filterStartTime).format('YYYY-MM-DD HH:mm:ss')}
          </span>
        </FormItem>
        <FormItem key="filterEndTime" {...formTailLayout}>
          <span className="ant-form-text">
            截止时间：
            {detail.filterEndTime && moment(detail.filterEndTime).format('YYYY-MM-DD HH:mm:ss')}
          </span>
        </FormItem>
        <FormItem key="filterIngestNetif" {...formTailLayout}>
          <span className="ant-form-text">
            {filterNetworkId ? '网络:' : '离线任务:'}
            {getNetworkName()}
          </span>
        </FormItem>
        {/* 选择 BPF 语法或者是六元组 */}
        <FormItem key="filterConditionType" {...formTailLayout} style={{ marginBottom: 0 }}>
          <span className="ant-form-text">
            过滤条件类型：{FILTER_CONDITION_TYPE_MAP[filterConditionType]}
          </span>
        </FormItem>
        {/* {detail.filterConditionType === EFilterConditionType.BPF && ( */}
        <FormItem key="filterBpf" {...formTailLayout}>
          <span className="ant-form-text">BPF语句：{detail.filterBpf || '[空]'}</span>
        </FormItem>
        {/* )} */}
        {/* {detail.filterConditionType === EFilterConditionType.TUPLE && ( */}
        <FormItem
          key="filterTuple"
          {...formTailLayout}
          extra={
            <Collapse
              bordered={false}
              defaultActiveKey={[]}
              expandIcon={({ isActive }) => <CaretRightOutlined rotate={isActive ? 90 : 0} />}
            >
              <Collapse.Panel
                header={`${FILTER_CONDITION_TYPE_MAP[filterConditionType]}规则说明`}
                key="1"
                className={styles.panel}
              >
                {filterTupleExtra}
              </Collapse.Panel>
            </Collapse>
          }
        >
          <Card bordered={false} bodyStyle={{ padding: 0 }} className={styles.tupleWrap}>
            {tupleList.length === 0 && '[没有配置规则条件]'}
            {tupleList?.map((ruleObj) => {
              const tmpRuleObj = (() => {
                const res: any = {};
                Object.keys(ruleObj)?.forEach((key) => {
                  if (!Array.isArray(ruleObj[key])) {
                    res[key] = ruleObj[key].replace('NOT_', '');
                    return;
                  }
                  res[key] = ruleObj[key]?.map((item: any) => item.replace('NOT_', ''));
                });
                return res;
              })();
              return (
                <div key={JSON.stringify(ruleObj)} className={styles.ruleItem}>
                  {Object.keys(ruleObj)?.map((field) => {
                    if (typeof ruleObj[field] === 'string') {
                      if (ruleObj[field] === '') {
                        return null;
                      }
                      const tmpOperator =
                        renderOperandText(field, ruleObj).split('NOT_').length > 1 ? '!=' : '=';
                      return (
                        <Tag color="blue" key={`${field}_${ruleObj[field]}`}>
                          {findNameByValue(field, FILTER_RULE_FIELD_LIST)}
                          {tmpOperator}
                          {renderOperandText(field, tmpRuleObj)}
                        </Tag>
                      );
                    }
                    return (ruleObj[field] as any[])?.map((item, index) => {
                      const tmpOperator = item.split('NOT_').length > 1 ? '!=' : '=';
                      return (
                        <Tag color="blue" key={`${field}_${item}`}>
                          {findNameByValue(field, FILTER_RULE_FIELD_LIST)}
                          {tmpOperator}
                          {renderOperandText(field, tmpRuleObj, index)}
                        </Tag>
                      );
                    });
                  })}
                </div>
              );
            })}
          </Card>
        </FormItem>
        {/* )} */}
        <FormItem
          key="filterRaw"
          {...formLayout}
          label="内容匹配"
          extra={
            <Collapse
              bordered={false}
              defaultActiveKey={[]}
              expandIcon={({ isActive }) => <CaretRightOutlined rotate={isActive ? 90 : 0} />}
            >
              <Collapse.Panel header="内容匹配规则说明" key="1" className={styles.panel}>
                {extraDesc}
              </Collapse.Panel>
            </Collapse>
          }
        >
          <Card bordered={false} bodyStyle={{ padding: 0 }} className={styles.tupleWrap}>
            {filterRawList.length === 0 && '[没有配置内容匹配规则]'}
            {filterRawList?.map((row) => (
              <div key={row.id} className={styles.ruleItem}>
                {row.group?.map((rule) => (
                  <Tag color="blue" key={rule.id}>
                    {FILTER_RAW_TYPE_MAP[rule.type]}={rule.value}
                  </Tag>
                ))}
              </div>
            ))}
          </Card>
        </FormItem>
        <FormItem key="mode" {...formLayout} label="导出模式">
          <span className="ant-form-text">{TASK_MODE_MAP[detail.mode]}</span>
        </FormItem>
        {detail.mode === ETransmitMode.REPLAY && [
          <>
            <FormItem {...formLayout} label="转发接口">
              <span className="ant-form-text">{detail.replayNetif}</span>
            </FormItem>
            <FormItem
              {...formLayout}
              label={
                <span>
                  转发速率&nbsp;
                  <Tooltip
                    title={
                      detail.replayRateUnit === EReplayRateUnit.KBPS
                        ? `最小支持 ${MIN_KBPS}Kbps，最大支持 ${MAX_GIGA}Gbps（${numeral(
                            MAX_KBPS,
                          ).format('0,0')}Kbps）`
                        : `最大支持 ${numeral(MAX_PPS).format('0,0')}pps`
                    }
                  >
                    <QuestionCircleOutlined />
                  </Tooltip>
                </span>
              }
            >
              <span className="ant-form-text">{detail.replayRate}</span>
              <span className="ant-form-text">{REPLAY_RATE_UNIT_MAP[detail.replayRate]}</span>
            </FormItem>
            <FormItem
              {...formLayout}
              label={
                <span>
                  转发策略&nbsp;
                  <Tooltip title={detail.forwardAction ? currentForwardAction.description : ''}>
                    <QuestionCircleOutlined />
                  </Tooltip>
                </span>
              }
            >
              <span className="ant-form-text">
                {currentForwardAction && currentForwardAction.label}
              </span>
            </FormItem>
            {ipChg ? (
              <FormItem {...formLayout} label={<span>IP修改</span>}>
                <span className="ant-form-text">
                  {ipChg?.mode === EIpChgMode.NOT_CHANGE
                    ? '不修改'
                    : ipChg?.rule
                    ? `${ipChg?.rule?.sourceIp} → ${ipChg?.rule?.destIp}`
                    : ''}
                </span>
              </FormItem>
            ) : (
              ''
            )}

            {/* 隧道封装 */}
            <FormItem {...formLayout} label="隧道封装">
              <span className="ant-form-text">
                {ipTunnelObj.mode === EIpTunnelMode.NONE && '不封装'}
                {ipTunnelObj.mode === EIpTunnelMode.GRE && 'GRE封装'}
                {ipTunnelObj.mode === EIpTunnelMode.VXLAN && 'VXLAN封装'}
              </span>

              {(ipTunnelObj.mode === EIpTunnelMode.GRE ||
                ipTunnelObj.mode === EIpTunnelMode.VXLAN) && (
                <Descriptions bordered size="small" column={2} className={styles.description}>
                  <Descriptions.Item label="源MAC">
                    {ipTunnelObj.params.sourceMac}
                  </Descriptions.Item>
                  <Descriptions.Item label="目的MAC">
                    {ipTunnelObj.params.destMac}
                  </Descriptions.Item>
                  <Descriptions.Item label="源IP">{ipTunnelObj.params.sourceIp}</Descriptions.Item>
                  <Descriptions.Item label="目的IP">{ipTunnelObj.params.destIp}</Descriptions.Item>
                  {ipTunnelObj.mode === EIpTunnelMode.GRE && (
                    <>
                      <Descriptions.Item label="KEY">{ipTunnelObj.params.key}</Descriptions.Item>
                      <Descriptions.Item label="是否计算校验和">
                        {ipTunnelObj.params.checksum === EIpTunnelCheckSum.YES ? '计算' : '不计算'}
                      </Descriptions.Item>
                    </>
                  )}
                  {ipTunnelObj.mode === EIpTunnelMode.VXLAN && (
                    <>
                      <Descriptions.Item label="源端口">
                        {ipTunnelObj.params.sourcePort}
                      </Descriptions.Item>
                      <Descriptions.Item label="目的端口">
                        {ipTunnelObj.params.destPort}
                      </Descriptions.Item>
                      <Descriptions.Item label="VNID">{ipTunnelObj.params.vnid}</Descriptions.Item>
                    </>
                  )}
                </Descriptions>
              )}
            </FormItem>
          </>,
        ]}
        {vlanProcess ? (
          <FormItem
            {...formLayout}
            label={<span>VLAN处理</span>}
            extra={
              vlanProcess?.mode === EVlanProcesslMode.CHGCLANID ? (
                '新增VALN会在重放时对所有包统一处理，更改VLANID功能可以设置特定VLANID修改为新设置的VLANID'
              ) : vlanProcess?.mode === EVlanProcesslMode.NEWVLAN ? (
                <Collapse
                  bordered={false}
                  defaultActiveKey={[]}
                  expandIcon={({ isActive }) => <CaretRightOutlined rotate={isActive ? 90 : 0} />}
                >
                  <Collapse.Panel header="新增VLAN说明" key="1" className={styles.panel}>
                    <section>
                      <ul style={{ listStyle: 'decimal', paddingLeft: 20 }}>
                        <li>设置VLANID会给所有无VLAN的包增加一个VLAN头</li>
                        <li>
                          已有VLAN头可选择处理：新增VLAN，原有VALN外嵌套一个。替换VLANID，直接替换原有VLANID
                        </li>
                      </ul>
                    </section>
                  </Collapse.Panel>
                </Collapse>
              ) : (
                ''
              )
            }
          >
            <span className="ant-form-text">
              {vlanProcess?.mode === EVlanProcesslMode.IGNORE ? (
                '不处理'
              ) : vlanProcess?.mode === EVlanProcesslMode.NEWVLAN ? (
                <>
                  <Row>
                    <Col>新增VLAN</Col>
                  </Row>
                  <Row>
                    <Col>VLAID:</Col>
                    <Col>{` ${vlanProcess?.rule?.vlanId}`}</Col>
                  </Row>
                  <Row>
                    <Col>已有VLAN头的包处理: </Col>
                    <Col>
                      {vlanProcess?.rule?.processType === EVlanProcessType.NEWVLAN
                        ? ' 新增VLAN'
                        : ' 替换VLANID'}
                    </Col>
                  </Row>
                </>
              ) : vlanProcess?.mode === EVlanProcesslMode.CHGCLANID ? (
                <>
                  <Row>
                    <Col>更改VLANID</Col>
                  </Row>
                  {(vlanProcess?.rule?.vlanIdAlteration || []).map((alteration: any) => {
                    const { source, target } = alteration;
                    return (
                      <Row>
                        <Col>{`更改前VLANID:${source} → 更改后VLANID:${target}`}</Col>
                      </Row>
                    );
                  })}
                  <Row>
                    <Col>
                      {vlanProcess?.rule?.extraVlanIdRule === '1'
                        ? '不命中上述VLANID时,继续回放'
                        : vlanProcess?.rule?.extraVlanIdRule === '0'
                        ? '不命中上述VLAN时直接丢包不回放'
                        : ''}
                    </Col>
                  </Row>
                </>
              ) : (
                ''
              )}
            </span>
          </FormItem>
        ) : (
          ''
        )}
        {macChg ? (
          <FormItem {...formLayout} label={<span>MAC修改</span>}>
            <span className="ant-form-text">
              {macChg?.mode === EMacChgMode.NOT_CHANGE
                ? '不修改'
                : macChg?.rule
                ? `${macChg?.rule?.sourceMac} → ${macChg?.rule?.destMac}`
                : ''}
            </span>
          </FormItem>
        ) : (
          ''
        )}
        <FormItem key="source" {...formLayout} label="任务来源">
          <span className="ant-form-text">{detail.source}</span>
        </FormItem>
        <FormItem key="description" {...formLayout} label="描述信息">
          <span className="ant-form-text">{detail.description}</span>
        </FormItem>
        {/* 任务执行统计 */}
        {Object.keys(executionTraceObj).length > 0 && (
          <>
            <Divider dashed />
            <FormItem>
              {state === ETransmitTaskState.FINISHED && (
                <Alert
                  type={
                    executionTraceObj.isWriteAllData === EWriteAllData.NO ? 'warning' : 'success'
                  }
                  showIcon
                  message={
                    executionTraceObj.isWriteAllData === EWriteAllData.NO ? (
                      <>
                        <div>落盘PCAP文件中的数据仅包含查询条件中的部分数据。</div>
                        <div>
                          如需获得全量数据，可以缩小时间范围或修改查询条件后，再次执行任务。
                        </div>
                      </>
                    ) : (
                      '落盘PCAP文件中的数据是查询条件中的全量数据。'
                    )
                  }
                />
              )}
            </FormItem>
            <FormItem {...metricFieldLayout} label="检索状态">
              <span className="ant-form-text">{taskSeekStatusText}</span>
            </FormItem>
            <FormItem {...metricFieldLayout} label="检索耗时">
              <span className="ant-form-text">
                {formatDuration(executionTraceObj.seekTime || 0)}
              </span>
            </FormItem>
            <FormItem {...metricFieldLayout} label="需要检索的总容量">
              <span className="ant-form-text">
                {bytesToSize(executionTraceObj.totalSeekBytes || 0)}
              </span>
            </FormItem>
            <FormItem {...metricFieldLayout} label="已检索的容量">
              <span className="ant-form-text">
                {bytesToSize(executionTraceObj.finishedSeekBytes || 0)}
              </span>
            </FormItem>
            <FormItem {...metricFieldLayout} label="落盘PCAP文件的数据量">
              <span className="ant-form-text">
                {bytesToSize(executionTraceObj.writeBytes || 0, 3, ONE_KILO_1024)}
              </span>
            </FormItem>
            <FormItem {...metricFieldLayout} label="落盘PCAP文件中的流数">
              <span className="ant-form-text">{executionTraceObj.writeFlowCount}</span>
            </FormItem>
            <FormItem {...metricFieldLayout} label="落盘PCAP文件中的包数">
              <span className="ant-form-text">{executionTraceObj.writePacketCount}</span>
            </FormItem>
            <FormItem {...metricFieldLayout} label="落盘PCAP文件数据的起始时间">
              <span className="ant-form-text">
                {executionTraceObj.writeDataStartTime
                  ? moment(executionTraceObj.writeDataStartTime * 1000).format(
                      'YYYY-MM-DD HH:mm:ss',
                    )
                  : 'null'}
              </span>
            </FormItem>
            <FormItem {...metricFieldLayout} label="落盘PCAP文件数据的结束时间">
              <span className="ant-form-text">
                {executionTraceObj.writeDataEndTime
                  ? moment(executionTraceObj.writeDataEndTime * 1000).format('YYYY-MM-DD HH:mm:ss')
                  : 'null'}
              </span>
            </FormItem>
          </>
        )}
      </Form>
    </Spin>
  );
};
export default connect(
  ({
    SAKnowledgeModel: { allApplicationMap },
    metadataModel: { allL7ProtocolMap },
    networkModel: { allNetworkMap },
    logicSubnetModel: { allLogicalSubnetMap },
    geolocationModel: { allCountryMap, allProvinceMap, allCityMap },
    storageSpaceModel: { transmitTaskFileLimitBytes },
    loading: { effects },
  }: ConnectState) => ({
    allApplicationMap,
    allL7ProtocolMap,
    allNetworkMap,
    allLogicalSubnetMap,
    allCountryMap,
    allProvinceMap,
    allCityMap,
    transmitTaskFileLimitBytes,
    queryStorageSpaceLoading: effects['storageSpaceModel/queryStorageSpaceSettings'],
  }),
)(TransmitTaskProfile);
