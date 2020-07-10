const withCSS = require('@zeit/next-css')

module.exports = withCSS({
  reactStrictMode: true,
  poweredByHeader: false,
  exportPathMap: function(defaultPathMap) {
    return Object.assign(defaultPathMap, {
      "/": { page: "/new" }
    });
  },
})
