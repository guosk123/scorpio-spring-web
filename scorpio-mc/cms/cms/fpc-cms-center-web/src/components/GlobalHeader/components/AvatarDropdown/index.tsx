import type { ConnectState } from '@/models/connect';
import type { ICurrentUser } from '@/models/frame/global';
import { LogoutOutlined, SettingOutlined, UserOutlined } from '@ant-design/icons';
import { Avatar, Menu, Modal, Spin } from 'antd';
import React from 'react';
import { history } from 'umi';
import type { ConnectProps } from 'umi';
import { connect } from 'dva';
import HeaderDropdown from '@/components/HeaderDropdown';
import styles from '../../style.less';

export interface GlobalHeaderRightProps extends Partial<ConnectProps> {
  currentUser?: ICurrentUser;
  menu?: boolean;
}

export const LOGIN_OUT_KEY = 'loginOut';

class AvatarDropdown extends React.Component<GlobalHeaderRightProps> {
  onMenuClick = (info: any) => {
    const { key } = info;
    const { dispatch } = this.props;
    // 个人信息
    if (key === 'userInfo') {
      if (dispatch) {
        dispatch({
          type: 'globalModel/updateState',
          payload: {
            userInfoModalVisible: true,
          },
        });
      }
    }
    // 修改密码
    if (key === 'changePassword') {
      if (dispatch) {
        dispatch({
          type: 'globalModel/updateState',
          payload: {
            changePwdModalVisible: true,
          },
        });
      }
    }

    if (key === 'logout') {
      Modal.confirm({
        title: '确定退出登录吗?',
        onOk: () => {
          history.replace({ query: { [LOGIN_OUT_KEY]: LOGIN_OUT_KEY } });
          if (dispatch) {
            dispatch({
              type: 'loginModel/logout',
            });
          }
        },
      });
    }
  };

  render(): React.ReactNode {
    const { currentUser } = this.props;
    const menuHeaderDropdown = (
      <Menu className={styles.menu} onClick={this.onMenuClick} key="avatar-menu">
        <Menu.Item key="userInfo">
          <UserOutlined />
          个人信息
        </Menu.Item>
        <Menu.Item key="changePassword">
          <SettingOutlined />
          修改密码
        </Menu.Item>
        <Menu.Divider key="divider" />

        <Menu.Item key="logout">
          <LogoutOutlined />
          退出登录
        </Menu.Item>
      </Menu>
    );
    return currentUser && currentUser.id ? (
      <HeaderDropdown overlay={menuHeaderDropdown}>
        <span className={`${styles.action} ${styles.account}`}>
          <Avatar size="small" icon={<UserOutlined />} />
          <span className={`${styles.name} anticon`}>{currentUser.fullname}</span>
        </span>
      </HeaderDropdown>
    ) : (
      <span className={`${styles.action} ${styles.account}`}>
        <Spin
          size="small"
          style={{
            marginLeft: 8,
            marginRight: 8,
          }}
        />
      </span>
    );
  }
}

export default connect(({ globalModel }: ConnectState) => ({
  currentUser: globalModel.currentUser,
}))(AvatarDropdown);
