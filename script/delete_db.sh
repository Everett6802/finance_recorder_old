# !/bash/sh

cd /home/super/Projects/finance_recorder_java/bin
java -Djava.library.path=lib -jar finance_recorder.jar -f history.conf --remove_old --read_only

