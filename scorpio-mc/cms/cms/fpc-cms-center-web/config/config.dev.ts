// https://umijs.org/config/
import { defineConfig } from 'umi';
import APPLICATION_CONFIG from '../src/common/applicationConfig';
import proxy from './proxy';

const { REACT_APP_ENV } = process.env;

export default defineConfig({
  plugins: [
    // https://github.com/zthxxx/react-dev-inspector
    // 'react-dev-inspector/plugins/umi/react-inspector',
  ],
  // https://github.com/zthxxx/react-dev-inspector#inspector-loader-props
  // inspectorConfig: {
  //   exclude: [],
  //   babelPlugins: [],
  //   babelOptions: {},
  // },
  scripts: [{ src: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/config/config.js` }],
  mock: {},
  fastRefresh: {},
  // scripts: [{ src: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/config/config.js` }],
  // a lower cost way to genereate sourcemap, default is cheap-module-source-map, could save 60% time in dev hotload
  devtool: 'cheap-module-source-map',
  // umi3 comple node_modules by default, could be disable
  nodeModulesTransform: {
    type: 'none',
    exclude: [],
  },
  proxy: proxy[REACT_APP_ENV || 'dev'],
});
