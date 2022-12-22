import { Modal, Tag } from 'antd';
import { Fragment, useEffect, useState } from 'react';
import type { PN } from '../PoAndNeBox';
import PoAndNeBox from '../PoAndNeBox';

interface Props {
  text: string;
}

export default function PoAndNeModel(props: Props) {
  const { text = '' } = props;
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [PNTextArr, setPNTextArr] = useState<PN[]>([]);
  useEffect(() => {
    const res = [] as PN[];
    const poOrNeArr = text.split(',');
    poOrNeArr.forEach((item) => {
      const pnItem = {} as PN;
      if (item.includes('+')) {
        pnItem.po = item;
      }
      if (item.includes('-')) {
        pnItem.ne = item;
      }
      res.push(pnItem);
    });
    setPNTextArr(res);
  }, [text]);

  return (
    <Fragment>
      {[...PNTextArr].splice(0, 3).map((item) => (
        <PoAndNeBox pn={item} />
      ))}
      {[...PNTextArr].splice(0, 3).length === 3 && (
        <Tag
          onClick={() => {
            setIsModalOpen(true);
          }}
          style={{
            minWidth: 50,
            cursor: 'pointer',
          }}
        >
          更多
        </Tag>
      )}
      <Modal
        visible={isModalOpen}
        title={'特征序列'}
        width={920}
        onCancel={() => {
          setIsModalOpen(false);
        }}
        footer={false}
      >
        {PNTextArr.map((item) => (
          <PoAndNeBox pn={item} />
        ))}
      </Modal>
    </Fragment>
  );
}
