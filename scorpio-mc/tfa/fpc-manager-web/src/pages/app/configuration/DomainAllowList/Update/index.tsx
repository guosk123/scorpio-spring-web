import { useSafeState } from 'ahooks';
import { Spin } from 'antd';
import { useEffect } from 'react';
import { useParams } from 'umi';
import DomainAllowListForm from '../Form';
import { queryDomainAllowListDetail } from '../service';
import type { DomainAllowListItem } from '../typings';

const Update = () => {
  const { id } = useParams() as any;

  const [detail, setDetail] = useSafeState<DomainAllowListItem>();

  useEffect(() => {
    queryDomainAllowListDetail(id).then((res) => {
      const { success, result } = res;
      if (success) {
        setDetail(result);
      }
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (detail === undefined) {
    return (
      <div className="center">
        <Spin />
      </div>
    );
  }

  return <DomainAllowListForm detail={detail} />;
};

export default Update;
