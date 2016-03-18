# !/bash/sh

cd ~/Projects/finance_recorder_java
java -Djava.library.path=lib -jar finance_recorder.jar -f today.conf --action W --check_error --run_daily

