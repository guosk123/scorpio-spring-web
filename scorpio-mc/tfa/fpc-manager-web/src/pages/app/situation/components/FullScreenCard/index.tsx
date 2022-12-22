import { FullscreenExitOutlined, FullscreenOutlined } from '@ant-design/icons';
import { useFullscreen } from 'ahooks';
import { Card, Divider, Tooltip } from 'antd';
import type { ReactNode } from 'react';
import React, { useRef } from 'react';
import { connect } from 'umi';

export const DEFAULT_HEIGHT = 920;

interface Props {
  title?: string;
  children?: ReactNode;
  extra?: ReactNode;
  // getHeight?: (hight: any) => void;
  // getFullScreenPattern?: (flag: any) => void;
}

export const HightContext = React.createContext<boolean>(false);

function FullScreenCard(props: Props) {
  const { title, children, extra } = props;
  const wrapRef = useRef<HTMLDivElement>();
  const [isFullscreen, { toggleFullscreen }] = useFullscreen(wrapRef);
  const fullBoxHeight = isFullscreen ? `calc(100vh - 40px)` : 'auto';
  return (
    // @ts-ignore
    <div ref={wrapRef}>
      <HightContext.Provider value={isFullscreen}>
        <Card
          size={'small'}
          title={isFullscreen && title}
          bodyStyle={{ height: fullBoxHeight }}
          extra={
            <div
              style={{
                display: 'flex',
                flexWrap: 'nowrap',
                justifyContent: 'space-around',
                alignItems: 'center',
              }}
            >
              {extra}
              <Divider type="vertical" />
              <Tooltip title={isFullscreen ? '还原' : '全屏'}>
                <span onClick={() => toggleFullscreen()}>
                  {isFullscreen ? <FullscreenExitOutlined /> : <FullscreenOutlined />}
                </span>
              </Tooltip>
            </div>
          }
        >
          {children}
        </Card>
      </HightContext.Provider>
    </div>
  );
}

export default connect()(FullScreenCard);
