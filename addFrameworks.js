const fs = require('fs');
const path = require('path');

const frameworksDir = path.join(__dirname, 'frameworks'); // frameworks directory
const pluginXmlPath = path.join(__dirname, 'plugin.xml');

const updatePluginXml = () => {
    fs.readdir(frameworksDir, (err, files) => {
        if (err) {
            console.error('Error reading frameworks directory:', err);
            return;
        }

        let frameworkEntries = '';

           // Read the existing plugin.xml
           fs.readFile(pluginXmlPath, 'utf8', (err, data) => {
            if (err) {
                console.error('Error reading plugin.xml:', err);
                return;
            }

            files.forEach(file => {
                if (file.endsWith(".xcframework") && !data.includes(file)) {
                    frameworkEntries += `    <framework src="./${path.join("frameworks",file)}" custom="true"/>\n`;
                }
            });

            // Insert frameworks before the closing </platform> tag
              const updatedXml = data.replace(/<platform name="ios">\s*([\s\S]*?)<\/platform>/,(match, content) => {
                  return `<platform name="ios">\n${content}${frameworkEntries}</platform>`;
                }
              );

              // Write the updated content back to plugin.xml
              fs.writeFile(pluginXmlPath, updatedXml, "utf8", (err) => {
                if (err) {
                  console.error("Error writing to plugin.xml:", err);
                } else {
                  console.log("plugin.xml updated successfully with frameworks!");
                }
              });
        });
});
};

// Execute the function
updatePluginXml();
