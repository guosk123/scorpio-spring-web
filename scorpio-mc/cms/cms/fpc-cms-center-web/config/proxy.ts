/**
 * 在生产环境 代理是无法生效的，所以这里没有生产环境的配置
 */
export default {
  dev: {
    '/api/': {
      target: 'http://10.0.0.226:41120',
      // target: 'http://10.0.0.121:41110',
      changeOrigin: true,
      pathRewrite: { '^/api': '' },
    },
  },
};
