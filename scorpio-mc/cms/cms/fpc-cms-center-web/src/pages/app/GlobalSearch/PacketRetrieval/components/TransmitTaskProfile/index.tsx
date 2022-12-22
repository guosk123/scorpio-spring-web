import { ONE_KILO_1024 } from '@/common/dict';
import type { ConnectState } from '@/models/connect';
import type {
  ICityMap,
  ICountryMap,
  IProvinceMap,
} from '@/pages/app/Configuration/Geolocation/typings';
import type { IApplicationMap } from '@/pages/app/Configuration/SAKnowledge/typings';
import { bytesToSize, formatDuration, parseObjJson } from '@/utils/utils';
import { CaretRightOutlined } from '@ant-design/icons';
import { Alert, Card, Collapse, Divider, Empty, Form, Progress, Spin, Tag } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import React, { useMemo } from 'react';
import type { IL7ProtocolMap } from '../../../../appliance/Metadata/typings';
import { FILTER_RAW_TYPE_MAP, IExecutionTrace, IFilterTuple, ITransmitTask } from '../../typings';
import {
  EFilterConditionType,
  ETaskSeekStatus,
  ETransmitTaskState,
  EWriteAllData,
  FILTER_CONDITION_TYPE_MAP,
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
  detail: ITransmitTask | any;
  allApplicationMap: IApplicationMap;
  allL7ProtocolMap: IL7ProtocolMap;
  allCountryMap: ICountryMap;
  allProvinceMap: IProvinceMap;
  allCityMap: ICityMap;
}
const TransmitTaskProfile: React.FC<ITransmitTaskProfileProps> = ({
  detail,
  allApplicationMap,
  allL7ProtocolMap,
  allCountryMap,
  allProvinceMap,
  allCityMap,
  loading,
}) => {
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

  const showDetail = useMemo(() => {
    const { sensorDetail, taskDetail } = detail;
    return {
      ...taskDetail,
      ...sensorDetail,
      sensorName: sensorDetail.fpcName,
    };
  }, [detail]);

  const { id, executionState, filterConditionType, executionStartTime, executionEndTime } =
    showDetail;

  // 处理规则条件
  let tupleList: IFilterTuple[] = [];
  if (showDetail.filterTuple) {
    tupleList = parseFilterTupleJson(showDetail.filterTuple);
  }

  // 内容匹配
  const filterRawList = useMemo(() => {
    return parseFilterRawJson(showDetail.filterRaw);
  }, [showDetail.filterRaw]);

  // 任务执行 json
  const executionTraceObj = parseObjJson(showDetail.executionTrace) as IExecutionTrace;

  /** 任务检索状态显示文案 */
  const taskSeekStatusText = useMemo(() => {
    let seekStatusText = TASK_SEEK_STATUS_MAP[executionTraceObj?.seekStatus];
    if (
      executionTraceObj?.seekStatus === ETaskSeekStatus.FINISHED &&
      executionState !== ETransmitTaskState.FINISHED
    ) {
      seekStatusText = '检索完成（正在读取数据）';
    }
    return seekStatusText;
  }, [executionTraceObj?.seekStatus, executionState]);

  if (!id) {
    return <Empty description="没有找到相关查询任务" />;
  }

  return (
    <Spin spinning={loading}>
      <Form className={styles.profileForm}>
        <FormItem key="id" {...formLayout} label="任务id">
          <span className="ant-form-text">{showDetail.id}</span>
        </FormItem>
        <FormItem key="name" {...formLayout} label="任务名称">
          <span className="ant-form-text">{showDetail.name}</span>
        </FormItem>
        <FormItem key="sensorName" {...formLayout} label="探针名称">
          <span className="ant-form-text">{showDetail.sensorName}</span>
        </FormItem>
        <FormItem key="taskState" {...formLayout} label="任务状态">
          <span className="ant-form-text">{showDetail.executionStateText}</span>
        </FormItem>
        <FormItem key="executionProgress" {...formLayout} label="任务执行进度">
          <Progress
            style={{ width: 200 }}
            percent={showDetail.executionProgress || 0}
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
        <FormItem key="filterStartTime" {...formLayout} label="过滤条件">
          <span className="ant-form-text">
            开始时间：
            {showDetail.filterStartTime &&
              moment(showDetail.filterStartTime).format('YYYY-MM-DD HH:mm:ss')}
          </span>
        </FormItem>
        <FormItem key="filterEndTime" {...formTailLayout}>
          <span className="ant-form-text">
            截止时间：
            {showDetail.filterEndTime &&
              moment(showDetail.filterEndTime).format('YYYY-MM-DD HH:mm:ss')}
          </span>
        </FormItem>
        {/* 选择 BPF 语法或者是六元组 */}
        <FormItem key="filterConditionType" {...formTailLayout} style={{ marginBottom: 0 }}>
          <span className="ant-form-text">
            过滤条件类型：{FILTER_CONDITION_TYPE_MAP[filterConditionType]}
          </span>
        </FormItem>
        {/* {showDetail.filterConditionType === EFilterConditionType.BPF && ( */}
        <FormItem key="filterBpf" {...formTailLayout}>
          <span className="ant-form-text">BPF语句：{showDetail.filterBpf || '[空]'}</span>
        </FormItem>
        {/* )} */}
        {/* {showDetail.filterConditionType === EFilterConditionType.TUPLE && ( */}
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
            {tupleList.map((ruleObj) => {
              const tmpRuleObj = (() => {
                const res: any = {};
                Object.keys(ruleObj).map((key) => {
                  if (!Array.isArray(ruleObj[key])) {
                    res[key] = ruleObj[key].replace('NOT_', '');
                    return;
                  }
                  res[key] = ruleObj[key].map((item: any) => item.replace('NOT_', ''));
                });
                return res;
              })();
              return (
                <div key={JSON.stringify(ruleObj)} className={styles.ruleItem}>
                  {Object.keys(ruleObj).map((field) => {
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
                    return (ruleObj[field] as any[]).map((item, index) => {
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
            {filterRawList.map((row) => (
              <div key={row.id} className={styles.ruleItem}>
                {row.group.map((rule) => (
                  <Tag color="blue" key={rule.id}>
                    {FILTER_RAW_TYPE_MAP[rule.type]}={rule.value}
                  </Tag>
                ))}
              </div>
            ))}
          </Card>
        </FormItem>
        <FormItem key="mode" {...formLayout} label="导出模式">
          <span className="ant-form-text">{TASK_MODE_MAP[showDetail.mode]}</span>
        </FormItem>
        <FormItem key="source" {...formLayout} label="任务来源">
          <span className="ant-form-text">{showDetail.source}</span>
        </FormItem>
        <FormItem key="description" {...formLayout} label="描述信息">
          <span className="ant-form-text">{showDetail.description}</span>
        </FormItem>
        {/* 任务执行统计 */}
        {Object.keys(executionTraceObj).length > 0 && (
          <>
            <Divider dashed />
            <FormItem>
              {executionState === ETransmitTaskState.FINISHED && (
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
            {/* <FormItem {...metricFieldLayout} label="落盘PCAP文件数据的起始时间">
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
            </FormItem> */}
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
    geolocationModel: { allCountryMap, allProvinceMap, allCityMap },
  }: ConnectState) => ({
    allApplicationMap,
    allL7ProtocolMap,
    allCountryMap,
    allProvinceMap,
    allCityMap,
  }),
)(TransmitTaskProfile);
