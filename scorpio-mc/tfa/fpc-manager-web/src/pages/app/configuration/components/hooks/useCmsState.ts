import { useEffect, useState } from "react";
import { queryCmsConnnectState } from "../ConnectCmsState/service";

const useCmsState = () => {
  const [connectFlag, setConnectFlag] = useState(false);

  useEffect(() => {
    queryCmsConnnectState().then((res) => {
      const { success, result } = res;
      if (success) {
        setConnectFlag(result.connectStatus === '0');
      }
    });
  }, []);

  return connectFlag;
}

export default useCmsState;
