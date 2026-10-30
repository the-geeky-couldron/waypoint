// Install the official validator with: npm ci --prefix tools/style-validation
const fs = require('node:fs');
const path = require('node:path');
const {validateStyleMin} = require('../tools/style-validation/node_modules/@maplibre/maplibre-gl-style-spec');
const root = path.join(__dirname, '../app/src/main/assets/styles');
let total = 0;
for (const file of fs.readdirSync(root).filter(n => n.endsWith('.json'))) {
  const errors = validateStyleMin(JSON.parse(fs.readFileSync(path.join(root, file), 'utf8')));
  for (const error of errors) console.error(`${file}: ${error.message}`);
  total += errors.length;
}
console.log(`${fs.readdirSync(root).length} styles checked; ${total} errors`);
process.exitCode = total ? 1 : 0;
