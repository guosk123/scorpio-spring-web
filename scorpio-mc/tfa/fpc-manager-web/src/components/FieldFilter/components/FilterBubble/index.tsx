import { Dropdown, Menu } from 'antd';
import { generateOperators } from '../..';
import { EFilterOperatorTypes } from '../../typings';
import type { EFieldOperandType, IFilter, EFieldType } from '../../typings';

interface IFilterBubbleProps {
  /** 字段 ID */
  dataIndex: string;
  /** 字段的类型 */
  fieldType?: EFieldType;
  /** 字段的值 */
  operand?: any;
  /** 字段值类型 */
  operandType?: EFieldOperandType;
  style?: React.CSSProperties;
  /** 枚举字段显示的名称 */
  label: React.ReactNode;
  /** 是否是简化版的过滤器，操作符只有=号 */
  filterSimple?: boolean;
  /** 操作符点击事件 */
  onClick?: (filter: IFilter) => void;
  /** 下钻菜单 */
  DrilldownMenu?: any;
  /** 容器样式 */
  containerStyle?: React.CSSProperties;
  /** */
  hasFilter?: boolean;
}
const FilterBubble = ({
  dataIndex,
  fieldType,
  operandType,
  operand,
  label,
  style,
  filterSimple,
  onClick,
  DrilldownMenu,
  containerStyle = {},
  hasFilter = true,
}: IFilterBubbleProps) => {
  return (
    <Dropdown
      trigger={['click']}
      overlay={
        <Menu style={style}>
          {hasFilter && (
            <Menu.ItemGroup key={`${dataIndex}_bubble`} title="添加过滤条件">
              {generateOperators(operandType, fieldType, filterSimple)
                .filter((item) => item.value !== EFilterOperatorTypes.LIKE)
                .filter((item) => item.value !== EFilterOperatorTypes.MATCH)
                .map((item) => {
                  return (
                    <Menu.Item
                      key={item.value}
                      onClick={({ key }) => {
                        if (onClick) {
                          onClick({
                            field: dataIndex,
                            operator: key as EFilterOperatorTypes,
                            operand,
                          });
                        }
                      }}
                    >
                      {item.label}
                    </Menu.Item>
                  );
                })}
            </Menu.ItemGroup>
          )}
          {DrilldownMenu}
        </Menu>
      }
    >
      <div style={{ cursor: 'pointer', display: 'inline', lineHeight: 1, ...containerStyle }}>
        {label}
      </div>
    </Dropdown>
  );
};

export default FilterBubble;
