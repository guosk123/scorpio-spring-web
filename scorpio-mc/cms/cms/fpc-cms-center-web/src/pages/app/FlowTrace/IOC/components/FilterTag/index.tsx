import { getFilterContent, getFilterGroupContent } from '@/components/FieldFilter';
import type {
  IField,
  IFilter,
  IFilterCondition,
  IFilterGroup,
} from '@/components/FieldFilter/typings';
import { CloseSquareOutlined } from '@ant-design/icons';
import { Tag } from 'antd';
import { v1 } from 'uuid';
import styles from './index.less';
interface Props {
  filter: IFilterCondition;
  onRemove: (index: number) => void;
  fields: IField[];
}

const FilterTag = (props: Props) => {
  const { filter, onRemove, fields } = props;
  return (
    <div className={styles.tableHeaderTag}>
      {filter.map((flt, index) => {
        return (
          <Tag
            color="blue"
            className={styles.tag}
            closable
            key={v1()}
            closeIcon={
              <span className={styles.removeTagBtn} onClick={() => onRemove(index)}>
                <CloseSquareOutlined />
              </span>
            }
            onClose={(e) => e.preventDefault()}
          >
            {flt.hasOwnProperty('operand')
              ? getFilterContent(flt as IFilter, false, fields)
              : getFilterGroupContent(flt as IFilterGroup, false, fields)}
          </Tag>
        );
      })}
    </div>
  );
};

export default FilterTag;
