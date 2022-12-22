import { StopOutlined } from '@ant-design/icons';
import { Button, message, Tooltip } from 'antd';
import { Fragment, useState } from 'react';
import { cancelQueryTask } from '../../../service';

interface Props {
  loading: boolean | undefined;
  resetRecordData: any;
  style?: React.CSSProperties | undefined
}

export default function CancelBtn(props: Props) {
  const { loading, resetRecordData, style } = props;

  const [disabled, setDisabled] = useState(false);

  const [cancelButtonVisible, setCancelButtonVisible] = useState(false);

  // ====== 停止查询按钮 =====
  // ====== 数据查询 =====
  const abortAjax = () => {
    const { cancelRequest = new Map() } = window;
    cancelRequest.forEach((value: any, key: string) => {
      if (
        value.apiUri.indexOf('/appliance/flow-logs') > -1 ||
        value.apiUri.indexOf('/appliance/flow-logs/as-statistics') > -1
      ) {
        // 取消ajax请求
        value.ajax.abort();
        // 删除
        cancelRequest.delete(key);
      }
    });
  };

  /**
   * 停止查询任务
   */
  const cancelQueryFlowRecords = () => {
    message.loading('正在停止...');
    setDisabled(true);
    cancelQueryTask({}).then((res) => {
      setDisabled(false);
      const { success } = res;
      if (!success) {
        message.destroy();
        message.warning('停止失败');
        return;
      }

      if (!disabled) {
        resetRecordData([]);
      }
      // 直接取消查询
      abortAjax();
      message.destroy();
      message.success('停止成功');

      setCancelButtonVisible(false);
      // setQueryTaskIds([]);
    });
  };

  return cancelButtonVisible ? (
    <Tooltip title="结束任务可能会导致查询不完整">
      <Button
        icon={<StopOutlined />}
        type="primary"
        danger
        loading={loading}
        disabled={disabled}
        onClick={cancelQueryFlowRecords}
        style={style}
      >
        停止
      </Button>
    </Tooltip>
  ) : (
    <Fragment />
  );
}
