# !/bash/sh

cd /home/super/Projects/finance_recorder_java
java -Djava.library.path=lib -jar finance_recorder.jar -f history.conf --action W --remove_old --multi_thread --check_error

