cd /d "%~dp0"
rem git pull

cd wildnote-article-parser
call npm install

title wildnote-article-parser
node index.js %*
