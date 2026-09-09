const express = require('express');
const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const app = express();
const PORT = 3000;

app.use(express.json());
app.use(express.static('public'));

// Add CORS headers
app.use((req, res, next) => {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type');
    next();
});


const PROPERTIES_FILE = path.join(
    __dirname,
    '..',
    'src',
    'test',
    'java',
    'testconfig',
    'test.properties'
);


console.log('Properties file path:', PROPERTIES_FILE);
console.log('File exists:', fs.existsSync(PROPERTIES_FILE));

function parseProperties(content) {
    const properties = {};
    const lines = content.split('\n');
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        const trimmedLine = line.trim();
        
        // Skip empty lines and comments
        if (trimmedLine === '' || trimmedLine.startsWith('#')) {
            continue;
        }
        
        const equalIndex = trimmedLine.indexOf('=');
        if (equalIndex !== -1) {
            const key = trimmedLine.substring(0, equalIndex).trim();
            const value = trimmedLine.substring(equalIndex + 1).trim();
            if (key) {
                properties[key] = value;
            }
        }
    }
    return properties;
}

function generatePropertiesContent(properties) {
    let content = '';
    const sections = {
        'Feature Settings': ['mobileEmulator', 'recordVideo', 'sendMailUponTetsCompletion', 'AutoLoggingDefect', 'autoTestResultUpdate'],
        'General Properties': ['ApplicationUrl', 'OrangeHRMUrl', 'browser', 'browserWidth', 'browserHeight', 'takeScreenShotFor', 'mobileEmulatorType', 'videoWidth', 'videoHeight'],
        'Auto-Heal Configuration': ['autoHealEnabled', 'autoHealScreenshot', 'autoHealMaxRetries', 'autoHealReport', 'autoHealRetryPrimary'],
        'Test Result Settings': ['testReporter', 'testResultOutputDirectory', 'extentResultMainHtmlFileName', 'testNGReportDir', 'allureTestResultOutputDirectory'],
        'Test Execution': ['testExecutionMode', 'remoteURL', 'webDriverTimeDuraiton', 'testDataDirectory'],
        'Mail Configuration': ['emailSMTPServer', 'emailAddress', 'alertForInitiate'],
        'Database Settings': ['DBMS_TYPE', 'DBMSServerIP', 'DBMSServerPort', 'DatabaseName', 'DBUserName', 'DBPassword'],
        'Test Management': ['TestManagementTool', 'TestManagementToolURL', 'TestManagementToolApiKey', 'TestManagementProjectUserName', 'TestManagementProejctKey', 'TestManagementIssueTypeID', 'DefectAssigneeName', 'DefectReportedBy'],
        'Zephyr Configuration': ['baseURI', 'accessKey', 'secretKey', 'accountId', 'projectId', 'versionId', 'testCycleName', 'cycleDesc', 'jqlQuery', 'apiToken'],
        'Logger Configuration': ['logLevel', 'logFilePath', 'logFileName']
    };
    for (const [sectionName, keys] of Object.entries(sections)) {
        content += '# ' + sectionName + '\n';
        for (const key of keys) {
            if (properties[key] !== undefined) {
                content += key + '=' + properties[key] + '\n';
            }
        }
        content += '\n';
    }
    return content;
}

app.get('/api/load-config', (req, res) => {
    try {
        console.log('API: Loading config from:', PROPERTIES_FILE);
        
        if (!fs.existsSync(PROPERTIES_FILE)) {
            console.log('API: Properties file not found');
            return res.json({ success: true, config: {} });
        }
        
        const content = fs.readFileSync(PROPERTIES_FILE, 'utf8');
        console.log('API: File read successfully, content length:', content.length);
        
        const properties = parseProperties(content);
        console.log('API: Parsed properties count:', Object.keys(properties).length);
        console.log('API: First 3 properties:', Object.entries(properties).slice(0, 3));
        
        res.json({ success: true, config: properties });
    } catch (error) {
        console.error('API: Error loading config:', error.message);
        res.status(500).json({ success: false, error: error.message });
    }
});

