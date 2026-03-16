cd /d "%~dp0"
git pull
call npm install
node index.js %*
