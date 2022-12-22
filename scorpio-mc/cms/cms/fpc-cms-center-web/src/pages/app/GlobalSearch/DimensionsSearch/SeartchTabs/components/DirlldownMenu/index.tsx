import { Dropdown, Menu } from 'antd';

interface IFilterBubbleProps {
  style?: React.CSSProperties;
  /** 枚举字段显示的名称 */
  label: React.ReactNode;
  /** 下钻菜单 */
  DrilldownMenu?: any;
}
const DirlldownMenu = ({ label, style, DrilldownMenu }: IFilterBubbleProps) => {
  return (
    <Dropdown trigger={['click']} overlay={<Menu style={style}>{DrilldownMenu}</Menu>}>
      <div style={{ cursor: 'pointer' }}>{label}</div>
    </Dropdown>
  );
};

export default DirlldownMenu;
