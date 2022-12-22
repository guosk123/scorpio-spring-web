import type { ConnectState } from '@/models/connect';
import { CaretDownOutlined, CaretUpOutlined, WarningOutlined } from '@ant-design/icons';
import { Alert, Button, Descriptions, Empty, PageHeader, Result, Skeleton, Tag } from 'antd';
import { isEmpty } from 'lodash';
import moment from 'moment';
import React, { Suspense, useEffect, useMemo, useState } from 'react';
import type { Dispatch, IScenarioTaskModelState } from 'umi';
import { history, useDispatch, useSelector, connect } from 'umi';
import { getTaskDurationTime, getTaskState, TASK_STATE_FINISHED } from '..';
import {
  ANALYSIS_SCENARIO_TYPE_BEACON,
  ANALYSIS_SCENARIO_TYPE_BRUTE_FORCE_RDP,
  ANALYSIS_SCENARIO_TYPE_BRUTE_FORCE_SSH,
  ANALYSIS_SCENARIO_TYPE_DYNAMIC_DOMAIN,
  ANALYSIS_SCENARIO_TYPE_IP_INTELLIGENCE,
  ANALYSIS_SCENARIO_TYPE_NONSTANDARD_PROTOCOL,
  ANALYSIS_SCENARIO_TYPE_SUSPICIOUS_HTTPS,
  CUSTOM_TEMPLATE_PREFIX,
} from '../components/ScenarioTaskForm';
import {
  getDataSourceInfo,
  getEvalFunctionDom,
  getGroupByInfo,
} from '../components/ScenarioTemplateProfile';
import type { IScenarioTask } from '../typings';
import CustomTemplateResult from './components/CustomTemplateResult';
import styles from './index.less';

const BeaconResult = React.lazy(() => import('./components/BeaconResult'));
const DynamicDomainResult = React.lazy(() => import('./components/DynamicDomainResult'));
const SuspiciousHttpsResult = React.lazy(() => import('./components/SuspiciousHttpsResult'));
const IpIntelligenceResult = React.lazy(() => import('./components/IpIntelligenceResult'));
const NonStandardProtocolResult = React.lazy(
  () => import('./components/NonStandardProtocolResult'),
);
const BruteForceResult = React.lazy(() => import('./components/BruteForceResult'));

export const ANALYSIS_RESULT_ID_PREFIX = 'analysis-result-id:';

// 倒序
export const SORT_DIRECTION_DESC = 'desc';
// 正序
export const SORT_DIRECTION_ASC = 'asc';

