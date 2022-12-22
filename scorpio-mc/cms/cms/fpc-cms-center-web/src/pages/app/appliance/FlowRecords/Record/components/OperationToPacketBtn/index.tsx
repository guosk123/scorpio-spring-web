import { ServiceAnalysisContext } from '@/pages/app/analysis/Service/index';
import type { IUriParams } from '@/pages/app/analysis/typings';
import { AnalysisContext } from '@/pages/app/Network/Analysis';
import { jumpToAnalysisTabNew } from '@/pages/app/Network/Analysis/constant';
import { ENetworkTabs } from '@/pages/app/Network/typing';
import { useContext } from 'react';
import { useParams, useSelector } from 'umi';
import moment from 'moment';
import useRecordToFilter from '../../hooks/useRecordToFilter';
import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';

export default function OperationToPacketBtn(props: any) {
  const { record } = props;
  const { networkId, serviceId }: IUriParams = useParams();

  const packetFilter = useRecordToFilter(record);
  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );
  const [state, dispatch] = useContext(serviceId ? ServiceAnalysisContext : AnalysisContext);

  let startTime = undefined as any;
  let endTime = undefined as any;
  if (record.start_time && record.duration) {
    startTime = moment(record.start_time).valueOf();
    endTime = startTime + record.duration + 60000;
  }
  if (!startTime || !endTime) {
    startTime = globalSelectedTime.startTime;
    endTime = globalSelectedTime.endTime;
  }

  return (
    <span
      className="link"
      onClick={() => {
        const jumpInfo: any = {};
        jumpInfo.filter = packetFilter;
        if (record.network_id.length) {
          jumpInfo.networkId = record.network_id[0];
        }
        if (startTime && endTime) {
          jumpInfo.globalSelectedTime = { startTime, endTime };
        }
        jumpToAnalysisTabNew(state, dispatch, ENetworkTabs.PACKET, jumpInfo);
      }}
    >
      数据包
    </span>
  );
}
