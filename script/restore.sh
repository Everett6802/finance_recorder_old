# !/bash/sh

usage() { echo "Usage: $0 [-f <folder_name> (Ex: 160322053543)]" 1>&2; exit 1; }

while getopts ":f:" o; do
    case "${o}" in
        f)
            f=${OPTARG}
            ;;
        *)
            usage
            ;;
    esac
done
shift $((OPTIND-1))

if [ -z "${f}" ]; then
    usage
fi

#echo "f = ${f}"

cd ~/Projects/finance_recorder_java
java -Djava.library.path=lib -jar finance_recorder.jar --restore ${f} --check_error

