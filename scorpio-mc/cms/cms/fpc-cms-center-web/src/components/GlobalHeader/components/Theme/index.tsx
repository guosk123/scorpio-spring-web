import type { ConnectState } from '@/models/connect';
import { Switch } from 'antd';
import { connect } from 'dva';
import React from 'react';
import type { Dispatch, TTheme } from 'umi';
import styles from '../../style.less';
import { useEffect } from 'react';

interface IThemeProps {
  dispatch: Dispatch;
  theme: TTheme;
}
const Theme: React.FC<IThemeProps> = ({ dispatch, theme }) => {
  useEffect(() => {}, [theme]);

  const handleThemeChange = (checked: boolean) => {
    dispatch({
      type: 'settings/changeTheme',
      payload: {
        theme: checked ? 'dark' : 'light',
      },
    });
  };

  return (
    <div className={styles.action}>
      <Switch
        checkedChildren="暗黑主题"
        unCheckedChildren="默认主题"
        className="anticon"
        checked={theme === 'dark'}
        onChange={handleThemeChange}
      />
    </div>
  );
};

export default connect(({ settings: { theme } }: ConnectState) => ({
  theme,
}))(Theme);
