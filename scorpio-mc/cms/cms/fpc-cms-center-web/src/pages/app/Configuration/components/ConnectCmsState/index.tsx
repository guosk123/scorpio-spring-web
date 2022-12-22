import { Alert } from 'antd';
import { useEffect, useState } from 'react';
import { queryCmsConnnectState } from './service';

interface Props {
  onConnectFlag: any;
}

export default function ConnectCmsState(props: Props) {
  const { onConnectFlag = () => {} } = props;

  const [connectFlag, setConnectFlag] = useState(false);

  useEffect(() => {
    queryCmsConnnectState().then((res) => {
      const { success, result } = res;
      if (success) {
        setConnectFlag(result.ConnectState === '0');
      }
    });
  }, []);

  useEffect(() => {
    onConnectFlag(connectFlag);
  }, [connectFlag, onConnectFlag]);

  return (
    <div>
      <Alert
        message="已启用集群模式，部分功能禁用，如需启用请断开集群连接"
        type="info"
        showIcon
        closable
        style={connectFlag ? {} : { display: 'none' }}
      />
    </div>
  );
}
