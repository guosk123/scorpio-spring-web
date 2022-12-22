import AnyWhereContainer from '@/components/AnyWhereContainer';
import AutoHeightContainer from '@/components/AutoHeightContainer';
import TopologyGraph from '@/components/TopologyGraph';
import { app, clientUser } from '@/components/TopologyGraph/template';
import type { ConnectState } from '@/models/connect';
import {
  fieldsMapping,
  flowCommonFields,
  flowSubFields,
} from '@/pages/app/analysis/components/fieldsManager';
import type { IUriParams } from '@/pages/app/analysis/typings';
import type { IApplicationMap } from '@/pages/app/Configuration/SAKnowledge/typings';
import type { IService, IServiceLink } from '@/pages/app/Configuration/Service/typings';
import type { IServiceLinkApiParams } from '@/services/app/serviceLink';
import type { Graph, Model } from '@antv/x6';
import { Alert, Button, Col, message, Row, Select } from 'antd';
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import type { Dispatch } from 'umi';
import { connect, useParams } from 'umi';
import { v1 as uuidv1 } from 'uuid';
import ConnectCmsState from '../../components/ConnectCmsState';
import styles from './index.less';

export interface ITopology {
  serviceDetail: IService;
  allApplicationMap: IApplicationMap;
  dispatch: Dispatch;
}

const stencilNodes = [clientUser, app];

const tooltipMsg = (
  <div className={styles.tooltipMsg}>
    <p>
      拖拽应用节点后，请为该节点分配具体应用。
      <br />
      请不要将节点之间的距离放置太近
    </p>
  </div>
);

const graphOperateTooltip = <Alert message={tooltipMsg} showIcon type="warning" />;

const notShowMetrics = [
  'networkId',
  'serviceId',
  'categoryId',
  'subcategoryId',
  'type',
  'applicationId',
];

const allMetric = flowCommonFields
  .concat(flowSubFields.applications)
  .filter((field) => !notShowMetrics.includes(field))
  .map((field) => {
    return {
      text: fieldsMapping[field].name,
      value: field,
    };
  });

const Topology: React.FC<ITopology> = ({ serviceDetail, dispatch, allApplicationMap }) => {
  const { serviceId } = useParams() as IUriParams;
  const graphRef = useRef<Graph>();
  const [serviceLinkMetrics, setServiceLinkMetrics] = useState<string[]>([]);
  const [serviceLink, setServiceLink] = useState<IServiceLink>({});
  const [cmsConnectFlag, setCmsConnectFlag] = useState(false);
  const id = useMemo(() => uuidv1(), []);

  useEffect(() => {
    dispatch({
      type: 'serviceModel/queryServiceDetail',
      payload: serviceId,
    });
    dispatch({
      type: 'serviceModel/queryServiceLink',
      payload: {
        serviceId,
      },
    }).then((link: IServiceLink) => {
      setServiceLink(link);
    });
  }, [dispatch, serviceId]);

  useEffect(() => {
    if (serviceLink.metric) {
      const res = JSON.parse(serviceLink.metric);
      setServiceLinkMetrics(res);
    }
  }, [serviceLink.metric]);

  const graphData = useMemo<Model.ToJSONData>(() => {
    if (serviceLink.link) {
      const res = JSON.parse(serviceLink.link) as Model.ToJSONData;
      return res;
    }
    return { cells: [] };
  }, [serviceLink]);

  const appList = useMemo(() => {
    if (!serviceDetail.application) {
      return [];
    }
    return serviceDetail.application.split(',')?.map((appId) => {
      const res = {
        label: (allApplicationMap && allApplicationMap[appId]?.nameText) || appId,
        value: appId,
      };
      return res;
    });
  }, [allApplicationMap, serviceDetail]);

  const handleSave = () => {
    if (graphRef.current && serviceId) {
      const savedEdges = graphRef.current.getEdges();
      savedEdges.forEach((edge) => {
        edge.attr('line/strokeDasharray', null);
        edge.attr('line/style', null);
        edge.removeTools();
      });
      const json = graphRef.current.toJSON();
      const updateServiceLinkParams: IServiceLinkApiParams = {
        serviceId,
        link: JSON.stringify(json),
        metric: JSON.stringify(serviceLinkMetrics),
      };
      dispatch({
        type: 'serviceModel/updateServiceLink',
        payload: updateServiceLinkParams,
      });
    }
  };

  const handleGraphReady = useCallback((graph: Graph) => {
    graphRef.current = graph;
  }, []);

  const handleChange = useCallback((value) => {
    // TODO 监听指标变化
    if (value.length > 4) {
      message.warning('最多选择4项指标');
      return;
    }
    setServiceLinkMetrics(value);
  }, []);
  const handleHeightChange = useCallback((height) => {
    if (graphRef.current) {
      graphRef.current.resize(undefined, height);
    }
  }, []);

  return (
    <>
      <ConnectCmsState onConnectFlag={setCmsConnectFlag} />
      <AutoHeightContainer
        onHeightChange={handleHeightChange}
        headerRender={
          <Row gutter={{ xs: 8, sm: 12, md: 16 }}>
            <Col span={16}>
              <Select
                mode={'multiple'}
                allowClear
                placeholder="请选择需要查看的指标"
                value={serviceLinkMetrics}
                style={{ width: '100%' }}
                onChange={handleChange}
                disabled={cmsConnectFlag}
              >
                {allMetric.map((item) => {
                  return (
                    <Select.Option key={item.value} value={item.value}>
                      {item.text}
                    </Select.Option>
                  );
                })}
              </Select>
            </Col>
            <Col span={8}>
              <Button type="primary" onClick={handleSave} disabled={cmsConnectFlag}>
                保存
              </Button>
            </Col>
          </Row>
        }
      >
        <div className={styles.graphContainer}>
          <TopologyGraph
            id={id}
            onGrapgReady={handleGraphReady}
            editable={!cmsConnectFlag}
            options={{
              node: {
                inputType: 'select',
                selectItem: appList,
              },
            }}
            cells={graphData}
            nodes={stencilNodes}
          />
          <AnyWhereContainer
            top={0}
            left={200}
            children={graphOperateTooltip}
            display={!cmsConnectFlag}
            style={{ padding: 0 }}
          />
        </div>
      </AutoHeightContainer>
    </>
  );
};

const mapStateToProps = ({
  serviceModel: { serviceDetail },
  SAKnowledgeModel: { allApplicationMap },
}: ConnectState) => ({
  serviceDetail,
  allApplicationMap,
});

export default connect(mapStateToProps)(Topology);
