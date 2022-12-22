import Ellipsis from '@/components/Ellipsis';
import EllipsisCom from '@/components/EllipsisCom';
import FilterBubble from '@/components/FieldFilter/components/FilterBubble';
import type { EFieldOperandType, EFieldType } from '@/components/FieldFilter/typings';
import { snakeCase } from '@/utils/utils';
import { Button, Modal, Tag } from 'antd';
import type { DataIndex } from 'rc-table/lib/interface';
import { useState } from 'react';
import type { IMetadataLog } from '../../typings';
import type { IColumnProps } from '../Template';
import { getFilterField } from '../utils';

interface Props {
  title: string;
  filterItems: string[];
  isNewIpFieldType?: boolean;
  dataIndex?: DataIndex;
  value: any;
  setFilterCondition: any;
  record: IMetadataLog;
  index: number;
  fieldType: EFieldType | undefined;
  newCol: IColumnProps<IMetadataLog>;
  operandType: EFieldOperandType | undefined;
}

export default function FilterBubbleForArray(props: Props) {
  const {
    title,
    filterItems,
    isNewIpFieldType,
    dataIndex,
    value,
    setFilterCondition,
    record,
    index,
    fieldType,
    newCol,
    operandType,
  } = props;
  const [isModalOpen, setIsModalOpen] = useState(false);
  console.log('record', record);
  const tdText = newCol?.render ? newCol?.render(value, record, index) : value;

  const showModal = () => {
    setIsModalOpen(true);
  };

  const handleOk = () => {
    setIsModalOpen(false);
  };

  const handleCancel = () => {
    setIsModalOpen(false);
  };

  return (
    <>
      <span
        onClick={showModal}
        style={{
          display: 'block',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          cursor: 'pointer',
        }}
      >
        {tdText}
      </span>
      {/* <Button type="text" size="small" onClick={showModal}>
        {tdText}
      </Button> */}
      <Modal
        title={title}
        visible={isModalOpen}
        footer={false}
        onOk={handleOk}
        width={900}
        onCancel={handleCancel}
      >
        {filterItems.map((item) => {
          return (
            <FilterBubble
              style={{ zIndex: 1200 }}
              dataIndex={
                isNewIpFieldType
                  ? snakeCase(dataIndex! as string)
                  : getFilterField(snakeCase(dataIndex! as string), value)
              }
              label={<Tag>{item}</Tag>}
              operand={item}
              fieldType={fieldType}
              operandType={operandType!}
              onClick={(newFilter) => {
                setFilterCondition((prev: any) => {
                  return [...prev, ...[newFilter]];
                });
              }}
            />
          );
        })}
      </Modal>
    </>
  );
}
