import { Button, Col, Row, Table } from 'antd';
import type { ColumnProps } from 'antd/lib/table';
import moment from 'moment';
import { useContext, useEffect, useMemo, useState } from 'react';
import { RestStatCommonDataContext } from './components/Layout';
import StatCard from './components/StatCard';
import { queryRestStat, queryRestStatList } from './service';
import type { IRestMetric } from './typings';
import { ERestStatType } from './typings';

const userColumns: ColumnProps<any>[] = [
  {
    title: '用户',
    dataIndex: 'userId',
    align: 'center',
  },
  {
    title: '次数',
    dataIndex: 'count',
    align: 'center',
  },
];

const apiColumns: ColumnProps<any>[] = [
  {
    title: 'api名称',
    dataIndex: 'apiName',
    align: 'center',
  },
  {
    title: '次数',
    dataIndex: 'count',
    align: 'center',
  },
];

const RestAPI = () => {
  const { startTime, endTime, interval, userMap } = useContext(RestStatCommonDataContext);
  const [userStatData, setUserStatData] = useState<IRestMetric[]>([]);
  const [apiStatData, setApiStatData] = useState<IRestMetric[]>([]);
  const [showAll, setShowAll] = useState<ERestStatType>();
  const [allData, setAllData] = useState<Record<string, any>[]>([]);

  const statParams = useMemo(() => {
    return {
      startTime,
      endTime,
      interval,
      count: 10,
    };
  }, [endTime, interval, startTime]);

  useEffect(() => {
    queryRestStat({ type: ERestStatType.User, ...statParams }).then((res) => {
      const { success, result } = res;
      if (success) {
        const data: Record<string, IRestMetric> = {};

        result.forEach((item) => {
          const { userId, count, timestamp } = item;
          if (!data[timestamp]) {
            data[timestamp] = { timestamp };
          }
          data[timestamp] = {
            ...data[timestamp],
            [userMap[userId]?.name || userId]: count,
          };
        });

        setUserStatData(Object.values(data));
      }
    });
    queryRestStat({ type: ERestStatType.API, ...statParams }).then((res) => {
      const { success, result } = res;
      if (success) {
        const data: Record<string, IRestMetric> = {};

        result.forEach((item) => {
          const { apiName, count, timestamp } = item;
          if (!data[timestamp]) {
            data[timestamp] = { timestamp };
          }
          data[timestamp] = {
            ...data[timestamp],
            [apiName]: count,
          };
        });
        setApiStatData(Object.values(data));
      }
    });
  }, [statParams, userMap]);

  useEffect(() => {
    if (showAll) {
      queryRestStatList({
        type: showAll,
        startTime,
        endTime,
      }).then((res) => {
        const { success, result } = res;
        if (success) {
          setAllData(result);
        }
      });
    }
  }, [endTime, showAll, startTime]);

  const handleQueryAllStat = (type: ERestStatType) => {
    setShowAll(type);
  };

  return (
    <div>
      <Row justify="end" style={{ marginBottom: 4 }}>
        <span>{showAll && <Button onClick={() => setShowAll(undefined)}>返回</Button>}</span>
      </Row>
      {showAll && (
        <>
          <Table
            bordered
            size="small"
            columns={showAll === ERestStatType.User ? userColumns : apiColumns}
            dataSource={allData}
            pagination={false}
          />
        </>
      )}
      {!showAll && (
        <Row gutter={4}>
          <Col span={12}>
            <StatCard
              title="用户访问TOP分布"
              data={userStatData}
              startTime={moment(startTime).valueOf()}
              endTime={moment(endTime).valueOf()}
              onQueryAll={() => {
                handleQueryAllStat(ERestStatType.User);
              }}
            />
          </Col>
          <Col span={12}>
            <StatCard
              title="API访问TOP分布"
              data={apiStatData}
              startTime={moment(startTime).valueOf()}
              endTime={moment(endTime).valueOf()}
              onQueryAll={() => {
                handleQueryAllStat(ERestStatType.API);
              }}
            />
          </Col>
        </Row>
      )}
    </div>
  );
};

export default RestAPI;
