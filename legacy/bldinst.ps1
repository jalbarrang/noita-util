Remove-Item .\noita-util -Recurse
pushd target
Remove-Item .\noita-util-1.1.0\ -Recurse
Expand-Archive -Path '..\build\distributions\noita-util-1.1.0.zip'
pushd noita-util-1.1.0\noita-util-1.1.0\lib
copy C:\Users\dkilm\code\noita-util\packr-config.json .
java -jar C:\Users\dkilm\code\packr-all-4.0.0.jar .\packr-config.json
popd
popd
Move-Item .\target\noita-util-1.1.0\noita-util-1.1.0\lib\noita-util .
