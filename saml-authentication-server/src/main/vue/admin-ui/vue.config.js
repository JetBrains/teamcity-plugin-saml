const LicenseChecker = require('@jetbrains/ring-ui-license-checker');
const path = require('path');

function createLicenseChecker(filename) {
    return new LicenseChecker({
        format: require('./third-party-licenses-json'),
        filename,
        exclude: [/@jetbrains/],
        surviveLicenseErrors: true,
    });
}

module.exports = {
    indexPath: "index.html",
    publicPath: "./",
    pluginOptions: {
    },
    configureWebpack: (config) => {
        config.plugins.push(
            createLicenseChecker(
                '../js-related-libraries.json'
            )
        );
    },
    filenameHashing: false,
    chainWebpack(config) {
        config.optimization.delete('splitChunks');
    }
};
