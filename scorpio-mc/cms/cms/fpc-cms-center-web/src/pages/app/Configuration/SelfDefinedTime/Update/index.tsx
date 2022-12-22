import { Spin } from 'antd';
import { Fragment, useEffect, useState } from 'react';
import { history, useParams } from 'umi';
import TimeForm from '../components/TimeForm';
import { querySingleDetailTime } from '../services';
import { EPageMode } from '@/pages/app/GlobalSearch/PacketRetrieval/components/TransmitTaskForm';

export default function Update() {
  const params = useParams() as { taskId: string };
  const [timeDetail, setTimeDetail] = useState<any>();
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    querySingleDetailTime({ id: params.taskId }).then((res) => {
      const { success, result } = res;
      setLoading(false);
      if (success) {
        const timeArr: any = {};
        if (result.type == '0') {
          JSON.parse(result.customTimeSetting).forEach((item: any, index: number) => {
            timeArr[`time${index}`] = Object.values(item);
          });
        }
        if (result.type == '1') {
          const disposeTypeTime: any = JSON.parse(result.customTimeSetting);
          console.log(disposeTypeTime, 'disposeTypeTime');
          timeArr.disposeTime = Object.values(disposeTypeTime[0]) || [{}];
        }
        if (Object.keys(timeArr).length === 0) {
          history.push('/configuration/objects/selfDefinedTime');
        }
        setTimeDetail({
          id: result.id,
          name: result.name,
          type: result.type,
          period: JSON.parse(result.period) || undefined,
          ...timeArr,
        });
      } else {
        history.push('/configuration/objects/selfDefinedTime');
      }
    });
  }, [params.taskId]);
  return (
    <Fragment>
      {loading || !timeDetail ? (
        <Spin />
      ) : (
        <TimeForm timeDetail={timeDetail} pageMode={EPageMode.Update} />
      )}
    </Fragment>
  );
}
