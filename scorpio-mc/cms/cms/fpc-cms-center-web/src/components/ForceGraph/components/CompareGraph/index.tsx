import { CloseSquareOutlined, DeleteOutlined } from '@ant-design/icons';
import { Card } from 'antd';
import { useMemo } from 'react';
import ForceGraph, { defaultDarkTheme, defaultLightTheme } from '../..';
import type {  IForceGraphProps } from '../../typings';
import type { EForceGraphIndex } from '../../typings';
import styles from './index.less';
import { connect } from 'umi';
import type { ConnectState } from '@/models/connect';

interface ICompareGraphProps {
  data: ({ id?: string; title: string; onDeleteRalation?: any } & IForceGraphProps)[];
  deleteGraph?: any;
  closeGraph?: any;
  theme?: string;
  historyGraph?: boolean;
  edgeIndex?: EForceGraphIndex[];
}

function Comparegraph({
  theme: globalTheme,
  data,
  deleteGraph,
  closeGraph,
  historyGraph = true,
  edgeIndex,
}: ICompareGraphProps) {
  const graphList = useMemo(() => {
    return data.map((item) => {
      const {
        theme,
        weightField,
        height,
        nodes,
        edges,
        nodeActions,
        onNodeClick,
        edgeActions,
        onEdgeClick,
        brushActions,
        onBrushEnd,
        title,
        id,
        onDeleteRalation,
      } = item;

      const accHeight = (height || 0) / (Math.ceil(data.length / 2) || 1);
      const width = data.length === 1 ? '100%' : '50%';
      return (
        <>
          <Card
            title={
              <>
                {title || 'untitled'}
                {id !== 'default' ? (
                  <span
                    style={{
                      marginLeft: '10px',
                      color: globalTheme === 'light' ? 'rgba(0, 0, 0, 0.45)' : 'white',
                    }}
                  >
                    历史画布仅用于展示
                  </span>
                ) : (
                  <span
                    style={{
                      marginLeft: '10px',
                      color: globalTheme === 'light' ? 'rgba(0, 0, 0, 0.45)' : 'white',
                    }}
                  >
                    默认线粗维度: 总字节数 默认会话对数量:100
                  </span>
                )}
              </>
            }
            style={{ width, height: accHeight }}
            bodyStyle={{ padding: '0px' }}
            size="small"
            extra={
              id !== 'default' ? (
                <>
                  <DeleteOutlined
                    className={styles.card_btn}
                    onClick={() => {
                      if (deleteGraph) {
                        deleteGraph(id);
                      }
                    }}
                  />
                  <CloseSquareOutlined
                    className={styles.card_btn}
                    onClick={() => {
                      if (closeGraph) {
                        closeGraph(id);
                      }
                    }}
                  />
                </>
              ) : (
                ''
              )
            }
          >
            <ForceGraph
              id={id}
              theme={theme!.mode === 'light' ? defaultLightTheme : defaultDarkTheme}
              weightField={weightField}
              height={accHeight}
              nodes={nodes}
              edges={edges}
              nodeActions={nodeActions}
              onNodeClick={onNodeClick}
              edgeActions={edgeActions}
              onEdgeClick={onEdgeClick}
              brushActions={brushActions}
              onBrushEnd={onBrushEnd}
              onDeleteRalation={onDeleteRalation}
              historyGraph={historyGraph}
              edgeIndex={edgeIndex}
            />
          </Card>
        </>
      );
    });
  }, [closeGraph, data, deleteGraph, edgeIndex, globalTheme, historyGraph]);

  return <div style={{ display: 'flex', flexWrap: 'wrap' }}>{graphList}</div>;
}

export default connect(({ settings: { theme } }: ConnectState) => ({
  theme: theme,
}))(Comparegraph);
