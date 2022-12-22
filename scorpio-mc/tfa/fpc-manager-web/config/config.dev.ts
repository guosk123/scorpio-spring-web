// https://umijs.org/config/
import { defineConfig } from 'umi';
import APPLICATION_CONFIG from '../src/common/applicationConfig';
import proxy from './proxy';
import path from 'path';

const { REACT_APP_ENV } = process.env;

export default defineConfig({
  mock: {},
  fastRefresh: {},
  scripts: [{ src: `${APPLICATION_CONFIG.CONTEXT_PATH}/web-static/config/config.js` }],
  // a lower cost way to genereate sourcemap, default is cheap-module-source-map, could save 60% time in dev hotload
  devtool: 'cheap-module-source-map',
  devServer: {
    https: {
      key: path.resolve(__dirname, './cert/machloop-key.pem'),
      cert: path.resolve(__dirname, './cert/machloop.pem'),
    },
  },
  // umi3 comple node_modules by default, could be disable
  nodeModulesTransform: {
    type: 'none',
    exclude: [],
  },
  proxy: proxy[REACT_APP_ENV || 'dev'],
});
