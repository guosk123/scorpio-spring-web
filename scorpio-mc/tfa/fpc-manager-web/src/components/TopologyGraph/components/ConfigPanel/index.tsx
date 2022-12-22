import type { Cell, Edge, Node, Graph } from '@antv/x6';
import type { CellTitleOption } from '../../typing';
import EdgeConfig from './EdgeConfig';
import NodeConfig from './NodeConfig';

interface Props {
  graph: Graph;
  cell?: Cell;
  options?: {
    node?: CellTitleOption;
    edge?: CellTitleOption;
  };
}

const ConfigPanel: React.FC<Props> = (props) => {
  const { cell, options } = props;

  return (
    <>
      {cell && cell.isNode() && <NodeConfig node={cell as Node} titleOption={options?.node} />}
      {cell && cell.isEdge() && <EdgeConfig edge={cell as Edge} titleOption={options?.edge} />}
    </>
  );
};

export default ConfigPanel;
