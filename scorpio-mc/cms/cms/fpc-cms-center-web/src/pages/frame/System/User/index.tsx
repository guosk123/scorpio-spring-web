import { getTablePaginationDefaultSettings, proTableSerchConfig } from '@/common/app';
import type { ConnectState } from '@/models/connect';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, Divider, Modal, Tag, Tooltip } from 'antd';
import moment from 'moment';
import { Fragment, useRef } from 'react';
import type { Dispatch, ICurrentUser } from 'umi';
import { connect, history } from 'umi';
import styles from './index.less';
import { queryUsers } from './services/users';
import type { IUser } from './typings';
import { EUserStatus } from './typings';

/**
 * 审计管理员
 */
export const ROLE_AUDIT_USER = 'ROLE_AUDIT_USER';
/**
 * 系统管理员
 */
export const ROLE_SYS_USER = 'ROLE_SYS_USER';
/**
 * 普通用户角色
 */
export const ROLE_USER = 'ROLE_USER';
/**
 * 业务管理用户
 */
export const ROLE_SERVICE_USER = 'ROLE_SERVICE_USER';

interface IProps {
  dispatch: Dispatch;
  currentUser: ICurrentUser;
}
const User = ({ dispatch, currentUser }: IProps) => {
  const actionRef = useRef<ActionType>();
  // 删除
  const onDelete = ({ id, fullname }: IUser) => {
    Modal.confirm({
      title: `确定删除用户【${fullname}】吗?`,
      okText: '删除',
      cancelText: '取消',
      onOk: () => {
        dispatch({
          type: 'usersModel/deleteUser',
          payload: id,
        }).then(() => actionRef?.current?.reload());
      },
    });
  };

  // 锁定 / 解锁
  const changeUserLocked = ({ id, fullname, locked }: IUser) => {
    const tipText = locked === EUserStatus.Locked ? '解锁' : '锁定';
    const isLocked = locked === EUserStatus.Locked ? EUserStatus.UnLocked : EUserStatus.Locked;
    Modal.confirm({
      title: `确定${tipText}用户【${fullname}】吗?`,
      okText: tipText,
      cancelText: '取消',
      onOk: () => {
        dispatch({
          type: 'usersModel/changeUserLocked',
          payload: { id, isLocked },
        }).then(() => actionRef?.current?.reload());
      },
    });
  };

  const columns: ProColumns<IUser>[] = [
    {
      title: '登录名称',
      dataIndex: 'name',
      key: 'name',
      align: 'center',
      width: 200,
      ellipsis: true,
      search: false,
    },
    {
      title: '用户名称',
      dataIndex: 'fullname',
      key: 'fullname',
      align: 'center',
      width: 200,
      ellipsis: true,
      search: false,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      align: 'center',
      width: 200,
      ellipsis: true,
      search: false,
    },
    {
      title: '角色',
      dataIndex: 'userRoles',
      key: 'userRoles',
      align: 'center',
      search: false,
      render: (_, { userRoles }) =>
        userRoles.map((item) => <Tag key={item.nameZh}>{item.nameZh}</Tag>),
    },
    {
      title: 'appKey',
      dataIndex: 'appKey',
      align: 'center',
      className: 'relative',
      ellipsis: true,
      search: false,
      renderText: (appKey) => (
        <Tooltip placement="topLeft" title={appKey}>
          {appKey || ''}
        </Tooltip>
      ),
    },
    {
      title: '状态',
      dataIndex: 'state',
      key: 'state',
      width: 100,
      search: false,
      align: 'center',
      renderText: (_, record) => (record.locked === EUserStatus.Locked ? '锁定' : '正常'),
    },
    {
      title: '最近登录时间',
      dataIndex: 'latestLoginTime',
      width: 170,
      search: false,
      align: 'center',
      render: (_, record) => {
        const { latestLoginTime } = record;
        if (!latestLoginTime) {
          return '';
        }
        return moment(latestLoginTime as string).format('YYYY-MM-DD HH:mm:ss');
      },
    },
    {
      title: '操作',
      dataIndex: 'action',
      key: 'action',
      align: 'center',
      width: 180,
      search: false,
      renderText: (_, record) => {
        const { id, locked, userRoles } = record;
        // 审计管理员和系统管理员不可以新建和删除
        const canNotDelete =
          userRoles.length === 1 &&
          (userRoles[0].nameEn === ROLE_SYS_USER || userRoles[0].nameEn === ROLE_AUDIT_USER);
        // 当前登录人
        const isLoggedUser = currentUser.id === id;
        const updateDom = (
          <a
            className={styles.dropdownMenu}
            onClick={() => history.push(`/system/user/update?id=${id}`)}
          >
            编辑
          </a>
        );
        const lockDom = (
          <a className={styles.dropdownMenu} onClick={() => changeUserLocked(record)}>
            {locked === EUserStatus.Locked ? '解锁' : '锁定'}
          </a>
        );
        // 当前登录人
        if (isLoggedUser) {
          return updateDom;
        }
        // 不可删除
        if (canNotDelete) {
          return (
            <Fragment>
              {updateDom}
              <Divider type="vertical" />
              {lockDom}
            </Fragment>
          );
        }
        return (
          <Fragment>
            {updateDom}
            {/* 当前登录用户不可以编辑和锁定自己 */}
            <Divider type="vertical" />
            <a className={styles.dropdownMenu} onClick={() => onDelete(record)}>
              删除
            </a>
            <Divider type="vertical" />
            {lockDom}
          </Fragment>
        );
      },
    },
  ];
  return (
    <ProTable
      rowKey="id"
      bordered
      size="small"
      columns={columns}
      actionRef={actionRef}
      request={async (params = {}) => {
        const { current = 0, pageSize, ...rest } = params;
        const newParams = {
          pageSize,
          page: current && current - 1,
          ...rest,
        };
        const { success, result } = await queryUsers(newParams);
        return {
          data: result.content,
          page: result.number,
          total: result.totalElements,
          success,
        };
      }}
      search={{
        ...proTableSerchConfig,
        span: 6,
        optionRender: () => [
          <Button
            icon={<PlusOutlined />}
            type="primary"
            key="create"
            style={{ marginLeft: 8 }}
            onClick={() => history.push('/system/user/create')}
          >
            新建
          </Button>,
        ],
      }}
      toolBarRender={false}
      pagination={getTablePaginationDefaultSettings()}
    />
  );
};

export default connect(({ globalModel: { currentUser } }: ConnectState) => ({
  currentUser,
}))(User);
