# !/bash/sh

#cd /home/super/Projects/finance_recorder_java/bin
#cp -r ../lib/ .
#cp -r ../conf/ .
cd ~/Projects/finance_recorder_java
cp -r ./bin/com .
jar cfvm ./finance_recorder.jar manifest com/price/finance_recorder/*.class lib conf
rm -rf ./com



