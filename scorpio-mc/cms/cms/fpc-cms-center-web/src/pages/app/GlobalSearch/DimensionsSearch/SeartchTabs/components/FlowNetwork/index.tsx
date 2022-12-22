import List from '@/pages/app/Network/List';
import { createContext, useCallback, useEffect, useState } from 'react';
import DimensionsChartCard from '../../../components/EditTab/components/DimensionsChartCard';
import { v1 as uuidv1 } from 'uuid';
import { pingQueryTask } from '@/pages/app/appliance/FlowRecords/service';
import { message } from 'antd';

export const FlowNetworkContext = createContext<any>(null);

export default function FlowNetwork() {
  const [payload, setPayload] = useState({});
  const [queryId, setQueryId] = useState(uuidv1());
  // ======维持查询心跳 S=====
  // ======维持查询心跳 S=====
  const pingQueryTaskFn = useCallback(() => {
    // 没有 ID 时不 ping
    if (queryId.length === 0) {
      return;
    }
    pingQueryTask({
      queryId,
    }).then((success: boolean) => {
      if (!success) {
        message.destroy();
      }
    });
  }, [queryId]);

  // ======维持查询心跳 E=====
  useEffect(() => {
    let timer: any;
    if (queryId.length > 0) {
      timer = window.setInterval(() => {
        pingQueryTaskFn();
      }, 3000);

      return () => window.clearTimeout(timer);
    }
    window.clearTimeout(timer);
    return undefined;
  }, [pingQueryTaskFn, queryId]);
  return (
    <FlowNetworkContext.Provider value={[payload, setPayload]}>
      <DimensionsChartCard chartPayload={payload} />
      <List
        queryId={queryId}
        onLoading={(id: string) => {
          setQueryId(id);
        }}
        noDisplayColKeys={['bytepsPeak', 'concurrentSessions']}
      />
    </FlowNetworkContext.Provider>
  );
}
