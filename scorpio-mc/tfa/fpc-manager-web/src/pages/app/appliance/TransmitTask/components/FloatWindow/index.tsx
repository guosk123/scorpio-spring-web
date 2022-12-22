import AutoHeightContainer from '@/components/AutoHeightContainer';
import { pageIsEmbed } from '@/utils/utils';
import { DoubleLeftOutlined, DoubleRightOutlined } from '@ant-design/icons';
import type { TreeProps } from 'antd';
import { Affix, Card, Tooltip } from 'antd';
import type { FC } from 'react';
import TransmitTaskForm from '../TransmitTaskForm';
import styles from './index.less';

// 展开状态保存在浏览器中的 key
export const SEARCH_TREE_COLLAPSED_KEY = 'search-tree-collapsed';
export interface ITreeData {
  key: string;
  title: string;
  disabled?: boolean;
  children?: ITreeData[];
}

export interface ISearchTreeProps extends Pick<TreeProps, 'onSelect'> {
  collapsed: boolean;
  onToggleCollapsed: (collapsed: boolean) => void;
}

const FloatWindow: FC<ISearchTreeProps> = ({ collapsed, onToggleCollapsed: setCollapsed }) => {
  /** 树搜索关键字 */
  const offsetTop = pageIsEmbed() ? 10 : 80;

  return (
    <>
      {collapsed ? (
        <Affix key="miniBar" offsetTop={offsetTop}>
          <div onClick={() => setCollapsed(false)} className={styles.miniBar}>
            <Tooltip title="展开" placement="right">
              <div className={styles.barWrap}>
                <DoubleRightOutlined />
              </div>
            </Tooltip>
          </div>
        </Affix>
      ) : (
        <div style={{ display: 'flex' }}>
          <Affix key="searchTreeWrap" offsetTop={offsetTop}>
            <div className={styles.searchTreeWrap}>
              <Card
                bodyStyle={{ padding: 6 }}
                // extra={
                //   <Button
                //     style={{ display: 'none' }}
                //     block
                //     type="primary"
                //     icon={<LeftSquareOutlined />}
                //     className={styles.collapsedBtn}
                //     onClick={handleCollapsed}
                //   >
                //     收起
                //   </Button>
                // }
              >
                <AutoHeightContainer contentStyle={{ overflowY: 'auto', overflowX: 'scroll' }}>
                  <TransmitTaskForm describeMode={false} smallSize={true} />
                </AutoHeightContainer>
              </Card>
            </div>
          </Affix>
          <Affix key="miniBar" offsetTop={offsetTop}>
            <div onClick={() => setCollapsed(true)} className={styles.miniBar}>
              <Tooltip title="收起" placement="right">
                <div className={styles.barWrap}>
                  <DoubleLeftOutlined />
                </div>
              </Tooltip>
            </div>
          </Affix>
        </div>
      )}
    </>
  );
};

export default FloatWindow;
