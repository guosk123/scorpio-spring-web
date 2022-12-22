import type { ConnectState } from '@/models/connect';
import { Card, Empty, Skeleton } from 'antd';
import { connect } from 'dva';
import React, { useEffect } from 'react';
import { useParams } from 'react-router';
import type { Dispatch } from 'redux';
import type { IAlertRule } from '../../typings';
import AlertRuleForm from '../components/RuleForm';

interface IUpdateAlertRuleProps {
  dispatch: Dispatch<any>;
  queryLoading: boolean;
  detail: IAlertRule;
}

const UpdateAlertRule: React.FC<IUpdateAlertRuleProps> = ({ dispatch, queryLoading, detail }) => {
  const params = useParams() as { ruleId: string };
  useEffect(() => {
    dispatch({
      type: 'alertModel/queryAlertRuleDetail',
      payload: {
        id: params.ruleId,
      },
    });
  }, [dispatch, params.ruleId]);

  return (
    <Card bordered={false}>
      <Skeleton active loading={queryLoading}>
        {detail.id ? (
          <AlertRuleForm detail={detail} operateType="UPDATE" />
        ) : (
          <Empty description="告警配置不存在或已被删除" />
        )}
      </Skeleton>
    </Card>
  );
};

export default connect(({ alertModel, loading }: ConnectState) => ({
  detail: alertModel.alertRuleDetail,
  queryLoading: loading.effects['alertModel/queryAlertRuleDetail'],
}))(UpdateAlertRule as any);