const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 3000,
    host: '0.0.0.0', // 允许外部访问
    proxy: {
      '/api': {
        target: 'http://backend-java:8080', // Docker 网络中的后端服务地址
        changeOrigin: true
      }
    }
  }
})