const ScenarioTaskResult: React.FC = () => {
  const [queryLoading, setQueryLoading] = useState(true);
  const dispatch = useDispatch<Dispatch>();
  const { scenarioCustomTemplateDetail, scenarioTaskDetail } = useSelector<
    ConnectState,
    IScenarioTaskModelState
  >((state) => state.scenarioTaskModel);

  const [templateDetailVisible, setTemplateDetailVisible] = useState(false);

  useEffect(() => {
    if (history.location.query?.id) {
      dispatch({
        type: 'scenarioTaskModel/queryScenarioTaskDetail',
        payload: {
          id: history.location.query?.id,
        },
      }).then((result: IScenarioTask) => {
        if (result.id && result.type.indexOf(CUSTOM_TEMPLATE_PREFIX) > -1) {
          const customTempid = result.type.replace(CUSTOM_TEMPLATE_PREFIX, '');
          dispatch({
            type: 'scenarioTaskModel/queryScenarioCustomTemplateDetail',
            payload: {
              id: customTempid,
            },
          }).then(() => {
            setQueryLoading(false);
          });
        } else {
          setQueryLoading(false);
        }
      });
    }

    return () => {
      dispatch({
        type: 'scenarioTaskModel/updateState',
        payload: {
          scenarioTaskDetail: {},
        },
      });
    };
  }, [dispatch]);

  const toogleTemplateDetail = () => {
    setTemplateDetailVisible((prevValue) => !prevValue);
  };

  const isCustomTemplate = useMemo(() => {
    return (
      !isEmpty(scenarioTaskDetail) && scenarioTaskDetail.type?.indexOf(CUSTOM_TEMPLATE_PREFIX) > -1
    );
  }, [scenarioTaskDetail]);

  const extraDom = isCustomTemplate
    ? [
        <Button
          onClick={toogleTemplateDetail}
          icon={templateDetailVisible ? <CaretDownOutlined /> : <CaretUpOutlined />}
        />,
      ]
    : null;

  const taskResult = useMemo(() => {
    if (scenarioTaskDetail.type === ANALYSIS_SCENARIO_TYPE_BEACON) {
      return <BeaconResult />;
    }
    if (scenarioTaskDetail.type === ANALYSIS_SCENARIO_TYPE_DYNAMIC_DOMAIN) {
      return <DynamicDomainResult />;
    }
    if (scenarioTaskDetail.type === ANALYSIS_SCENARIO_TYPE_SUSPICIOUS_HTTPS) {
      return <SuspiciousHttpsResult />;
    }
    if (scenarioTaskDetail.type === ANALYSIS_SCENARIO_TYPE_IP_INTELLIGENCE) {
      return <IpIntelligenceResult />;
    }
    if (scenarioTaskDetail.type === ANALYSIS_SCENARIO_TYPE_NONSTANDARD_PROTOCOL) {
      return <NonStandardProtocolResult />;
    }
    if (scenarioTaskDetail.type === ANALYSIS_SCENARIO_TYPE_BRUTE_FORCE_SSH) {
      return <BruteForceResult />;
    }
    if (scenarioTaskDetail.type === ANALYSIS_SCENARIO_TYPE_BRUTE_FORCE_RDP) {
      return <BruteForceResult />;
    }

    if (isCustomTemplate) {
      return <CustomTemplateResult />;
    }
    return null;
  }, [isCustomTemplate, scenarioTaskDetail.type]);

  const resultContent = useMemo(() => {
    if (scenarioTaskDetail.executionProgress !== 100) {
      return <Result status="warning" title="分析任务尚未执行完成" subTitle="请稍候查看分析结果" />;
    }
    // 执行完成了但是有错误信息
    if (scenarioTaskDetail.state !== TASK_STATE_FINISHED) {
      return (
        <Result
          status="warning"
          icon={<WarningOutlined />}
          title={scenarioTaskDetail.executionTrace || '分析异常'}
        />
      );
    }
    return <Suspense fallback={null}>{taskResult}</Suspense>;
  }, [
    scenarioTaskDetail.executionProgress,
    scenarioTaskDetail.executionTrace,
    scenarioTaskDetail.state,
    taskResult,
  ]);

  const renderTemplateDetail = () => {
    const { dataSource, groupBy, sliceTimeInterval, avgTimeInterval, filterSpl } =
      scenarioCustomTemplateDetail;

    return (
      <>
        <Descriptions.Item label="数据源">{getDataSourceInfo(dataSource).label}</Descriptions.Item>
        <Descriptions.Item label="计算方法">
          {getEvalFunctionDom(scenarioCustomTemplateDetail, 'vertical')}
        </Descriptions.Item>
        <Descriptions.Item label="按时间平均">
          {avgTimeInterval === 0 ? '不平均' : `${avgTimeInterval}s`}
        </Descriptions.Item>
        <Descriptions.Item label="按时间分片">
          {sliceTimeInterval === 0 ? '不分片' : `${sliceTimeInterval}s`}
        </Descriptions.Item>
        <Descriptions.Item label="分组" span={2}>
          {groupBy ? getGroupByInfo(groupBy).label : '不分组'}
        </Descriptions.Item>
        <Descriptions.Item label="过滤条件" span={2}>
          {filterSpl}
        </Descriptions.Item>
      </>
    );
  };

  const TaskDetail = () => (
    <PageHeader
      className={styles.header}
      title={scenarioTaskDetail.name}
      subTitle={scenarioTaskDetail.typeText}
      tags={
        isCustomTemplate ? <Tag color="magenta">自定义模板</Tag> : <Tag color="blue">内置模板</Tag>
      }
      extra={extraDom}
    >
      <Descriptions size="small" column={2}>
        <Descriptions.Item label="查询时间范围">
          {moment(scenarioTaskDetail.analysisStartTime).format('YYYY-MM-DD HH:mm:ss')} -{' '}
          {moment(scenarioTaskDetail.analysisEndTime).format('YYYY-MM-DD HH:mm:ss')}
        </Descriptions.Item>
        <Descriptions.Item label="任务开始时间">
          {scenarioTaskDetail.executionStartTime &&
            moment(scenarioTaskDetail.executionStartTime).format('YYYY-MM-DD HH:mm:ss')}
        </Descriptions.Item>
        <Descriptions.Item label="任务状态">{getTaskState(scenarioTaskDetail)}</Descriptions.Item>
        <Descriptions.Item label="执行时间">
          {getTaskDurationTime(scenarioTaskDetail)}
        </Descriptions.Item>
        {isCustomTemplate && templateDetailVisible && renderTemplateDetail()}
      </Descriptions>
    </PageHeader>
  );

  if (queryLoading) {
    return <Skeleton />;
  }

  if (!scenarioTaskDetail.id) {
    return <Empty description="分析任务不存在或已被删除" />;
  }

  if (isCustomTemplate && !scenarioCustomTemplateDetail.id) {
    return <Empty description="分析任务依赖的分析场景不存在或已被删除" />;
  }

  return (
    <>
      <TaskDetail />
      {/* 错误信息 */}
      {scenarioTaskDetail.executionTrace && (
        <Alert
          style={{ marginBottom: 10 }}
          message={scenarioTaskDetail.executionTrace}
          type="warning"
          showIcon
        />
      )}
      {resultContent}
    </>
  );
};

export default connect()(ScenarioTaskResult);
