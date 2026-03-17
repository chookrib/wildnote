import express from 'express';
import {extractFromHtml} from '@extractus/article-extractor';
import {chromium} from 'playwright';
import {writeFile} from 'node:fs/promises';

const app = express();

app.get('/parse', async (req, res) => {
    try {
        const {url} = req.query;

        if (!url) {
            res.status(500).send('缺少 url 参数');
            return;
        }

        const browser = await chromium.connectOverCDP('http://127.0.0.1:9222');

        // const page = await browser.newPage();
        // await page.goto(url);

        const context = browser.contexts()[0] || await browser.newContext();
        const page = context.pages()[0] || await context.newPage();

        // await page.goto(url, {waitUntil: 'domcontentloaded', timeout: 60000});
        // await page.waitForLoadState('networkidle');

        const response = await page.goto(url, {waitUntil: 'domcontentloaded', timeout: 60000});
        if (!response) {
            res.status(500).send('页面未返回响应');
            return;
        }

        if (!response.ok()) {
            res.status(500).send(`页面打开失败，HTTP 状态码: ${response.status()}`);
            return;
        }

        // await page.waitForLoadState('networkidle', {timeout: 10000}).catch(() => {});
        // if (page.url() === 'about:blank') {
        //     res.status(500).send('页面仍是空白页，可能未正确打开');
        // }

        // console.log(await page.title());
        // console.log(await page.content());

        let pageContent = await page.content();
        // console.log(pageContent);
        // await writeFile(`./page-content-${Date.now()}.html`, pageContent, 'utf8');

        // pageContent = pageContent.replace(/(\d{4})\s*年\s*(\d{1,2})\s*月\s*(\d{1,2})\s*日/g, "$1-$2-$3");
        // pageContent = pageContent.replace(
        //     /(\d{4})\s*年\s*(\d{1,2})\s*月\s*(\d{1,2})\s*日/g,
        //     (_, y, m, d) => `${y}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`
        // );

        // await page.close();
        // await browser.close();

        const extractData = await extractFromHtml(pageContent, url,
            {
                // wordsPerMinute: 300,   // 默认 300, to estimate time to read, 估算阅读所需时间
                // descriptionTruncateLen: 210,    // 默认 210, max num of chars generated for description
                descriptionLengthThreshold: 0,   // 默认 180, min num of chars required for description
                // 微信小绿书提取的 content 过短，关闭 content 长度限制
                contentLengthThreshold: 0   // 默认 200, min num of chars required for content
            });
        // console.log(extractData);

        if (!extractData) {
            res.status(500).send('没有解析到内容');
            return;
        }

        // 如果未解析到发布日期
        // if (!extractData.published) {

        // 手工提取微信公众号文章发布日期
        // 文章模式（传统图文），提取位置有以下三处
        // var createTime = '2026-03-13 20:48';
        // create_time: JsDecode('2026-03-13 20:48'),
        // <em id="publish_time" className="rich_media_meta rich_media_meta_text">2026年3月13日 20:48</em>
        // 图片/文字消息（小绿书），提取位置仅有两处，有一处没有年份
        // create_time: JsDecode('2026-03-13 19:52'),
        // <span id="publish_time">3月13日 19:52</span>
        if (url.toLowerCase().startsWith('https://mp.weixin.qq.com/')) {
            // const match = pageContent.match(/createTime\s*=\s*['"](\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2})['"]/i);
            const match = pageContent.match(/create_time\s*:\s*JsDecode\(['"](\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2})['"]\)/i);
            if (match?.[1]) {
                extractData.published = match[1];
            }
        }
        //}

        console.log(extractData);

        res.status(200).json(extractData);
    } catch (err) {
        // console.log(err);
        // throw err;
        return res.status(500).send(`发生异常: ${err.message}`);
    }
});


const port = 9223;
app.listen(port, () => {
    console.log(`wildnote-article-parser 服务已在端口 ${port} 启动`);
});
