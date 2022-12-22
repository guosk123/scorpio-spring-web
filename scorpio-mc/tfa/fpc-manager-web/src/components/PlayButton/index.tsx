import { CaretRightOutlined, PauseOutlined } from '@ant-design/icons';
import { Tooltip } from 'antd';
import { useCallback, useEffect, useMemo, useState } from 'react';
import type { Dispatch } from 'umi';
import { history } from 'umi';

enum EPlayStatus {
  PLAY = 'play',
  STOP = 'stop',
}

interface Props {
  refreshFlag?: [flag: boolean, setFlagFn: any];
}

// 改变是否刷新的状态 true <===> stop <===> 显示是否关闭刷新
export const refreshFlagFn = (flag: boolean, dispatch: Dispatch) => {
  dispatch({
    type: 'npmdModel/changeRefreshFlag',
    payload: { isRefreshFlag: flag },
  });
};

// 暂时不做显示，用来监听路由变化，pathname不同时将刷新模式开启
export default function PlayButton(props: Props) {
  const { refreshFlag = [] } = props;
  const [flag, setFlagFn] = refreshFlag;

  const playState = useMemo(() => {
    if (flag) {
      return EPlayStatus.STOP;
    }
    return EPlayStatus.PLAY;
  }, [flag]);

  const [oldPathname, setOldPathname] = useState(history.location.pathname);

  useEffect(() => {
    if (oldPathname !== history.location.pathname) {
      setFlagFn(true);
      setOldPathname(history.location.pathname);
    }
  }, [setFlagFn, oldPathname]);

  const changeState = useCallback(() => {
    setFlagFn(!flag);
  }, [flag, setFlagFn]);

  return (
    <div style={{ display: 'none' }}>
      <Tooltip title={`${playState === EPlayStatus.PLAY ? '开启' : '关闭'}页面更新`}>
        <div style={{ display: 'inline-block', cursor: 'pointer' }} onClick={changeState}>
          {playState === EPlayStatus.PLAY ? <CaretRightOutlined /> : <PauseOutlined />}
        </div>
      </Tooltip>
    </div>
  );
}
