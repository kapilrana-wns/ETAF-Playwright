const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { after, test } = require('node:test');

const {
    parseProperties,
    rewriteReportScreenshotPaths,
    saveProperties
} = require('../server');

const tempDirectory = fs.mkdtempSync(path.join(os.tmpdir(), 'etaf-dashboard-test-'));

after(() => {
    fs.rmSync(tempDirectory, { recursive: true, force: true });
});

test('configuration saves merge updates and preserve unrecognized settings', () => {
    const propertiesFile = path.join(tempDirectory, 'test.properties');
    const originalContent = '# Existing configuration\nbrowser=Chrome\nlegacySetting=keep-me\n';
    fs.writeFileSync(propertiesFile, originalContent);

    const saved = saveProperties(propertiesFile, { browser: 'Firefox', autoHealEnabled: 'true' });
    const persisted = fs.readFileSync(propertiesFile, 'utf8');

    assert.equal(saved.browser, 'Firefox');
    assert.equal(parseProperties(persisted).legacySetting, 'keep-me');
    assert.match(persisted, /browser=Firefox/);
    assert.match(persisted, /autoHealEnabled=true/);
    assert.match(persisted, /legacySetting=keep-me/);
    assert.equal(fs.readFileSync(propertiesFile + '.backup', 'utf8'), originalContent);
});

test('configuration save rejects malformed payloads without modifying the file', () => {
    const propertiesFile = path.join(tempDirectory, 'unchanged.properties');
    const originalContent = 'browser=Chrome\n';
    fs.writeFileSync(propertiesFile, originalContent);

    assert.throws(() => saveProperties(propertiesFile, null), /must be a JSON object/);
    assert.throws(() => saveProperties(propertiesFile, { browser: null }), /scalar values/);
    assert.equal(fs.readFileSync(propertiesFile, 'utf8'), originalContent);
});

test('legacy absolute screenshot paths are rewritten to dashboard report URLs', () => {
    const runName = 'Result_test-123';
    const runDirectory = path.join(tempDirectory, runName);
    const screenshotPath = path.join(runDirectory, 'screenshots', 'result image.png');
    const html = `<img src="${screenshotPath}" data-featherlight='${screenshotPath}'>`;

    const rewritten = rewriteReportScreenshotPaths(html, runName, tempDirectory);

    assert.equal(
        rewritten,
        `<img src="/reports/${runName}/screenshots/result%20image.png" data-featherlight='/reports/${runName}/screenshots/result%20image.png'>`
    );
});
