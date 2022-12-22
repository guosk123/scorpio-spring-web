import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import type { ConnectState } from '@/models/connect';
import { jumpToMetadataDetailTab } from '@/pages/app/appliance/Metadata';
import type { IL7ProtocolMap } from '@/pages/app/appliance/Metadata/typings';
import { matadataDetailKV } from '@/pages/app/appliance/Metadata/typings';
import { EMetadataTabType } from '@/pages/app/appliance/Metadata/typings';
import { Button } from 'antd';
import moment from 'moment';
import { useSelector } from 'umi';
import useRecordToMetadataFilter from '../../hooks/useRecordToMetadataFilter';

interface Props {
  record: any;
}

export default function JumpToMatadata(props: Props) {
  const { record } = props;
  const globalSelectedTime = useSelector<ConnectState, Required<IGlobalTime>>(
    (state) => state.appModel.globalSelectedTime,
  );
  const packetFilter = useRecordToMetadataFilter(record);
  return (
    <Button
      disabled={!matadataDetailKV[record.l7_protocol_id]}
      type={'link'}
      size={'small'}
      onClick={() => {
        jumpToMetadataDetailTab(
          {
            startTime: moment(globalSelectedTime.startTime).valueOf(),
            endTime: moment(globalSelectedTime.endTime).valueOf(),
          },
          matadataDetailKV[record.l7_protocol_id],
          packetFilter,
        );
      }}
    >
      应用层协议
    </Button>
  );
}