app.post('/api/save-config', (req, res) => {
    try {
        const properties = req.body;
        console.log('API: Saving config, properties count:', Object.keys(properties).length);
        
        const content = generatePropertiesContent(properties);
        const backupFile = PROPERTIES_FILE + '.backup';
        
        if (fs.existsSync(PROPERTIES_FILE)) {
            fs.copyFileSync(PROPERTIES_FILE, backupFile);
            console.log('API: Backup created at:', backupFile);
        }
        
        const dir = path.dirname(PROPERTIES_FILE);
        if (!fs.existsSync(dir)) {
            fs.mkdirSync(dir, { recursive: true });
        }
        
        fs.writeFileSync(PROPERTIES_FILE, content, 'utf8');
        console.log('API: Config saved successfully to:', PROPERTIES_FILE);
        res.json({ success: true, message: 'Configuration saved successfully' });
    } catch (error) {
        console.error('API: Error saving config:', error.message);
        res.status(500).json({ success: false, error: error.message });
    }
});

const TEST_RESULT_DIR = path.join(__dirname, '..', 'test_result');

app.use('/reports', express.static(TEST_RESULT_DIR));

app.get('/api/reports', (req, res) => {
    try {
        if (!fs.existsSync(TEST_RESULT_DIR)) {
            return res.json({ success: true, reports: [] });
        }
        const entries = fs.readdirSync(TEST_RESULT_DIR, { withFileTypes: true });
        const reports = entries
            .filter(e => e.isDirectory() && e.name.startsWith('Result_'))
            .map(e => {
                const dirPath = path.join(TEST_RESULT_DIR, e.name);
                const htmlPath = path.join(dirPath, 'Result.html');
                return {
                    name: e.name,
                    timestamp: e.name.replace('Result_', '').replace(/_/g, ' '),
                    path: '/reports/' + e.name + '/Result.html',
                    exists: fs.existsSync(htmlPath),
                    mtime: fs.statSync(dirPath).mtimeMs
                };
            })
            .sort((a, b) => b.mtime - a.mtime)
            .slice(0, 5);
        res.json({ success: true, reports });
    } catch (error) {
        console.error('API: Error listing reports:', error.message);
        res.status(500).json({ success: false, error: error.message });
    }
});

function fixJSObject(str) {
    return str
        .replace(/'/g, '"')
        .replace(/(?<=[{,]\s*)(\w+)(?=\s*:)/g, '"$1"')
        .replace(/,\s*}/g, '}');
}

function parseTestResults(html) {
    const tests = [];
    const re = /<li class="test-item"\s+status="([^"]+)"[^>]*>[\s\S]*?<p class="name">([^<]+)<\/p>/g;
    let m;
    while ((m = re.exec(html)) !== null) {
        tests.push({ name: m[2].trim(), status: m[1] });
    }
    return tests;
}

