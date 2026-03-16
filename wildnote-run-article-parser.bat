cd /d "%~dp0"
rem git pull

cd wildnote-article-parser
call npm install
node index.js %*
