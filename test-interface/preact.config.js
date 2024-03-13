export default (config, env, helpers) => {
 const chunks = helpers.getPluginsByName(config, 'CommonsChunkPlugin')[0]
 config.plugins[chunks.index].minChunks = 1
}