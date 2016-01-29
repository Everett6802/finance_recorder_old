# !/bash/sh

usage() { echo "Usage: $0 [-m <month_string> (Ex: 2016-01)]" 1>&2; exit 1; }

while getopts ":m:" o; do
    case "${o}" in
        m)
            m=${OPTARG}
            ;;
        *)
            usage
            ;;
    esac
done
shift $((OPTIND-1))

if [ -z "${m}" ]; then
    usage
fi

#echo "m = ${m}"

cd /home/super/Projects/finance_recorder_java
java -Djava.library.path=lib -jar finance_recorder.jar--action W -t ${m} -s all --check_error
