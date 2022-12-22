import type { IGlobalTime } from '@/components/GlobalTimeSelector';
import { CheckCircleTwoTone, SyncOutlined } from '@ant-design/icons';
import { Tooltip } from 'antd';
import { connect } from 'dva';
import moment from 'moment';
import { useMemo } from 'react';
import type { IPacket, IPacketConnectState, IPacketRefine } from '../../typings';
import { EPacketRefineStatus } from '../../typings';
import * as style from './index.less';

interface IProgressProps {
  listData: IPacket[];
  data: IPacketRefine;
  globalSelectedTime: Required<IGlobalTime>;
  timeInfo: { startTime: string; endTime: string };
}
function Progress({ data, listData, timeInfo }: IProgressProps) {
  /** 轨道的开始时间 */
  const trackStartTime = new Date(timeInfo.startTime!).valueOf();
  /** 轨道的结束时间 */
  const trackEndTime = new Date(timeInfo.endTime!).valueOf();

  /** 数据包列表：第一个包的时间 */
  let packetListStartTime: number = 0;
  /** 数据包列表：最后一个包的时间 */
  let packetListEndTime: number = 0;
  if (listData.length > 0) {
    packetListStartTime = new Date(listData[0].timestamp).valueOf();
    packetListEndTime = new Date(listData[listData.length - 1].timestamp).valueOf();
  }

  /** 数据包统计：第一个包的时间 */
  let packetRefineStartTime: number = 0;
  /** 数据包统计：最后一个包的时间 */
  let packetRefineEndTime: number = 0;
  const { matchMinTimestamp, matchMaxTimestamp } = data.execution;
  if (matchMinTimestamp && matchMaxTimestamp) {
    packetRefineStartTime = new Date(matchMinTimestamp).valueOf();
    packetRefineEndTime = new Date(matchMaxTimestamp).valueOf();
  }

  // 总时间跨度
  const trackTimeDiff = trackEndTime - trackStartTime;
  // 列表数据的开始时间位置
  const listTimeStartPosition = `${
    ((packetListStartTime - trackStartTime) / trackTimeDiff) * 100
  }%`;
  // 列表数据的结束时间位置
  const listTimeEndPosition = `${((trackEndTime - packetListEndTime) / trackTimeDiff) * 100}%`;

  // 统计数据的开始时间位置
  const refineTimeStartPosition = `${
    ((packetRefineStartTime - trackStartTime) / trackTimeDiff) * 100
  }%`;
  // 统计数据的结束时间位置
  const refineTimeEndPosition = `${((trackEndTime - packetRefineEndTime) / trackTimeDiff) * 100}%`;

  const progressTips = useMemo(() => {
    return (
      <>
        <span>
          已选中开始时间:{' '}
          {!trackStartTime ? 0 : moment(trackStartTime).format('YYYY-MM-DD HH:mm:ss')}
          <br />
          已选中结束时间: {!trackEndTime ? 0 : moment(trackEndTime).format('YYYY-MM-DD HH:mm:ss')}
          <br />
          已统计最早时间:{' '}
          {!packetRefineStartTime ? 0 : moment(packetRefineStartTime).format('YYYY-MM-DD HH:mm:ss')}
          <br />
          已统计最晚时间:{' '}
          {!packetRefineEndTime ? 0 : moment(packetRefineEndTime).format('YYYY-MM-DD HH:mm:ss')}
          <br />
          列表展示最早时间:{' '}
          {!packetListStartTime ? 0 : moment(packetListStartTime).format('YYYY-MM-DD HH:mm:ss')}
          <br />
          列表展示最晚时间:{' '}
          {!packetListEndTime ? 0 : moment(packetListEndTime).format('YYYY-MM-DD HH:mm:ss')}
          <br />
        </span>
      </>
    );
  }, [
    trackStartTime,
    trackEndTime,
    packetRefineStartTime,
    packetRefineEndTime,
    packetListStartTime,
    packetListEndTime,
  ]);

  const refineRunning = useMemo(() => {
    return data.status === EPacketRefineStatus.RUNNING;
  }, [data.status]);

  return (
    <>
      <div className={style['progress-status']}>
        {refineRunning ? <SyncOutlined spin /> : <CheckCircleTwoTone twoToneColor="#52c41a" />}
      </div>
      <div className={style['progress-pane']}>
        <Tooltip placement="bottomLeft" title={progressTips} overlayInnerStyle={{ width: 300 }}>
          <div className={style['progress-bar']}>
            {/* 总时间：全部的时间区间 */}
            <div className={style['progress-bar_track']} />
            {/* 数据包统计的时间分布 */}
            <div
              className={style['progress-bar_box']}
              style={{
                left: refineTimeStartPosition,
                right: refineTimeEndPosition,
              }}
            >
              <div
                className={style['progress-bar_refine']}
                data-start-time={moment(packetRefineStartTime).format('YYYY-MM-DD HH:mm:ss')}
                data-end-time={moment(packetRefineEndTime).format('YYYY-MM-DD HH:mm:ss')}
              />
            </div>
            {/* 数据包列表的时间分布 */}
            <div
              className={style['progress-bar_list']}
              style={{ left: listTimeStartPosition, right: listTimeEndPosition }}
            />
          </div>
        </Tooltip>
        <div className={style['progress-time']}>
          <div className={style['progress-time_label_left']}>
            <div className={style['progress-time_label_left_tick']} />
            <div className={style['progress-time_label_left_text']}>
              {moment(trackStartTime).format('YYYY-MM-DD HH:mm:ss')}
            </div>
          </div>
          <div className={style['progress-time_label_right']}>
            <div className={style['progress-time_label_right_tick']} />
            <div className={style['progress-time_label_right_text']}>
              {moment(trackEndTime).format('YYYY-MM-DD HH:mm:ss')}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
export default connect(
  ({ appModel: { globalSelectedTime } }: IPacketConnectState) => ({
    globalSelectedTime,
  }),
)(Progress);
