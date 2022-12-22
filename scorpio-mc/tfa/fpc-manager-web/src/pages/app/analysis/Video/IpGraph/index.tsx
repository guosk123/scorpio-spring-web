import { useContext, useEffect, useMemo, useRef } from 'react';
import IpGraph, { EActionMenuKey, EIpGraphMode } from '../../Network/IpGraph';
import { VideoTabsContext } from '../components/VideoEditTabs';
import { videoFields } from './typing';
import { v4 as uuidv4 } from 'uuid';
import type { IFilter } from '@/components/FieldFilter/typings';
import { EFieldType } from '@/components/FieldFilter/typings';
import { EFilterGroupOperatorTypes, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { VideoContext } from '../components/NetworkTimeLayout';
import { jumpNewPage } from '@/utils/utils';
import { EMetadataTabType } from '@/pages/app/appliance/Metadata/Analysis/typings';
import type { INode } from '../../components/ForceGraph';
import { openNewVideoTab } from '../components/VideoEditTabs/constant';
import { EVideoTabType } from '../typings';

const Index = () => {
  const [state, videoDispatch] = useContext(VideoTabsContext);
  /** 网络时间选择器上下文 */
  const { network } = useContext(VideoContext)!;

  const {
    shareInfo: { record },
  } = state;

  const initialCondition = [
    {
      id: uuidv4(),
      field: 'l_7_protocol_id',
      operand: '292',
      operandText: 'RTP',
      operator: '=',
    },
    {
      id: uuidv4(),
      field: 'l_7_protocol_id',
      operand: '307',
      operandText: 'SIP',
      operator: '=',
    },
    {
      field: 'ip_address',
      id: uuidv4(),
      operand: (record as any)?.deviceIp,
      operandText: (record as any)?.deviceIp,
      operator: '=',
    },
  ];

  const handleNodeClick: (action: any, node: INode, id?: string | undefined) => void = (
    action,
    node,
    id,
  ) => {
    const { key } = action;
    const nodeIp = node.id;
    switch (key) {
      case EActionMenuKey.SIP_REOCORD:
        jumpNewPage(
          `/analysis/trace/metadata/record?jumpTabs=${
            EMetadataTabType.SIP
          }&filter=${encodeURIComponent(
            JSON.stringify([
              {
                group: [
                  {
                    field: 'src_ip',
                    operator: EFilterOperatorTypes.EQ,
                    operand: nodeIp,
                  },
                  {
                    field: 'dest_ip',
                    operator: EFilterOperatorTypes.EQ,
                    operand: nodeIp,
                  },
                ],
                operator: EFilterGroupOperatorTypes.OR,
              },
            ]),
          )}`,
        );
        break;
    }
  };

  return (
    <div style={{ marginTop: '5px' }}>
      <IpGraph
        historyGraph={false}
        customField={videoFields}
        initialCondition={initialCondition as IFilter[]}
        customNeworkId={network?.id}
        mode={EIpGraphMode.RTP}
        onNodeClick={handleNodeClick}
        onEdgeClick={(action, edge, id) => {
          const { key } = action;
          const { source, target } = edge?.__proto__;
          switch (key) {
            case EActionMenuKey.RTP_SEGMENT:
              openNewVideoTab(
                state,
                videoDispatch,
                EVideoTabType.SEGMENT,
                {
                  filter: [
                    {
                      group: [
                        {
                          group: [
                            {
                              field: 'src_ip',
                              operator: EFilterOperatorTypes.EQ,
                              operand: source,
                            },
                            {
                              field: 'dest_ip',
                              operator: EFilterOperatorTypes.EQ,
                              operand: target,
                            },
                          ],
                          operator: EFilterGroupOperatorTypes.AND,
                        },
                        {
                          group: [
                            {
                              field: 'src_ip',
                              operator: EFilterOperatorTypes.EQ,
                              operand: target,
                            },
                            {
                              field: 'dest_ip',
                              operator: EFilterOperatorTypes.EQ,
                              operand: source,
                            },
                          ],
                          operator: EFilterGroupOperatorTypes.AND,
                        },
                      ],
                      operator: EFilterGroupOperatorTypes.OR,
                    },
                  ],
                },
                `${source}⇋${target}_分段分析`,
              );
              break;
            case EActionMenuKey.RTP_FLOW:
              openNewVideoTab(
                state,
                videoDispatch,
                EVideoTabType.RTP_FLOW_LIST,
                {
                  filter: [
                    {
                      group: [
                        {
                          group: [
                            {
                              field: 'src_ip',
                              operator: EFilterOperatorTypes.EQ,
                              operand: source,
                              type: EFieldType.IP,
                            },
                            {
                              field: 'dest_ip',
                              operator: EFilterOperatorTypes.EQ,
                              operand: target,
                              type: EFieldType.IP,
                            },
                          ],
                          operator: EFilterGroupOperatorTypes.AND,
                        },
                        {
                          group: [
                            {
                              field: 'src_ip',
                              operator: EFilterOperatorTypes.EQ,
                              operand: target,
                              type: EFieldType.IP,
                            },
                            {
                              field: 'dest_ip',
                              operator: EFilterOperatorTypes.EQ,
                              operand: source,
                              type: EFieldType.IP,
                            },
                          ],
                          operator: EFilterGroupOperatorTypes.AND,
                        },
                      ],
                      operator: EFilterGroupOperatorTypes.OR,
                    },
                  ],
                },
                `${source}⇋${target}_RTP流分析`,
              );
              break;
          }
        }}
      />
    </div>
  );
};

export default Index;
