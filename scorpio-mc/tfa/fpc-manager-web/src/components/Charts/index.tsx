import numeral from 'numeral';
import ChartCard from './ChartCard';
import Field from './Field';
import Liquid from './Liquid';

const yuan = (val: number | string) => `¥ ${numeral(val).format('0,0')}`;

const Charts = {
  yuan,
  ChartCard,
  Field,
  Liquid,
};

export { Charts as default, yuan, ChartCard, Field, Liquid };
