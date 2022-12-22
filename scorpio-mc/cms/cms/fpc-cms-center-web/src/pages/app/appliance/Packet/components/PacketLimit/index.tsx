import { InputNumber } from 'antd';
import _ from 'lodash';
import { useState } from 'react';
import type { Dispatch } from 'umi';
import { connect } from 'dva';
import type { IPacketConnectState } from '../../typings';
import styles from './index.less';

interface IPacketLimitProps {
  limit: number;
  updatePacketListLimit: (limit: number) => any;
}
const PacketLimit = ({ limit, updatePacketListLimit }: IPacketLimitProps) => {
  const [limitValue, setLimitValue] = useState<number>(limit);

  const handleLimitChange = (value: number) => {
    setLimitValue(value);
  };

  const handlePressEnter = _.debounce(() => updatePacketListLimit(limitValue), 200);

  return (
    <div>
      <span className = {styles['limit-tips']}>显示包数{' '}</span>
      <InputNumber
        size="small"
        min={1}
        max={10000}
        value={limitValue}
        onChange={handleLimitChange}
        onPressEnter={handlePressEnter}
      />
    </div>
  );
};

export default connect(
  ({ packetModel: { limit } }: IPacketConnectState) => ({ limit }),
  (dispatch: Dispatch) => ({
    updatePacketListLimit: (limit: number) => {
      return dispatch({ type: 'packetModel/updatePacketListLimit', payload: { limit } });
    },
  }),
)(PacketLimit);
