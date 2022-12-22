import type { ConnectState } from '@/models/connect';
import type { ICurrentUser } from '@/models/frame/global';
import type { Settings as ProSettings } from '@ant-design/pro-layout';
import React from 'react';
import type { ConnectProps } from 'umi';
import { connect } from 'dva';
import Avatar from './components/AvatarDropdown';
import ChangePassword from './components/ChangePassword';
import ProductInfo from './components/ProductInfo';
import Theme from './components/Theme';
import UserInfo from './components/UserInfo';
import styles from './style.less';

export interface GlobalHeaderRightProps extends Partial<ConnectProps>, Partial<ProSettings> {
  theme?: ProSettings['navTheme'] | 'realDark' | undefined;
  userInfoModalVisible: boolean;
  changePwdModalVisible: boolean;
  currentUser: ICurrentUser;
}

const GlobalHeaderRight: React.SFC<GlobalHeaderRightProps> = (props) => {
  const { theme, layout, userInfoModalVisible, currentUser, changePwdModalVisible } = props;
  let className = styles.right;

  if (theme === 'dark' && layout === 'top') {
    className = `${styles.right}  ${styles.dark}`;
  }

  return (
    <div className={className}>
      {/* 切换黑暗模式 */}
      <Theme />
      {/* 版权信息 */}
      <ProductInfo />
      {/* 当前登录人 */}
      <Avatar />
      {/* <SelectLang className={styles.action} /> */}
      {/* 个人信息框 */}
      {userInfoModalVisible && (
        <UserInfo currentUser={currentUser} visible={userInfoModalVisible} />
      )}
      {/* 修改密码 */}
      {changePwdModalVisible && (
        <ChangePassword currentUser={currentUser} visible={changePwdModalVisible} />
      )}
    </div>
  );
};

export default connect(
  ({
    settings: { navTheme, layout },
    globalModel: { userInfoModalVisible, changePwdModalVisible, currentUser },
  }: ConnectState) => ({
    theme: navTheme,
    layout,
    userInfoModalVisible,
    changePwdModalVisible,
    currentUser,
  }),
)(GlobalHeaderRight);
