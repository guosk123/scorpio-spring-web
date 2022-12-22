import { Empty, Skeleton } from 'antd';
import { useEffect, useState } from 'react';
import { useParams } from 'umi';
import ExternalStorageForm, { formatCapacity2GB } from '../components/Form';
import { queryExternalStorageDetail } from '../service';
import type { IExternalStorage } from '../typings';

function UpdateExternalStorage() {
  const params: { id: string } = useParams();
  const [queryLoading, setQueryLoading] = useState(true);
  const [externalStorageDetail, setExternalStorageDetail] = useState({} as IExternalStorage);

  useEffect(() => {
    setQueryLoading(true);
    queryExternalStorageDetail(params.id).then(({ success, result }) => {
      setQueryLoading(false);
      setExternalStorageDetail(success ? result : ({} as IExternalStorage));
    });
  }, [params.id]);

  if (queryLoading) {
    return <Skeleton />;
  }
  if (!externalStorageDetail.id) {
    return <Empty description="存储服务器不存在或已被删除" />;
  }

  return (
    <ExternalStorageForm
      detail={{
        ...externalStorageDetail,
        capacityGigaByte: formatCapacity2GB(externalStorageDetail.capacity),
      }}
    />
  );
}

export default UpdateExternalStorage;
