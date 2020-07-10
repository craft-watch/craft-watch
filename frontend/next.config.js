module.exports = {
  reactStrictMode: true,
  poweredByHeader: false,
  exportPathMap: function(defaultPathMap) {
    return Object.assign(defaultPathMap, {
      "/": { page: "/new" }
    });
  },
}
