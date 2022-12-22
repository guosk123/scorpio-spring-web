import type { ConnectState } from '@/models/connect';
import { parseArrayJson } from '@/utils/utils';
import { QuestionCircleOutlined } from '@ant-design/icons';
import { Divider, Popconfirm, Table, Tooltip } from 'antd';
import type { ColumnsType } from 'antd/lib/table';
import { connect } from 'dva';
import React, { useEffect } from 'react';
import type { Dispatch } from 'umi';
import { Link } from 'umi';
import type { ISystemAlertRuleResponse } from './typings';
import { AlertLevelTypeList, ESystemAlertSourceType, ESystemAlertState } from './typings';

interface ISystemAlertProps {
  dispatch: Dispatch;
  systemAlertRuleList: ISystemAlertRuleResponse[];
  queryLoading?: boolean;
  submitLoading?: boolean;
}
const SystemAlert: React.FC<ISystemAlertProps> = ({
  dispatch,
  queryLoading,
  systemAlertRuleList,
}) => {
  useEffect(() => {
    querySystemAlertRules();
  }, []);

  const querySystemAlertRules = () => {
    dispatch({
      type: 'systemAlertModel/querySystemAlertRules',
    });
  };

  const columns: ColumnsType<ISystemAlertRuleResponse> = [
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '级别',
      dataIndex: 'level',
      key: 'level',
      render: (level) => {
        const target = AlertLevelTypeList.find((item) => item.value === level);
        if (target) {
          return target.label;
        }
        return level;
      },
    },
    {
      title: '状态',
      dataIndex: 'state',
      key: 'state',
      render: (state) => (state === ESystemAlertState.开启 ? '已启用' : '已禁用'),
    },
    {
      title: '操作',
      dataIndex: 'action',
      key: 'action',
      render: (_, { id, state, fireCriteria, sourceType }) => {
        const newState =
          state === ESystemAlertState.开启 ? ESystemAlertState.关闭 : ESystemAlertState.开启;
        const newStateText = newState === ESystemAlertState.开启 ? '启用' : '禁用';

        let operandBtn = (
          <Popconfirm
            title={`确定${newStateText}吗？`}
            onConfirm={() => handleChangeState(id, newState)}
            icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
          >
            <span className="link">{newStateText}</span>
          </Popconfirm>
        );

        // 硬盘剩余空间告警，并且没有选择监控分区时
        const fireCriteriaList = parseArrayJson(fireCriteria);
        if (sourceType === ESystemAlertSourceType.DISK && fireCriteriaList.length <= 0) {
          operandBtn = (
            <Tooltip title="尚未配置监控分区">
              <span className="disabled">{newStateText}</span>
            </Tooltip>
          );
        }

        return (
          <>
            <Link to={`/system/setting/alert/${id}`}>编辑</Link>
            <Divider type="vertical" />
            {operandBtn}
          </>
        );
      },
    },
  ];

  const handleChangeState = (id: string, newState: ESystemAlertState) => {
    dispatch({
      type: 'systemAlertModel/updateSystemAlertRuleState',
      payload: {
        id,
        state: newState,
      },
    }).then(() => {
      querySystemAlertRules();
    });
  };

  return (
    <Table
      rowKey="id"
      loading={queryLoading}
      bordered
      size="small"
      columns={columns}
      dataSource={systemAlertRuleList}
      pagination={false}
    />
  );
};

export default connect(
  ({ loading: { effects }, systemAlertModel: { systemAlertRuleList } }: ConnectState) => ({
    systemAlertRuleList,
    queryLoading: effects['systemAlertModel/querySystemAlertRules'],
    submitLoading: effects['systemAlertModel/updateSystemAlertRuleState'],
  }),
)(SystemAlert);
