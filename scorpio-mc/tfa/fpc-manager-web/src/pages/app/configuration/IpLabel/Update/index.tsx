import { useSafeState } from 'ahooks';
import { Spin } from 'antd';
import { useEffect } from 'react';
import { useParams } from 'umi';
import IpLabelForm from '../components/IpLabelForm';
import { queryIpLabelDetail } from '../service';
import type { IIpLabel } from '../typings';

const Update = () => {
  const { labelId } = useParams() as { labelId: string };
  const [detail, setDetail] = useSafeState<IIpLabel>();

  useEffect(() => {
    console.log(labelId);
    queryIpLabelDetail(labelId).then((res) => {
      if (res.success) {
        setDetail(res.result);
      }
    });
  }, [labelId, setDetail]);

  if (detail === undefined) {
    return <Spin />;
  }

  return <IpLabelForm detail={detail} />;
};

export default Update;
