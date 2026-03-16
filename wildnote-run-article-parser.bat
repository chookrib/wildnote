cd /d "%~dp0"
git pull

cd wildnote-article-parser
call npm install
node index.js %*
