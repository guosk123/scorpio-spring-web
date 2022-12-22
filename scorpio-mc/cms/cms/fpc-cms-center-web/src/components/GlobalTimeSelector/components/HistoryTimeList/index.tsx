import storage from '@/utils/frame/storage';
import { List } from 'antd';
import { history } from 'umi';

interface Props {
  onClick: any;
}

function HistoryTimeList(props: Props) {
  const { onClick = () => {} } = props;
  const data = () => {
    return JSON.parse(storage.get('timeHistory') || '[]').reverse();
  };
  return (
    <List
      bordered={false}
      style={{ minWidth: 450, maxHeight: 240, overflowY: 'scroll' }}
      size="small"
      dataSource={data()}
      renderItem={(item: any) => (
        <List.Item
          style={{ cursor: 'pointer' }}
          onClick={() => {
            onClick();
            const timeKeys = ['from', 'relative', 'timeType', 'to'];
            const oldQuery = {};
            Object.keys(history.location.query || {}).forEach((element) => {
              if (!timeKeys.includes(element)) {
                oldQuery[element] = history.location.query ? history.location.query[element] : '';
              }
            });
            history.replace({
              query: {
                ...oldQuery,
                ...item.info,
              },
            });
          }}
        >
          {item.timeInfoName}
        </List.Item>
      )}
    />
  );
}

export default HistoryTimeList;
