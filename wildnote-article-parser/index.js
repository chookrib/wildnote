

const url = 'https://mp.weixin.qq.com/s/6GTGKy_iGmTi5wK60OeSsg';
// const url = 'https://www.zhihu.com/question/1995213825918641040';
// const url = 'https://mp.weixin.qq.com/s/K_hmtdkVwSknGjriL6Yo6w'

// import Parser from '@postlight/parser';
// Parser.parse(url).then(result => console.log(result));


import { extract, extractFromHtml } from '@extractus/article-extractor';
// const data = await extract(url)
// console.log(data)

// const { chromium } = require('playwright');
import { chromium } from  'playwright';

import { writeFile } from 'node:fs/promises';

// & 'C:\Program Files\Google\Chrome\Application\chrome.exe' --remote-debugging-port=9222 --user-data-dir='C:\temp\pw-profile'

// (async () => {
//     const browser = await chromium.launch();
//     const page = await browser.newPage();
//     await page.goto(url);
//     console.log(await page.title());
//     console.log(await page.content());
//     await browser.close();
// })();

(async () => {

    const browser = await chromium.connectOverCDP('http://127.0.0.1:9222');
    const context = browser.contexts()[0] || await browser.newContext();
    const page = context.pages()[0] || await context.newPage();

    // await page.goto(url);

    await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 60000 });
    await page.waitForLoadState('networkidle');

    console.log(await page.title());
    // console.log(await page.content());
    let pageContent = await page.content();

    // pageContent = pageContent.replace(/(\d{4})\s*年\s*(\d{1,2})\s*月\s*(\d{1,2})\s*日/g, "$1-$2-$3");
    await writeFile('./page-content.html', pageContent, 'utf8');

    // const out = s.replace(
    //     /(\d{4})\s*年\s*(\d{1,2})\s*月\s*(\d{1,2})\s*日/g,
    //     (_, y, m, d) => `${y}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`
    // );

    const data = await extractFromHtml(pageContent, url);
    if(!data.published) {
        if (url.toLowerCase().startsWith('https://mp.weixin.qq.com/')) {
            const match = pageContent.match(/createTime\s*=\s*['"](\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2})['"]/i);
            if (match?.[1]) {
                data.published = match[1];
            }
        }
    }


    console.log(data)

    // await page.close();

    await browser.close();
})();


