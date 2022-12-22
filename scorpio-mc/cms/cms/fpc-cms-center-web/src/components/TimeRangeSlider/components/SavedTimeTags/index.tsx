import storage from '@/utils/frame/storage';
import { ClearOutlined } from '@ant-design/icons';
import { Divider, Popconfirm, Tag } from 'antd';
import { useState } from 'react';
import { history } from 'umi';

const oldArr = () => JSON.parse(storage.get('localTimeInfoObj') || '[]');

interface Props {
  onClick?: any;
}

export default function SavedTimeTags(props: Props) {
  const { onClick } = props;
  const closeTag = (key: string) => {
    const newArr = oldArr().filter((item: any) => {
      return item.timeInfoName !== key;
    });
    storage.put('localTimeInfoObj', JSON.stringify(newArr));
  };
  const [tmpTag, setTmpTag] = useState([]);
  return (
    <div>
      <Divider dashed orientation="left" plain>
        已存时间
        <Popconfirm
          title="确认清空已保存时间吗？"
          onConfirm={() => {
            storage.put('localTimeInfoObj', '[]');
            setTmpTag([]);
          }}
        >
          <Tag icon={<ClearOutlined />} color="#f50" style={{ marginLeft: 10, cursor: 'pointer' }}>
            清空
          </Tag>
        </Popconfirm>
      </Divider>
      {oldArr()
        .concat(tmpTag)
        .map((item: any) => {
          return (
            <Tag
              closable
              onClick={() => {
                onClick();
                const timeKeys = ['from', 'relative', 'timeType', 'to'];
                const oldQuery = {};
                Object.keys(history.location.query || {}).forEach((element) => {
                  if (!timeKeys.includes(element)) {
                    oldQuery[element] = history.location.query
                      ? history.location.query[element]
                      : '';
                  }
                });
                history.replace({
                  query: {
                    ...oldQuery,
                    ...item.info,
                  },
                });
              }}
              onClose={() => {
                closeTag(item.timeInfoName);
              }}
              color="blue"
              style={{ cursor: 'pointer' }}
            >
              {item.timeInfoName}
            </Tag>
          );
        })}
    </div>
  );
}