app.get('/api/reports/analytics', (req, res) => {
    try {
        if (!fs.existsSync(TEST_RESULT_DIR)) {
            return res.json({ success: true, analytics: null });
        }
        const entries = fs.readdirSync(TEST_RESULT_DIR, { withFileTypes: true });
        const dirs = entries
            .filter(e => e.isDirectory() && e.name.startsWith('Result_'))
            .map(e => ({ name: e.name, dirPath: path.join(TEST_RESULT_DIR, e.name), mtime: fs.statSync(path.join(TEST_RESULT_DIR, e.name)).mtimeMs }))
            .sort((a, b) => b.mtime - a.mtime)
            .slice(0, 5);

        if (!dirs.length) return res.json({ success: true, analytics: null });

        const runs = [];
        let totalTests = 0, totalPassed = 0, totalFailed = 0, totalSkipped = 0, totalWarning = 0;
        const allTimelines = [];
        const failCounts = {};

        for (const dir of dirs) {
            const htmlPath = path.join(dir.dirPath, 'Result.html');
            if (!fs.existsSync(htmlPath)) continue;

            const content = fs.readFileSync(htmlPath, 'utf8');

            const sgMatch = content.match(/var statusGroup\s*=\s*(\{[^;]+\});/);
            const tlMatch = content.match(/var timeline\s*=\s*(\{[^;]+\});/);
            const startMatch = content.match(/<!--\s*Started:\s*(.+?)\s*-->/);
            const endMatch = content.match(/<!--\s*Ended:\s*(.+?)\s*-->/);

            if (!sgMatch) continue;

            let sg;
            try {
                sg = JSON.parse(fixJSObject(sgMatch[1]));
            } catch { continue; }

            const parentCount = sg.parentCount || 0;
            const passParent = sg.passParent || 0;
            const failParent = sg.failParent || 0;
            const skipParent = sg.skipParent || 0;
            const warningParent = sg.warningParent || 0;

            let timeline = {};
            if (tlMatch) {
                try { timeline = JSON.parse(fixJSObject(tlMatch[1])); } catch {}
            }

            const durations = Object.values(timeline).filter(v => typeof v === 'number');
            const avgDuration = durations.length ? (durations.reduce((a, b) => a + b, 0) / durations.length) : 0;

            const run = {
                name: dir.name,
                timestamp: dir.name.replace('Result_', '').replace(/_/g, ' '),
                startTime: startMatch ? startMatch[1].trim() : null,
                endTime: endMatch ? endMatch[1].trim() : null,
                total: parentCount,
                passed: passParent,
                failed: failParent,
                skipped: skipParent,
                warning: warningParent,
                passRate: (passParent + failParent + skipParent)? Math.round((passParent / (passParent + failParent + skipParent)) * 100): 0,
                avgDuration: Math.round(avgDuration * 10) / 10,
                testCount: durations.length
            };
            runs.push(run);

            totalTests += parentCount;
            totalPassed += passParent;
            totalFailed += failParent;
            totalSkipped += skipParent;
            totalWarning += warningParent;

            for (const [testName, duration] of Object.entries(timeline)) {
                if (typeof duration === 'number') {
                    allTimelines.push({ name: testName, duration, run: dir.name });
                }
            }

            // Track failing tests across runs
            const runTests = parseTestResults(content);
            for (const t of runTests) {
                if (t.status === 'fail') {
                    if (!failCounts[t.name]) failCounts[t.name] = { count: 0, runs: [] };
                    failCounts[t.name].count++;
                    failCounts[t.name].runs.push(dir.name);
                }
            }
        }

        // Build failing tests list sorted by count desc
        const failingTests = Object.entries(failCounts)
            .map(([name, data]) => ({ name, failCount: data.count, runs: data.runs }))
            .sort((a, b) => b.failCount - a.failCount);

        allTimelines.sort((a, b) => b.duration - a.duration);
        const slowestTests = allTimelines.slice(0, 10);

        const analytics = {
            summary: {
                totalRuns: runs.length,
                totalTests,
                totalPassed,
                totalFailed,
                totalSkipped,
                totalWarning,
                avgPassRate: runs.length ? Math.round(runs.reduce((s, r) => s + r.passRate, 0) / runs.length) : 0,
                avgDuration: runs.length ? Math.round((runs.reduce((s, r) => s + r.avgDuration, 0) / runs.length) * 10) / 10 : 0
            },
            runs,
            slowestTests,
            failingTests
        };

        res.json({ success: true, analytics });
    } catch (error) {
        console.error('API: Error generating analytics:', error.message);
        res.status(500).json({ success: false, error: error.message });
    }
});

app.post('/api/run-tests', (req, res) => {
    const projectDir = path.join(__dirname, '..');

    const suite = req.body.suite || 'all';
    let args;
    switch (suite) {
        case 'orangehrm':
            args = ['test', '-Dsurefire.suiteXmlFiles=src\\test\\resources\\testng-orangehrm.xml'];
            break;
        case 'skillmatrix':
            args = ['test', '-Dsurefire.suiteXmlFiles=src\\test\\resources\\testng-skillmatrix.xml'];
            break;
        case 'failed':
            args = ['test', '-Dsurefire.suiteXmlFiles=src\\test\\resources\\testng-failedcases.xml'];
            break;
        case 'bdd':
            args = ['test', '-Dtest=bdd.runner.TestRunner'];
            break;
        default:
            args = ['test', '-Dsurefire.suiteXmlFiles=src\\test\\resources\\testng.xml'];
    }

    res.writeHead(200, { 'Content-Type': 'text/plain' });
    res.write('> mvn ' + args.join(' ') + '\n\n');
    const child = spawn('mvn', args, { cwd: projectDir, shell: true });

    child.stdout.on('data', (data) => res.write(data));
    child.stderr.on('data', (data) => res.write(data));

    child.on('error', (err) => {
        res.write('\n=== Spawn error: ' + err.message + ' ===\n');
        res.end();
    });

    child.on('close', (code, signal) => {
        const why = signal ? ' (signal: ' + signal + ')' : '';
        res.write('\n=== Exit code: ' + code + why + ' ===\n');
        res.end();
    });

    res.on('close', () => {
        try { child.kill(); } catch (e) { /* ignore */ }
    });
});

app.listen(PORT, () => {
    console.log('Server is running on http://localhost:' + PORT);
    console.log('Open your browser and navigate to http://localhost:' + PORT);
});
