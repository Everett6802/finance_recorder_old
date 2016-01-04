# !/bash/sh

cd /home/super/Projects/finance_recorder_java/bin
cp -r ../lib/ .
cp -r ../conf/ .
jar cfvm ./finance_recorder.jar manifest com/price/finance_recorder/*.class lib conf



