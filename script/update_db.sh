# !/bash/sh

cd /home/super/Projects/finance_recorder_java/bin
java -Djava.library.path=lib -jar finance_recorder.jar -f today.conf --check_error --run_daily

