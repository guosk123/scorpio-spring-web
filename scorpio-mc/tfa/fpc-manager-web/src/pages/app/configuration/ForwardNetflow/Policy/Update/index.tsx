import { Spin } from 'antd';
import { useEffect, useState } from 'react';
import { useParams } from 'umi';
import PolicyForm from '../../components/PolicyForm';
import { queryForwardPolicyDetail } from '../../service';
import type { IForwardPolicy } from '../../typings';

const PolicyUpdate = () => {
  const { id } = useParams() as unknown as { id: string };

  const [detail, setDetail] = useState<IForwardPolicy>();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    queryForwardPolicyDetail(id).then(({ success, result }) => {
      if (success) {
        setDetail(result);
      }
      setLoading(false);
    });
  }, [id]);

  if (loading) {
    return <Spin />;
  }

  return <PolicyForm detail={detail} />;
};

export default PolicyUpdate;
